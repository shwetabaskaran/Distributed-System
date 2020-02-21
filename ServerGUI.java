//Shweta Baskaran
//1001667586

import java.applet.*;
import java.awt.*;
import java.util.*;

public class ServerGUI extends Applet
{
	private static TextArea updates = new TextArea();			//to display updates about connection with client like connections, terminations, server wait times, etc
	private static TextArea clientList = new TextArea();		//to display clients connected to the server
	private static TextArea toClient = new TextArea();			//to display unParsed Http messages sent from server to client
	private static TextArea fromClient = new TextArea();		//to display unParsed Http messages sent from client to server
	
	public void init()
	{
		updates.setEditable(false);
		clientList.setEditable(false);
		toClient.setEditable(false);
		fromClient.setEditable(false);
		
		Label title = new Label("Server", Label.CENTER);
		title.setFont(new Font("Verdana", Font.BOLD, 16));
		Label leftTitle1 = new Label("Server Updates", Label.CENTER);
		leftTitle1.setFont(new Font("Verdana", Font.BOLD, 14));
		Label rightTitle1 = new Label("List of clients connected", Label.CENTER);
		rightTitle1.setFont(new Font("Verdana", Font.BOLD, 14));
		Label leftTitle2 = new Label("Http Messages to client", Label.CENTER);
		leftTitle2.setFont(new Font("Verdana", Font.BOLD,14));
		Label rightTitle2 = new Label("Http Messages from client", Label.CENTER);
		rightTitle2.setFont(new Font("Verdana", Font.BOLD,14));
		
		Panel left1 = new Panel();
		left1.setLayout(new BorderLayout());
		left1.add("North",leftTitle1);
		left1.add("South",updates);
		
		Panel right1 = new Panel();
		right1.setLayout(new BorderLayout());
		right1.add("North",rightTitle1);
		right1.add("South",clientList);
		
		Panel left2 = new Panel();
		left2.setLayout(new BorderLayout());
		left2.add("North",leftTitle2);
		left2.add("South",toClient);
		
		Panel right2 = new Panel();
		right2.setLayout(new BorderLayout());
		right2.add("North",rightTitle2);
		right2.add("South",fromClient);
		
		Panel panel = new Panel();
		panel.setLayout(new GridLayout(2,2));
		panel.add(left1); panel.add(right1); panel.add(left2); panel.add(right2);
		
		setLayout(new BorderLayout());
		add("North",title); 
		add("South",panel);
	}
	
	//Updates the 'updates' TextArea with messages regarding connection with client
	public static void updateServer(String message)
	{
		updates.append(message+"\n");
	}
	
	//Updates the 'clientList' TextArea to add or remove clients connected with server
	public static void updateClient(ArrayList<String> al)		//https://stackoverflow.com/questions/17125270/pass-arraylist-as-argument-to-function
	{
		clientList.setText(" ");
		if(al.isEmpty())
			clientList.setText(" ");
		else
		{
			
			for(String a : al)										//https://stackoverflow.com/questions/30222157/displaying-arrayliststring-in-jtextarea
			{
				clientList.append("\n" + a);
			}
		}
	}
	
	//Updates the 'toClient' TextArea to display unparsed Http messages sent to client
	public static void updateToClient(String clnam, String plainText)
	{
		toClient.append("\nHttp message to " + clnam + ":\n" + "\n" + plainText);
	}
	
	//Updates the 'fromClient' TextArea to display unparsed Http messages received from client
	public static void updateFromClient(String clnam, String plainText)
	{
		fromClient.append("\nHttp message from " + clnam + ":\n" + "\n" + plainText);
	}
}
