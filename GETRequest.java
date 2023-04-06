import java.net.*;
import java.io.*;
import java.util.*;
import java.nio.file.*;

public class GETRequest {
	public GETRequest(ObjectOutputStream oos) throws IOException {
		// convert text file to string to send to client
		String aggregateFeed = new String(Files.readAllBytes(Paths.get("atom.txt")));
		// check if empty feed
		if (aggregateFeed.equals(""))
		{
			oos.writeObject("204 - No Content\n~" + AggregationServer.clock.get()); // send response code
			return;
		}
		// send aggregate feed to client
		oos.writeObject("200 - OK\n\n" + aggregateFeed + "~" + AggregationServer.clock.increment());
		System.out.println("Successful GET Request @ LAMPORT TIME: " + AggregationServer.clock.get());
	}
}
