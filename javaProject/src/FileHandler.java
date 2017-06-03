import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileHandler {

	File file = null;
	
	FileReader fileReader = null;
	BufferedReader bufferedReader = null;

	FileWriter fileWriter = null;	
	BufferedWriter bufferedWriter = null;
	
	String string = new String();
	
	public FileHandler(String filename)
	{	
		try {
			file = new File(filename);
			if (!file.exists())
				file.createNewFile();
		
			fileReader = new FileReader(filename);
			bufferedReader = new BufferedReader(fileReader);
		
			fileWriter = new FileWriter(filename, true);
			bufferedWriter = new BufferedWriter(fileWriter);
		}
		catch (IOException ioe){
		}
	}

	// 파일에서 한 줄을 읽어옴 
	// 파일의 끝에서는 null을 반환
	public String ReadLine()
	{
		try {
			string = bufferedReader.readLine();	
			return string;
		}
		catch (IOException ioe){
			System.out.println("File Read Error: " + ioe.getMessage());
		}
		return string;
	}	
	
	public void Write(String str)
	{
		try {
			fileWriter.append(str);
			fileWriter.append("\r\n");
			fileWriter.flush();
		}
		catch (IOException ioe){
			System.out.println("File Write Error: " + ioe.getMessage());
		}
	}	
}
