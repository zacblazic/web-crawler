package crawler;

//Synchronization is good in this class
public final class CrawlStatistics 
{
	private long totalPagesCrawled;
	
	public CrawlStatistics()
	{
	}
	
	public CrawlStatistics(long totalPagesCrawled)
	{
		this.totalPagesCrawled = totalPagesCrawled;
	}
	
	public synchronized void incrementTotalPagesCrawled()
	{
		totalPagesCrawled++;
	}
	
	public synchronized long getTotalPagesCrawled()
	{
		return totalPagesCrawled;
	}
	
	public synchronized String toString()
	{
		return "Total pages crawled: " + totalPagesCrawled
			   + "\n";
	}
}
