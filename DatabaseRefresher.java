package Server;

import javax.swing.plaf.SliderUI;

public class DatabaseRefresher implements Runnable{
	
	HQInteractiveServer iHqServer;
	boolean iKeepRunning = true;
	
	public DatabaseRefresher(HQInteractiveServer aHqServer)
	{
		iHqServer = aHqServer;
	}
	
	public void Stop()
	{
		iKeepRunning = false;
	}
	
	public void run()
	{
		while(iKeepRunning) //keep running forever
		{
			//Sleep for 6 hours and then refresh the Database Connection
			int lOneSecond = 1000;
			try
			{
			Thread.sleep(lOneSecond * 60 * 60 * 5); //5 hours
			}
			catch(Exception e)
			{
				System.out.println("Database Refresh sleep cycle has been interrupted!");
			}
			
			if(iKeepRunning) //might have changed while we were sleeping
			{
				SQLQuery lSQLQuery = new SQLQuery(iHqServer, 0);
				lSQLQuery.GetEntryFromHighscoreTableByRank("Campaign1Mission1", 1);
				
				//print out complete message
				System.out.println("Database has connections has been refresed!");
			}
		}
	}

}
