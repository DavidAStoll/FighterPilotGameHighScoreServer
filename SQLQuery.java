package Server;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

public class SQLQuery
{
	final int NUMBER_OF_ENTRIES_PER_HIGHSCORE_TABLE = 10;
	final int MAX_RANK_IN_A_HIGHSCORE_TABLE = 10;
	
	long iThreadId;
	SQLServer iSQLServer;
	HQInteractiveServer iHqServer;
	
	SQLQuery(HQInteractiveServer aHqServer, long aThreadId)
	{
		iHqServer = aHqServer;
		iSQLServer = aHqServer.iSQLServer;	
		iThreadId = aThreadId;
	}
	
	private void printf(String aString)
	{
		System.out.println("SQLQuery" +iThreadId+">" + aString);
	}
	
	private void MoveDownEntriesByRank(String aHighscoreTable, int aStartingRank, int aLastRankToInclude)
	{
		Statement lStatement;
		String lQuery = "";
		
		try
		{
			lStatement = iSQLServer.iConnection.createStatement();

			//decrement ranks by 1
			iHqServer.updateTotalQueries(1);
			lQuery = "UPDATE hqsqlhighscoreserverdatabase." + aHighscoreTable.toLowerCase() 
					+ " SET Rank=Rank+1 WHERE Rank>=" + aStartingRank + "&& Rank<="+ aLastRankToInclude;
			try 
			{
				lStatement.executeUpdate(lQuery);
				iHqServer.updateSuccessfulQueries(1);
			}
			catch(SQLException eSQLException)
			{
				iHqServer.updateFailedQueries(1);
				printf("Error on decerementing rank!");
				printf(lQuery);
				printf(eSQLException.getLocalizedMessage());
			}
			
			iHqServer.updateTotalQueries(1);
			//delete all entries which rank is bigger than the max rank in a table
			lQuery = "DELETE FROM hqsqlhighscoreserverdatabase." + aHighscoreTable.toLowerCase()
					+ " WHERE Rank>" + MAX_RANK_IN_A_HIGHSCORE_TABLE;
			try
			{
				lStatement.executeUpdate(lQuery);
				iHqServer.updateSuccessfulQueries(1);
			}
			catch(SQLException eSQLException)
			{
				iHqServer.updateFailedQueries(1);
				printf("Error on deleting entries outside the MAX rank!");
				printf(lQuery);
				printf(eSQLException.getLocalizedMessage());
			}
		}
		catch(Exception eException)
		{
			printf("Error on moving down entries rank down!");
			printf(lQuery);
			printf(eException.getLocalizedMessage());
		}
	}
	
	public void InsertEntryIntoHighscoreTableIfGoodEnough(String[] aNewEntry)
	{
		int lScoreOfNewEntry = Integer.parseInt(aNewEntry[3]);
		int lRankOfNewEntry = -1;
		String lHighscoreTableName = aNewEntry[0];
		String lPhoneID = aNewEntry[1];
		String lNameOfUser = aNewEntry[2];
		
		//check if Score from the User is good enough to make it into the Highscore table
		lRankOfNewEntry = FindRankForScoreInHighscoreTable(lHighscoreTableName, lScoreOfNewEntry);
		
		if(lRankOfNewEntry == -1)
		{
			// was not good enough, don't insert
		}
		else //-------------------- Good enough Score to make it into the table --------------------//
		{
			//first check if the user has already an entry in the highscore table
			int lRankOfUserCurrently = GetRankFromHighscoreTableByPhoneID(lHighscoreTableName, lPhoneID);
			
			if(lRankOfUserCurrently == -1) //-------------- User does not have an Entry --------------//
			{
				//new entry has  a better score, therefor insert it here
				//first move the old entry and all following entries down by 1 rank
				//move down entries
				MoveDownEntriesByRank(lHighscoreTableName, lRankOfNewEntry,MAX_RANK_IN_A_HIGHSCORE_TABLE);
				
				//insert new entry at the this now free position
				InsertIntoHighscoreTable(aNewEntry, lRankOfNewEntry);
				return; // all done
			} 
			else //-------------------- User has already an Entry ---------------------------//
			{
				//user already added an Entry, check if the new score is better otherwise don't do anything
				int lOldScoreFromUser = GetScoreFromHighscoreTableByRank(lHighscoreTableName, lRankOfUserCurrently);
				
				if(lOldScoreFromUser == lScoreOfNewEntry ) //the score did not change, just update the name of the user
				{
					//did not change his score, but might have changed his name
					UpdateNameFromFromHighscoreTableByRank(lHighscoreTableName, lRankOfUserCurrently, lNameOfUser);
				}
				else if(lOldScoreFromUser < lScoreOfNewEntry)
				{
					//improved his score, delete his old entry and add the new one
					//did he improve his rank?
					if(lRankOfNewEntry < lRankOfUserCurrently)
					{
						//rank did changed, need to change other depend entries as well
						DeleteEntryFromHighscoreTableByRank(lHighscoreTableName, lRankOfUserCurrently);
						//move down all entries that are between the new and the old position
						MoveDownEntriesByRank(lHighscoreTableName, lRankOfNewEntry, lRankOfUserCurrently);
						//insert his new entry
						InsertIntoHighscoreTable(aNewEntry, lRankOfNewEntry);
					}
					else
					{
						//rank did not change, just remove the old entry and update it with the new one
						//no need to do anything to other entries
						DeleteEntryFromHighscoreTableByRank(lHighscoreTableName, lRankOfUserCurrently);
						InsertIntoHighscoreTable(aNewEntry, lRankOfNewEntry);
					}
				}
				else
				{
					//did not improve and a user can only have one entry in a table, don't add it
					return;
				}
			}
		}
	}
	
	public int FindRankForScoreInHighscoreTable(String aHighscoreTable, int aScore)
	{
		int lRank = -1;
		
		for(int lIndex = 0; lIndex < NUMBER_OF_ENTRIES_PER_HIGHSCORE_TABLE; lIndex++)
		{
			int lRankIndex = lIndex + 1; //need to add 1, because counting starts at 0
			int lScoreForRank = GetScoreFromHighscoreTableByRank(aHighscoreTable, lRankIndex); 
			
			if(lScoreForRank == -1)
			{
				//something went wrong, don't do anything
			}
			else if(lScoreForRank < aScore)
			{
				lRank = lRankIndex;
				return lRank; // all done
			}
		}
		
		//was not good enough
		return lRank;
	}
	
	public void UpdateNameFromFromHighscoreTableByRank(String aHighscoreTable, int aRank, String aNewName)
	{
		Statement lStatement;
		String lQuery = "";
		iHqServer.updateTotalQueries(1);
		
		try
		{
			lStatement = iSQLServer.iConnection.createStatement();
	
			//delete all entries which rank is bigger than the max rank in a table
			lQuery = "UPDATE hqsqlhighscoreserverdatabase." + aHighscoreTable.toLowerCase()
					+ " SET Name=\'" + aNewName +"\' WHERE Rank=" + aRank;
			lStatement.executeUpdate(lQuery);
			iHqServer.updateSuccessfulQueries(1);
		}
		catch(SQLException eSQLException)
		{
			iHqServer.updateFailedQueries(1);
			printf("Error on updating Name By Rank:");
			printf(lQuery);
			printf(eSQLException.getLocalizedMessage());
		}
	}
	
	private void DeleteEntryFromHighscoreTableByRank(String aHighscoreTable, int aRank)
	{
		Statement lStatement;
		String lQuery = "";
		iHqServer.updateTotalQueries(1);
		
		try
		{
			lStatement = iSQLServer.iConnection.createStatement();
	
			//delete all entries which rank is bigger than the max rank in a table
			lQuery = "DELETE FROM hqsqlhighscoreserverdatabase." + aHighscoreTable.toLowerCase()
					+ " WHERE Rank=" + aRank;
			lStatement.executeUpdate(lQuery);
			iHqServer.updateSuccessfulQueries(1);
		}
		catch(SQLException eSQLException)
		{
			iHqServer.updateFailedQueries(1);
			printf("Error on retrieving Rank by PhoneID:");
			printf(lQuery);
			printf(eSQLException.getLocalizedMessage());
		}
	}
	
	private int GetScoreFromHighscoreTableByRank(String aHighscoreTable, int aRank)
	{
		int lScore = -1; //make it negative to signal error, in case something goes wrong
		String lResult = null;
		
		try
		{	lResult = GetEntryFromHighscoreTableByRank(aHighscoreTable, aRank);
			String lSplitMessage[] = lResult.split(" ");
			lScore = Integer.parseInt( lSplitMessage[3]); //should be the correct index to retrieve the Score
		}
		catch(Exception aExeception)
		{
			printf("Error occured while retriving Score by Rank");
			printf("Result returned: " + lResult);
			printf(aExeception.getLocalizedMessage());
		}
		
		return lScore;
	}
	
	private int GetRankFromHighscoreTableByPhoneID(String aHighscoreTable, String aPhoneID)
	{
		int lRank = -1; //signals not found
		Statement lStatement;
		String lQuery = "";
		iHqServer.updateTotalQueries(1);
		
		try
		{
			lStatement = iSQLServer.iConnection.createStatement();
			lQuery = "SELECT * FROM hqsqlhighscoreserverdatabase." + aHighscoreTable.toLowerCase() + " WHERE PhoneID=" + aPhoneID;
			
			//Query should have proper format now
			ResultSet lQueryResult = lStatement.executeQuery(lQuery);
			iHqServer.updateSuccessfulQueries(1);
			
			if(lQueryResult.next()) //move to the first result
			{
				//retrieve column values
				lRank = lQueryResult.getInt("Rank");
			}
			else
			{
				//was not found, PhoneID is not in table
				lRank = -1;
			}
		}
		catch(SQLException eSQLException)
		{
			lRank = -1;
			iHqServer.updateFailedQueries(1);
			printf("Error on retrieving Rank by PhoneID:");
			printf(lQuery);
			printf(eSQLException.getLocalizedMessage());
		}
		
		return lRank;
	}
	
	public String GetEntryFromHighscoreTableByRank(String aHighscoreTable, int aRank)
	{
		String lReturnValue = null;
		Statement lStatement;
		String lQuery = "";
		iHqServer.updateTotalQueries(1);
		
		try
		{
			lStatement = iSQLServer.iConnection.createStatement();
			lQuery = "SELECT * FROM hqsqlhighscoreserverdatabase." + aHighscoreTable.toLowerCase() + " WHERE Rank=" + aRank;
			
			//Query should have proper format now
			ResultSet lQueryResult = lStatement.executeQuery(lQuery);
			
			if(lQueryResult.next()) //move to the first result
			{
				//retrieve column values
				String lPhoneId = lQueryResult.getString("PhoneID");
				String lRank = Integer.toString(lQueryResult.getInt("Rank"));
				String lName = 	lQueryResult.getString("Name");
				String lScore = Integer.toString(lQueryResult.getInt("Score"));
				String lDate = lQueryResult.getDate("Date").toString();
				String lTime = lQueryResult.getString("MissionTime").toString();
				String lCountry = lQueryResult.getString("Country").toString();
				
				lReturnValue = lPhoneId + " " + lRank + " " + lName + " " + lScore + " " + lDate + " " + lTime + " " + lCountry;
				iHqServer.updateSuccessfulQueries(1);
			}
		}
		catch(SQLException eSQLException)
		{
			lReturnValue = null;
			iHqServer.updateFailedQueries(1);
			printf("Error on getting Entry from table by Rank:");
			printf(lQuery);
			printf(eSQLException.getLocalizedMessage());
		}
		
		return lReturnValue;
	}
	
	private void InsertIntoHighscoreTable(String[] aSplitMessage, int aRank)
	{
		iHqServer.updateTotalQueries(1);
		Statement lStatement;
		String lQuery = "";
		
		//Message structer
		//TableName PhoneID Name Score Date MissionTime Country
		try
		{
			lStatement = iSQLServer.iConnection.createStatement();
			//need to handle a specific case for the time value, since it can be undefined				
			
			// query
			String lColumns = "(PhoneID, Rank, Name, Score, Date, MissionTime, Country)";
			String lValues  ="(\'" + aSplitMessage[1]+ "\', " //PhoneID
							+ aRank + ", "  //Rank, not part of the message but determined by server
							+ "\'"+ aSplitMessage[2] + "\', " //Name
							+ aSplitMessage[3] + ", "//Score
							+ "\'"+ aSplitMessage[4] + "\', "//Date
							+ "\'"+ aSplitMessage[5] + "\', " //MissionTime
							+ "\'"+ aSplitMessage[6] + "\' )"; //Country
			lQuery = "insert into hqsqlhighscoreserverdatabase." + aSplitMessage[0].toLowerCase() + lColumns + " values " + lValues;

			//Query should have proper format now
			lStatement.executeUpdate(lQuery);
			iHqServer.updateSuccessfulQueries(1);
		}
		catch(SQLException eSQLException)
		{
			iHqServer.updateFailedQueries(1);
			printf("Error on Inserting Entry into Highscore table:");
			printf(lQuery);
			printf(eSQLException.getLocalizedMessage());
		}
	}

	public void UpdateHighscoreTables(String aClientMessage)
	{
		String lSplitMessage[] = aClientMessage.split(" ");
		
		//aquire lock for this database table
		iHqServer.AcquireDatabaseHandle(lSplitMessage[0].toLowerCase());
		
		//insert it if it is good enough
		InsertEntryIntoHighscoreTableIfGoodEnough(lSplitMessage);
		
		//release lock
		iHqServer.ReleaseDatabaseHandle(lSplitMessage[0].toLowerCase());
	}
}
