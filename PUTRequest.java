import java.net.*;
import java.io.*;
import java.util.*;

public class PUTRequest {
	public PUTRequest(ObjectOutputStream oos, ObjectInputStream ois, String body, String[] inputArr) throws IOException {
			// identify number of entries
			Vector<Integer> entryIndex = new Vector<Integer>();
			for (int i = 6; i < inputArr.length; i++)
			{
				if (inputArr[i].equals("entry"))
				{
					entryIndex.add(i);
				}
			}
			// check if any content exists
			if (entryIndex.size() == 0)
			{
				oos.writeObject("204 - No Content~" + AggregationServer.clock.get()); // send response code
				return;
			}
			// verify validity of entries
			for (int i = 0; i < entryIndex.size(); i++)
			{
				if (!inputArr[entryIndex.get(i)+1].startsWith("title:"))
				{
					oos.writeObject("500 - Internal Server Error~" + AggregationServer.clock.get()); // send response code
					return;
				} else if (!inputArr[entryIndex.get(i)+2].startsWith("link:"))
				{	
					oos.writeObject("500 - Internal Server Error~" + AggregationServer.clock.get()); // send response code
					return;
				} else if (!inputArr[entryIndex.get(i)+3].startsWith("id:"))
				{
					oos.writeObject("500 - Internal Server Error~" + AggregationServer.clock.get()); // send response code
					return;
				}
			}
			// make vector of entries
			entryIndex.add(inputArr.length-2);
			Vector<String> entries = new Vector<String>();
			String temp;
			for (int i = 0; i < entryIndex.size()-1; i++)
			{
				temp = "";
				for (int j = entryIndex.get(i)+1; j < entryIndex.get(i+1); j++)
				{
					temp += inputArr[j] + "\n";
				}
				entries.add(temp);
			}
			
			// send array of entries to feed handler
			FeedHandler feedHandler = new FeedHandler(entries,oos,ois);
	}
}
