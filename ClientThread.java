//Shweta Baskaran
//1001667586

//References
//http://pirate.shu.edu/~wachsmut/Teaching/CSAS2214/Virtual/Lectures/chat-client-server.html


import java.net.*;
import java.io.*;

public class ClientThread extends Thread
{
	private Client       client    = null;
	private Socket           socket    = null;
	private DataInputStream  dis  =  null;
	private DataOutputStream dos = null;
	
	//Parameterized constructor
	public ClientThread(Client client, Socket socket)
	{  
		this.client = client;
	    this.socket = socket;
	}
	
	@SuppressWarnings("deprecation")
	
	//Sends messages(msg) to server in Http format
	public void messageToServer(String msg)
	{   
		try
	    {  
			System.out.println("\nHttp message to Server\n"+msg);
			dos.writeUTF(msg);
	        dos.flush();
	    }
	    catch(IOException ioe)
	    {  
	    	//System.out.println(" ERROR sending: " + ioe.getMessage());
	    	client.display("Server terminated connection. Close window to Exit.");
	        //server.remove(ID);
	    	stop();
	    }
	}
	
	@SuppressWarnings("deprecation")
	
	//This method is invoked when server attempts to read an unparsed String from the server 
	public void inp()
	{
		try
		{
			String unparsedString = dis.readUTF();			//unparsedString - in http format
			System.out.println("\nHttp message from Server:\n"+unparsedString);
			client.messageFromServer(splitunParsedString(unparsedString));	//invokes method to respond to the message received from server after parsing it
		}
		catch(IOException ioe)
		{
			//System.out.println(" Listening ERROR " + ioe.getMessage());
            client.display("Server terminated connection. Close window to Exit.");
			stop();
			//client.stop();
		}
	}
	
	public void run()
	{ 
		while (true)
	    {    
			inp();
	    }
	}
	
	//to split and parse the unparsed String message received from server to obtain the data correctly
	public String splitunParsedString(String unparsedString)
	{
		String parts[] = unparsedString.split("Data:\t");		//https://www.geeksforgeeks.org/split-string-java-examples/
		String parsedString = parts[1];
		return parsedString;	
	}
	
	//Establishes data input and output streams through which communication between client and server could be made possible
	public void establishIOStreams() throws IOException
	{ 
		try
		{
			dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
			dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
		}
		catch(IOException ioe)
		{
			System.out.println("Error creating input output streams"); 
			client.stop();
		}
	}
	
	//terminates data input and output streams by which communication between client and server can be stopped
	public void terminateIOStreams() throws IOException
	{  
		try
		{
			if (socket != null)    
				socket.close();
			if (dis != null)  
				dis.close();
			if (dos != null) 
				dos.close();
		}
		catch(IOException ioe)
		{
			System.out.println("Error closing input output streams");
			client.stop();
		}
	}	
}