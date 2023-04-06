import java.net.*;
import java.io.*;
import java.util.*;

class RequestHandler extends Thread {
	private Socket socket;
	RequestHandler(Socket socket) {
		// assign socket to class variable
		this.socket = socket;
	}
	
	@Override
	public void run() {
		try {
			// create input stream object
			ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
			// read & convert to string
			String body = (String) ois.readObject();
			// create output stream object
			ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
			/* CHECK REQUEST TYPE AND CALL RESPECTIVE FUNCTION */
			String[] inputArr = body.split("\n",-1);
			// PUT request (content)
			if (inputArr[0].equals("PUT /atom.txt HTTP/1.1") && inputArr[1].equals("User-Agent: ATOMClient/1/0") && inputArr[2].equals("Content-Type: text/txt") && inputArr[3].startsWith("Content-Length: ") && inputArr[4].startsWith("Lamport Clock Time:"))
			{	
				// obtain lamport clock time from request
				String[] split = inputArr[4].split(":",2);
				int receivedLamportTime = Integer.parseInt(split[1]);
				// adjust local lamport time
				AggregationServer.clock.compareAndSet(receivedLamportTime);
				// PUT request
				PUTRequest Request = new PUTRequest(oos,ois,body,inputArr);
			}
			// GET request (client)
			else if (inputArr[0].equals("GET /atom.txt HTTP/1.1") && inputArr[1].equals("Host: localhost") && inputArr[2].startsWith("Lamport Clock Time:"))
			{
				// obtain lamport clock time from request
				String[] split = inputArr[2].split(":",2);
				int receivedLamportTime = Integer.parseInt(split[1]);
				// adjust local lamport time
				AggregationServer.clock.compareAndSet(receivedLamportTime);
				// GET request
				GETRequest Request = new GETRequest(oos);
			} else {
				// invalid request
				oos.writeObject("400 - Invalid Request\n"); // send response code
			}
			// free up critical section
			RequestScheduler.free = true;
			// close input/output streams
			ois.close();
			oos.close();
			// close socket connection
			socket.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
