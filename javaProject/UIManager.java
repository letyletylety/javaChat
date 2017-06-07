
public class UIManager {
	
	public void CleanUp()
	{		
		for(int i = 0 ; i < 100; i++)
		{
			System.out.println("");
		}
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
