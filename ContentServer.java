import java.net.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class ContentServer {
	public static void main(String[] args) throws IOException {
		// lamport clock initialisation
		LamportClock clock = new LamportClock();
		// obtain command line parameter for server
		String param = args[0];
		String[] connection = param.split(":",2);
		String fileName = args[1];
		// verify command line parameter
		if (args.length != 2 && !connection[0].equals("localhost") && !connection[1].equals("4567"))
		{
			System.out.println("Invalid call.");
			System.exit(0);
		}
		/* ESTABLISH CONNECTION */
		Socket socket;
		boolean loop = true;
		boolean connect = true;
		boolean alive = true;
		// loop until program manually stopped
		while (loop)
		{
			socket = null;
			// retry on fail every five seconds, or until user manually stops
			connect = true;
			while (connect)
			{
				try
				{
					// create socket connection
					socket = new Socket(connection[0], Integer.parseInt(connection[1]));
				} 
				catch (Exception e) {}
				if (socket != null) // if connection is valid
				{
					connect = false; // leave loop
				}
				else // if connection is invalid
				{
					System.out.println("Connection failed. Will retry in 5 seconds.");
					try
					{
						Thread.sleep(5000); // sleep 5 seconds, try again
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
			// read input file into string
			String entry = new String(Files.readAllBytes(Paths.get(fileName)));
			// write to server
			ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
			oos.writeObject	("PUT /atom.txt HTTP/1.1\n"+
						"User-Agent: ATOMClient/1/0\n"+
						"Content-Type: text/txt\n"+
						"Content-Length: 1000\n"+
						"Lamport Clock Time:" + clock.increment() +"\n"+
						"\n"+
						entry+
						"\n");
			// read server response
			ObjectInputStream ois = null;
			String response = "";
			String lamportTime = "";
			try {
				ois = new ObjectInputStream(socket.getInputStream());
				response = (String) ois.readObject();
			} catch (Exception e) {
				e.printStackTrace();
			}
			// split string to remove lamport time
			String[] arr = response.split("~",2);
			// if failed request, exit
			if (!(arr[0].startsWith("200") || arr[0].startsWith("201")))
			{
				// write to file for testing purposes
				try 
				(FileWriter fw = new FileWriter("outputCS.txt", true);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter out = new PrintWriter(bw)) 
				{
					out.print(arr[0]+"\n");	
				} catch (Exception e) {
					e.printStackTrace();
				}
				// exit
				System.exit(0);
			}
			// print response (minus lamport)
			System.out.println(arr[0]);
			// update local lamport time
			clock.compareAndSet(Integer.parseInt(arr[1]));
			// write to file for testing purposes
			try 
			(FileWriter fw = new FileWriter("outputCS.txt", true);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter out = new PrintWriter(bw)) 
			{
				out.print(arr[0]+"\n");	
			} catch (Exception e) {
				e.printStackTrace();
			}
			// send heartbeats every 12sec to keep content alive
			alive = true;
			while (alive)
			{
				try {
					oos.writeObject("I'm alive!");
					Thread.sleep(12000);
				} catch (Exception e) {
					System.out.println("Connection lost. Will attempt to reconnect.");
					// document connection loss to file for testing
					try 
					(FileWriter fw = new FileWriter("outputCS.txt", true);
					BufferedWriter bw = new BufferedWriter(fw);
					PrintWriter out = new PrintWriter(bw)) 
					{
						out.print("Connection lost. Will attempt to reconnect.\n");	
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					// leave heartbeat loop, attempt to reconnect
					alive = false;
				}				
			}
			try {
				// close resources
				ois.close();
				oos.close();
				// close socket
				socket.close();
			} catch (Exception e) {}
		}
	}
}
