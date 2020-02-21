//Shweta Baskaran
//1001667586

//References
//https://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html
//https://github.com/aboullaite/Multi-Client-Server-chat-application/tree/master/javaSwing-Server_Client/src/aboullaite
//https://www.geeksforgeeks.org/multi-threaded-chat-application-set-1/
//https://www.geeksforgeeks.org/multi-threaded-chat-application-set-2/
//http://pirate.shu.edu/~wachsmut/Teaching/CSAS2214/Virtual/Lectures/chat-client-server.html
//http://makemobiapps.blogspot.com/
//https://www.jmarshall.com/easy/http/


import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.io.*;
import java.applet.*;
import java.awt.*;

public class Client extends Applet
{
	private Socket socket              = null;
	private ClientThread client    = null;
	private ClientThread client2   = null;
	private TextArea  updates = new TextArea();					//For the updates about connection with Server
	private TextField clientInput   = new TextField();			//For the user to provide a name
	private Button    connect = new Button("Connect"),			//To establish connection with Server
					  register = new Button("Register"),		//To provide the Server with client name
					  start    = new Button("Start"), 			//To start generating random numbers				
                      stop    = new Button("Stop");				//To stop generating random numbers and end the connection
	private String    serverName = "localhost";
	private int       serverPort = 6000;
	private String clientname = null;
	private long startTime = 0;									//Starts the timer when random number is sent to the server
	private long elapsedTime = 0;								//total time taken by the server to respond to the client request in nanoseconds
	private double seconds = 0.0;								////total time taken by the server to respond to the client request in seconds

	/* Method	:	init
	 * Purpose	:	init method is the first method that is called in an Applet. It initializes the components in the applet.
	 * Inputs	:	No inputs are passed to this method
	 * Outputs	:	Does not return any value
	 * Functionality	:	Sets size of window, sets layout and adds components- buttons(start,
	 	* connect, register and stop that help user carry out different tasks, Text area(updates)
	 	* to display the status of connection with server, interactions with the server and 
	 	* Text box(clientInput) for the user to provide input(clientname) */
	public void init()											
	{
		this.setSize(new Dimension(350,180));					//Sets size of the applet window
		updates.setEditable(false);								//Makes the TextArea(updates) 'read-only'
		
		Panel buttons = new Panel(); 							//Adding a set of buttons for user control on the process
		buttons.setLayout(new GridLayout(1,2)); 
		buttons.add(connect);
		buttons.add(register);
		buttons.add(start);
		buttons.add(stop);
		
		Panel panel = new Panel(); 
		panel.setLayout(new BorderLayout());
		panel.add("Center", clientInput);  panel.add("West", buttons);
		
		Label title = new Label("Random Number Generation", Label.CENTER);
		title.setFont(new Font("Verdana", Font.BOLD, 16));
		
		setLayout(new BorderLayout());
		add("North", title); add("Center", updates);  add("South",  panel);
		stop.setEnabled(false); start.setEnabled(false); register.setEnabled(false);
		clientInput.setEditable(false);
		display("Click on Connect to begin");
	}	
	
	
	/* Method	:	action
	 * Purpose	:	enables user to interact with the API using components(like buttons)
	 * Inputs	:	event - performed by the user, object - data provided by user
	 * Outputs	:	boolean value(in this case, always true)
	 * Functionality	:	This method invokes the appropriate methods depending on the action 
	 	*performed by the user(here, depending on which button is clicked) 		
	 * */
	public boolean action(Event e, Object o)
	{
		if(e.target == connect)
		{
			//Establishes connection with Server through port
			connect(serverName,serverPort);					//serverName(Stores name of server, here it is 'localhost'), server port(port for communicating with server-here, it holds value 6000)		
		}
		
		else if(e.target == register)
		{
			//Receives the client name entered by the user
			clientname = clientInput.getText();				
			if(clientname.isEmpty())
				display("Enter a client name.");			//Makes sure that client name is entered and the textbox 'clientInput' is not left empty
			else
				register(clientname);						//Checks with the server if entered client name is valid to be used 
		}
		else if(e.target == start)
		{
			start.setEnabled(false);
			stop.setEnabled(true);
			/*A second thread 'client2' is created - to generate and send random numbers to the server automatically one after the other
			while the first thread 'client' monitors activity on GUI and waits for the user to stop the process of generating random numbers*/
			createThread2();									
			generateRandom();
			display("Generating random numbers!");
		}
		else if(e.target == stop)
		{
			if(client2.isAlive())
				terminateThread2();								//Terminates thread 'client2' that is generating random numbers automatically
			else
			{
				terminateThread();								//Terminates thread 'client' that was running earlier to detect activity on GUI after user registered client name
				display("Connection Terminated! Close window to exit.");
				stop.setEnabled(false);
			}
		}
		return true;
	}
	
	/* Method	:	connect
	 * Purpose	:	To establish connection with the Server
	 * Input	:	serverName(IP address of host(server)-localhost with IP address 127.0.0.1), serverPort(Port number) 
	 * Output	:	Does not return any value/Displays update about connection with server on GUI
	 * Functionality	:	A socket(socket) is created for connection, a thread(client)is created to handle the connection and monitor user inputs 
	 	* from client from time to time. Buttons for next course of action are enabled and message of success or failure on establishing connectoin
	 	* with server is displayed on the GUI */
	public void connect(String serverName,int serverPort)
	{
		display("Establishing connection. Please wait ...");
	    try
	    { 
	    	socket = new Socket(serverName, serverPort);					//socket is created for connection between client and server
	    	//Thread 'client' created to handle the client process - for sending client name and managing activity on GUI(check if user clicks 'stop')
	    	createThread();													
	    	connect.setEnabled(false);
	    	register.setEnabled(true);
	    	clientInput.setEditable(true);									//Allows user to enter client name
	    	display("Connection established with Server!\nEnter client name in the box and click on Register.");
	    }
	      catch(UnknownHostException uhe)
	      {  
	    	  display("Host unknown: " + uhe.getMessage()); 
	      }
	      catch(IOException ioe)
	      {  
	    	  display("Unexpected exception: " + ioe.getMessage()); 
	      }
	}
	
	@SuppressWarnings("deprecation")
	
	/* Method	:	register
	 * Purpose	:	to read the client name provided by the user and send it to the Server
	 * Inputs	:	clientname(entered by the user on the client GUI in the textbox - clientInput)
	 * Outputs	:	no output is returned by this method. Thread 'client' is started that will monitor client name being sent and user activity on GUI
	 * Functionality	: 	This method reads the client name provided in the text box by the user
	 	* sends it to the server for checking if it is valid after converting to Http format. This method
	 	* also results in the thread 'client' being started to monitor the transfer of user name to server
	 	* and will continue to monitor activity on GUI once thread 'client2' starts generating 
	 	* random numbers */
	public void register(String clientname)										
	{
			client.messageToServer(toHttpFormat(clientname,2));		//Sends client name to server after converting it to Http format
			
			if(client.isAlive()==false)								
			{
				client.start();										//The thread 'client' is started after server confirms validity of client name 
			}
			else
			{
				client.resume();									//Thread 'client' is resumed after temporarily being suspended in case of an invalid client name entered by user
			}
	}
	
	
	/* Method	:	messageFromServer
	 * Purpose	:	For appropriate response by client depending on the message from server
	 * Inputs	:	msg(The string returned by the server in response to client name or random number sent by the client)
	 * Functionality	:	This method first checks response by the server to the client name 
	 	* sent by client. If valid(returns 'Success'),client allows user to start generating random numbers. If not('Failure'), it asks user
	 	* to enter another valid client name. */
		public void messageFromServer(String msg)
		{  
			/*Server responds with 'Failure' message when 
			the client name entered by the user already exists in the 
			client list that the server maintains of all connected clients*/
			if(msg.equals("Failure"))				 			 
			{
				display("Client exists! Enter another client name");
				client.interrupt();								//Thread that works on sending client name is paused so that it can resume once user enters another name for client
				clientInput.setText("");						//Clears invalid client name entered previously by the user
			}
			
			/*Server responds with a 'Success' message when user enters a valid client name-
			  a name that doesn't already exist in the client list that server maintains*/
			else if(msg.equals("Success"))
			{
				connect.setEnabled(false);						//disable button to make a connection with already connected server
				register.setEnabled(false);						//Since client name has already been registered with user
		        start.setEnabled(true);  						//Since a valid client name is registered, 'start' button is enabled to allow users to start generating the random number
		        clientInput.setEditable(false);
		        display("Client registered successfully with Server.");
		        display("Click on Start to begin generating random numbers.");
			}
		    else
		    {
		    	elapsedTime = (System.nanoTime() - startTime);	
		    	seconds = (double)elapsedTime / 1000000000.0;	//https://stackoverflow.com/questions/924208/how-to-convert-nanoseconds-to-seconds-using-the-timeunit-enum/16738065
		   		display(msg);									//Displays updates from server(about the time it spent waiting for the client) on the client GUI 
		    	display("Total wait time : "+(int)seconds +" seconds");
		   		generateRandom();								//After client responds that it waited for a said period of time, client generates a random number again
		   	}
		}
	
	//Converts all messages to be sent to the server into Http format and returns the message(of type String) in http format
	/* Method	:	toHttpFormat
	 * Purpose	:	to convert messages(sent by the client to the server) into Http format
	 * Inputs	:	plainText(message to be sent to the server from the client), choice(integer value that decided if it is of GET(1) or POST(2) type)
	 * Outputs	:	httpText(plainText appended with details if sent in http format- Type String)
	 * Functionality	:	This method obtains the current date and time and sets it into Http format
	 	* and then appends it to http details. It also checks if the message to be sent is polling the 
	 	* server(GET-1) or supplying data to the server(POST-2) and appropriately appends to Http converted
	 	* message * */
	public String toHttpFormat(String plainText,int choice)					
	{
		String date;
		String messageType=null;
		Calendar calendar = Calendar.getInstance();
		//https://stackoverflow.com/questions/7707555/getting-date-in-http-format-in-java
		SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
		//The time is set according to the time zone(In this case, CST for Texas)
		dateFormat.setTimeZone(TimeZone.getTimeZone("CST"));			//https://stackoverflow.com/questions/9429357/date-and-time-conversion-to-some-other-timezone-in-java
		date = dateFormat.format(calendar.getTime());
		
		if(choice==1)
			messageType = "GET";
		else if(choice ==2)
			messageType = "POST";
		
		//Encoding the message to Http Format
		String httpText = messageType+"\t/ HTTP/1.1"+
				"\nHost:\t127.0.0.1:"+serverPort+
				"\nUser-Agent:\tClient"+
				"\nContent-Type:\ttext/plain"+
				"\nContent-Length:\t"+plainText.length()+
				"\nDate:\t"+ date +
				"\nData:\t"+plainText;
		return httpText;
	}
	
	/* Method	:	generateRandom
	 * Purpose	:	to generate a random number between 5 and 15 and send it to the server
	 * Inputs	:	no inputs are provided
	 * Outputs	:	Method does not return any values
	 * Functionality	:	This method generates a random number between 5 and 15, converts it to a string format
	 	* and passes it to the server after converting it into the Http format	 */
	public void generateRandom()
	{
	   int randomNumber = 3+(int)(Math.random()*((10-3)+1));	//randomNumber - Stores randomNumber generated
	   String randomNum = Integer.toString(randomNumber);		//randomNum - stores random number in String format
	   client2.messageToServer(toHttpFormat(randomNum,2));		//calls a method to convert the (string)random number to Http format
	   startTime = System.nanoTime();
	}
	
	/* Method	:	createThread
	 * Purpose	:	to create a thread 'client' and establish Input Output Streams for the thread to communicate with the server
	 * Inputs	:	no inputs are provided
	 * Outputs	:	no outputs are returned by the method
	 * Functionality	:	Creates a thread 'client' that communicates with server over the socket*/
	public void createThread()
	{   
		client = new ClientThread(this, socket); 				//establishes a connection between client and server to pass data through the socket
		try
	    {
			client.establishIOStreams();						//Thread 'client' sets up IO streams for data transfer between client and server
	    }
		catch(IOException ioe)
	    {
			display("Error opening input output streams");
		}
	}

	@SuppressWarnings("deprecation")
	
	/* Method	:	terminateThread
	 * Purpose	:	to terminate a running thread(client) that helps maintain connection with the server
	 * Inputs	:	no inputs are provided to the function
	 * Outputs	:	no outputs are returned
	 * Functionality	:	terminates the Input Output streams and stops the running thread(client)
	 	 * to end connection between client and server*/
	public void terminateThread()
	{
		try
		{
			client.terminateIOStreams(); 						//closes IO streams- data cannot be exchanged between client and server
		}
		catch(IOException ioe)
		{
			System.out.println("Error Closing");
		}
	   client.stop();
	   //display("Connection Terminated");
	}
	
	/* Method	:	createThread2
	 * Purpose	:	to create a thread client2 and establish Input Output Streams for the thread to send random number to and receive response from the server
	 * Inputs	:	no inputs are provided
	 * Outputs	:	no outputs are returned by the method
	 * Functionality	:	Creates a thread 'client2' that sends random numbers to server and receives its response*/
	public void createThread2()
	{   
		client2 = new ClientThread(this, socket); 
		try
	    {
			client2.establishIOStreams();
	    }
		catch(IOException ioe)
	    {
			display("Error opening input output streams");
		}
	}

	@SuppressWarnings("deprecation")
	
	/* Method	:	terminateThread2
	 * Purpose	:	to terminate a running thread(client2) that send random numbers to and receive response from the server
	 * Inputs	:	no inputs are provided to the function
	 * Outputs	:	no outputs are returned
	 * Functionality	:	terminates the Input Output streams and stops the running thread(client2)
	 	 * to stop generating random numbers to the server*/
	public void terminateThread2()
	{
		try
		{
			client2.terminateIOStreams(); 
		}
		catch(IOException ioe)
		{
			System.out.println("Error Closing");
		}
	   client2.stop();
	   stop.setEnabled(false);
	   display("Connection Terminated. Close window to exit.");
	}
	
	/* Method	:	display
	 * Purpose	:	to display messages on the client GUI
	 * Input	:	msg(message to be displayed on the client GUI)
	 * Output	:	No output is returned
	 * Functionality	:	Print a message to client user on a new line in the 'updates' textArea
	 */
	public void display(String msg)
	{
		updates.append(msg + "\n");		//https://kodejava.org/how-do-i-append-text-to-jtextarea/
	}
}