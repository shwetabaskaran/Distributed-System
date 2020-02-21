//Shweta Baskaran
//1001667586

//References
//http://pirate.shu.edu/~wachsmut/Teaching/CSAS2214/Virtual/Lectures/chat-client-server.html

import java.net.*;
import java.io.*;

public class ServerThread extends Thread
{  
	private Server       server    = null;
	private Socket           socket    = null;
	private int              clientID        = -1;
	private DataInputStream  dis  =  null;
	private DataOutputStream dos = null;
	String clnam = null;
	String unParsed = null;
	
	public ServerThread(Server _server, Socket _socket)
	{  
		super();
	    server = _server;
	    socket = _socket;
	    clientID     = socket.getPort();
	}
	
	@SuppressWarnings("deprecation")
	
	//Sends messages(msg) to client in Http format
	public void messageToClient(String clnam,String msg)
	{  
		try
	    {  
			ServerGUI.updateToClient(clnam,msg);
			dos.writeUTF(msg);
	        dos.flush();
	    }
	    catch(IOException ioe)
	    {  
	    	//System.out.println(clientID + " ERROR sending: " + ioe.getMessage());
	    	ServerGUI.updateServer("Client "+clnam+ " terminated connection");
	        server.removeClient(clientID,clnam);
	        stop();
	    }
	}
	
	//returns the ID of the thread requesting for ID
	public int getClientID()
	{
		return clientID;
	}
	
	//This method parses the 'unparsedString' to find where the actual data is present in the encoded http string and returns parsed String
	public String splitunParsedString(String unparsedString)
	{
		String parts[] = unparsedString.split("Data:\t");
		String parsedString = parts[1];
		return parsedString;	
	}
	
	//to read the first client name provided by the client and send it to 'cn' method to check validity of the client name
	public void run()
	{ 
		try
		{
			unParsed = dis.readUTF();
			clnam = splitunParsedString(unParsed);
			ServerGUI.updateFromClient(clnam,unParsed);
			server.cn(clnam,getClientID());
	   	}
		catch(Exception e)
		{}
		System.out.println("Server Thread " + clientID + " running for "+clnam);
	}
	@SuppressWarnings("deprecation")
	
	//for reading the unparsed http message from client, parsing it and sending to 'messageFromClient' to respond to input received from client
	public void input(String clnam)
	{
		while (true)
	    {
			try
	        {
				String unParsed = dis.readUTF();
				String msgFromClient = splitunParsedString(unParsed);
				ServerGUI.updateFromClient(clnam,unParsed);
				server.addToQueue(clientID,msgFromClient,clnam);
				//server.messageFromClient(clientID, msgFromClient,clnam);
	        }
	        catch(IOException ioe)
	        {  
	        	//System.out.println(clientID + " ERROR reading: " + ioe.getMessage());
	        	ServerGUI.updateServer("Client "+clnam+ " terminated connection");
	        	server.removeClient(clientID,clnam);
	            stop();
	        }
	    }
	}
	@SuppressWarnings("deprecation")
	
	//To read the client name provided by the client after the previous name was stated invalid by the server, invoke the cn method and send the client name to verify validity
	public void in(int clientID, String clnam)
	{
		try
		{
			String unParsed = dis.readUTF();
			ServerGUI.updateFromClient(clnam, unParsed);
			server.cn(splitunParsedString(unParsed),clientID);
		}
		catch(IOException ioe)
		{
			//System.out.println(clientID + " ERROR reading: " + ioe.getMessage());
			ServerGUI.updateServer("Client "+clnam+ " terminated connection");
			server.removeClient(clientID,clnam);
            stop();
		}
	}
	
	//Establishes data input and output streams through which communication between client and server could be made possible
	public void establishIOStreams() throws IOException
	{  
		dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
	    dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
	}
	
	//terminates data input and output streams by which communication between client and server can be stopped
	public void terminateIOStreams() throws IOException
	{
		if (socket != null)    
			socket.close();
	    if (dis != null)  
	    	dis.close();
	    if (dos != null) 
	    	dos.close();
	}
}
	
	