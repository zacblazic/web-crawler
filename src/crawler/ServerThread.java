package crawler;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import properties.UserAgentProperties;

public final class ServerThread extends Thread
{
	//Add serverIP and serverName
	private final String server;
	private final LinkFrontier frontier;
	private final LinkBuffer inputBuffer;
	private final LinkBuffer outputBuffer;
	private final Set<URL> URLHashSet;
	private final UserAgentProperties userAgentProperties;
	private final ServerThreadController threadController;
	private String robots;
	private long lastPageDownloadTime;
	
	public ServerThread(String server, LinkFrontier frontier, LinkBuffer inputBuffer, LinkBuffer outputBuffer, Set<URL> URLHashSet, UserAgentProperties userAgentProperties, ServerThreadController threadController)
	{
		this.server = server;
		this.frontier = frontier;
		this.inputBuffer = inputBuffer;
		this.outputBuffer = outputBuffer;
		this.URLHashSet = URLHashSet;
		this.userAgentProperties = userAgentProperties;
		this.threadController = threadController;
	}
	
	public void run()
	{
		robots = downloadRobots();
		
		while(!frontier.isEmpty())
		{
			processLink();
			//Delays even if page is not crawled
		}
		
		threadController.threadStopped(server);
		System.out.println(this.getName() + " died...");
	}
	
	private void processLink()
	{
		Link currentLink = frontier.takeLink();
		
		//Precaution in case some thread does nasty stuff
		if(currentLink != null)
		{
			try
			{
				String currentPath = getPathWithQuery(currentLink.getURL());
				String currentPage = null;
				String currentPageTitle = null;
				
				if(robots != null && !robots.isEmpty())
				{
					if(RobotExclusionProtocol.isAllowed(userAgentProperties.getName(), robots, currentPath))
					{
						currentPage = downloadPage(currentPath);
						
						if(currentPage == null)
						{
							return;
						}
						
						Document currentDocument = Jsoup.parse(currentPage);
						currentPageTitle = currentDocument.title();
						LinkBuffer buffer = parsePageLinks(currentDocument, currentLink.getURL().getHost());
						
						if(buffer != null)
						{
							processPageLinks(buffer, currentLink.getURL().getHost());
							threadController.getCrawlStatistics().incrementTotalPagesCrawled();
							//System.out.println(currentPageTitle);
						}
						
						addPageToDatabase(currentLink.getURL().toString(), currentPageTitle, currentLink.getText());
						delayDownload();
					}
					else
					{
						return;
					}
				}
				else
				{
					currentPage = downloadPage(currentPath);
					
					if(currentPage == null)
					{
						return;
					}
					
					Document currentDocument = Jsoup.parse(currentPage);
					currentPageTitle = currentDocument.title();
					LinkBuffer buffer = parsePageLinks(currentDocument, currentLink.getURL().getHost());
					
					if(buffer != null)
					{
						processPageLinks(buffer, currentLink.getURL().getHost());
						threadController.getCrawlStatistics().incrementTotalPagesCrawled();
						//System.out.println(currentPageTitle);
					}
					
					addPageToDatabase(currentLink.getURL().toString(), currentPageTitle, currentLink.getText());
					delayDownload();
				}
			}
			catch(MalformedURLException mue)
			{
				System.err.println(mue.toString());
			}
		}
	}
	
	private void addPageToDatabase(String url, String title, String text)
	{
		Connection connection = threadController.getEstablishedConnetion();
		
		Statement statement = null;
		
		try
		{
			statement = connection.createStatement();
			statement.executeUpdate("INSERT INTO links(url, title, text) VALUES('" + url + "', '" + title + "', '" + text + "')");
		}
		catch(SQLException sqle)
		{
			System.out.println(sqle.toString());
		}
		finally
		{
			try
			{
				if(statement != null)
				{
					statement.close();
				}
			}
			catch(SQLException sqle)
			{
				System.out.println(sqle.toString());
			}	
		}
	}	
	
	private String downloadRobots()
	{
		String robots = null;
		BufferedReader in = null;
		
		try
		{
			//Testing redirects here
			URL url = new URL("http://" + server + "/robots.txt");
			HttpURLConnection.setFollowRedirects(false);
			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			connection.setRequestProperty("Accept", "text/plain");
			connection.setRequestProperty("User-Agent", userAgentProperties.getName() + " (" + userAgentProperties.getEmailAddress() + ")");
			connection.setRequestMethod("GET");
			
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			
			String line = in.readLine();
			StringBuffer stringBuffer = new StringBuffer();
			
			while(line != null)
			{
				stringBuffer.append(line).append("\n");
				line = in.readLine();
			}
			
			robots = stringBuffer.toString();
		}
		catch(MalformedURLException mue)
		{
			System.err.println(mue.toString());
		}
		catch(FileNotFoundException fnfe)
		{
			System.err.println(fnfe.toString());
		}
		catch(UnknownHostException uhe)
		{
			System.err.println(uhe.toString());
		}
		catch(IOException ioe)
		{
			System.err.println(ioe.toString());
		}
		finally
		{
			try
			{
				if(in != null)
				{
					in.close();
				}
			}
			catch(IOException ioe)
			{
				System.err.println(ioe.toString());
			}
		}
		
		return robots;
	}
	
	private String downloadPage(String path)
	{
		BufferedReader in = null;
		String page = null;
		
		try
		{
			//Testing redirects here
			URL url = new URL("http://" + server + path);
			HttpURLConnection.setFollowRedirects(false);
			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			connection.setRequestProperty("Accept", "text/html");
			connection.setRequestProperty("User-Agent", userAgentProperties.getName() + " (" + userAgentProperties.getEmailAddress() + ")");
			connection.setRequestMethod("GET");
			
			//Timer start
			long timeStart = System.currentTimeMillis();
			
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			
			String line = in.readLine();
			StringBuffer stringBuffer = new StringBuffer();
			
			while(line != null)
			{
				stringBuffer.append(line).append("\n");
				line = in.readLine();
			}
			
			//Timer end
			long timeEnd = System.currentTimeMillis();
			
			lastPageDownloadTime = timeEnd - timeStart;
			
			page = stringBuffer.toString();
		}
		catch(MalformedURLException mue)
		{
			System.out.println(mue.toString());
		}
		catch(FileNotFoundException fnfe)
		{
			System.err.println(fnfe.toString());
		}
		catch(UnknownHostException uhe)
		{
			System.err.println(uhe.toString());
		}
		catch(IOException ioe)
		{
			System.err.println(ioe.toString());
		}
		finally
		{
			try
			{
				if(in != null)
				{
					in.close();
				}
			}
			catch(IOException ioe)
			{
				System.err.println(ioe.toString());
			}
		}
		
		return page;
	}
	
	private LinkBuffer parsePageLinks(Document document, String host)
	{
		Elements elements = document.getElementsByTag("a");
		
		if(elements.size() < 1)
		{
			return null;
		}
		
		LinkBuffer links = new LinkBuffer(elements.size());
		
		for(int i = 0; i < elements.size(); i++)
		{
			try
			{
				URI currentURI = new URI(elements.get(i).attr("href"));
				String currentText = (elements.get(i).ownText());
				
				if(currentURI.isAbsolute() && !currentURI.getScheme().equals("http"))
				{
					continue;
				}
				
				String currentHost = currentURI.getHost();
				
				if(currentHost == null)
				{
					currentHost = host;
				}
				
				String currentPath = currentURI.getPath();
				
				if(currentPath.indexOf('/') != 0)
				{
					currentPath = "/" + currentPath;
				}
				
				currentURI = new URI("http", currentHost.toLowerCase(), currentPath, currentURI.getQuery(), null);
				currentURI = currentURI.normalize();
				
				//TODO: Add/remove ports to/from URI
	
				links.putLink(new Link(currentURI.toURL(), currentText));
			}
			catch(URISyntaxException urise)
			{
				System.err.println(urise.toString());
			}
			catch(MalformedURLException mue)
			{
				System.err.println(mue.toString());
			}
		}
		
		return links;
	}
	
	private void processPageLinks(LinkBuffer buffer, String host)
	{		
		while(!buffer.isEmpty())
		{
			Link currentLink = buffer.takeLink();
			
			if(currentLink != null)
			{
				try
				{
					boolean insertedIntoURLHashSet = false;
					
					//Externally synchronized the hashset
					synchronized(URLHashSet)
					{
						insertedIntoURLHashSet = URLHashSet.add(currentLink.getURL());
					}
					
					if(insertedIntoURLHashSet)
					{
						if(currentLink.getURL().getHost().equals(host) && !frontier.isFull() && !threadController.isStopping())
						{
							frontier.putLink(currentLink);
						}
						//TODO: Allow the percentage to be set as property/make method for this
						else if(((inputBuffer.getSize() / (double)inputBuffer.getCapcity()) < 0.3) && !threadController.isStopping())
						{
							inputBuffer.putLink(currentLink);
						}
						else
						{
							boolean insertedIntoOutputBuffer = false;
							
							while(!insertedIntoOutputBuffer)
							{
								insertedIntoOutputBuffer = outputBuffer.putLink(currentLink);
								
								if(!insertedIntoOutputBuffer)
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
				}
				catch(MalformedURLException mue)
				{
					System.err.println(mue.toString());
				}
			}
		}
	}
	
	private void delayDownload()
	{
		//TODO: Can be in some properties at some point
		int politenessFactor = 1;
		
		try
		{
			//Thread.sleep(lastPageDownloadTime * politenessFactor);
			Thread.sleep(200);
		}
		catch(InterruptedException ie)
		{
			System.err.println(ie.toString());
		}
	}
	
	private String getPathWithQuery(URL url)
	{
		String path = null;
		
		if(url.getQuery() != null)
		{
			path = url.getPath() + "?" + url.getQuery();
		}
		else
		{
			path = url.getPath();
		}
		
		return path;
	}
	
	public String getServer()
	{
		return server;
	}
	
	public LinkFrontier getFrontier()
	{
		return frontier;
	}
}
