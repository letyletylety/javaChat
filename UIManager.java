
public class UIManager {
	
	public void CleanUp()
	{		
		System.out.print("\033[H\033[2J");
		System.out.flush();
	}
	
	public void TriBlank()	
	{
		System.out.println("");
		System.out.println("");
		System.out.println("");
	}

	public String setColor(int num, String original)
	{
		String ret = "\u001b[" + Integer.toString(num+31) + ";1m";
		ret += original;
		ret += "\u001b[0m";
		return ret;
	}
}
