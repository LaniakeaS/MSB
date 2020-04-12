package src;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class FetchPath
{
	private static List<FetchThread> threads;
	private static List<String> roots;
	private static final String target = "Steam\\userdata\\*\\582010\\remote\\";
	
	private FetchPath() throws InterruptedException
	{
		File listRoots[] =  File.listRoots();
		roots = new ArrayList<String>();
		
		for(File listRoot : listRoots)
			roots.add(listRoot.toString());
		
		threads = new ArrayList<FetchThread>();
		
		for(String root : roots)
			threads.add(new FetchThread(root, target));
		
		for(int i = 0; i < threads.size(); i++)
		{
			FetchThread otherThreads[] = new FetchThread[threads.size() - 1];
			
			for(int j = i + 1, pointer = 0; j != i && pointer < otherThreads.length; j++, pointer++)
			{
				j = j >= threads.size() ? 0 : j;
				otherThreads[pointer] = threads.get(j);
			}
			
			threads.get(i).setOtherThreads(otherThreads);
		}
		
		for(FetchThread thread : threads)
			thread.start();
		
		for(int i = 0;; i++)
		{
			i = i == threads.size() ? 0 : i;
			
			if(!threads.get(i).isAlive() && FetchThread.isFound)
			{
				FetchThread.isFound = false;
				
				for(FetchThread thread : threads)
					thread.stop();
				
				break;
			}
			
			boolean isAllDead = true;
			
			for(FetchThread thread : threads)
				isAllDead = isAllDead & !thread.isAlive();
			
			if(isAllDead && !FetchThread.isFound)
			{
				System.out.println("Not Found!");
				System.exit(-1);
			}
				
		}
		
		System.out.println("Fetch Success!");
	}
	
	public static void start() throws InterruptedException
	{
		System.out.println("Fetching......");
		new FetchPath();
	}
}
