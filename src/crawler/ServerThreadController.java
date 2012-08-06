package crawler;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import properties.ThreadControllerProperties;
import io.BufferInputThread;
import io.BufferOutputThread;
import io.LoggerThread;

public final class ServerThreadController extends Thread
{
	private Connection connection;
	private final LinkBuffer inputBuffer;
	private final LinkBuffer outputBuffer;
	private final ServerThreadList threadList;
	private final BufferInputThread inputThread;
	private final BufferOutputThread outputThread;
	private final Set<URL> URLHashSet;
	private final CrawlStatistics statistics;
	private final LoggerThread loggerThread;
	private final ThreadControllerProperties controllerProperties;
	private volatile boolean stopping;
	private volatile boolean stopped;
	
	public ServerThreadController(ThreadControllerProperties controllerProperties)
	{
		this.controllerProperties = controllerProperties;
		inputBuffer = new LinkBuffer(controllerProperties.getInputBufferCapacity());
		outputBuffer = new LinkBuffer(controllerProperties.getOutputBufferCapacity());
		threadList = new ServerThreadList(controllerProperties.getThreadCapacity());
		inputThread = new BufferInputThread(controllerProperties.getFrontierFile(), inputBuffer, this);
		outputThread = new BufferOutputThread(controllerProperties.getFrontierFile(), outputBuffer, this);
		URLHashSet = new HashSet<URL>(controllerProperties.getOutputBufferCapacity());
		
		//TODO: May need to get stats from outside
		statistics = new CrawlStatistics();
		loggerThread = new LoggerThread(controllerProperties.getLoggerProperties(), statistics, this);
		
		connection = getConnection();
	}
	
	public void run()
	{
		//Start up all threads
		inputThread.start();
		outputThread.start();
		loggerThread.start();
		
		//TODO: Make sure this will work.. double check...
		while(!(isStopping() && inputBuffer.isEmpty() && !inputThread.isBusy()))
		{
			processLinkFrontier();
		}
		
		stopped = true;	
		
		System.out.println("Server thread controller has stopped. Waiting for other threads to terminate...");
		
		try
		{
			//Wait indefinitely for output thread to finish
			outputThread.join();
		}
		catch(InterruptedException ie)
		{
			System.err.println(ie.toString());
		}
		
		System.out.println("Crawler has terminated...");
	}
	
	private Connection getConnection()
	{
		Connection connection = null;
		
		try
		{
			connection = DriverManager.getConnection("jdbc:" + controllerProperties.getDatabaseProperties().getDbms().toLowerCase()
				     + "://" + controllerProperties.getDatabaseProperties().getServer() + ":" + controllerProperties.getDatabaseProperties().getPort()
				     + "/" + "crawler_repository", controllerProperties.getDatabaseProperties().getUsername(), 
				     controllerProperties.getDatabaseProperties().getPassword());
		}
		catch(SQLException sqle)
		{
			System.out.println(sqle.toString());
		}
		
		return connection;
	}
	
	public Connection getEstablishedConnetion()
	{
		return connection;
	}
	
	private void processLinkFrontier()
	{
		boolean inserted = false;
		Link currentLink = inputBuffer.takeLink();
		
		if(currentLink != null)
		{
			try
			{
				//At this point the link is guarunteed to be non-malformed
				InetAddress currentIp = InetAddress.getByName(currentLink.getURL().getHost());
				ServerThread currentThread = threadList.getThread(currentIp.getHostAddress());
				
				if(currentThread != null)
				{
					boolean insertedIntoThreadFrontier = false;
					boolean insertedIntoOutputBuffer = false;
					
					//TODO: Synchronization issues may arise
					while(!inserted)
					{
						insertedIntoThreadFrontier = currentThread.getFrontier().putLink(currentLink);
						
						if(insertedIntoThreadFrontier)
						{
							inserted = true;
							System.out.println("Inserted into thread frontier...");
						}
						else
						{
							insertedIntoOutputBuffer = outputBuffer.putLink(currentLink);
							
							if(insertedIntoOutputBuffer)
							{
								inserted = true;
								System.out.println("Inserted into output buffer...");
							}
							else
							{
								try
								{
									Thread.sleep(1000);
								}
								catch(InterruptedException ie)
								{
									System.err.println(ie.toString());
								}
							}
						}
					}
				}
				else 
				{
					//Could cause the other threads to die prematurely
					
					LinkFrontier newFrontier = new LinkFrontier(controllerProperties.getThreadFrontierCapacity());
					ServerThread newThread = new ServerThread(currentIp.getHostAddress(), newFrontier, inputBuffer, outputBuffer, URLHashSet, controllerProperties.getUserAgentProperties() , this);
					newThread.getFrontier().putLink(currentLink);
					boolean insertedIntoThreadList = false;
					
					while(!inserted)
					{
						insertedIntoThreadList = threadList.insertThread(newThread);
						
						if(insertedIntoThreadList)
						{
							newThread.start();
							inserted = true;
							
							System.out.println("Inserted into thread list...");
						}
						else
						{
							try
							{
								Thread.sleep(1000);
							}
							catch(InterruptedException ie)
							{
								System.err.println(ie.toString());
							}
						}
					}
				}
			}
			catch(MalformedURLException mue)
			{
				System.err.println(mue.getMessage());
			}
			catch(UnknownHostException uhe)
			{
				System.err.println(uhe.getMessage());
			}
		}
		else
		{
			try
			{
				Thread.sleep(1000);
			}
			catch(InterruptedException ie)
			{
				System.err.println(ie.toString());
			}
		}
	}

	public void threadStopped(String server)
	{
		threadList.removeThread(server);
	}
	
	public void stopCrawling()
	{
		stopping = true;
	}
	
	public boolean isStopping()
	{
		return stopping;
	}
	
	public boolean isStopped()
	{
		return stopped;
	}
	
	public LinkBuffer getInputBuffer()
	{
		return inputBuffer;
	}
	
	public LinkBuffer getOutputBuffer()
	{
		return outputBuffer;
	}
	
	public ServerThreadList getThreadList()
	{
		return threadList;
	}
	
	public BufferInputThread getInputThread()
	{
		return inputThread;
	}
	
	public BufferOutputThread getOutputThread()
	{
		return outputThread;
	}
	
	public CrawlStatistics getCrawlStatistics()
	{
		return statistics;
	}
	
	public LoggerThread getLoggerThread()
	{
		return loggerThread;
	}
}
