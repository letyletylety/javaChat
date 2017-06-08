import java.net.*;
import java.io.*;

public class ChatClient implements Runnable
{ 
	private static final int BUFFER_SIZE = 1024;
	private Socket socket              = null;
	private Thread thread              = null;
	private BufferedReader  console   = null;
	private DataOutputStream streamOut = null;
	private ChatClientThread client    = null;
	private FileInputStream fstreamIn = null;
	private FileOutputStream fstreamOut = null;
	private enum Status { LOGIN, MAIN, ROOM }
	private Status status;
	private UIManager ui = new UIManager();

	public ChatClient(String serverName, int serverPort)
	{  
		ui.CleanUp();
		System.out.println("Establishing connection. Please wait ...");
		try
		{  
			socket = new Socket(serverName, serverPort);
			System.out.println("Connected: " + socket);
			start();
		}
		catch(UnknownHostException uhe)
		{ 
			System.out.println("Host unknown: " + uhe.getMessage());
		}
		catch(IOException ioe)
		{  
			System.out.println("Unexpected exception: " + ioe.getMessage()); 
		}
	}

	public void run()
	{  
		System.out.println("Type \"/help\" to check list of commands");
		status = Status.LOGIN;
		while (thread != null)
		{  
			try
			{  
				String msg = console.readLine();
				if (msg.split(" ").length == 0) {
					streamOut.writeUTF(msg);
					streamOut.flush();
					continue;
				}
				String command = msg.split(" ")[0];
				if (command.equals("/help")){
					ui.CleanUp();
					System.out.println("-------------- List of Commands --------------");
					System.out.println("/stat : Show where you are");
					System.out.println("/quit : Exit the chat program\r\n");
					System.out.println("In Login Screen");
					System.out.println("/register <ID> <PW> : Sign up");
					System.out.println("<ID> <PW> : Sign in\r\n");
					System.out.println("In Main Menu");
					System.out.println("/list : Show room list");
					System.out.println("/join <Room_ID> : Join the room");
					System.out.println("/open <Room_name> : Open a new room");
					System.out.println("/log : Show the list of log");
					System.out.println("/log <Log_ID> : Show the log\r\n");
					System.out.println("In Chat Room");
					System.out.println("/list : Show user list");
					System.out.println("/file : Show file list");
					System.out.println("/up <File_name> : Upload a file");
					System.out.println("/down <File_ID> : Download a file");
					System.out.println("/exit : Leave the room");
					System.out.println("/w <User_ID> <Msg>: Whisper to a user");
					System.out.println("----------------------------------------------");
					continue;
				}
				else if(status == Status.ROOM && command.equals("/up")){
					if (msg.length() < 9){
						System.out.println("Type File name.");
						continue;
					}
					File file = new File(msg.substring(4));
					if (!file.exists()) {
						System.out.println("File does not exists.");
						continue;
					}
					else if(!file.isFile()){
						System.out.println("Not a file.");
						continue;
					}
					System.out.println("File transfer started. Please wait...");
					streamOut.writeUTF("/up " + file.getName());
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
					System.out.println("File transfer Completed.");
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
		String s[] = msg.split(" ");
		String command = s[0];
		if (command.equals("/quit"))
		{  
			System.out.println("Good bye. Press RETURN to exit ...");
			stop();
		}
		else if (command.equals("/login")) {
			ui.CleanUp();
			System.out.println("Login Success!");
			status = Status.MAIN;
		}
		else if (command.equals("/join")) {
			status = Status.ROOM;
		}
		else if (command.equals("/exit")) {
			status = Status.MAIN;
		}
		else if (command.equals("/down")){
			if (s.length < 2){
				System.out.println("Error: File name isn't received");
				return;
			}
			client.download(s[1]);
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
