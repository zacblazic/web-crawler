package io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import crawler.Link;
import crawler.LinkBuffer;
import crawler.ServerThreadController;

public final class BufferInputThread extends Thread
{
	private final File frontierFile;
	private final LinkBuffer inputBuffer;
	private final ServerThreadController threadController;
	private volatile boolean busy;
	
	public BufferInputThread(File frontierFile, LinkBuffer inputBuffer, ServerThreadController threadController)
	{
		this.frontierFile = frontierFile;
		this.inputBuffer = inputBuffer;
		this.threadController = threadController;
	}
	
	public void run()
	{
		//TODO: Changed to stop on command
		while(!threadController.isStopping())
		{
			try 
			{
				if(!threadController.getOutputThread().isBusy())
				{
					fillBuffer();
				}
				
				Thread.sleep(1000);
			}
			catch (InterruptedException ie) 
			{
				System.out.println(ie.toString());
			}
		}
		
		System.out.println("Buffer input thread has stopped...");
	}
	
	//TODO: Might put this method into link buffer
	private void fillBuffer()
	{
		busy = true;
		
		ObjectInputStream in = null;
		ObjectOutputStream out = null;
		File temporaryFile = null;
		boolean readSuccessfully = false;
		boolean frontierFileEmpty = false;
		
		if(inputBuffer.isEmpty())
		{
			//Attempt to fill the buffer
			try
			{
				in = new ObjectInputStream(new FileInputStream(frontierFile));
				
				//Create temporary file in the same directory
				temporaryFile = new File(frontierFile.getParent() + "/temp.ser");
				temporaryFile.createNewFile();
				
				//Synchronize the actual filling of the buffer only
				synchronized(inputBuffer)
				{
					while(!inputBuffer.isFull())
					{
						//Do we need this here?
						readSuccessfully = true;
						Link link = (Link)in.readObject();
						inputBuffer.putLink(link);
					}
				}
			}
			catch(ClassNotFoundException cnfe)
			{
				System.err.println(cnfe.toString());
			}
			catch(EOFException eofe)
			{
				frontierFileEmpty = true;
			}
			catch(IOException ioe)
			{	
				System.err.println(ioe.toString());
			}
		
			//Attempt to write out the remainder of the file
			try
			{
				if(readSuccessfully && !frontierFileEmpty)
				{
					out = new ObjectOutputStream(new FileOutputStream(temporaryFile));
					
					while(true)
					{
						Link link = (Link)in.readObject();
						out.writeObject(link);
						out.reset();
					}
				}
			}
			catch(ClassNotFoundException cnfe)
			{
				System.err.println(cnfe.toString());
			}
			catch(EOFException eofe)
			{
				System.err.println(eofe.toString());
			}
			catch(IOException ioe)
			{
				System.err.println(ioe.toString());
			}
			finally
			{
				try
				{
					if(out != null)
					{
						out.close();
					}
					
					if(in != null)
					{
						in.close();
					}	
					
					if(readSuccessfully)
					{
						frontierFile.delete();
						temporaryFile.renameTo(frontierFile);
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
	
	public boolean isBusy()
	{
		return busy;
	}
}
