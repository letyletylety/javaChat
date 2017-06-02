import java.net.*;
import java.io.*;

public class ChatServerThread extends Thread
{ 
	private ChatServer       server    = null;
	private Socket           socket    = null;
	private int              clientNum        = -1;
	private DataInputStream  streamIn  =  null;
	private DataOutputStream streamOut = null;
	private String username = null;
	private ChatRoom room = null;

	public ChatServerThread(ChatServer _server, Socket _socket, int _clientNum)
	{
		super();
		server = _server;
		socket = _socket;
		clientNum = _clientNum;
	}
	public void send(String msg)
	{
		try
		{
			streamOut.writeUTF(msg);
			streamOut.flush();
		}
		catch(IOException ioe)
		{  
			System.out.println(clientNum + " ERROR sending: " + ioe.getMessage());
			server.remove(clientNum);
			server.logout(clientNum);
			stop();
		}
	}
	public int getclientNum()
	{
		return clientNum;
	}

	public void setUsername(String name){
		username = name;
	}
	
	public String getUsername(){
		return username;
	}
	
	public void setRoom(ChatRoom _room){
		room = _room;
	}
	
	public ChatRoom getRoom(){
		return room;
	}

	public void run()
	{  
		// ·Î±×ÀÎ
		System.out.println("Server Thread " + clientNum + " running.");
		try {
			send("Enter ID, PW");
			while (server.login(clientNum, streamIn.readUTF()) != true){
				send("Enter ID, PW");
			}
		}
		catch (IOException ioe){
			System.out.println(clientNum + " Login Error: " + ioe.getMessage());
			server.remove(clientNum);
			server.logout(clientNum);
			stop();
		}
		
		server.roomList(this);
		while (true){
			try {
				while (server.handle_r(this, streamIn.readUTF()) != true){
				;
				}
			}
			catch(IOException ioe){
				System.out.println(clientNum + " Chat Room Error: " + ioe.getMessage());
				server.remove(clientNum);
				server.logout(clientNum);
				stop();
			}
		
			try {
				while (server.handle(room, this, streamIn.readUTF()) != true){
					;
				}
			}
			catch(IOException ioe)
			{  
				System.out.println(clientNum + " ERROR reading: " + ioe.getMessage());
				server.remove(clientNum);
				server.logout(clientNum);
				stop();
			}
		}
	}

	public void open() throws IOException
	{
		streamIn = new DataInputStream(new 
				BufferedInputStream(socket.getInputStream()));
		streamOut = new DataOutputStream(new
				BufferedOutputStream(socket.getOutputStream()));
	}
	public void close() throws IOException
	{
		if (socket != null)    socket.close();
		if (streamIn != null)  streamIn.close();
		if (streamOut != null) streamOut.close();
	}
}
