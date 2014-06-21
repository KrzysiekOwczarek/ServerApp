package main;

import java.util.ArrayList;

public class BroadcastThread extends Thread{
	private ArrayList<ClientThread> clientThreads;
	private boolean isRunning;
	private ClientDAO databaseConnection;
	private ArrayList<SQLLocationResult> result = null;
	private MyServerApp server = null;
	
	public BroadcastThread(ArrayList<ClientThread> clientThreads, MyServerApp server) {
		this.clientThreads = clientThreads;
		this.isRunning = true;
		this.databaseConnection = new ClientDAO();
		this.server = server;
	}
	
	public void run() {
		while(isRunning) {
			synchronized(this) {
				result = databaseConnection.getLocations();
				
				clientThreads = server.getClientThreads();
				
				for(ClientThread clientThread: clientThreads) {
					for(SQLLocationResult res: result){
						if(clientThread.getPhoneNum() != null && !res.getPhoneNum().equals(clientThread.getPhoneNum()) && res.getEventId() == clientThread.getEventId() ){
							System.out.println("send to " + clientThread.getPhoneNum() + " about " + res.getPhoneNum());
							clientThread.getOs().println("USR_LOC|"+res.getPhoneNum()+"|"+res.getEventId()+"|"+res.getLat()+"|"+res.getLon());
						}
					}
				}
				
	
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					terminateThread();
					e.printStackTrace();
				}
				
			}
		}
	}
	
	public synchronized void terminateThread() {
		this.isRunning = false;
	}
}
