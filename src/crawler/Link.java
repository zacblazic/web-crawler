package crawler;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

public final class Link implements Serializable
{
	private static final long serialVersionUID = 1L;
	private final URL url;
	private final String text;
	
	public Link(URL url, String text) throws MalformedURLException
	{
		this.url = new URL(url.toString());
		this.text = text;
	}
	
	public URL getURL() throws MalformedURLException
	{
		return new URL(url.toString());
	}
	
	public String getText()
	{
		return text;
	}
	
	public String toString()
	{
		return url.toString() + " - " + text;
	}
}
