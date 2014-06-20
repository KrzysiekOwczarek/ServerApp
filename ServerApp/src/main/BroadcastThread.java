package main;

import java.util.ArrayList;

public class BroadcastThread extends Thread{
	private ArrayList<ClientThread> clientThreads;
	private boolean isRunning;
	private ClientDAO databaseConnection;
	private ArrayList<SQLLocationResult> result = null;
	
	public BroadcastThread(ArrayList<ClientThread> clientThreads) {
		this.clientThreads = clientThreads;
		this.isRunning = true;
		this.databaseConnection = new ClientDAO();
	}
	
	public void run() {
		while(isRunning) {
			synchronized(this) {
				result = databaseConnection.getLocations();
				
				for(ClientThread clientThread: clientThreads) {
					//if(clientThread) WYSŁANIE W RAMACH SESJI
					for(SQLLocationResult res: result){
						if(clientThread.getClientPhoneNum() != null && !res.getPhoneNum().equals(clientThread.getClientPhoneNum()) && res.getEventId() == clientThread.getClientEventId() ){
							System.out.println("send to " + clientThread.getClientPhoneNum() + " about " + res.getPhoneNum());
							clientThread.getOs().println("USR_LOC|"+res.getPhoneNum()+"|"+res.getEventId()+"|"+res.getLat()+"|"+res.getLon());
						}
					}
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
