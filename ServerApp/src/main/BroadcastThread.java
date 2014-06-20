package main;

import java.util.ArrayList;

public class BroadcastThread extends Thread{
	private ArrayList<ClientThread> clientThreads;
	private boolean isRunning;
	
	public BroadcastThread(ArrayList<ClientThread> clientThreads) {
		this.clientThreads = clientThreads;
		this.isRunning = true;
	}
	
	public void run() {
		while(isRunning) {
			synchronized(this) {
				for(ClientThread clientThread: clientThreads) {
					//if(clientThread) WYSŁANIE W RAMACH SESJI
					clientThread.getOs().println("TEST");
				}
				
	
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}
	}
	
	public synchronized void terminateThread() {
		this.isRunning = false;
        //try {
            //this.clientSocket.close();
        //} catch (IOException e) {
        //    throw new RuntimeException("Error closing client socket", e);
        //}
	}
}
