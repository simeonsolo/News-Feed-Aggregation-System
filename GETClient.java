import java.net.*;
import java.io.*;
import java.nio.file.*;

public class GETClient {
	public static void main(String[] args) throws IOException {
		// lamport clock initialisation
		LamportClock clock = new LamportClock();
		// obtain command line parameter for server
		String param = args[0];
		String[] connection = param.split(":",2);
		// validate command line parameter for server
		if (!connection[0].equals("localhost") && !connection[1].equals("4567"))
		{
			System.out.println("Invalid call.");
			System.exit(0);
		}
		/* ESTABLISH CONNECTION */
		Socket socket = null;
		boolean loop = true;
		// retry on fail every five seconds, until user manual stop
		while (loop)
		{
			try
			{
				// create socket connection
				socket = new Socket(connection[0], Integer.parseInt(connection[1]));
			} 
			catch (Exception e) {}
			if (socket != null) // if connection valid
			{
				loop = false;
			}
			else // if connection invalid
			{
				System.out.println("Connection failed. Will retry in 5 seconds.");
				try
				{
					Thread.sleep(5000); // try again in 5 seconds
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		// write to server
		ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
		oos.writeObject	("GET /atom.txt HTTP/1.1\n"+
					"Host: localhost\n"+
					"Lamport Clock Time:" + clock.increment() + "\n"+
					"\n");
		// read server response
		String response = "";
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(socket.getInputStream());
			response = (String) ois.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// split string to remove lamport time
		String[] arr = response.split("~",2);
		// print response (minus lamport)
		System.out.print(arr[0]);
		// update local lamport time
		clock.compareAndSet(Integer.parseInt(arr[1]));
		// write to local file (for testing purposes)
		try 
		(FileWriter fw = new FileWriter("output.txt", true);
		BufferedWriter bw = new BufferedWriter(fw);
		PrintWriter out = new PrintWriter(bw)) 
		{
			out.print(arr[0]);	
		} catch (Exception e) {
			e.printStackTrace();
		}
		// close resources
		ois.close();
		oos.close();
		// close socket
		socket.close();
	}
}
