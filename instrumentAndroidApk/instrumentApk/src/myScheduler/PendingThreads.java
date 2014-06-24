package myScheduler;


import java.util.ArrayList;

public class PendingThreads {
	private static ArrayList<ThreadData> threads = new ArrayList<ThreadData>();	
	private static int walkerIndex = -1;  // index of the threadData to be scheduled next, updated when a thread terminates
	
	public void pendingThreads(){
		
	}
	
	public synchronized boolean isEmpty(){
		return threads.isEmpty();
	}
	
	public synchronized int getSize(){
		return threads.size();
	}
	
	public synchronized void increaseWalker(){
		walkerIndex = (walkerIndex + 1) % threads.size();
	}
	
	public synchronized void decreaseWalker(){
		walkerIndex = (walkerIndex - 1) % threads.size();;
	}
	
	public synchronized int getWalker(){
		return walkerIndex;
	}
	
	public synchronized void addThread(ThreadData td){
		threads.add(td);
	}
	
	public synchronized void removeThread(ThreadData td){
		threads.remove(td);
	}
	
	public synchronized ThreadData removeThreadById(long id)
	{	
		threads.remove(getThreadById(id));
		decreaseWalker();
		return null;
	}
	
	public synchronized boolean capturedBefore(long id)
	{
		for(ThreadData td: threads){
			if(td.getId() == id)
				return true;
		}
		return false;
	}
	
	public synchronized ThreadData getThreadById(long id)
	{
		for(ThreadData td: threads){
			if(td.getId() == id){
				return td;
			}
		}
		return null;
	}
	
	public synchronized ThreadData getThreadByIndex(int index)
	{
		return threads.get(index);
	}
	
	public synchronized ThreadData getCurrentThread()
	{
		return getThreadByIndex(walkerIndex);	
	}
	
}
