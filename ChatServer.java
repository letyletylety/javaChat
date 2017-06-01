import java.net.*;
import java.io.*;

public class ChatServer implements Runnable
{  private ChatServerThread clients[] = new ChatServerThread[50];
   private ServerSocket server = null;
   private Thread       thread = null;
   private int clientCount = 0;
   private LoginHandler sign = null; 
   
   
   public ChatServer(int port) throws IOException
   {  try
      {  System.out.println("Binding to port " + port + ", please wait  ...");
         server = new ServerSocket(port);
         System.out.println("Server started: " + server);
         sign = new LoginHandler("Login.txt");
         start(); 
      }
      catch(IOException ioe)
      {  System.out.println("Can not bind to port " + port + ": " + ioe.getMessage()); }
   }
   public void run()
   {  while (thread != null)
      {  try
         {  System.out.println("Waiting for a client ..."); 
            addThread(server.accept()); }
         catch(IOException ioe)
         {  System.out.println("Server accept error: " + ioe); stop(); }
      }
   }
   public void start()
   {  if (thread == null)
      {  thread = new Thread(this); 
         thread.start();
      }
   }
   public void stop()
   {  if (thread != null)
      {  thread.stop(); 
         thread = null;
      }
   }
   private int findClient(int ID)
   {  for (int i = 0; i < clientCount; i++)
         if (clients[i].getID() == ID)
            return i;
      return -1;
   }
   public synchronized void handle(int ID, String input)
   {  if (input.equals(".bye"))
      {  clients[findClient(ID)].send(".bye");
         remove(ID); }
      else
         for (int i = 0; i < clientCount; i++)
            clients[i].send(ID + ": " + input);   
   }
   
   public boolean login(int ID, String input) throws IOException{
	   String s[] = input.split(" ");
	   int clientID = findClient(ID);
	   if (s[0] == "/create"){
		   if(sign.create(s[1], s[2]) != true){
			clients[clientID].send("The ID already exists.");
			return false;
		   }
		   clients[clientID].setUsername(s[1]);
		   return true;
	   }
	   else {
		   int i = sign.signIn(s[0], s[1]);
		   switch (i){
			   case 0:
				   clients[clientID].send("ID does not exist");
				   return false;
			   case 1:
				   clients[clientID].send("Incorrect ID/PW");
				   return false;
			   case 2:
				   clients[clientID].send("User already logged in");
				   return false;
		   }
		   clients[clientID].setUsername(s[0]);
		   return true;
	   }
   }
   
   public void logout(int ID){
	   sign.signOut(clients[findClient(ID)].getUsername());
   }
   
   public synchronized void remove(int ID)
   {  int pos = findClient(ID);
      if (pos >= 0)
      {  ChatServerThread toTerminate = clients[pos];
         System.out.println("Removing client thread " + ID + " at " + pos);
         if (pos < clientCount-1)
            for (int i = pos+1; i < clientCount; i++)
               clients[i-1] = clients[i];
         clientCount--;
         try
         {  toTerminate.close(); }
         catch(IOException ioe)
         {  System.out.println("Error closing thread: " + ioe); }
         toTerminate.stop(); }
   }
   private void addThread(Socket socket)
   {  if (clientCount < clients.length)
      {  System.out.println("Client accepted: " + socket);
         clients[clientCount] = new ChatServerThread(this, socket);
         try
         {  clients[clientCount].open(); 
            clients[clientCount].start();  
            clientCount++; }
         catch(IOException ioe)
         {  System.out.println("Error opening thread: " + ioe); } }
      else
         System.out.println("Client refused: maximum " + clients.length + " reached.");
   }
   public static void main(String args[]) throws IOException
   {  ChatServer server = null;
      if (args.length != 1)
         System.out.println("Usage: java ChatServer port");
      else
         server = new ChatServer(Integer.parseInt(args[0]));
   }
}
