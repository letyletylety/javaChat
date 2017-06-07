import java.io.*;

public class ChatRoom {
	private ChatServerThread clients[]= new ChatServerThread[10];
	private int roomID = -1;
	private int clientCount = 0;
	private String roomName = null;
	private File repository = null;
	private Integer fileTicket = 0;
	private Integer fileCount = 1;
	private String[] fileName = new String[500];
	
	public ChatRoom(String name, int _roomID){
		roomName = name;
		roomID = _roomID;
		repository = new File("repository\\" + _roomID);
		if (!repository.exists())
			repository.mkdirs();
	}
	
	public void setRoomName(String name){
		roomName = name;
	}
	
	public String getRoomName(){
		return roomName;
	}
	
	public boolean setFileName(int fileID, String _fileName) {
		synchronized (fileCount) {
			if (fileID < fileName.length) {
				fileName[fileID] = _fileName;
				if (fileCount < fileID + 1){
					fileCount = fileID + 1;
				}
				return true;
			}
			return false;
		}
	}
	
	public int getFileNumber(){
		return fileName.length;
	}
	
	public File getFile(int fileID) {
		File file = new File("repository\\" + roomID + "\\" + fileID);
		if (!file.exists())
			return null;
		return file;
	}
	
	public String getFileName(int fileID) {
		if (fileID > fileName.length)
			return null;
		return fileName[fileID];
	}
	
	public int getRoomID(){
		return roomID;
	}
	
	public int f_ticket() {
		synchronized (fileTicket){
			fileTicket++;
			return fileTicket;
		}
	}
	
	public synchronized boolean join(ChatServerThread client){
		if (clientCount < clients.length){
			clients[clientCount] = client;
			clientCount++;
			for (int i = 0; i < clientCount; ++i){
				clients[i].send(client.getUsername() + " joined");
				FileHandler log = new FileHandler("log\\" + clients[i].getUsername() + "\\" + roomName);
				log.Write(client.getUsername() + " joined");
			}
			client.setRoom(this);
			return true;
		}
		client.send("Room number " + roomID + " is full");
		return false;
	 }
	
	public synchronized int quit(ChatServerThread client){
		if (clientCount > 0){
			client.setRoom(null);
			for (int i = 0; i < clientCount; ++i){
				if (clients[i].getclientNum() == client.getclientNum()){
					for (int j = 0; j < clientCount; ++j) {
						if (clients[j].error != 1)
							clients[j].send(client.getUsername() + " left");
						FileHandler log = new FileHandler("log\\" + clients[j].getUsername() + "\\" + roomName);
						log.Write(client.getUsername() + " left");
					}
					clients[i] = null;
					clientCount--;

					if (clientCount == 0 && repository != null){
						File userfiles[] = repository.listFiles();
						for (int j = 0; j < userfiles.length; ++j){
							userfiles[j].delete();
						}
						repository.delete();
						return roomID;
					}
					if (clients[clientCount] != null)
						clients[i] = clients[clientCount];
					return -1;
				}
			}
		}
		System.out.println("Error: Unable to quit room - " + client.getUsername());
		return -1;
	}
	
	public synchronized void chat(String name, String msg){
		for (int i = 0; i < clientCount; ++i){
			FileHandler log = new FileHandler("log\\" + clients[i].getUsername() + "\\" + roomName);
			log.Write(name + ": " + msg);
			clients[i].send(name + ": " + msg);
		}
	}
	
	public synchronized void whisper(ChatServerThread from, String to, String msg){
		ChatServerThread opponent = null;
		for (int i = 0; i < clientCount; ++i) {
			if (clients[i].getUsername().equals(to)) {
				opponent = clients[i];
				break;
			}
		}
		if (opponent == null)
			from.send(to + " is not in the room");
		else {
			FileHandler log = new FileHandler("log\\" + from.getUsername() + "\\" + roomName);
			log.Write(from.getUsername() + "(whisper) : " + msg);
			from.send(from.getUsername() + "(whisper) : " + msg);
			log = new FileHandler("log\\" + to + "\\" + roomName);
			log.Write(from.getUsername() + "(whisper) : " + msg);
			opponent.send(from.getUsername() + "(whisper) : " + msg);
		}
	}
	
	public void list(ChatServerThread client){
		String msg = "-------------- User List --------------\r\n";
		for (int i = 0; i < clientCount; ++i)
			msg = msg.concat(clients[i].getUsername() + "\r\n");
		msg = msg.concat("---------------------------------------");
		client.send(msg);
	}
	
	public void fileList(ChatServerThread client) {
		String msg = "-------------- File List --------------\r\n";
		msg = msg.concat("File ID\tFile Name\r\n");
		for (int i = 1; i < fileCount; ++i){
			msg = msg.concat("" + i + "\t" + fileName[i] + "\r\n");
		}
		msg = msg.concat("---------------------------------------");
		client.send(msg);
	}
	
}
