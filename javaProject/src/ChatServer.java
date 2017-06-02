import java.net.*;
import java.io.*;

public class ChatServer implements Runnable
{
	private ChatServerThread clients[] = new ChatServerThread[50];
	private ServerSocket server = null;
	private Thread       thread = null;
	private int clientCount = 0;
	private int clientTicket = 0;
	private LoginHandler sign = null; 

	public ChatServer(int port) throws IOException
	{
		try
		{ 
			System.out.println("Binding to port " + port + ", please wait  ...");
			server = new ServerSocket(port);
			System.out.println("Server started: " + server);
			sign = new LoginHandler("Login.txt");
			start(); 
		}
		catch(IOException ioe)
		{  
			System.out.println("Can not bind to port " + port + ": " + ioe.getMessage());
		}
	}
	public void run()
	{  
		while (thread != null)
		{
			try
			{ 
				System.out.println("Waiting for a client ..."); 
				addThread(server.accept());
			}
			catch(IOException ioe)
			{ 
				System.out.println("Server accept error: " + ioe); stop();
			}
		}
	}
	public void start()
	{ 
		if (thread == null)
		{ 
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
	}
	public synchronized void handle(int clientNum, String input)
	{  
		if (input.equals(".bye"))
		{
			clients[clientNum].send(".bye");
			remove(clientNum); 
		}
		else
			for (int i = 0; i < clients.length; i++){
				if (clients[i] != null)
					clients[i].send(clients[clientNum].getUsername() + ": " + input);  
			}
	}

	public synchronized boolean login(int clientNum, String input) throws IOException{
		String s[] = input.split(" ");
		if (s.length < 2)
			return false;
		if (s[0].compareTo("/create") == 0)
		{
			if(sign.signUp(s[1], s[2]) != true)
			{
				clients[clientNum].send("The ID already exists.");
				return false;
			}
			clients[clientNum].setUsername(s[1]);
			clients[clientNum].send("Login Success!");
			return true;
		}
		else {
			int i = sign.signIn(s[0], s[1]);
			switch (i){
			case 0:
				clients[clientNum].send("ID does not exist");
				return false;
			case 1:
				clients[clientNum].send("Incorrect ID/PW");
				return false;
			case 2:
				clients[clientNum].send("User already logged in");
				return false;
			}
			clients[clientNum].setUsername(s[0]);
			clients[clientNum].send("Login Success!");
			return true;
		}
	}

	public void logout(int clientNum)
	{
		sign.signOut(clients[clientNum].getUsername());
	}

	public synchronized void remove(int clientNum)
	{
		if (clientNum >= 0)
		{
			ChatServerThread toTerminate = clients[clientNum];
			logout(clientNum);
			clients[clientNum] = null;
			System.out.println("Removing client thread " + clientNum );
			clientCount--;
			try
			{ 
				toTerminate.close(); 
			}
			catch(IOException ioe)
			{  
				System.out.println("Error closing thread: " + ioe); 
			}
			toTerminate.stop(); 
		}
	}
	
	private int ticket(){
		for (int i = 0; i < clients.length; ++i) {
			if (clients[clientTicket] == null){
				return clientTicket;
			}
			clientTicket = (clientTicket + 1) % clients.length;
		}
		return clientTicket;
	}
	
	
	private void addThread(Socket socket)
	{  
		if (clientCount < clients.length)
		{
			int clientNum = ticket();
			System.out.println("Client accepted: " + socket);
			clients[clientNum] = new ChatServerThread(this, socket, clientNum);
			try
			{ 
				clients[clientNum].open(); 
				clients[clientNum].start();  
				clientCount++;
			}
			catch(IOException ioe)
			{ 
				System.out.println("Error opening thread: " + ioe); 
			}
		}
		else
			System.out.println("Client refused: maximum " + clients.length + " reached.");
	}
	public static void main(String args[]) throws IOException
	{  
		ChatServer server = null;
		if (args.length != 1)
			System.out.println("Usage: java ChatServer port");
		else
			server = new ChatServer(Integer.parseInt(args[0]));
	}
}
