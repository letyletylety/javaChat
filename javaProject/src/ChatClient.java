import java.net.*;
import java.io.*;

public class ChatClient implements Runnable
{ 
	private Socket socket              = null;
	private Thread thread              = null;
	private BufferedReader  console   = null;
	private DataOutputStream streamOut = null;
	private ChatClientThread client    = null;

	public ChatClient(String serverName, int serverPort)
	{  
		System.out.println("Establishing connection. Please wait ...");
		try
		{  
			socket = new Socket(serverName, serverPort);
			System.out.println("Connected: " + socket);
			start();
		}
		catch(UnknownHostException uhe)
		{ 
			System.out.println("Host unknown: " + uhe.getMessage()); }
		catch(IOException ioe)
		{  
			System.out.println("Unexpected exception: " + ioe.getMessage()); }
	}

	public void run()
	{  
		System.out.println("Type \"/help\" to check list of commands");
		while (thread != null)
		{  
			try
			{  
				String msg = console.readLine();
				String command = msg.split(" ")[0];
				if (command.equals("/help")){
					System.out.println("-------------- List of Commands --------------");
					System.out.println("/status : Show where you are");
					System.out.println("/quit : Exit the chat program\r\n");
					System.out.println("In Login Screen");
					System.out.println("/register <ID> <PW> : Sign up");
					System.out.println("<ID> <PW> : Sign in\r\n");
					System.out.println("In Main Menu");
					System.out.println("/list : Show the list of opened chat rooms");
					System.out.println("/join <Room_ID> : Join the room");
					System.out.println("/open <Room_name> : Open a new room");
					System.out.println("/log : Show the list of log");
					System.out.println("/log <Log_name> : Show the log\r\n");
					System.out.println("In Chat Room");
					System.out.println("/list : Show the list of users in the room");
					System.out.println("/exit : Leave the room");
					System.out.println("----------------------------------------------");
					continue;
				}
				streamOut.writeUTF(msg);
				streamOut.flush();
			}
			catch(IOException ioe)
			{  
				System.out.println("Sending error: " + ioe.getMessage());
				stop();
			}
		}
	}
	
	public void handle(String msg)
	{  
		if (msg.equals("/quit"))
		{  
			System.out.println("Good bye. Press RETURN to exit ...");
			stop();
		}
		else
			System.out.println(msg);
	}

	public void start() throws IOException
	{ 
		console   = new BufferedReader(new InputStreamReader((System.in)));
		streamOut = new DataOutputStream(socket.getOutputStream());
		if (thread == null)
		{  
			client = new ChatClientThread(this, socket);
			thread = new Thread(this);                   
			thread.start();
		}
	}
	
	public void stop()
	{  
		if (thread != null)
		{  
			thread.stop();  
			thread = null;
		}
		try
		{  
			if (console   != null)  console.close();
			if (streamOut != null)  streamOut.close();
			if (socket    != null)  socket.close();
		}
		catch(IOException ioe)
		{  
			System.out.println("Error closing ..."); }
		client.close();  
		client.stop();
	}
	
	public static void main(String args[])
	{  
		ChatClient client = null;
		if (args.length != 2)
			System.out.println("Usage: java ChatClient host port");
		else
			client = new ChatClient(args[0], Integer.parseInt(args[1]));
	}
}
