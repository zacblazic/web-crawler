package crawler;

public class LinkBuffer
{
	private final int capacity;
	private int size;
	private LinkNode head;
	private LinkNode tail;
	
	public LinkBuffer(int capacity) throws IllegalArgumentException
	{
		if(capacity < 1)
		{
			throw new IllegalArgumentException("Capacity cannot be less than 1");
		}
		
		this.capacity = capacity;
	}
	
	private static class LinkNode
	{
		private Link link;
		private LinkNode next;
		
		public LinkNode(Link link, LinkNode next)
		{
			this.link = link;
			this.next = next;
		}
		
		public Link getLink()
		{
			return link;
		}
		
		public LinkNode getNext()
		{
			return next;
		}
		
		@SuppressWarnings("unused")
		public void setLink(Link link)
		{
			this.link = link;
		}
		
		public void setNext(LinkNode next)
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
	
	public synchronized boolean putLink(Link link)
	{
		if(!isFull())
		{
			LinkNode newNode = new LinkNode(link, null);
			
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
	
	public synchronized Link takeLink()
	{
		if(!isEmpty())
		{
			LinkNode current = head;
			head = head.getNext();
			
			if(head == null)
			{
				tail = null;
			}
			
			size--;
			
			return current.getLink();
		}
		else
		{
			return null;
		}
	}
	
	//Not synchronized since it is declared to be final
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
