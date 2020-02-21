//Shweta Baskaran
//1001667586

//References
//https://www.geeksforgeeks.org/multi-threaded-chat-application-set-1/
//https://www.geeksforgeeks.org/multi-threaded-chat-application-set-2/
//http://pirate.shu.edu/~wachsmut/Teaching/CSAS2214/Virtual/Lectures/chat-client-server.html
//https://www.jmarshall.com/easy/http/
//https://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html

import java.net.*;
import java.text.SimpleDateFormat;
import javax.swing.JFrame;
import java.io.*;
import java.util.*;
import java.awt.*;

public class Server implements Runnable
{
	private ServerThread clients[] = new ServerThread[50];
	private SleepThread st = new SleepThread(this);					//thread that is used to maintain the queue of clients
	public LinkedHashMap<String, LinkedHashMap<Integer, String>> outerMap = new LinkedHashMap<String, LinkedHashMap<Integer, String>>();
	ArrayList<String> clientList = new ArrayList<String>();			//https://stackoverflow.com/questions/4494018/if-multiple-threads-are-updating-the-same-variable-what-should-be-done-so-each
	private ServerSocket server = null;
	public Thread       thread = null;
	private int numOfClients = 0;
	boolean f = false;
	
	/* Constructor	:	Server
	 * Purpose	:	Sets up the server and makes it available for connection with a client
	 * Input	:	port - on which the server will be available for client to connect
	 * Output	:	no output is returned
	 * Functionality	:	A server socket is created where server listens for connection requests
	 	* from client. It starts a new thread('thread') for every client attempting to connect to the Server*/
	public Server(int port)
	{
		try
	    {
			ServerGUI.updateServer("Binding to port... please wait");
	        server = new ServerSocket(port);  
	        ServerGUI.updateServer("Server up and running");
	        ServerGUI.updateServer("Waiting for a client...");
	        startThread(); 
	    }
	    catch(IOException ioe)
	    {   
	    	ServerGUI.updateServer("Server could not bind to port "+": " + ioe.getMessage());
	    }
	}
	
	/* Method	:	run	
	 * Purpose	:	it is the first method that runs in this class
	 * Input	:	no inputs are provided
	 * Output	:	no outputs are returned
	 * Functionality	:	Once a server accepts a client connection, method addClient is called and the details about 
	 	* client accepted(socket) is sent to that method */
	public void run()
	{  
		while (thread != null)
	    {
			try
	        { 
	         	addClient(server.accept());
	        }
	        catch(IOException ioe)
	        {
	        	ServerGUI.updateServer("Server accept error: " +ioe);
	        	stopThread(); 
	        }
	     }
	}
	
	/* Method	:	startThread
	 * Purpose	:	to start a thread 'thread' that is assigned for a client that is connected to the server
	 * Input	:	no inputs
	 * Output	:	no outputs returned
	 * Functionality	:	it is called everytime a new client connection is accepted and starts a thread for that client
	 */
	public void startThread()  
	{
		if (thread == null)
		{  
			thread = new Thread(this); 
			thread.start();
		}
	}
	
	@SuppressWarnings("deprecation")
	
	/* Method	:	stopThread	
	 * Purpose	:	to stop a running thread in order to end connections with a client
	 * Inputs	:	no inputs
	 * Outputs	:	no outputs
	 */
	public void stopThread()   
	{
		if (thread != null)
		{
			thread.stop(); 
			thread = null;
		}
	}
	
	/* Method	:	returnClient
	 * Purpose	:	returns the thread ID for the particular client
	 * Inputs	:	clientID - position of the thread for the client in an array of thread IDs
	 * Outputs	:	i(integer value that returns position if thread for the client is present, if not returns -1) 
	 */
	private int returnClient(int clientID)
	{
		for (int i = 0; i <numOfClients; i++)
	    if (clients[i].getClientID() == clientID)
	    	return i;
	    return -1;
	}
	
	/* Method	:	cn	
	 * Purpose	:	To respond to the client name provided by the client to the server
	 * Inputs	:	input(String that contains the name provided by the client for the user), clientID(thread ID of the client in the array)
	 * Outputs	:	no outputs returned
	 * Functionality	: 	This method first check if the client name already exists in the clientList(array list)
	 	* that it maintains. If not, it sends a Success message to the client, adds the new client to its client 
	 	* list and calls the 'input' method that reads the random number provided by client on successful connection.
	 	* If not, it responds to the client with a failure message and invokes the 'in' method that waits for a new valid 
	 	* client name. 
	 */
	public void cn(String input, int clientID)
	{
		boolean b = true;													//set to false if client name is not valid
		if(clientList.isEmpty())
		{
			int i = returnClient(clientID);									//i - position of thread in array of threads for that client
			clients[i].messageToClient(input,toHttpFormat("Success",2));	//sends a success message to client after encoding in http format
			ServerGUI.updateServer("Server connected with " + input);
			
			clientList.add(input);											//clientList- arraylist that maintains list of connected clients
			ServerGUI.updateClient(clientList);	
			clients[i].input(input);										//to read random number generated by client

		}
		else
		{
			for(String str : clientList)										//https://stackoverflow.com/questions/16218863/java-return-if-list-contains-string
			{
				if(str.trim().equalsIgnoreCase(input.toLowerCase()))		//str - to read each client in the client list
				{
	   				b = false;
	   				break;	
				}
			}
			if(b==false)
			{
				int i = returnClient(clientID);
   				clients[i].messageToClient(input,toHttpFormat("Failure",2));
				clients[i].in(clientID,input);
			}
			else
			{
				int i = returnClient(clientID);
	   			clients[i].messageToClient(input,toHttpFormat("Success",2));
				ServerGUI.updateServer("Server connected with " + input);
				clientList.add(input);
				ServerGUI.updateClient(clientList);	
				clients[i].input(input);
			}
		}
	}
	
		/* Method	:	toHttpFormat
		 * Purpose	:	to convert messages(sent by the server to the client) into Http format
		 * Inputs	:	plainText(message to be sent to the client from the server), choice(integer value that decides if it is of GET(1) or POST(2) type)
		 * Outputs	:	httpText(plainText appended with details for sending in http format- Type String)
		 * Functionality	:	This method obtains the current date and time and sets it into Http format
		 	* and then appends it to http details. It also checks if the message to be sent is polling the 
		 	* server(GET-1) or supplying data to the server(POST-2) and appropriately appends to Http encoded
		 	* message * */
	public String toHttpFormat(String plainText, int choice)
	{
		String date;
		String messageType = null;
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
		dateFormat.setTimeZone(TimeZone.getTimeZone("CST"));
		date = dateFormat.format(calendar.getTime());
		
		if(choice==1)
			messageType = "GET";
		else if(choice==2)
			messageType = "POST";
		
		String httpText = messageType+"\t HTTP/1.1"+
				"\nHost:\t127.0.0.1:6000"+
				"\nUser-Agent:\tServer"+
				"\nContent-Type:\ttext/plain"+
				"\nContent-Length:\t"+plainText.length()+
				"\nDate:\t"+ date +
				"\nData:\t"+plainText;
		return httpText;
		
		//https://stackoverflow.com/questions/7707555/getting-date-in-http-format-in-java
	}
	
	
	/* Method	:	addToQueue
	 * Purpose	:	to add client threads to a queue made of a LinkedHashMap data structure
	 * Inputs	:	clientID(ID of the thread for that client), msgFromClient(parsed random number(of type string) generated by the client and sent to server),clnam(name of the client that sent the random num)
	 * Outputs	:	no outputs returned
	 * Functionality	:  This method adds clientID, random number and client name of an input client to a LinkedHashMap that is
	 	* used here as a queue - A LinkedHashMap is used to queue contents of more than one data type. Since three data items
	 	* are required to be maintained in the queue, a combination of Hash Map and Queues(Nested LinkedHashMap) is used.
	 	* The first time an entry is added to the queue, a thread handling the queue is started to be active which contiues
	 	* handling the entries in the queue one by one */ 
	public void addToQueue(int clientID,String msgFromClient,String clnam)
	{
		outerMap.put(clnam, new LinkedHashMap(){{put(clientID, msgFromClient);}});		//The new client which generated the random number is added to the queue(LinkedHashMap) in the order : clientname, clientID, random number	
		if(f==false)
			st.start();																	//thread for handling the queue of clients is started upon addition of first entry into the queue(LinkedHashMap)
		f = true;
	}
	
	
	/* Method	:	messageFromClient
	 * Purpose	:	for appropriate response by server depending on the message from client
	 * Inputs	:	clientID(ID of the thread for that client), input(parsed random number(of type string) generated by the client and sent to server),clnam(name of the client that sent the random num)
	 * Outputs	:	no outputs returned
	 * Functionality	: This method first converts the parsed String type number sent by client to integer and sleeps for that 
	 	* many seconds and then responds to that client that it waited for so many seconds. If it detects that it is not able to
	 	* send the response because client has terminated connection, then it also terminates the thread running for that client 
	 	* and removes the client from its client list  */
	public void messageFromClient(int clientID, String input, String clnam)
	{
		if(input!=null)
		{
			try
			{
				int sleepTime = Integer.parseInt(input)*1000;
	        	ServerGUI.updateServer("Server waiting for "+input+" seconds for "+clnam);
	   			Thread.sleep(sleepTime);
	   			String msg = "Server waited for "+input+" seconds for "+clnam;
	   			if(server!=null)
	   			{
	   				try
	   				{
	   					int i = returnClient(clientID);
	   					clients[i].messageToClient(clnam,toHttpFormat(msg,2)); 
	   				}
	   				catch(Exception e)
	   				{}
	   			}
	   			else
	   			{
	   				ServerGUI.updateServer("Client " +clnam+ " terminated connection");
	   				removeClient(clientID,clnam);
	   			}
	        }
			catch(InterruptedException e)
			{
				ServerGUI.updateServer("Server failed to wait for "+input+" seconds.");
			}
		}
		else
		{
			removeClient(clientID,clnam);
		}
		outerMap.remove(clnam);									//once server has responded to the client after waiting, client is removed from the queue(LinkedHashMap)
	}
	
	/* Method	:	addClient	
	 * Purpose	:	starts a new thread for the client accepted and allows thread to start
	 * Inputs	:	socket - assigned for the connected client to communicate
	 * Outputs	:	no outputs returned
	 * Functionality	: Displays that a new client is accepted over a said socket and establishes 
	 	* input output streams for connection with client and also allows the thread allocated for 
	 	* the client to start. */
	private void addClient(Socket socket)
	{  
		if (numOfClients < clients.length)
	    {  
			ServerGUI.updateServer("Client accepted: "+ socket);
	        clients[numOfClients] = new ServerThread(this, socket);  
	        try
	        {
	        	clients[numOfClients].establishIOStreams();
	            clients[numOfClients].start(); 
	            numOfClients++; 
	        }
	        catch(IOException ioe)
	        {
	        	ServerGUI.updateServer("Error allocating thread: "+ ioe);
	        }
	    }
	    else
			ServerGUI.updateServer("Could not add client, maximum " + clients.length + " reached.");
	}
			
	@SuppressWarnings("deprecation")
	
	/* Method	:	removeClient	
	 * Purpose	:	to remove a client for list of connected clients and terminated IO Streams for communication with that client
	 * Inputs	:	clientID(ID of thread allocated for the client to be removed),clnam(name of client to be removed)
	 * Outputs	:	no outputs returned */
	public void removeClient(int clientID, String clnam)
	{
		int pos = returnClient(clientID);
		if (pos >= 0)
	    {
			clientList.remove(clnam);
			ServerGUI.updateClient(clientList);
			ServerThread toRemove = clients[pos];
	        //ServerGUI.updateServer("Removing client thread " + clientID + " at " + pos + " for "+clnam);
	        if (pos < numOfClients-1)
	        	for (int i = pos+1; i < numOfClients; i++)
	        		clients[i-1] = clients[i];
	        numOfClients--;
	        ServerGUI.updateServer("Client '"+clnam+"' removed from clientlist.");
	        try
	        {  
	        	toRemove.terminateIOStreams();
	        }
	        catch(IOException ioe)
	        {
	        	//ServerGUI.updateServer("Error closing thread: " + ioe);
	        }
	        toRemove.stop(); 
	    }
	}
	
	//The main method embeds the applet defined in ServerGUI class into a JFrame.
	//Properties of the JFrame are defined
	//Server socket is invoked
	public static void main(String args[]) 
	{ 
		ServerGUI obj = new ServerGUI();
		JFrame Frame = new JFrame();		//https://stackoverflow.com/questions/22315193/how-to-call-an-applet-from-another-classs-main-method
		obj.setSize(600, 420);
		Frame.setPreferredSize(new Dimension(650,470));			//https://stackoverflow.com/questions/9536804/setsize-doesnt-work-for-jframe				
		Frame.getContentPane().add(obj);
		Frame.pack();
		Frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);		//https://stackoverflow.com/questions/246228/why-does-my-application-still-run-after-closing-main-window
		Frame.setVisible(true);
		obj.init();
		new Server(6000);  
	}
}