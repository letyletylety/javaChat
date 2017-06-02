import java.net.*;
import java.io.*;

public class ChatServer implements Runnable
{
	private ChatServerThread clients[] = new ChatServerThread[50];
	private ChatRoom rooms[] = new ChatRoom[10];
	private ServerSocket server = null;
	private Thread       thread = null;
	private int clientCount = 0;
	private int clientTicket = 0;
	private Integer roomCount = 0;
	private int roomTicket = 1;
	private LoginHandler sign = null; 
	
	public static int atoi(String sTmp)
	{
		String tTmp = "0", cTmp = "";
		
	    sTmp = sTmp.trim();
	    for(int i=0;i < sTmp.length();i++)
	    {
	    	cTmp = sTmp.substring(i,i+1);
	    	if(cTmp.equals("0") ||
	    			cTmp.equals("1") ||
	    			cTmp.equals("2") ||
	    			cTmp.equals("3") ||
	    			cTmp.equals("4") ||
	    			cTmp.equals("5") ||
	    			cTmp.equals("6") ||
	    			cTmp.equals("7") ||
	    			cTmp.equals("8") ||
	    			cTmp.equals("9")) tTmp += cTmp;
	    	else if(cTmp.equals("-") && i==0)
	    		tTmp = "-";
	    	else
	    		break;
	    }	 
	    return(Integer.parseInt(tTmp));
	}

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

	public boolean login(int clientNum, String input) throws IOException{
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
	
	public int createRoom(String name){
		synchronized (roomCount){
			synchronized(rooms){
				if (roomCount < rooms.length){
					int roomID = ticket_r();
					rooms[roomCount] = new ChatRoom(name, roomID);
					roomCount++;
					return roomID;
				}
				return -1;
			}
		}
	}
	
	public void removeRoom(int roomID){
		synchronized (roomCount){
			synchronized(rooms){
				for (int i = 0; i < roomCount; ++i){
					if (rooms[i].getRoomID() == roomID){
						rooms[i] = null;
						for (int j = i; j < roomCount - 1; ++j)
							rooms[j] = rooms[j + 1];
						roomCount--;
					}
				}
			}
		}
	}
	
	public void roomList(ChatServerThread client){
		synchronized (roomCount){
			synchronized(rooms){
				client.send("-------------- Room List --------------");
				for (int i = 0; i < roomCount; ++i){
					client.send("" + rooms[i].getRoomID() + '\t' + rooms[i].getRoomName());
				}
				client.send("---------------------------------------");
			}
		}
	}
	
	public boolean join(int roomID, ChatServerThread client) {
		synchronized (roomCount){
			synchronized(rooms){
				for (int i = 0; i < roomCount; ++i){
					if (rooms[i].getRoomID() == roomID){
						if (rooms[i].join(client))
							return true;
						else 
							return false;
					}
				}
				client.send("Room " + roomID + " doesn't exist");
				return false;
			}
		}
	}
	
	public boolean handle_r(ChatServerThread client, String msg){
		String s[] = msg.split(" ");
		int roomID;
		if (s[0].compareTo("/join") == 0){
			if (s.length < 2){
				client.send("Type room ID");
				return false;
			}

			roomID = atoi(s[1]);
			if (roomID >= 10000 || roomID == 0){
				client.send("Invalid room ID");
				return false;
			}
			if (join(roomID,client))
				return true;
			return false;
		}
		else if (s[0].compareTo("/open") == 0){
			if (s.length < 2){
				client.send("Type room name");
				return false;
			}
			roomID = createRoom(msg.substring(5));
			if (roomID == -1){
				client.send("Max Room Reached: " + rooms.length);
				return false;
			}
			if (join(roomID, client))
				return true;
			return false;
		}
		else if (s[0].compareTo("/list") == 0){
			roomList(client);
			return false;
		}
		else if (s[0].equals("/quit")){
			client.send("Goodbye");
			remove(client.getclientNum());
		}
		
		client.send("Unknown command");
		return false;
	}
	
	public synchronized boolean handle(ChatRoom room, ChatServerThread client, String input)
	{  
		String command = input.split(" ")[0];
		if (command.equals("/quit"))
		{
			if(room.quit(client))
				removeRoom(room.getRoomID());
			return true;
		}
		else if (command.equals("/list")){
			room.list(client);
		}
		else
			room.chat(client.getUsername(), input);
		return false;
	}
	
	private int ticket_r(){
		for (int i = 0; i < roomCount; ++i) {
			if (rooms[i].getRoomID() != roomTicket){
				return roomTicket;
			}
			roomTicket = (roomTicket + 1) % 9999 + 1;
		}
		return roomTicket;
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
