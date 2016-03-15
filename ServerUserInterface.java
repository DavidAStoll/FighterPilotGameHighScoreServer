package Server;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.plaf.SliderUI;

public class ServerUserInterface
{
	static HQInteractiveServer iHQServer;
	static Thread iHQInteractiveThread;
	static JFrame iMainWindow;
	static JPanel iButtonPanel;
	static JPanel iButtonSubPanel;
	static JPanel iConnectionPanel;
	static JPanel iConnectionLabelPanel;
	static JPanel iConnectionValuePanel;
	static JPanel iQueryPanel;
	static JPanel iQueryLabelPanel;
	static JPanel iQueryValuePanel;
	static JPanel iBottomPanel;

	static JLabel iDateStarted;
	static JLabel iDateStartedNum;
	static JLabel iTotalConnections;
	static JLabel iTotalConnectionsNum;
	static JLabel iSuccessfulConnections;
	static JLabel iSuccessfulConnectionsNum;
	static JLabel iFailedConnections;
	static JLabel iFailedConnectionsNum;
	
	static JLabel iQueryBlank;
	static JLabel iQueryBlankNum;
	static JLabel iTotalQueries;
	static JLabel iTotalQueriesNum;
	static JLabel iSuccessfulQueries;
	static JLabel iSuccessfulQueriesNum;
	static JLabel iFailedQueries;
	static JLabel iFailedQueriesNum;

	static JButton iStartButton;
	static JButton iStopButton;

	static JTextArea iTextArea;
	static boolean iLogFile;
	static String testString;

	// creating the window
	public static void createWindow()
	{
		iMainWindow = new JFrame("HQ Interactive HighScore Server");
		iMainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	// creating separators for the window
	public static void createPanel()
	{

		//panel diagram
		//|-----------------------------------------------------------------------------------------------------|
		//|iMainWindow                                                                                          |
		//| +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ |
		//| +iButtonPanel                                                                                     + |
		//| + |---------------------------------------------------------------------------------------------| + |
		//| + |iButtonSubPanel                                                                              | + |
		//| + | ++++++++++++++++++++++++++++++++++++++++++++++++++ ++++++++++++++++++++++++++++++++++++++++ | + |
		//| + | +iConnectionPanel                                + +iQueryPanel                           + | + |
		//| + | + |---------------------||---------------------| + + |----------------||----------------| + | + |
		//| + | + |iConnectionLabelPanel||iConnectionValuePanel| + + |iQueryLabelPanel||iQueryValuePanel| + | + |
		//| + | + |---------------------||---------------------| + + |----------------||----------------| + | + |
		//| + | ++++++++++++++++++++++++++++++++++++++++++++++++++ ++++++++++++++++++++++++++++++++++++++++ | + |
		//| + |---------------------------------------------------------------------------------------------| + |
		//| +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ |
		//|                                                                                                     |
		//| +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ |
		//| +BottomPanel                                                                                      + |
		//| +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ |
		//|-----------------------------------------------------------------------------------------------------|

		// initializing panels
		iButtonPanel = new JPanel(new BorderLayout());
		iButtonSubPanel = new JPanel(new BorderLayout());
		iConnectionPanel = new JPanel(new BorderLayout());
		iConnectionLabelPanel = new JPanel(new BorderLayout());
		iConnectionValuePanel = new JPanel(new BorderLayout());
		iQueryPanel = new JPanel(new BorderLayout());
		iQueryLabelPanel = new JPanel(new BorderLayout());
		iQueryValuePanel = new JPanel(new BorderLayout());
		iBottomPanel = new JPanel(new BorderLayout());

		// structuring the panels
		iButtonPanel.add(iButtonSubPanel, BorderLayout.SOUTH);
		iButtonSubPanel.add(iConnectionPanel, BorderLayout.WEST);
		iConnectionPanel.add(iConnectionLabelPanel, BorderLayout.WEST);
		iConnectionPanel.add(iConnectionValuePanel, BorderLayout.EAST);
		iButtonSubPanel.add(iQueryPanel, BorderLayout.EAST);
		iQueryPanel.add(iQueryLabelPanel, BorderLayout.WEST);
		iQueryPanel.add(iQueryValuePanel, BorderLayout.EAST);

		//title of the connection labels
		iDateStarted = new JLabel("Date Started: ", JLabel.CENTER);
		iTotalConnections = new JLabel("Total Connections: ");
		iSuccessfulConnections = new JLabel("Successful Connections: ");
		iFailedConnections = new JLabel("Failed Connections: ");

		//values of the connection labels
		iDateStartedNum = new JLabel(" ");
		iTotalConnectionsNum = new JLabel("0");
		iSuccessfulConnectionsNum = new JLabel("0");
		iFailedConnectionsNum = new JLabel("0");

		//title of the query labels
		iQueryBlank = new JLabel(" ");
		iTotalQueries = new JLabel("Total Queries: ");
		iSuccessfulQueries = new JLabel("Successful Queries: ");
		iFailedQueries = new JLabel("Failed Queries: ");
		
		//values of the query labels
		iQueryBlankNum = new JLabel(" ");
		iTotalQueriesNum = new JLabel("0");
		iSuccessfulQueriesNum = new JLabel("0");
		iFailedQueriesNum = new JLabel("0");
		
		//container to stack up the connection labels
		Container lConnectionLabel = new Container();
		lConnectionLabel.setLayout(new BoxLayout(lConnectionLabel, BoxLayout.Y_AXIS));

		//adding connection labels to the container
		iConnectionLabelPanel.add(lConnectionLabel);
		lConnectionLabel.add(iDateStarted, BorderLayout.WEST);
		lConnectionLabel.add(iTotalConnections, BorderLayout.WEST);
		lConnectionLabel.add(iSuccessfulConnections, BorderLayout.WEST);
		lConnectionLabel.add(iFailedConnections, BorderLayout.WEST);

		//container to stack up the connection values
		Container lConnectionValue = new Container();
		lConnectionValue.setLayout(new BoxLayout(lConnectionValue, BoxLayout.Y_AXIS));

		//adding connection values to the container
		iConnectionValuePanel.add(lConnectionValue);
		lConnectionValue.add(iDateStartedNum, BorderLayout.WEST);
		lConnectionValue.add(iTotalConnectionsNum, BorderLayout.WEST);
		lConnectionValue.add(iSuccessfulConnectionsNum, BorderLayout.WEST);
		lConnectionValue.add(iFailedConnectionsNum, BorderLayout.WEST);
		
		//container to stack up the query labels
		Container lQueryLabel = new Container();
		lQueryLabel.setLayout(new BoxLayout(lQueryLabel, BoxLayout.Y_AXIS));
		
		//adding query labels to the container
		iQueryLabelPanel.add(lQueryLabel);
		lQueryLabel.add(iQueryBlank, BorderLayout.WEST);
		lQueryLabel.add(iTotalQueries, BorderLayout.WEST);
		lQueryLabel.add(iSuccessfulQueries, BorderLayout.WEST);
		lQueryLabel.add(iFailedQueries, BorderLayout.WEST);
		
		//container to stack up the query values
		Container lQueryValue = new Container();
		lQueryValue.setLayout(new BoxLayout(lQueryValue, BoxLayout.Y_AXIS));
		
		//adding query values to the container
		iQueryValuePanel.add(lQueryValue);
		lQueryValue.add(iQueryBlankNum, BorderLayout.WEST);
		lQueryValue.add(iTotalQueriesNum, BorderLayout.WEST);
		lQueryValue.add(iSuccessfulQueriesNum, BorderLayout.WEST);
		lQueryValue.add(iFailedQueriesNum, BorderLayout.WEST);

		iTextArea = new JTextArea();
		iTextArea.setEditable(false);
		iTextArea.setRows(28);
		JScrollPane lScrollPane = new JScrollPane(iTextArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		iBottomPanel.add(lScrollPane, BorderLayout.CENTER);
	}

	// start server button----------------------------------------
	public static void startServerButton()
	{
		iLogFile = true;
		iHQServer = new HQInteractiveServer();
		iHQServer.setDateStarted(printCurrentDateTime());
		iStartButton = new JButton("Start Server");
		iStartButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				iHQServer = new HQInteractiveServer();
				iHQServer.setDateStarted(printCurrentDateTime());
				System.out.println("Server started on:" + printCurrentDateTime());
				iHQServer.startMySQLServer();
				iDateStartedNum.setText(iHQServer.getDateStarted());
				iStartButton.setEnabled(false);
				iHQInteractiveThread = (new Thread(iHQServer));
				iHQInteractiveThread.start();
			}
		});
	}

	// stop server button----------------------------------------
	public static void stopServerButton()
	{
		iStopButton = new JButton("Stop Server");
		iStopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				System.out.println(printCurrentDateTime() + "\n" + "Stop server Button pressed");
				iHQServer.SetIsShuttingDown(true); //will not accept any new clients
				//wait for a bit to make sure that all client connections closed
				try
				{
					System.out.println("Shutting down...");
					Thread.sleep(1000 * 30); //wait for 30 seconds, should be enough time to finish up all clients
				}
				catch(Exception aException)
				{
					System.out.println("Sleeping during shutdown phase failed!");
					System.out.println(aException.getLocalizedMessage());
					System.out.println(aException.getStackTrace());
				}
				
				iHQServer.stopMySQLServer();
				iStartButton.setEnabled(true);
				if(iHQInteractiveThread != null)
				{
					iHQInteractiveThread.interrupt();
					iHQInteractiveThread = null;
				}
				if(iHQServer != null)
				{
					iHQServer.iServerSocketChannel = null;
				}
				System.out.println("Shutdown complete!");
			}
		});
	}

	// adjusting the window to the center of the screen
	public static void PositionWindow()
	{
		iMainWindow.pack();
		iMainWindow.setSize(800, 600);

		Dimension lScreenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension lWindowSize = iMainWindow.getSize();
		int lWindowX = Math.max(0, (lScreenSize.width - lWindowSize.width) / 2);
		int lWindowY = Math.max(0, (lScreenSize.height - lWindowSize.height) / 2);

		iMainWindow.setLocation(lWindowX, lWindowY);
		iMainWindow.validate();
		iMainWindow.setVisible(true);
	}

	// adding stuff to the main window
	public static void addComponentsToMainWindow()
	{
		iButtonPanel.add(iStartButton, BorderLayout.WEST);
		iButtonPanel.add(iStopButton, BorderLayout.EAST);

		iMainWindow.add(iButtonPanel, BorderLayout.NORTH);
		iMainWindow.add(iBottomPanel, BorderLayout.SOUTH);
	}

	public static String printCurrentDateTime()
	{
		DateFormat lDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date lDate = new Date();
		String lCurrentDate = lDateFormat.format(lDate);
		return lCurrentDate;
	}

	public ServerUserInterface() throws IOException
	{
		new ReaderThread2().start();
	}

	class FilteredStream extends FilterOutputStream
	{
		public FilteredStream(OutputStream aStream)
		{
			super(aStream);
		}

		public void write(byte b[], int off, int len) throws IOException
		{
			String lString = new String(b, off, len);
			iTextArea.append(lString);
			// Make sure the last line is always visible
			iTextArea.setCaretPosition(iTextArea.getDocument().getLength());
			// keep the text area down to a certain character size
			int lIdealSize = 10000;
			int lMaxExcess = 500;
			int lExcess = iTextArea.getDocument().getLength() - lIdealSize;
			if (lExcess >= lMaxExcess)
			{
				iTextArea.replaceRange("", 0, lExcess);
			}
			if (iLogFile)
			{
				DateFormat lShortDateFormat = new SimpleDateFormat("yyyy-MM-dd");
				Date lDate = new Date();
				String lCurrentShortDate = lShortDateFormat.format(lDate);
				FileWriter lWriter = new FileWriter(lCurrentShortDate + ".log", true);
				lWriter.write(lString);
				lWriter.close();
			}
		}
	}
	
	class ReaderThread2 extends Thread
	{
		public void run()
		{
			PrintStream lPrintStream = new PrintStream(new FilteredStream(new ByteArrayOutputStream()));
			System.setOut(lPrintStream);
			System.setErr(lPrintStream);
		}
	}

	public static void UpdateTotalConnections(int aValue)
	{
		UpdateIntegerLabel(aValue, iTotalConnectionsNum);
	}

	public static void UpdateSuccessfulConnections(int aValue)
	{
		UpdateIntegerLabel(aValue, iSuccessfulConnectionsNum);
	}

	public static void UpdateFailedConnections(int aValue)
	{
		UpdateIntegerLabel(aValue, iFailedConnectionsNum);
	}
	
	public static void UpdateTotalQueries(int aValue)
	{
		UpdateIntegerLabel(aValue, iTotalQueriesNum);
	}
	
	public static void UpdateSuccessfulQueries(int aValue)
	{
		UpdateIntegerLabel(aValue, iSuccessfulQueriesNum);
	}
	
	public static void UpdateFailedQueries(int aValue)
	{
		UpdateIntegerLabel(aValue, iFailedQueriesNum);
	}
	
	private static void UpdateIntegerLabel(int aValue, JLabel aLabelToUpdate)
	{
		try
		{
			aLabelToUpdate.setText(Integer.toString(aValue));
		}
		catch (Exception aException)
		{
			aException.printStackTrace();
		}
	}

	public static void main(String[] args)
	{
		Runtime.getRuntime().addShutdownHook(new Thread() //this code will run when the application is closed
		{
			public void run()
			{
				//this code will be run when the application closes
				System.out.println("Server closes! Goodbye!");
				iHQServer.SetIsShuttingDown(true); //will not accept any new clients
				
				//wait for a bit to make sure that all client connections closed
				try
				{
					System.out.println("Server is closing...");
					Thread.sleep(1000 * 30); //wait for 30 seconds, should be enough time to finish up all clients
					System.out.println("Server closed down!");
				}
				catch(Exception aException)
				{
					System.out.println("Sleeping during Closing phase failed!");
					System.out.println(aException.getLocalizedMessage());
					System.out.println(aException.getStackTrace());
				}
			}
		});
		
		createWindow();
		createPanel();
		startServerButton();
		stopServerButton();
		addComponentsToMainWindow();
		PositionWindow();
		try
		{
			new ServerUserInterface();
		}
		catch (IOException aIOException)
		{
			aIOException.printStackTrace();
		}
	}
}
