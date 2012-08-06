package crawler;

public final class ServerThreadList
{
	private final int capacity;
	private int size;
	private	ServerThreadNode head;
	private ServerThreadNode tail;
	
	public ServerThreadList(int capacity) throws IllegalArgumentException
	{
		if(capacity < 1)
		{
			throw new IllegalArgumentException("Capacity cannot be less than 1");
		}
		
		this.capacity = capacity;
	}
	
	private static class ServerThreadNode
	{
		private ServerThread thread;
		private ServerThreadNode next;
		
		public ServerThreadNode(ServerThread thread, ServerThreadNode next)
		{
			this.thread = thread;
			this.next = next;
		}
		
		public ServerThread getThread()
		{
			return thread;
		}
		
		public ServerThreadNode getNext()
		{
			return next;
		}
		
		@SuppressWarnings("unused")
		public void setThread(ServerThread thread)
		{
			this.thread = thread;
		}
		
		public void setNext(ServerThreadNode next)
		{
			this.next = next;
		}
	}
	
	public synchronized boolean isEmpty()
	{
		return(head == null);
	}
	
	public synchronized boolean isFull()
	{
		return(size == capacity);
	}
	
	public synchronized boolean insertThread(ServerThread thread)
	{
		if(!isFull())
		{
			ServerThreadNode newNode = new ServerThreadNode(thread, null);
			
			if(isEmpty())
			{
				head = newNode;
				tail = newNode;
			}
			else
			{
				tail.setNext(newNode);
				tail = newNode;
			}
			
			size++;
			
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public synchronized boolean removeThread(String server)
	{
		if(!isEmpty())
		{
			if(head.getNext() == null)
			{
				if(head.getThread().getServer().equals(server))
				{
					head = null;
					tail = null;
					size--;
					
					return true;
				}
				else
				{
					return false;
				}
			}
			else
			{
				ServerThreadNode trailing = head;
				ServerThreadNode current = head.getNext();
				
				while(current != null)
				{
					if(current.getThread().getServer().equals(server))
					{
						trailing.setNext(current.getNext());
						
						if(current == tail)
						{
							tail = trailing;
						}
						
						size--;
						
						return true;
					}
					else
					{
						trailing = current;
						current = current.getNext();
					}
				}
				
				return false;
			}
		}
		else
		{
			return false;
		}
	}
	
	public synchronized boolean containsThread(String server)
	{
		ServerThreadNode current = head;
		
		while(current != null)
		{
			if(current.getThread().getServer().equals(server))
			{
				return true;
			}
			else
			{
				current = current.getNext();
			}
		}
		
		return false;
	}
	
	public synchronized ServerThread getThread(String server)
	{
		ServerThreadNode current = head;
		
		while(current != null)
		{
			if(current.getThread().getServer().equals(server))
			{
				return current.getThread();
			}
			else
			{
				current = current.getNext();
			}
		}
		
		return null;
	}
	
	public int getCapcity()
	{
		return capacity;
	}
	
	public synchronized int getSize()
	{
		return size;
	}
	
	public synchronized int getRemainingCapacity()
	{
		return capacity - size;
	}
}
