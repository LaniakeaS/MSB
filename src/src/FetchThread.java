package src;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.EmptyStackException;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

public class FetchThread extends Thread
{

	protected boolean isFinished, isFound;
	private Stack<String> beenThere, currentPath, searched, substitutes, unsearched;
	private String currentPathString, root;


	public FetchThread(String root, String targetPath)
	{
		isFinished = false;
		isFound = false;

		beenThere = new Stack<String>();
		currentPath = new Stack<String>();
		searched = new Stack<String>();
		substitutes = new Stack<String>();

		unsearched = new Stack<String>();
		List<String> temp = Arrays.asList(targetPath.split("/"));
		Collections.reverse(temp);
		unsearched.addAll(temp);

		currentPathString = "";
		this.root = root;
	}


	private void back()
	{
		if (searched.contains(currentPath.pop()))
		{
			unsearched.push(searched.pop());

			if (unsearched.peek().equals("*"))
				substitutes.pop();
		}
	}


	private void forward()
	{
		File[] fileListOnCurrentPath = new File(root + currentPathString).listFiles();
		boolean isLeft = false;

		try
		{
			for (File file : fileListOnCurrentPath)
			{
				if (file.isDirectory() && !isBeenThere(file.getPath()) && !file.isHidden() && file.canExecute())
				{
					currentPath.push(file.getName());
					beenThere.push(file.getPath());
					isLeft = true;
					break;
				}
			}
		}
		catch (NullPointerException e)
		{
		}
		
		if (!isLeft)
			back();
	}


	private String getPathFromStack(Stack<? extends String> stack)
	{
		String result = "";
		Iterator<String> iterator = substitutes.iterator();

		for (int i = 0; i < stack.size(); i++)
		{
			if (stack.get(i).equals("*"))
				result += iterator.next();
			else
				result += stack.get(i);

			if (i != stack.size() - 1)
				result += "/";
		}

		return result;
	}


	private boolean isBeenThere(String path)
	{
		if (beenThere.contains(path))
			return true;

		return false;
	}


	private void setIsFinished(boolean isFinished)
	{
		this.isFinished = isFinished;
	}


	private void setIsFound(boolean isFound)
	{
		this.isFound = isFound;
	}


	private void traverse()
	{
		try
		{
			String currentTarget = unsearched.peek();
			currentPathString = getPathFromStack(currentPath);

			if (currentTarget.equals("*"))
			{
				File[] fileListOnCurrentPath = new File(root + currentPathString).listFiles();
				boolean isLeft = false;

				for (File file : fileListOnCurrentPath)
				{
					if (file.isDirectory() && !isBeenThere(file.getPath()))
					{
						currentTarget = file.getName();
						substitutes.push(currentTarget);
						isLeft = true;
						break;
					}
				}

				if (!isLeft)
				{
					back();
					return;
				}
			}

			File targetFile = new File(root + currentPathString + "/" + currentTarget);

			if
			(
				targetFile.exists() &&
				(
					(unsearched.size() != 1 && targetFile.isDirectory()) ||
					(unsearched.size() == 1)
				) &&
				!isBeenThere(targetFile.getPath())
			)
			{
				searched.push(unsearched.pop());
				currentPath.push(searched.peek());
				beenThere.push(targetFile.getPath());
			}
			else if (unsearched.peek().equals("*") || searched.size() == 0)
				forward();
			else
				back();
		}
		catch (EmptyStackException e)
		{
			if (unsearched.isEmpty())
				setIsFound(true);

			if (currentPath.isEmpty())
				setIsFinished(true);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println(currentPathString);
			System.exit(-1);
		}
	}


	private void write()
	{
		try
		{
			FileOutputStream out = new FileOutputStream(new File("config/Path"));
			out.write((root + currentPathString + "/" + searched.peek()).getBytes());
			out.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
	}


	@Override
	public void run()
	{
		while (!isFound && !isFinished)
			traverse();

		if (isFound)
		{
			synchronized(this)
			{
				write();
			}
		}
		else
			System.out.println(root + " NOT FOUND");
	}

}
