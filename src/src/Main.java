package src;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main
{

	public static void main(String[] args) throws InterruptedException, IOException
	{
		if(isFirstStart())
			FetchPath.start();
		
		Update.start();
	}
	
	public static boolean isFirstStart()
	{
		try
		{
			FileInputStream in = new FileInputStream(new File("config\\FirstStart"));
			byte buffer[] = new byte[in.available()];
			in.read(buffer);
			in.close();
			String firstStart = new String(buffer);
			
			if(firstStart.equals("0"))
			{
				FileOutputStream out = new FileOutputStream(new File("config\\FirstStart"));
				buffer = "1".getBytes();
				out.write(buffer);
				out.close();
				return true;
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
		
		return false;
	}

}
