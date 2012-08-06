package properties;

public final class UserAgentProperties 
{
	//TODO: Might need to change websiteAddress to URL if need arises
	private final String name;
	private final String emailAddress;
	private final String message;
	
	public UserAgentProperties(String name, String emailAddress, String message)
	{
		this.name = name;
		this.emailAddress = emailAddress;
		this.message = message;
	}

	public String getName() 
	{
		return name;
	}

	public String getEmailAddress() 
	{
		return emailAddress;
	}

	public String getMessage() 
	{
		return message;
	}
	
	public String toString()
	{
		return "User agent: " + name
			   + "\nEmail address: " + emailAddress
			   + "\nMessage: " + message;
	}
}
