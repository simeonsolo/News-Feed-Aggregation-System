import java.net.*;
import java.io.*;
import java.nio.*;
import java.util.*;

public class HeartbeatCheck {
	public HeartbeatCheck(ObjectInputStream ois, Vector<String> myEntries)
	{
		// while content server is alive
		boolean alive = true;
		while (alive)
		{
			try // check if heartbeat has been sent by CS in last 12sec
			{
				ois.readObject();
				Thread.sleep(12000);
			} 
			catch (Exception e) // if heartbeat does not exist
			{	
				// call request scheduler as now entering critical section
				RequestScheduler heartbeatRequest = new RequestScheduler();
				// cancel loop
				alive = false;
				// remove this process' entries from feed
				boolean removed = false;
				for (int i = 0 ; i < FeedHandler.entries.size() ; i++)
				{
					if (removed == true)
					{
						i--;
						removed = false;
					}
					for (int j = 0 ; j < myEntries.size() ; j++)
					{
						// if entry == my entry/s
						if (FeedHandler.entries.get(i).equals(myEntries.get(j)))
						{
							FeedHandler.entries.remove(i);
							// decrement index as an entry has been removed
							removed = true;
						}
					}
				}
				// convert new entries vector to string
				String aggregateFeed = "";
				for (int i = 0; i < FeedHandler.entries.size(); i++)
				{
					aggregateFeed += FeedHandler.entries.get(i) + "\n";
					FeedHandler.empty = false;
				}
				// rewrite to file
				try 
				(FileWriter fw = new FileWriter("atom.txt", false);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter out = new PrintWriter(bw)) 
				{
					out.print(aggregateFeed);	
				} catch (Exception error) {
					error.printStackTrace();
				}
				// print statement & lamport clock addition to signify update to feed
				System.out.println("CS connection lost - feed updated @ LAMPORT TIME: " + AggregationServer.clock.increment());
			}	
		}
	}
}
