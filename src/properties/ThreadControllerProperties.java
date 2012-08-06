package properties;

import java.io.File;

public final class ThreadControllerProperties 
{	
	//TODO: Possibly need to add logger directory
	private final UserAgentProperties userAgentProperties;
	private final DatabaseProperties databaseProperties;
	private final LoggerProperties loggerProperties;
	private final int inputBufferCapacity;
	private final int outputBufferCapacity;
	private final int threadCapacity;
	private final int threadFrontierCapacity;
	private final File frontierFile;
	
	public ThreadControllerProperties(UserAgentProperties userAgentProperties, DatabaseProperties databaseProperties, LoggerProperties loggerProperties, int inputBufferCapacity, int outputBufferCapacity, int threadCapacity, int threadFrontierCapacity, File frontierFile) throws IllegalArgumentException
	{
		//TODO: Check if file is .ser
		if(!frontierFile.exists())
		{
			throw new IllegalArgumentException("Frontier file does not exist");
		}
		
		if(!frontierFile.canRead() || !frontierFile.canWrite())
		{
			throw new IllegalArgumentException("No permissions to read from or write to frontier file");
		}
		
		//Check arguments for non-negativity
		if(inputBufferCapacity < 1)
		{
			throw new IllegalArgumentException("Input buffer capacity cannot be less than 1");
		}
		
		if(outputBufferCapacity < 1)
		{
			throw new IllegalArgumentException("Ouput buffer capacity cannot be less than 1");
		}
		
		if(outputBufferCapacity < 2 * inputBufferCapacity)
		{
			throw new IllegalArgumentException("Ouput buffer capacity should be at least double the capacity of the input buffer");
		}
		
		if(threadCapacity < 1)
		{
			throw new IllegalArgumentException("Thread capacity cannot be less than 1");
		}
		
		if(threadFrontierCapacity < 1)
		{
			throw new IllegalArgumentException("Thread frontier capacity cannot be less than 1");
		}
		
		this.userAgentProperties = userAgentProperties;
		this.databaseProperties = databaseProperties;
		this.loggerProperties = loggerProperties;
		this.inputBufferCapacity = inputBufferCapacity;
		this.outputBufferCapacity = outputBufferCapacity;
		this.threadCapacity = threadCapacity;
		this.threadFrontierCapacity = threadFrontierCapacity;
		this.frontierFile = new File(frontierFile.toString());
	}

	public UserAgentProperties getUserAgentProperties()
	{
		return userAgentProperties;
	}
	
	public DatabaseProperties getDatabaseProperties() 
	{
		return databaseProperties;
	}
	
	public LoggerProperties getLoggerProperties()
	{
		return loggerProperties;
	}
	
	public int getInputBufferCapacity()
	{
		return inputBufferCapacity;
	}
	
	public int getOutputBufferCapacity()
	{
		return outputBufferCapacity;
	}
	
	public int getThreadCapacity()
	{
		return threadCapacity;
	}
	
	public int getThreadFrontierCapacity()
	{
		return threadFrontierCapacity;
	}
	
	public File getFrontierFile() 
	{
		return new File(frontierFile.toString());
	}
	
	public String toString()
	{
		return userAgentProperties.toString()
				+ "\n" + loggerProperties.toString()
				+ "\n" + databaseProperties.toString()
				+ "\nInput buffer capacity: " + inputBufferCapacity
				+ "\nOutput buffer capacity: " + outputBufferCapacity
				+ "\nThread capacity: " + threadCapacity
				+ "\nThread frontier capacity: " + threadFrontierCapacity
				+ "\nFrontier file: " + frontierFile;
	}
}
