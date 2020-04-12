package src;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public final class Update
{
	private static String sourcePath;
	private static String targetPath;
	
 	private Update()
	{
		try
		{
			FileInputStream in = new FileInputStream(new File("config/Path"));
			byte buffer[] = new byte[in.available()];
			in.read(buffer);
			sourcePath = new String(buffer);
			in.close();
			targetPath = "MHW\\" + new File(sourcePath).getName();
			
			if(!new File("MHW").exists())
				new File("MHW").mkdir();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public static void start() throws IOException
	{
		System.out.println("Updating......");
		new Update();
		copy(sourcePath, targetPath);
		System.out.println("Update Success!");
	}
	
	public static void copy(String SOURCEPATH, String TARGETPATH) throws IOException
	{
		File source = new File(SOURCEPATH);
		
		if(!source.exists())
		{
			try
			{
				FetchPath.start();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
				System.exit(-1);
			}
		}
		
		File target = new File(TARGETPATH);
		
		if(!(target.isDirectory() && target.exists()))
			Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
		
		if(target.isFile())
			return;

		File[] fileList = source.listFiles();
		
		for(File file : fileList)
			copy(file.toString(), (target.toString() + "\\" + file.getName()));
	}

}
