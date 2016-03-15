package Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

public class HQInteractiveServer implements Runnable
{
	// instance variables
	ServerSocketChannel iServerSocketChannel;
	Socket iSocket;
	String iDateStarted;
	boolean iShuttingDown;
	int iTotalNumberOfConnections;
	int iSuccessfulConnections;
	int iFailedConnections;
	int iTotalQueries;
	int iSuccessfulQueries;
	int iFailedQueries;
	long iThreadCounter;
	DatabaseRefresher iDatabaseRefresher;
	SQLServer iSQLServer;
	HashMap<String, Semaphore> iDatabaseLocks = new HashMap();
	
	
	public static String[] MissionNames = 
	{		"Campaign1Mission1", "Campaign1Mission2", "Campaign1Mission3", "Campaign1Mission4",
			"Campaign2Mission1", "Campaign2Mission2", "Campaign2Mission3", "Campaign2Mission4",
			"Campaign3Mission1", "Campaign3Mission2", "Campaign3Mission3", "Campaign3Mission4",
			"Campaign4Mission1", "Campaign4Mission2", "Campaign4Mission3", "Campaign4Mission4",
	};
	
	
	Connection iConnection = null;

	HQInteractiveServer()
	{
		iShuttingDown = false;
		iTotalNumberOfConnections = 0;
		iSuccessfulConnections = 0;
		iFailedConnections = 0;
		iThreadCounter = 0;
		
		//update the interface
		ServerUserInterface.UpdateTotalConnections(0);
		ServerUserInterface.UpdateSuccessfulConnections(0);
		ServerUserInterface.UpdateFailedConnections(0);
		
		//create Decoder functions
		Decoder.CreateCodeValues();
		
		//create semaphores for database locks
		for(int lIndex = 0; lIndex < HQInteractiveServer.MissionNames.length; lIndex++)
		{
			iDatabaseLocks.put(HQInteractiveServer.MissionNames[lIndex].toLowerCase(), new Semaphore(1, true)); //only one thread at a time can access a particular table
		}
	}

	public void run()
	{
		try
		{
			//create DatabaseRefesher to keep the database Connection online
			iDatabaseRefresher = new DatabaseRefresher(this);
			Thread lDatabaseRefresherThread = (new Thread(iDatabaseRefresher));
			lDatabaseRefresherThread.start();
			
			// 1. creating a server socket
			iServerSocketChannel = ServerSocketChannel.open();
			iServerSocketChannel.socket().bind(new InetSocketAddress(6666));

			while (true)
			{
				// 2. Wait for connection
				System.out.println("");// new line
				System.out.println("Date: " + printCurrentDateTime());
				System.out.println("Waiting for connection");

				if(!iShuttingDown) //don't except new connection if in shutting down mode
				{
					SocketChannel lNewSocketChannel = iServerSocketChannel.accept();
					if (lNewSocketChannel != null)
					{
						// create new dealer to handle client connection
						ClientDealer lClientDealer = new ClientDealer(lNewSocketChannel, this, iThreadCounter++);
						new Thread(lClientDealer).start();
					}
				}
			}
		}
		catch (ClosedByInterruptException aServerThreadInterrupted)
		{
			// wait for client to finish
		}
		catch (Exception aException)
		{
			aException.printStackTrace();
		}
		finally
		// will be called once connection with client is done or an error
		// occurred
		{
			//close database refresher thread
			iDatabaseRefresher.Stop();
			
			// 4: Closing connection
			try
			{
				if(iServerSocketChannel != null)
					iServerSocketChannel.close();
			}
			catch (IOException aIOException)
			{
				aIOException.printStackTrace();
			}
			catch(Exception aException) //got a memory exception here
			{
				aException.printStackTrace();
			}
		}
	}
	
	public void AcquireDatabaseHandle(String aTableName)
	{
		try
		{
			iDatabaseLocks.get(aTableName).acquire();
		}
		catch(Exception aException)
		{
			System.out.println("An error occured during acquiring the Database Table : " + aTableName);
			System.out.println(aException.getLocalizedMessage());
			System.out.println(aException.getStackTrace());
		}
	}
	
	public void ReleaseDatabaseHandle(String aTableName)
	{
		try
		{
			iDatabaseLocks.get(aTableName).release();
		}
		catch(Exception aException)
		{
			System.out.println("An error occured during releasing the Database Table : " + aTableName);
			System.out.println(aException.getLocalizedMessage());
			System.out.println(aException.getStackTrace());
		}
	}

	public static String printCurrentDateTime()
	{
		DateFormat lDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date lDate = new Date();
		String lCurrentDate = lDateFormat.format(lDate);
		return lCurrentDate;
	}
	
	public void startMySQLServer()
	{
		iSQLServer = new SQLServer();
		iSQLServer.startMySQLServer();
	}

	public void stopMySQLServer()
	{
		iSQLServer.stopMySQLServer();
	}

	public Connection getConnection()
	{
		return iConnection;
	}

	public String getDateStarted()
	{
		return iDateStarted;
	}

	public void setDateStarted(String aDateStarted)
	{
		iDateStarted = aDateStarted;
	}
	
	public void UpdateTotalNumberOfConnections(int aValue)
	{
		iTotalNumberOfConnections += aValue;
		ServerUserInterface.UpdateTotalConnections(iTotalNumberOfConnections);
	}
	
	public void UpdateSuccessfulConnection(int aValue)
	{
		iSuccessfulConnections += aValue;
		ServerUserInterface.UpdateSuccessfulConnections(iSuccessfulConnections);
	}
	
	public void UpdateFailedConnections(int aValue)
	{
		iFailedConnections += aValue;
		ServerUserInterface.UpdateFailedConnections(iFailedConnections);
	}
	
	public void updateTotalQueries(int aValue)
	{
		iTotalQueries += aValue;
		ServerUserInterface.UpdateTotalQueries(iTotalQueries);
	}
	
	public void updateSuccessfulQueries(int aValue)
	{
		iSuccessfulQueries += aValue;
		ServerUserInterface.UpdateSuccessfulQueries(iSuccessfulQueries);
	}
	
	public void updateFailedQueries(int aValue)
	{
		iFailedQueries += aValue;
		ServerUserInterface.UpdateFailedQueries(iFailedQueries);
	}
	
	public void SetIsShuttingDown(boolean aValue)
	{
		iShuttingDown = aValue;
	}
}