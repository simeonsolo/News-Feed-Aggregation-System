import java.net.*;
import java.io.*;
import java.util.*;

public class RequestScheduler {
	static PriorityQueue<Integer> PQ = new PriorityQueue<Integer>(); // priority queue of processes waiting for critical section
	static boolean free = true; // indicates if critical section is being used or not
	
	public RequestScheduler()
	{
		// add this requests' lamport time to queue
		int arrivalTime = AggregationServer.clock.increment();
		PQ.add(arrivalTime);
		// while request is unfulfilled
		while (true)
		{	
			// if critical section is free
			if (free)
			{
				// if head of queue
				if (PQ.poll() == arrivalTime)
				{
					// signify critical section is being used
					free = false;
					return;
				}
			}
			try {
				Thread.sleep(1000);
			} catch (Exception e) {}
		}
	}
}
