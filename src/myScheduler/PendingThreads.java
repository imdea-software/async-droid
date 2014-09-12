package myScheduler;


import java.util.ArrayList;

/* 
 * keeps the threads registered to the scheduler
 */
public class PendingThreads {
	private ArrayList<ThreadData> threads = new ArrayList<ThreadData>();	
	private int walkerIndex = -1;  // index of the threadData to be scheduled next, updated when a thread terminates
	
	public void pendingThreads(){
		
	}
	
	public synchronized boolean isEmpty(){
		return threads.isEmpty();
	}
	
	public synchronized int getSize(){
		return threads.size();
	}
	
	public synchronized void increaseWalker(){
		if (threads.size() >= 1)
			walkerIndex = (walkerIndex + 1) % threads.size();
		else 
			walkerIndex = (walkerIndex + 1);
	}
	
	public synchronized void decreaseWalker(){
		if (threads.size() > 0)
			walkerIndex = (walkerIndex - 1 + threads.size()) % threads.size();
		else
			walkerIndex = -1;
	}
	
	public synchronized int getWalkerIndex(){
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
	
	public void clear(){
	    threads.clear();
	    walkerIndex = -1;
	}
	
}
