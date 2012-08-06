package testing;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import crawler.Link;
import crawler.ServerThreadController;
import properties.DatabaseProperties;

import properties.LoggerProperties;
import properties.ThreadControllerProperties;
import properties.UserAgentProperties;

public class RunCrawler 
{
	public static void main(String[] args) throws MalformedURLException
	{
		File frontierFile = new File("C:/Users/Revar/Desktop/frontier.ser");
		
		DatabaseProperties databaseProperties = new DatabaseProperties("MySQL", "127.0.0.1", "3306", "root", "password");
		LoggerProperties loggerProperties = new LoggerProperties(1);
		UserAgentProperties userAgentProperties = new UserAgentProperties("Spyderbot", "info.spyderbot@gmail.com", "Testing - May access server without delays");
		ThreadControllerProperties threadControllerProperties = new ThreadControllerProperties(userAgentProperties, databaseProperties, loggerProperties, 1000, 2000, 50, 600, frontierFile);

		ServerThreadController threadController = new ServerThreadController(threadControllerProperties);		
		threadController.getInputBuffer().putLink(new Link(new URL("http://www.newegg.com"), "Newegg"));
		threadController.start();
	}
}