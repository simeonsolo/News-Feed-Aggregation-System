import java.net.*;
import java.io.*;

public class ServerListenerThread extends Thread {
	
	private int port; // port number	
	private ServerSocket serverSocket; // socket
	
	public ServerListenerThread(int port) throws IOException {
		// assign port to class variable
		this.port = port;
		// create socket
		this.serverSocket = new ServerSocket(this.port);
	}
	
	@Override
	public void run() {
		try {
			// while aggregate server is alive
			while(serverSocket.isBound() && !serverSocket.isClosed()) {
			// accept clients
			Socket socket = serverSocket.accept();
			// call request scheduler
			RequestScheduler scheduler = new RequestScheduler();
			// call request handler
			RequestHandler requestHandler = new RequestHandler(socket);
			requestHandler.start();
			}	
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (serverSocket != null) {
				try {
				serverSocket.close();	
				} catch (IOException e) {}
			}
		}
	}
	
}
