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
	
	public FileHandler(String filename) throws IOException
	{	
		file = new File(filename);

		if(!file.exists())
			file.createNewFile();
		
		fileReader = new FileReader(filename);
		bufferedReader = new BufferedReader(fileReader);
		
		fileWriter = new FileWriter(filename, true);
		bufferedWriter = new BufferedWriter(fileWriter);
	}

	// 파일에서 한 줄을 읽어옴 
	// 파일의 끝에서는 null을 반환
	public String ReadLine() throws IOException
	{
		string = bufferedReader.readLine();	
		return string;
	}	
	
	public void Write(String str) throws IOException
	{
		fileWriter.append(str);
		fileWriter.append("\r\n");
		fileWriter.flush();
	}	
}
