import java.net.*;
import java.io.*;

public class ChatClientThread extends Thread
{ 
	private static final int BUFFER_SIZE = 1024;
	private Socket           socket   = null;
	private ChatClient       client   = null;
	private DataInputStream  streamIn = null;
	private FileOutputStream fstreamOut = null;

	public ChatClientThread(ChatClient _client, Socket _socket)
	{  
		client   = _client;
		socket   = _socket;
		open();  
		start();
	}
	
	public void open()
	{  
		File repository = new File("Downloads");
		if (!repository.exists()){
			repository.mkdir();
		}
		else if (!repository.isDirectory()) {
			System.out.println("Unable to set download directory. Please remove \"Downloads\" file");
			client.stop();
		}
		
		try
		{ 
			streamIn  = new DataInputStream(socket.getInputStream());
		}
		catch(IOException ioe)
		{
			System.out.println("Error getting input stream: " + ioe);
			client.stop();
		}
	}
	
	public void close()
	{  
		try
		{
			if (streamIn != null) streamIn.close();
		}
		catch(IOException ioe)
		{  
			System.out.println("Error closing input stream: " + ioe);
		}
	}
	
	public void download(String filename){
		File file = new File("Downloads/" + filename);
		int i = 2;
		while (file.exists()){
			file = new File("Downloads/" + filename + " (" + i + ")");
		}
		
		try {
			file.createNewFile();
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
		catch (IOException ioe){
			System.out.println("Download error: " + ioe.getMessage());
			client.stop();
		}
	}
	
	public void run()
	{
		while (true)
		{
			try
			{ 
				client.handle(streamIn.readUTF());
			}
			catch(IOException ioe)
			{  
				System.out.println("Listening error: " + ioe.getMessage());
				client.stop();
			}
		}
	}
}
