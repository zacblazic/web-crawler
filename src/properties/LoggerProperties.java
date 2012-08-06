package properties;

public final class LoggerProperties 
{
	private final int refreshRate;
	
	public LoggerProperties(int refreshRate)
	{	
		if(refreshRate < 1)
		{
			throw new IllegalArgumentException("Refresh rate cannot be less than 1");
		}
		
		this.refreshRate = refreshRate;
	}
	
	public int getRefreshRateSeconds()
	{
		return refreshRate;
	}
	
	public int getRefreshRateMillis()
	{
		return refreshRate * 1000;
	}
	
	public String toString()
	{
		return "Refresh rate: " + refreshRate + " second(s)";
	}
}
