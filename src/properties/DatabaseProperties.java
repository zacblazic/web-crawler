package properties;

public final class DatabaseProperties 
{
	private final String dbms;
	private final String server;
	private final String port;
	private final String username;
	private final String password;
	
	public DatabaseProperties(String dbms, String server, String port, String username, String password)
	{
		this.dbms = dbms;
		this.server = server;
		this.port = port;
		this.username = username;
		this.password = password;
	}
	
	public String getDbms() 
	{
		return dbms;
	}
	
	public String getServer() 
	{
		return server;
	}
	
	public String getPort() 
	{
		return port;
	}
	
	public String getUsername() 
	{
		return username;
	}
	
	public String getPassword() 
	{
		return password;
	}

	public String toString() 
	{
		return "DBMS: " + dbms
			   + "\nServer: " + server
			   + "\nPort: " + port
			   + "\nUsername: " + username
			   + "\nPassword: " + password;
	}
}
