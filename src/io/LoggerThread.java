package io;

import properties.LoggerProperties;

import crawler.CrawlStatistics;
import crawler.ServerThreadController;

public final class LoggerThread extends Thread
{
	private final LoggerProperties loggerProperties;
	private final CrawlStatistics statistics;
	private final ServerThreadController threadController;
	private double pagesCrawledPerSecond;
	private double previousTotalPages;
	
	public LoggerThread(LoggerProperties loggerProperties, CrawlStatistics statistics, ServerThreadController threadController)
	{
		this.loggerProperties = loggerProperties;
		this.statistics = statistics;
		this.threadController = threadController;
	}
	
	public void run()
	{
		//When all threads are dead we can stop the logger
		//TODO: This may not be the best way to stop this thread
		while(!(threadController.isStopped() && threadController.getThreadList().isEmpty()))
		{
			try
			{
				Thread.sleep(loggerProperties.getRefreshRateMillis());
				updatePagesCrawledPerSecond();
				
				//TODO: Implement as part of graphical user interface
				System.out.println(getPagesCrawledPerSecond());
				
				System.out.println("Input buffer size: " + threadController.getInputBuffer().getSize());
				System.out.println("Output buffer size: " + threadController.getOutputBuffer().getSize());
				System.out.println("Threads running: " + threadController.getThreadList().getSize());
				System.out.println("Total pages crawled: " + statistics.getTotalPagesCrawled());
			}
			catch(InterruptedException ie)
			{
				ie.printStackTrace();
			}
		}
		
		System.out.println("Logger thread has stopped...");
	}
	
	//Synchronizing both objects so that changes are visible
	private synchronized void updatePagesCrawledPerSecond()
	{
		//Accuqire the lock for both statements so they can be performed atomically
		synchronized(statistics)
		{
			pagesCrawledPerSecond = (statistics.getTotalPagesCrawled() - previousTotalPages) / (double)loggerProperties.getRefreshRateSeconds();
			previousTotalPages = statistics.getTotalPagesCrawled();
		}
	}
	
	//Synchronized because doubles are not atomic
	public synchronized double getPagesCrawledPerSecond()
	{
		return pagesCrawledPerSecond;
	}
}
