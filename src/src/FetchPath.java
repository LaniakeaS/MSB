package src;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class FetchPath
{
	private static List<FetchThread> threads;
	private static List<String> roots;
	private static final String target = "Steam/userdata/*/582010/remote/";
	
	private FetchPath() throws InterruptedException
	{
		File listRoots[] =  File.listRoots();
		roots = new ArrayList<String>();
		
		for(File listRoot : listRoots)
			roots.add(listRoot.toString());
		
		threads = new ArrayList<FetchThread>();
		
		for(String root : roots)
			threads.add(new FetchThread(root, target));
		
		for(FetchThread thread : threads)
			thread.start();
		
		for(int i = 0;; i++)
		{
			i = i == threads.size() ? 0 : i;
			
			if(!threads.get(i).isAlive() && threads.get(i).isFound)
			{
				threads.get(i).isFound = false;
				
				for(FetchThread thread : threads)
					thread.stop();
				
				break;
			}
			
			boolean isAllDead = true;
			
			for(FetchThread thread : threads)
				isAllDead = isAllDead & !thread.isAlive();
			
			if(isAllDead && !threads.get(i).isFound)
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
