import java.io.*;
import java.util.HashMap;

public class LoginHandler {
	private FileHandler fs = null;
	private HashMap<String, String> user = new HashMap<String, String>();
	private HashMap<String, Integer> logged = new HashMap<String, Integer>(); 
	
	public LoginHandler (String filename) {
		fs = new FileHandler(filename);
		init();
	}
	
	public void init() {
		String temp;
		String s[];
		while ((temp = fs.ReadLine()) != null){
			s = temp.split(" ");
			if (s.length < 2)
				continue;
			user.put(s[0], s[1]);
			File log = new File("log\\" + s[0]);
			log.mkdirs();
		}
	}
	
	public boolean signUp(String ID, String PW) {
		String temp;
		if (user.containsKey(ID)){
			return false;
		}
		user.put(ID, PW);
		logged.put(ID, 1);
		File log = new File("log\\" + ID);
		log.mkdirs();
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
