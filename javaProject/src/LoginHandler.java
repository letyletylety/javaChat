import java.io.*;
import java.util.HashMap;

public class LoginHandler {
	private FileHandler fs = null;
	private HashMap<String, String> user = new HashMap<String, String>();
	private HashMap<String, Integer> logged = new HashMap<String, Integer>(); 
	
	public LoginHandler (String filename) throws IOException {
		File login_file = new File(filename);
		try {
			if (login_file.exists() != true){
       	 		login_file.createNewFile();
        	}
		}
		catch (IOException ioe){
			System.out.println("Login File Error :" + ioe.getMessage());
		}
		fs = new FileHandler(filename);
		init();
	}
	
	public void init() throws IOException{
		String temp;
		String s[];
		while ((temp = fs.ReadLine()) != null){
			s = temp.split(" ");
			if (s.length < 2)
				continue;
			user.put(s[0], s[1]);
		}
	}
	
	public boolean signUp(String ID, String PW) throws IOException{
		String temp;
		if (user.containsKey(ID)){
			return false;
		}
		user.put(ID, PW);
		logged.put(ID, 1);
		temp = ID + " " + PW;
		fs.Write(temp);
		return true;
	}
	
	public int signIn(String ID, String PW){
		if (user.containsKey(ID) != true)
			return 0;
		if (user.get(ID).equals(PW) != true)
			return 1;
		if (logged.containsKey(ID))
			return 2;
		
		logged.put(ID, 1);
		return 3;
	}
	
	public void signOut(String ID){
		if (logged.containsKey(ID) != true){
			System.out.println("System : User is not signed in");
			return;
		}
		logged.remove(ID);
	}
	
	
}
