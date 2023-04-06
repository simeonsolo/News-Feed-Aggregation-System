import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.file.*;

public class AggregationServer {
	static LamportClock clock = null; // initialisation of lamport clock for AS
	public static void main(String[] args) throws Exception {
		System.out.println("Server starting...\n");
		/* LAMPORT CLOCK INITIALISATION */
		clock = new LamportClock();
		/* RESTORING CONTENT PRIOR TO RESTART (OR CRASH) */
		RestoreContent restoreContent = new RestoreContent();
		restoreContent.start();
		/* OPEN SERVER LISTENER THREAD */
		try {
			ServerListenerThread serverListenerThread = new ServerListenerThread(4567);
			serverListenerThread.start();
		} catch  (IOException e) {
			e.printStackTrace();
		}
	}
}
