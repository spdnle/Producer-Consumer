/*
 * Passed all test cases for parts 1, 2, 3, and extra credit
 * Hopefully.
 */

import java.time.LocalDateTime;
import java.util.*;

public class Buffer
{
	public int bufferSize; //size of buffer
	public int producerNum; //number of producers
	public int consumerNum; //number of consumers
	public int producerSleep; //max number of milliseconds for the producer to sleep
	public int consumerSleep; //max number of milliseconds for the consumer to sleep
	public int producedMessage; //number of messages that each producer should produce
	
	public Queue<LocalDateTime> queue;
	private Object lock = new Object(); //Shared lock

	public ArrayList<Producer> producerList = new ArrayList<Producer>(); //holds Producers
	public ArrayList<Consumer> consumerList = new ArrayList<Consumer>(); //holds Consumers
	
	public Buffer(int bufferSize, int producerNum, int consumerNum, 
			int producerSleep, int consumerSleep, int producerMessage)
	{
		this.bufferSize = bufferSize;
		this.producerNum = producerNum;
		this.consumerNum = consumerNum;
		this.producerSleep = producerSleep;
		this.consumerSleep = consumerSleep;
		this.producedMessage = producerMessage;
		this.queue = new LinkedList<LocalDateTime>();
	}
	
	//nested producer class
	class Producer extends Thread
	{
		String name;
		Producer(String name)
		{
			this.name = name;
		}
		
		public String name()
		{
			return name;
		}
		
		public void run()
		{
			try
			{
				produceItem(this);
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	//nested consumer class
	class Consumer extends Thread
	{
		String name;
		Consumer(String name)
		{
			this.name = name;
		}
		
		public String name()
		{
			return name;
		}
		
		public void run()
		{
			try
			{
				consumeItem(this);
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	//synchronized produceItem()
	public void produceItem(Producer producer) throws InterruptedException
	{
		int producedCount = 0;
		boolean produceItemGlobal = true; //Boolean value for produceItem(). Determines whether the while loop continues to run.
		while(produceItemGlobal)
		{			
			synchronized(lock)
			{
				while(produceItemGlobal)
				{
					if(producedCount == producedMessage - 1)
					{
						produceItemGlobal = false; //Ends the producer thread once it finish producing the necessary amount.
					}
					LocalDateTime ldt = LocalDateTime.now();
					while(queue.size() == bufferSize)
					{
						lock.wait(); //waits() if the producer fills the queue.
					}
					queue.add(ldt);
					producedCount++;
					System.out.println(producer.name()
							+ " produced time stamp " 
							+ ldt 
							+ ". "
							+ "The queue has "
							+ queue.size()
							+ " elements.");
					Thread.sleep(producerSleep);
					}
			}
		}
		producerList.remove(producer);
		System.out.println(producer.name() + " ending");
	}
	
	//synchronized consumeItem()
	boolean consumeItemGlobal = true; //Boolean value for consumeItem(). Determines whether the while loop continues to run.
	public void consumeItem(Consumer consumer) throws InterruptedException
	{
		while(consumeItemGlobal)
		{
			synchronized(lock)
			{
				while(queue.isEmpty() && (!producerList.isEmpty())) 
				{		
					lock.wait(); //wait() when queue is empty. Only runs if queue is empty AND there are still producer(s) running. 
				}
				if(producerList.isEmpty() && queue.isEmpty())
				{
					consumeItemGlobal = false; //If there are no producers running AND the queue is empty. Then there's nothing to consume, close thread.
				}
				else
				{			
					LocalDateTime ldt = queue.poll();
					System.out.println(consumer.name()
							+ " consumed time stamp " 
							+ ldt 
							+ ". "
							+ "The queue has "
							+ queue.size()
							+ " elements.");
					lock.notifyAll();
				}
			}
			Thread.sleep(consumerSleep);		
		}
		System.out.println(consumer.name() + " ending");
	}
	
	//creates thread name
	private static String changeName(int i, String type)
	{
		String s = type;
		String is = Integer.toString(i);
		String name = s + is;
		return name;
	}
	
	//runSimulation()
	public void runSimulation() throws InterruptedException
	{
		for(int i = 0; i < producerNum; i++) //Creates Producer Threads
		{
			producerList.add(new Producer(changeName(i+1, "P")));
		}
		
		for(int i = 0; i < consumerNum; i++) //Creates Consumer Threads
		{
			consumerList.add(new Consumer(changeName(i+1, "C")));
		}
		for(int i = 0; i < producerNum; i++) //Starts Producer Threads
		{
			producerList.get(i).start();
		}
		for(int i = 0; i < consumerNum; i++) //Starts Consumer Threads
		{
			consumerList.get(i).start();
		}
	}
	
	public static void main(String[] args) throws InterruptedException
	{
//		Buffer b = new Buffer(5,1,1,10,40,10); //1 producer, 1 consumer *part 1
//		Buffer b = new Buffer(10,2,2,20,20,10); //2 producer, 2 consumer *part 2
//		Buffer b = new Buffer(5,3,3,20,40,5); //3 producer, 3 consumer *part 3
//		Buffer b = new Buffer(5,6,4,20,4,5); //6 producer, 4 consumer *Extra credit
//		Buffer b = new Buffer(30,6,5,20,40,5); //6 producer, 4 consumer *Extra credit, more items
//		Buffer b = new Buffer(10,1,1,20,20,10); //1 producer, 1 consumer *part 1
//		Buffer b = new Buffer(10,1,0,20,20,10); //1 producer, ZERO consumer *part 1
//		Buffer b = new Buffer(5,1,1,20,20,10); //1 producer, 1 consumer *part 1
//		Buffer b = new Buffer(5,2,2,20,40,10); //2 producer, 2 consumer *part 2
//		Buffer b = new Buffer(5,1,1,20,40,10); //1 producer, 1 consumer *part 1
//		Buffer b = new Buffer(10,2,1,20,20,5); //2 producer, 1 consumer *part 2
		Buffer b = new Buffer(5,2,1,20,20,5); //2 producer, 1 consumer *part 2
//		Buffer b = new Buffer(10,1,2,20,20,10); //1 producer, 2 consumer *part 2	
		b.runSimulation();
	}
}