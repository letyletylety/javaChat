import java.net.*;
import java.io.*;

public class ChatServerThread extends Thread
{ 
	private static final int BUFFER_SIZE = 1024;
	private ChatServer       server    = null;
	private Socket           socket    = null;
	private int              clientNum        = -1;
	private DataInputStream  streamIn  =  null;
	private DataOutputStream streamOut = null;
	private FileInputStream fstreamIn = null;
	private FileOutputStream fstreamOut = null;
	private String username = null;
	private ChatRoom room = null;
	public int error = 0;

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
			error = 1;
			server.remove(clientNum);
			if (room != null){
				int flag = room.quit(this);
				if (flag != -1)
					server.removeRoom(flag);
			}
			stop();
		}
	}
	
	public void sendFile(File file, String fileName) {
		if (file == null || fileName == null){
			send("Error: File does not exist.");
			return;
		}
		
		try {
			streamOut.writeUTF("/down " + fileName);
			streamOut.flush();
			
			fstreamIn = new FileInputStream(file);
			byte[] buffer = new byte[BUFFER_SIZE];
			long filesize = file.length();
			int readBytes;
			streamOut.writeLong(filesize);
			while((readBytes = fstreamIn.read(buffer)) != -1){
				streamOut.write(buffer, 0, readBytes);
			}
			streamOut.flush();
			fstreamIn.close();
			send("Download Completed.");
		}
		catch (IOException ioe) {
			System.out.println(clientNum + " ERROR file sending: " + ioe.getMessage());
			error = 1;
			server.remove(clientNum);
			if (room != null){
				int flag = room.quit(this);
				if (flag != -1)
					server.removeRoom(flag);
			}
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
	
	public void upload(String filename){
		try {
			int fileID = room.f_ticket();
			boolean flag = true;
			File file = new File("repository\\" + room.getRoomID() + "\\" + fileID);
			file.createNewFile();
			if (room.setFileName(fileID, filename) == false){
				send("File maximum reached. Cannot submit file");
				flag = false;
			}
			
			fstreamOut = new FileOutputStream(file);
			long filesize = streamIn.readLong();
			byte[] buffer = new byte[BUFFER_SIZE];
			while (filesize > BUFFER_SIZE){
				filesize -= streamIn.read(buffer, 0, BUFFER_SIZE);
				fstreamOut.write(buffer);
			}
			streamIn.read(buffer, 0, (int)filesize);
			fstreamOut.write(buffer);
			fstreamOut.close();
		}
		catch (IOException ioe) {
			System.out.println(clientNum + " ERROR reading: " + ioe.getMessage());
			error = 1;
			if (room != null){
				int flag = room.quit(this);
				if (flag != -1)
					server.removeRoom(flag);
			}
			server.remove(clientNum);
		}
	}

	public void run()
	{  
		System.out.println("Thread Number " + clientNum + " running.");
		try {
			send("Enter ID, PW");
			while (server.handle_login(clientNum, streamIn.readUTF()) != true){
				send("Enter ID, PW");
			}
		}
		catch (IOException ioe){
			System.out.println(clientNum + " Login Error: " + ioe.getMessage());
			error = 1;
			server.remove(clientNum);
			stop();
		}
		send("/login");
		
		server.roomList(this);
		while (true){
			try {
				while (server.handle_main(this, streamIn.readUTF()) != true){
				;
				}
			}
			catch(IOException ioe){
				System.out.println(clientNum + " Chat Room Error: " + ioe.getMessage());
				error = 1;
				server.remove(clientNum);
				stop();
			}
			send("/join");
		
			try {
				while (server.handle_room(room, this, streamIn.readUTF())){
					;
				}
			}
			catch(IOException ioe)
			{  
				System.out.println(clientNum + " ERROR reading: " + ioe.getMessage());
				error = 1;
				if (room != null){
					int flag = room.quit(this);
					if (flag != -1)
						server.removeRoom(flag);
				}
				server.remove(clientNum);
			}
			send("/exit");
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
