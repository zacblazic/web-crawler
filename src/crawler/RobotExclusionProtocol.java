package crawler;

import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.StringTokenizer;

public class RobotExclusionProtocol
{
	public static boolean isAllowed(String robotName, String robots, String path)
	{
		Scanner in = new Scanner(robots);
		boolean userAgentNameFound = false;
		
		try
		{
			while(in.hasNextLine())
			{
				String line = in.nextLine();

				//Skip empty lines
				if(!line.isEmpty())
				{
					StringTokenizer tokenizer = new StringTokenizer(line, " ");
					String userAgentDirective = tokenizer.nextToken();

					//We are only looking for user-agent directives and make sure that we have a user-agent present
					//TODO: Change made in this line (modify in original)
					if(userAgentDirective.equalsIgnoreCase("User-agent:") && tokenizer.hasMoreTokens())
					{
						String userAgentName = tokenizer.nextToken();

						//If we find our robot then we don't want to follow all robots instructions
						if(userAgentName.equals(robotName))
						{
							userAgentNameFound = true;

							while(in.hasNextLine() && !line.isEmpty())
							{
								line = in.nextLine();

								if(!line.isEmpty())
								{
									tokenizer = new StringTokenizer(line, " ");
									String accessDirective  = tokenizer.nextToken();
									
									if(accessDirective.equalsIgnoreCase("Disallow:") && tokenizer.hasMoreTokens())
									{
										String pathDirective = tokenizer.nextToken();
										
										if(pathDirective.indexOf("*") > 0)
										{
											tokenizer = new StringTokenizer(pathDirective, "*");
											int tokenCount = tokenizer.countTokens();
											String[] components = new String[tokenCount];

											for(int i = 0; i < tokenCount; i++)
											{
												components[i] = tokenizer.nextToken();
											}

											String firstComponent = components[0];

											//If our directive specifies the last component must be at the end of the line
											if(pathDirective.lastIndexOf("$") == pathDirective.length() - 1)
											{
												//Last component needs to be trimmed of the $ character
												String lastComponent = components[components.length - 1].substring(0, components[components.length - 1].length() - 1);

												//If the path contains the last component
												if(path.indexOf(lastComponent) > 0)
												{
													int lastComponentIndex = path.lastIndexOf(lastComponent);

													if(path.indexOf(firstComponent) == 0 && lastComponentIndex == (path.length() - lastComponent.length()))
													{
														return false;
													}
												}
											}
											else
											{
												String lastComponent = components[components.length - 1];

												if(path.indexOf(lastComponent) >= 0)
												{
													int lastComponentIndex = path.lastIndexOf(lastComponent);

													if(path.indexOf(components[0]) == 0 && lastComponentIndex >= components[0].length())
													{
														return false;
													}
												}
											}

										}
										else if(path.indexOf(pathDirective) == 0)
										{
											return false;
										}
									}
								}
							}
						}

						//If our robot was not found then we follow * robots instructions
						if(userAgentName.equals("*") && !userAgentNameFound)
						{
							while(in.hasNextLine() && !line.isEmpty())
							{
								line = in.nextLine();

								if(!line.isEmpty())
								{
									tokenizer = new StringTokenizer(line, " ");
									String accessDirective  = tokenizer.nextToken();
									
									if(accessDirective.equalsIgnoreCase("Disallow:") && tokenizer.hasMoreTokens())
									{
										String pathDirective = tokenizer.nextToken();
										
										if(pathDirective.indexOf("*") > 0)
										{
											tokenizer = new StringTokenizer(pathDirective, "*");
											int tokenCount = tokenizer.countTokens();
											String[] components = new String[tokenCount];

											for(int i = 0; i < tokenCount; i++)
											{
												components[i] = tokenizer.nextToken();
											}

											String firstComponent = components[0];

											//If our directive specifies the component must be last
											if(pathDirective.lastIndexOf("$") == pathDirective.length() - 1)
											{
												//Last component needs to be trimmed of the $ character
												String lastComponent = components[components.length - 1].substring(0, components[components.length - 1].length() - 1);

												//If the path contains the last component
												if(path.indexOf(lastComponent) > 0)
												{
													int lastComponentIndex = path.lastIndexOf(lastComponent);

													if(path.indexOf(firstComponent) == 0 && lastComponentIndex == (path.length() - lastComponent.length()))
													{
														return false;
													}
												}
											}
											else
											{
												String lastComponent = components[components.length - 1];

												if(path.indexOf(lastComponent) >= 0)
												{
													int lastComponentIndex = path.lastIndexOf(lastComponent);

													if(path.indexOf(components[0]) == 0 && lastComponentIndex >= components[0].length())
													{
														return false;
													}
												}
											}
										}
										else if(path.indexOf(pathDirective) == 0)
										{
											return false;
										}
									}
								}
							}
						}
					}
				}
			}
		}
		catch(NoSuchElementException nsee)
		{
			System.err.println("Malformed robots.txt file not allowing retrieval");
			return false;
		}

		return true;
	}
}
