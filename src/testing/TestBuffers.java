package testing;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class TestBuffers 
{
	public static void main(String[] args) throws MalformedURLException
	{
		StringBuffer buffer = new StringBuffer();
		
		String test = buffer.toString();
		
		if(test.isEmpty())
		{
			System.out.println("Empty");
		}
		
	}
}
