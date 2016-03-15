package Server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLServer
{
	Connection iConnection = null;
	
	SQLServer()
	{
		//constructor does nothing
	}
	
	//testing method which prints out the asdf table
	public void printEntireTable(Connection aConnection)
	{
		// get statement from the connection
		Statement lStatement;
		try
		{
			lStatement = aConnection.createStatement();

			// for mysql the select statement is different compare to other
			// database software
			// SELECT * FROM database.schema.table
			// query
			ResultSet lResult = lStatement.executeQuery("select * from test.asdf");
			ResultSetMetaData lResultMeta = lResult.getMetaData();
			int lColumnCount = lResultMeta.getColumnCount();
			System.out.println("number of columns:" + lColumnCount);
			for (int i = 1; i < lResultMeta.getColumnCount() + 1; i++)
			{
				// formatting table titles using tabs
				if (i > 1 && i < lResultMeta.getColumnCount() + 1)
				{
					System.out.print("\t");
				}
				System.out.print(lResultMeta.getColumnName(i));
			}
			// end of printing column name
			System.out.println();
			// loop through the result
			while (lResult.next())
			{
				for (int i = 1; i < lResultMeta.getColumnCount() + 1; i++)
				{
					// formatting data values using tabs
					if (i > 1 && i < lResultMeta.getColumnCount() + 1)
					{
						System.out.print("\t");
					}
					System.out.print(lResult.getString(i));
				}
			}
			// end of printing table
			System.out.println();
			// close the result set and statement
			lResult.close();
			lStatement.close();
		}
		catch (SQLException aSQLException)
		{
			aSQLException.printStackTrace();
		}
	}
	
	//connects to mysql
	public void startMySQLServer()
	{
		iConnection = null;
		String url = "jdbc:mysql://localhost:3306/";
		String dbName = "hqsqlhighscoreserverdatabase";
		String driver = "com.mysql.jdbc.Driver";
		String userName = "root";
		String password = "";
		try
		{
			Class.forName(driver).newInstance();
			iConnection = DriverManager.getConnection(url + dbName, userName, password);
			System.out.println("Connected to the database");
			//testing print out the table asdf
			//printEntireTable(iConnection);
		}
		catch (Exception aException)
		{
			aException.printStackTrace();
		}
	}
	
	//disconnects from mysql
	public void stopMySQLServer()
	{
		try
		{
			// close the connection
			iConnection.close();
			System.out.println("Disconnected from database");
		}
		catch (SQLException aSQLException)
		{
			aSQLException.printStackTrace();
		}
	}
}
