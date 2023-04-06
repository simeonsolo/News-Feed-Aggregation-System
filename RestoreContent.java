import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.file.*;

class RestoreContent extends Thread {
	RestoreContent() {}
	
	@Override
	public void run() {
		// convert file to string
		String aggregateFeed = "";
		try
		{
			aggregateFeed = new String(Files.readAllBytes(Paths.get("atom.txt")));
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		// separate into entries
		String[] temp = aggregateFeed.split("\n\n",-1);
		// add entries to static entries vector
		for (int i = 0 ; i < temp.length-1 ; i++)
		{
			FeedHandler.entries.add(temp[i]+"\n");
			FeedHandler.empty = false;
		}
		/* 1 minute window for content servers to reconnect before respective content is removed */
		// sleep for 1min
		try {
			Thread.sleep(60000);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		// call request scheduler as entering a critical section
		RequestScheduler requestScheduler = new RequestScheduler();
						
		// clear the unclaimed, dead entries
		boolean dead = true;
		boolean removed = false;
		for (int i = 0 ; i < FeedHandler.entries.size() ; i++)
		{
			if (removed == true)
			{
				i--;
				removed = false;
			}
			for (int j = 0 ; j < FeedHandler.connections.size() ; j++)
			{
				if (FeedHandler.entries.get(i).equals(FeedHandler.connections.get(j)))
				{	
					dead = false;
				}
			}
			if (dead == true)
			{
				FeedHandler.entries.remove(i);
				removed = true;
			}
			dead = true;
		}
		// convert new entries vector to string
		aggregateFeed = "";
		for (int i = 0; i < FeedHandler.entries.size(); i++)
		{
			aggregateFeed += FeedHandler.entries.get(i) + "\n";
			FeedHandler.empty = false;
		}
		// write to file
		try 
		(FileWriter fw = new FileWriter("atom.txt", false);
		BufferedWriter bw = new BufferedWriter(fw);
		PrintWriter out = new PrintWriter(bw)) 
		{
			out.print(aggregateFeed);	
		} catch (Exception e) {
			e.printStackTrace();
		}
		// print message to console
		System.out.println("Dead entries following restart have been removed from feed @ LAMPORT TIME: " + AggregationServer.clock.increment());
		// free up critical section
		RequestScheduler.free = true;
	}
}
