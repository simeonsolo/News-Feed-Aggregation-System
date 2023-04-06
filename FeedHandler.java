import java.net.*;
import java.io.*;
import java.nio.*;
import java.util.*;

public class FeedHandler {
	static boolean empty = true; // true if feed is empty
	static Vector<String> entries = new Vector<String>(); // static vector of entries
	static Vector<String> connections = new Vector<String>(); // helper vector for restoring content following restart/crash
	
	public FeedHandler(Vector<String> myEntries, ObjectOutputStream oos, ObjectInputStream ois) throws IOException {
		// append entries to static entries vector
		boolean exists = false;
		for (int i = 0 ; i < myEntries.size() ; i++)
		{
			// if not a duplicate entry, add
			for (int j = 0 ; j < entries.size() ; j++)
			{
				if (myEntries.get(i).equals(entries.get(j)))
				{
					exists = true;
				}
			}
			if (exists == false)
			{
				entries.add(myEntries.get(i));
			}
			connections.add(myEntries.get(i));
			exists = false;
		}
		// return response codes & print statements
		if (empty == true)
		{
			oos.writeObject("201 - HTTP_CREATED~" + AggregationServer.clock.increment());
			System.out.println("Successful PUT Request @ LAMPORT TIME: " + AggregationServer.clock.get());
		} else {
			oos.writeObject("200 - OK~" + AggregationServer.clock.increment());
			System.out.println("Successful PUT Request @ LAMPORT TIME: " + AggregationServer.clock.get());
		}
		// send lamport time
		oos.writeObject(AggregationServer.clock.get());
		// convert static entries vector to string
		String aggregateFeed = "";
		for (int i = 0; i < entries.size(); i++)
		{
			aggregateFeed += entries.get(i) + "\n";
			empty = false;
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
		// signify that critical section is complete (for scheduler)
		RequestScheduler.free = true;
		/* HEARTBEAT CHECK */
		HeartbeatCheck heartbeat = new HeartbeatCheck(ois, myEntries);
	}
}
