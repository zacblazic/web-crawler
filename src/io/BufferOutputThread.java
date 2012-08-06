package io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import crawler.Link;
import crawler.LinkBuffer;
import crawler.ServerThreadController;

public final class BufferOutputThread extends Thread
{
	private final File frontierFile;
	private final LinkBuffer outputBuffer;
	private final ServerThreadController threadController;
	private volatile boolean busy;
	
	public BufferOutputThread(File frontierFile, LinkBuffer outputBuffer, ServerThreadController threadController)
	{
		this.frontierFile = frontierFile;
		this.outputBuffer = outputBuffer;
		this.threadController = threadController;	
	}
	
	public void run()
	{
		//When isStopped, all link in buffer will be removed, but ServerThreadController will be waiting
		//for this thread to die.
		
		while(!(threadController.isStopped() && threadController.getThreadList().isEmpty()))
		{
			try
			{
				if(!threadController.getInputThread().isBusy())
				{
					drainBuffer();
				}
				
				Thread.sleep(1000);
			}
			catch(InterruptedException ie)
			{
				System.out.println(ie.toString());
			}
		}
		
		flushBuffer();
		
		try
		{
			//Try join to logger thread if it is not already dead
			threadController.getLoggerThread().join();
		}
		catch(InterruptedException ie)
		{
			System.err.println(ie.toString());
		}
		
		System.out.println("Buffer output thread has stopped...");
	}

	//TODO: Might put this method into link buffer
	private void drainBuffer()
	{
		busy = true;
		
		ObjectOutputStream out = null;
		
		if(outputBuffer.isFull())
		{
			try
			{
				out = new ObjectOutputStream(new FileOutputStream(frontierFile, true));
				
				synchronized(outputBuffer)
				{
					while(!outputBuffer.isEmpty())
					{
						Link link = outputBuffer.takeLink();
						out.writeObject(link);
						out.reset();
					}
				}
			}
			catch(IOException ioe)
			{
				System.out.println(ioe.toString());
			}
			finally
			{
				try
				{
					if(out != null)
					{
						out.close();
					}
				}
				catch(IOException ioe)
				{
					System.out.println(ioe.toString());
				}
			}
		}
		
		busy = false;
	}
	
	private void flushBuffer() 
	{
		ObjectOutputStream out = null;
		
		try
		{
			out = new ObjectOutputStream(new FileOutputStream(frontierFile, true));
			
			synchronized(outputBuffer)
			{
				while(!outputBuffer.isEmpty())
				{
					Link link = outputBuffer.takeLink();
					out.writeObject(link);
					out.reset();
				}
			}
		}
		catch(IOException ioe)
		{
			System.out.println(ioe.toString());
		}
		finally
		{
			try
			{
				if(out != null)
				{
					out.close();
				}
			}
			catch(IOException ioe)
			{
				System.out.println(ioe.toString());
			}
		}
	}
	
	public boolean isBusy()
	{
		return busy;
	}
}
