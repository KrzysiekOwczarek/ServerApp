package main;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

class ClientThread extends Thread {
	private Socket clientSocket = null;
	private DataInputStream is = null;
	private PrintStream os = null;
	private BufferedReader reader = null;
	private MyServerApp server = null;
	private ClientDAO databaseConnection = new ClientDAO(); //DO SINGLETONA
	
	private int eventId = 0;
	private String phoneNum = null;
	
	private ArrayList<ClientThread> clientThreads;
	
	private int timeout = 120000;
	private int maxTimeout = 180000;
	
	volatile long lastReadTime;
	
	private boolean isRunning;
	
	public ClientThread(Socket clientSocket, ArrayList<ClientThread> clientThreads, MyServerApp server) {
		this.clientSocket = clientSocket;
		this.clientThreads = clientThreads;
		this.server = server;
		this.isRunning = true;
	}
	
	public void run() {
		try {
			this.clientSocket.setSoTimeout(timeout);
			is = new DataInputStream(clientSocket.getInputStream());
			os = new PrintStream(clientSocket.getOutputStream());
			reader = new BufferedReader(new InputStreamReader(is));
			
			while(isRunning) {
				try {
					String[] parts = null;
					String line = reader.readLine();
					
					if(line != null) {
						parts = line.split("\\|");
						lastReadTime = System.currentTimeMillis();
					}
					
					if(parts != null)
						switch(parts[0]) {
							case "HELLO": //PHONE_NUM
								try{
									this.phoneNum = parts[1];
									this.sendMsg("self", "HELLO_OK");
								}catch(ArrayIndexOutOfBoundsException ex) {
									this.sendMsg("self", "RE|HELLO");
								}
								
								break;
								
							case "LOC": //PHONE_NUM|LAT|LON
								//PRZYJECIE LOKALIZACJI OD USERA
								if(this.eventId != 0) {
									Date date = new Date();
									SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
									try {
										this.databaseConnection.writeLocation(parts[1], this.eventId, parts[2], parts[3], ft.format(date));
									}catch(ArrayIndexOutOfBoundsException ex) {
										this.sendMsg("self", "WRG_COMM");
									}
								}
								break;
								
							case "EVENT": //PHONE_NUM|EVENT_NAME|LAT|LON|DATE
								//REJESTRACJA EVENTU -> ODESŁANIE PRZYDZIELONEGO UNIQUE ID
								try {
									this.databaseConnection.writeEvent(parts[1], parts[2], parts[3], parts[4], parts[5]);
									
									if(this.databaseConnection.checkEvent(parts[1], parts[2], parts[3], parts[4], parts[5]).getId() != 0){
										this.eventId = this.databaseConnection.checkEvent(parts[1], parts[2], parts[3], parts[4], parts[5]).getId();
										String eventName = this.databaseConnection.checkEvent(parts[1], parts[2], parts[3], parts[4], parts[5]).getName();
										this.sendMsg("self", "EVENT_OK|"+eventName+"|"+eventId);
									}else {
										this.sendMsg("self", "EVENT_NOT_OK");
									}
									//ZWRACA NOWE ID JESLI STWORZY A STARE JESLI JEST
								}catch(ArrayIndexOutOfBoundsException ex) {
									this.sendMsg("self", "WRG_COMM");
								}
								
								break;
								
							case "REG": //PHONE_NUMBER|EVENTID
								//ZAPISANIE USERA NA WYDARZENIE
								try{
									if(parts[2] != null && Integer.parseInt(parts[2]) != 0) {
										if(this.databaseConnection.checkEventById(Integer.parseInt(parts[2])) != 0) {
											this.eventId = Integer.parseInt(parts[2]);
											//this.active = true;
											
											//this.databaseConnection.writeUser(parts[1], Integer.parseInt(parts[2]));
											
											SQLEventResult result = this.databaseConnection.getEventById(this.eventId);
											this.sendMsg("self", "REG_OK|"+result.getId()+"|"+result.getName()+"|"+result.getLat()+"|"+result.getLon()+"|"+result.getDate());
										}else{
											this.sendMsg("self", "REG_NOT_OK");
										}
									}
									//SPRAWDŹ CZY ISTNIEJE
									
								}catch(ArrayIndexOutOfBoundsException ex) {
									this.sendMsg("self", "WRG_COMM");
								}
								//READ FROM EVENT DB
								break;
							
						}
					
				}catch(SocketTimeoutException e) {
					if (!isConnectionAlive()) {
				        server.log("CLIENT " + this.phoneNum + " TIMEOUT!");
				       
				        for (int i = 0; i < clientThreads.size(); i++) {
				        	if(clientThreads.get(i) != null && clientThreads.get(i).phoneNum == this.phoneNum) {
				        		clientThreads.get(i).terminateThread();
				        		clientThreads.remove(i);
				        	}
				        }
				        
				    }
				}catch(IOException ex) {
					ex.printStackTrace();
				}
			}
			
			this.clientSocket.close();
			is.close();
			os.close();
		}catch(IOException ex) {
			ex.printStackTrace();
		}
		
		this.server.log("Client " + this.phoneNum + " terminated");
	}
	
	public void sendMsg(String phoneNum, String msg) {
		if(msg != null)
			if(phoneNum == "self")
				this.os.println(msg);
			else
	        	for (int i = 0; i < clientThreads.size(); i++) {
	        		if (clientThreads.get(i) != null && clientThreads.get(i).phoneNum == phoneNum) {
	        			clientThreads.get(i).os.println(msg);
	        			this.server.log("Msg: " + msg + " send to client " + clientThreads.get(i).phoneNum);
	        		}
	            }
	}
	
	/*public synchronized void broadcastMsg(String msg) {
		if(msg != null)
        	for (int i = 0; i < clientThreads.size(); i++) {
        		if (clientThreads.get(i) != null && clientThreads.get(i).phoneNum != null) {
        			clientThreads.get(i).os.println(msg);
        			this.server.log("Msg: " + msg + " send to client " + clientThreads.get(i).phoneNum);
        		}
            }
	}*/
	
	public synchronized void terminateThread() {
		this.isRunning = false;
        try {
            this.clientSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing client socket", e);
        }
	}
	
	public boolean isConnectionAlive() {
	    return System.currentTimeMillis() - lastReadTime < maxTimeout;
	}

	public PrintStream getOs() {
		return os;
	}

	public int getEventId() {
		return eventId;
	}

	public String getPhoneNum() {
		return phoneNum;
	}
	
	
}
