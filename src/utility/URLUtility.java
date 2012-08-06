package utility;

public class URLUtility 
{
	public static int countSlashes(String path)
	{
		int slashCount = 0;
		
		for(int i = 0; i < path.length(); i++)
		{
			if(path.charAt(i) == '/')
			{
				slashCount++;
			}
		}
		
		//Increment slash count for paths not ending with '/'
		if(path.charAt(path.length() - 1) != '/')
		{
			slashCount++;
		}
		
		return slashCount;
	}
	
	public static String[] splitPathBackward(String path)
	{
		int slashCount = countSlashes(path);
		String[] paths = new String[slashCount];
		
		paths[0] = path;
		
		for(int i = 1; i < slashCount; i++)
		{
			int lastSlashIndex = path.lastIndexOf('/');
			
			if(path.length() > 0 && lastSlashIndex == path.length() - 1)
			{
				//Go to next slash because we already have this one
				lastSlashIndex = path.substring(0, lastSlashIndex).lastIndexOf('/');
			}
			
			path = path.substring(0, lastSlashIndex + 1);
			paths[i] = path;
		}
		
		return paths;
	}
	
	public static String[] splitPathForward(String path)
	{
		int slashCount = countSlashes(path);
		String[] paths = new String[slashCount];
		int slashIndex = path.indexOf('/');
		
		for(int i = 0; i < slashCount - 1; i++)
		{
			paths[i] = path.substring(0, slashIndex + 1);			
			slashIndex = path.indexOf('/', slashIndex + 1);
		}
		
		//Put last one in seperate in case it is not a directory
		paths[slashCount - 1] = path;
		
		return paths;
	}
}
