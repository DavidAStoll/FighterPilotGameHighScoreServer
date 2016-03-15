package Server;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.plaf.SliderUI;

public class ClientDealer implements Runnable {
	
	public enum EClientState 
	{
		UploadingScores,
		DownloadingHighscoreTables
	}

	// constants
	final String DISCONNECT_MESSAGE = "SOS";
	final String END_OF_UPDATING_SCORES_MESSAGE = "UPDATING SCORES DONE";
	final String CLIENT_MESSAGE_DELIMITER = "|";
	final int MAX_MESSAGE_SIZE_IN_BYTES = 300 * 4; // 1200 bytes, which is 300 characters
	final int SLEEP_TIME_IF_NO_DATA_RECEIVED_IN_MILLI_SECS = 500; //0.5 of a second
	final int MAX_NUMBER_OF_NO_DATA_RECEIVED_UNTIL_MESSAGE_GETS_DROPPED = 10;
	final int MAX_NUMBER_OF_FAILED_MESSAGES_UNTIL_DISCONNECT = 10;
	final int NUMBER_OF_RANKS_PER_HIGHSCORE_TABLE = 10;
	
	long iThreadId;
	EClientState iState;
	DateFormat iDateFormat;
	String iIPOfClient;
	Socket iSocket;
	SocketChannel iSocketChannel;
	HQInteractiveServer iHqServer;
	ByteBuffer iReadBuffer;
	ByteBuffer iWriteBuffer;
	byte[] iResetBuffer = new byte [MAX_MESSAGE_SIZE_IN_BYTES];
	int iMissionRankIndex; //keeps track what rank in a mission we are sending
	int iMissionIndex; //keeps track what mission we are currently sending

	ClientDealer(SocketChannel aServerSocketChannel, HQInteractiveServer aHqServer, long aThreadId) {
		
		iThreadId  = aThreadId;
		iMissionRankIndex = 0;
		iMissionIndex = 0;
		iState = EClientState.UploadingScores;
		iSocketChannel = aServerSocketChannel;
		iHqServer = aHqServer;
		iReadBuffer = ByteBuffer.allocateDirect(MAX_MESSAGE_SIZE_IN_BYTES);
		iSocket = iSocketChannel.socket();
		iIPOfClient = iSocket.getInetAddress().getHostAddress();
		iDateFormat = new SimpleDateFormat("yyyy/MM/dd");
		
		for(int lIndex = 0; lIndex < MAX_MESSAGE_SIZE_IN_BYTES; lIndex++)
		{
			iResetBuffer[lIndex] = 0;
		}
	}

	public static String printCurrentDateTime() {
		DateFormat lDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date lDate = new Date();
		String lCurrentDate = lDateFormat.format(lDate);
		return lCurrentDate;
	}
	
	private void printf(String aString)
	{
		System.out.println("ClientDealer" +iThreadId+">" + aString);
	}
	
	private boolean SendNextTableEntry()
	{
		String lMessageForClient = "";
		boolean lDisconnectMessageSend = false;
		
		if(iMissionIndex < HQInteractiveServer.MissionNames.length)
		{
			SQLQuery lSQLQuery = new SQLQuery(iHqServer, iThreadId);
			
			//aquire lock for this database table
			iHqServer.AcquireDatabaseHandle(HQInteractiveServer.MissionNames[iMissionIndex].toLowerCase());
			
			//get highscore from this table with this particular rank
			String lTableEntry = lSQLQuery.GetEntryFromHighscoreTableByRank(HQInteractiveServer.MissionNames[iMissionIndex], iMissionRankIndex + 1); //rankIndex starts at 0
			
			//release lock from this database table
			iHqServer.ReleaseDatabaseHandle(HQInteractiveServer.MissionNames[iMissionIndex].toLowerCase());
			
			//need to split it up, since we only use part of the table data
			String lSplitMessage[] = lTableEntry.split(" ");
			lMessageForClient = HQInteractiveServer.MissionNames[iMissionIndex] //Mission Name
										+ " " + lSplitMessage[1]	//Rank
										+ " " + lSplitMessage[2]	//Name
										+ " " + lSplitMessage[3]	//Score
										+ " " + lSplitMessage[4]	//Date
										+ " " + lSplitMessage[5];	//Time
			//need to add delimiter to the message
			lMessageForClient = CLIENT_MESSAGE_DELIMITER + lMessageForClient + CLIENT_MESSAGE_DELIMITER;
			
			//need to update our indexes
			iMissionRankIndex++;
			if(iMissionRankIndex == NUMBER_OF_RANKS_PER_HIGHSCORE_TABLE)
			{
				//we are done with all ranks
				iMissionRankIndex = 0;
				iMissionIndex++;
			}
		}
		else //send disconnect message
		{
			lDisconnectMessageSend = true;
			lMessageForClient =  CLIENT_MESSAGE_DELIMITER + DISCONNECT_MESSAGE + CLIENT_MESSAGE_DELIMITER;
		}
		
		try
		{
			iWriteBuffer = Decoder.EncodeMessage(lMessageForClient);
			int lBytesWriten = iSocketChannel.write(iWriteBuffer);
		}
		catch(Exception aException)
		{
			printf("Error occurred during sending messsage!!");
			printf(lMessageForClient);
			printf(aException.getLocalizedMessage());
		}
		return lDisconnectMessageSend;
	}

	public void run() 
	{
		try {
			//some information that always need
			
			//got a connection
			int lMessageFailedCounter = 0;
			iSocket.setReceiveBufferSize(MAX_MESSAGE_SIZE_IN_BYTES * 200); //at least buffer up to 200 messages
			printf("Date: " + printCurrentDateTime());
			printf("Connection received from " + iIPOfClient);

			// update connection stats
			iHqServer.UpdateTotalNumberOfConnections(1);

			while(true)
			{
				if(iState == EClientState.UploadingScores)
				{
					// read next message from socket
					String lMessageFromClient = ReadMessageFromSocketChannel(iSocketChannel);
					
					if(lMessageFromClient == null && iState == EClientState.UploadingScores) 
					{
						//some problem occurred or last message
						lMessageFailedCounter++;
						printf("Message is NULL!");
						if(lMessageFailedCounter == MAX_NUMBER_OF_FAILED_MESSAGES_UNTIL_DISCONNECT)
						{
							break; //need to break here since client might have shut down, otherwise server will keep connection alive forever
						}
					}
					else if(lMessageFromClient.equals(END_OF_UPDATING_SCORES_MESSAGE))
					{
						//all scores have been send by the client, start sending score data to the client
						 SendNextTableEntry();
						iState = EClientState.DownloadingHighscoreTables;
					}
					else if(lMessageFromClient.equals(DISCONNECT_MESSAGE))
					{
						break;//disconnect message, close connection
					}
					else 
					{	
						//need to split up message and add some data to it that only the server can know
						String lSplitMessage[] = lMessageFromClient.split(" ");
						String lNewHighscoreTableEntry = 
						lSplitMessage[0] + //TableName
						" " + lSplitMessage[1] + //PhoneID
						" " + lSplitMessage[2] + //Username
						" " + lSplitMessage[3] + //Score
						" " + iDateFormat.format(new Date()) + //Date
						" " + lSplitMessage[4] + //Time
						" " + iIPOfClient;//Country
						
						//update highscore tables with client scores
						SQLQuery lSQLQuery = new SQLQuery(iHqServer, iThreadId);
						lSQLQuery.UpdateHighscoreTables(lNewHighscoreTableEntry);
						
						//print out complete message
						printf(lNewHighscoreTableEntry);
					}
				}
				else if(iState == EClientState.DownloadingHighscoreTables)
				{
					 if(SendNextTableEntry() == true)
					 {
						 break; //send disconnect message
					 }
				}
				else
				{
					//state not defined, abort with error
					printf("Error, ClientDealer is in a undefined State!! Aborting Connection!");
					break;
				}
			} 

			// all done, close Connection
			printf("Connection closed");
			iSocketChannel.close();
			iHqServer.UpdateSuccessfulConnection(1);
		} 
		catch (ClosedChannelException aClosedException) 
		{
			//happens when sockets gets closed too early
			printf("Error!!! ClientDealer Thread has to terminate!");
			printf("Socket was Closed too early it seems!");
			aClosedException.printStackTrace();
			iHqServer.UpdateFailedConnections(1);
			try
			{
				iSocketChannel.close();
			}
			catch(IOException lIOException)
			{
				lIOException.printStackTrace();
			}
		}
		catch (Exception aException) 
		{
			printf("Error!!! ClientDealer Thread has to terminate!");
			aException.printStackTrace();
			iHqServer.UpdateFailedConnections(1);
			try
			{
				iSocketChannel.close();
			}
			catch(IOException lIOException)
			{
				lIOException.printStackTrace();
			}
		}
	}

	public String ReadMessageFromSocketChannel(SocketChannel aSocketChannel) {
		try {

			iReadBuffer.clear();
			int lTotalBytesAlreadyRead = 0;
			int lNumberOfNoDataReceived = 0;
			
			while(lTotalBytesAlreadyRead != MAX_MESSAGE_SIZE_IN_BYTES)
			{
				int lBytesRead = 0;
				aSocketChannel.configureBlocking(false); //non blocking, in case no new data comes in
				lBytesRead = aSocketChannel.read(iReadBuffer);
				lTotalBytesAlreadyRead += lBytesRead;
				
				if(lBytesRead == -1)
				{
					return null; //client close connection
				}
				else if(lBytesRead == 0)
				{
					if(lNumberOfNoDataReceived == MAX_NUMBER_OF_NO_DATA_RECEIVED_UNTIL_MESSAGE_GETS_DROPPED)
					{
						return null;
					}
					
					//nothing received, maybe connection got interrupted
					lNumberOfNoDataReceived++;
					Thread.sleep(SLEEP_TIME_IF_NO_DATA_RECEIVED_IN_MILLI_SECS);
				}
				
				if(lTotalBytesAlreadyRead > MAX_MESSAGE_SIZE_IN_BYTES)
				{
					//in theory should never happen, but put it in just in case to avoid infinite loop
					return null;
				}
			}

			aSocketChannel.configureBlocking(true); //want it to block again
			iReadBuffer.flip(); // shrinks buffer to message size and resets
			// extract the message from ByteBuffer
			String lResult = Decoder.DecodeMessage(iReadBuffer.asIntBuffer());
			
			//reset buffer
			iReadBuffer.put(iResetBuffer);
				
			int lDelimiterStartPosition = lResult.indexOf(CLIENT_MESSAGE_DELIMITER);
			if(lDelimiterStartPosition == -1)
			{
				printf("Error!!! Message Corrupted!");
				printf(lResult);
				return null; //start delimiter not present
			}
			int lDelimiterEndPosition = lResult.indexOf(CLIENT_MESSAGE_DELIMITER, lDelimiterStartPosition + 1);
			if (lDelimiterEndPosition == -1) 
			{
				printf("Error!!! Message Corrupted!");
				printf(lResult);
				return null; //end delimiter not in message, message seems to be corrupted
			} else 
			{
				lResult = lResult.substring(lDelimiterStartPosition + 1, lDelimiterEndPosition); //don't include delimiter
			}
			return lResult;
		} catch (Exception aException) {
			aException.printStackTrace();
			return null;
		}
	}
}
