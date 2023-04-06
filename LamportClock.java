import java.net.*;
import java.io.*;

public class LamportClock {
	private int time = 0; // time
	
	public LamportClock() {}
	
	public synchronized int get() { // get function (synchronized)
		return time;
	}
	
	public synchronized int increment() { // increment and get function (synchronized)
		time++;
		return time;
	}
	
	public synchronized void compareAndSet(int receivedTime) { // synchronize lamport clock time with connected clients following event (synchronized)
		time = Math.max(time,receivedTime);
		increment();
	} 
}
