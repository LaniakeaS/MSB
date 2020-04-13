package src;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FetchThread extends Thread
{
	private String root;
	
	private String targets[];
	private List<String> targetsContainer;
	private int targetsPointer;
	private String useTarget;
	
	private List<String> pathStack;
	private String currentPath;
	private List<String> beenThere;
	
	public static boolean isFound;
	private FetchThread otherThreads[];
	
	public FetchThread(String ROOT, String TARGET)
	{
		root = new String(ROOT);
		pathStack = new ArrayList<String>();
		pathStack.add(root);
		pathStack.add("");
		currentPath = new String();
		useTarget = "";
		beenThere = new ArrayList<String>();
		updateCurrentPath();
		isFound = false;
		String temp[] = TARGET.split("\\*\\\\");
		targets = new String[2 * temp.length - 1];
		
		for(int i = 0, j = 0; i < targets.length && j < temp.length; i++)
		{
			if(i % 2 == 0)
			{
				targets[i] = temp[j];
				j++;
			}
			else
				targets[i] = "*\\";
		}
		
		targetsPointer = 0;
		targetsContainer = new ArrayList<String>();
	}
	
	private boolean isBeenThere(String PATH)
	{
		for(String ele : beenThere)
		{
			if(ele.equals(PATH) && !ele.equals(pathStack.get(0)))
				return true;
		}
		
		return false;
	}
	
	@Override
	public void run()
	{	
		for(; targetsPointer < targets.length && targetsPointer >= 0;)
		{
			if(targets[targetsPointer].equals("*\\"))
			{
				boolean isBack = false;
				
				for(File file : new File(currentPath + useTarget).listFiles())
				{
					if(isBeenThere(file.toString() + "\\"))
						continue;
					
					targetsContainer.add(file.getName() + "\\");
					targetsPointer++;
					updateUseTarget();
					
					if(search(targets[targetsPointer]))
					{
						targetsContainer.add(targets[targetsPointer]);
						targetsPointer++;
						updateUseTarget();
						isBack = false;
						break;
					}
					else
					{
						targetsPointer--;
						
						if(!targetsContainer.isEmpty())
						{
							targetsContainer.remove(targetsContainer.size() - 1);
							updateUseTarget();
							isBack = true;
						}
					}
				}
				
				if(isBack)
				{
					targetsPointer--;
					
					if(!targetsContainer.isEmpty())
					{
						targetsContainer.remove(targetsContainer.size() - 1);
						updateUseTarget();
					}
				}
			}
			else
			{
				if(search(targets[targetsPointer]))
				{
					targetsContainer.add(targets[targetsPointer]);
					targetsPointer++;
					updateUseTarget();
				}
				else
				{
					targetsPointer--;
					
					if(!targetsContainer.isEmpty())
					{
						targetsContainer.remove(targetsContainer.size() - 1);
						updateUseTarget();
					}
				}
			}
		}
		
		stopHandle();
	}
	
	private boolean search(String TARGET)
	{
		boolean result = false;
		File targetFile = new File(currentPath + useTarget + TARGET);
		
		if(targetFile.exists() && !isBeenThere(currentPath + useTarget + TARGET))
		{
			beenThere.add(currentPath + useTarget + TARGET);
			return true;
		}
		else if((targetsPointer - 1) % 2 == 1)
			return false;
		
		File[] fileList = new File(currentPath + useTarget).listFiles();
		
		if(fileList != null)
		{
			for(File file : fileList)
			{
				if(file.isDirectory() && !isBeenThere(file.toString()))
				{
					beenThere.add(file.toString());
					pathStack.add(file.getName() + "\\");
					updateCurrentPath();
					
					if(search(TARGET))
					{
						result = true;
						break;
					}
				}
			}
		}
		
		if(!result)
		{
			pathStack.remove(pathStack.size() - 1);
			updateCurrentPath();
		}
		
		return result;
	}
	
	public void setOtherThreads(FetchThread OTHERTHREADS[])
	{
		otherThreads = OTHERTHREADS;
	}
	
	private synchronized void stopHandle()
	{
		if(targetsPointer >= targets.length)
		{
			for(FetchThread otherThread : otherThreads)
				otherThread.stop();
			
			write();
			isFound = true;
		}
	}
	
	private synchronized void write()
	{
		try
		{
			FileOutputStream out = new FileOutputStream(new File("config/Path"));
			out.write((currentPath + useTarget).getBytes());
			out.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	private void updateCurrentPath()
	{
		currentPath = "";
		
		for(String path : pathStack)
			currentPath += path;
	}
	
	private void updateUseTarget()
	{
		useTarget = "";
		
		for(String target : targetsContainer)
			useTarget += target;
	}
	
}
