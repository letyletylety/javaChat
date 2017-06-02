import java.io.*;

public class ChatRoom {
	private ChatServerThread clients[]= new ChatServerThread[10];
	private int roomID = -1;
	private int clientCount = 0;
	private String roomName = null;
	
	public ChatRoom(String name, int _roomID){
		roomName = name;
		roomID = _roomID;
	}
	
	public void setRoomName(String name){
		roomName = name;
	}
	
	public String getRoomName(){
		return roomName;
	}
	
	public int getRoomID(){
		return roomID;
	}
	
	public synchronized boolean join(ChatServerThread client){
		if (clientCount < clients.length){
			clients[clientCount] = client;
			clientCount++;
			for (int i = 0; i < clientCount; ++i)
				clients[i].send(client.getUsername() + " joined");
			client.setRoom(this);
			return true;
		}
		client.send("Room number " + roomID + " is full");
		return false;
	}
	
	public synchronized boolean quit(ChatServerThread client){
		if (clientCount > 0){
			for (int i = 0; i < clientCount; ++i){
				if (clients[i].getclientNum() == client.getclientNum()){
					for (int j = 0; j < clientCount; ++j) 
						clients[j].send(client.getUsername() + " left");
					clients[i] = null;
					clientCount--;
					client.setRoom(null);
					if (clientCount == 0)
						return true;
					if (clients[clientCount] != null)
						clients[i] = clients[clientCount];
					return false;
				}
			}
		}
		System.out.println("Error: Unable to quit room - " + client.getUsername());
		return false;
	}
	
	public synchronized void chat(String name, String msg){
		for (int i = 0; i < clientCount; ++i)
			clients[i].send(name + ": " + msg);
	}
	
	public void list(ChatServerThread client){
		String userlist = "-------------- User List --------------\r\n";
		for (int i = 0; i < clientCount; ++i)
			userlist = userlist.concat(clients[i].getUsername() + "\r\n");
		userlist = userlist.concat("---------------------------------------");
		client.send(userlist);
	}
	
}
