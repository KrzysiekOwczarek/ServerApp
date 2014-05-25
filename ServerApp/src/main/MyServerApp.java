package main;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.sql.Timestamp;
import java.util.Date;

public class MyServerApp implements Runnable {
	
	protected int serverPort = 4000;
	protected ServerSocket serverSocket = null;
	protected ArrayList<ClientThread> clientThreads;
	protected boolean isStopped = false;
	
	public static void main(String[] args) {
		MyServerApp server = new MyServerApp(4021);
		new Thread(server).start();
	}
	
	public MyServerApp(int port) {
		this.serverPort = port;
		this.clientThreads = new ArrayList<ClientThread>();
	}

	@Override
	public void run() {
		try {
			serverSocket = new ServerSocket(this.serverPort);
		}catch(IOException ex) {
			throw new RuntimeException("Cannot open port for server!");
		}
		
		if(serverSocket != null)
			System.out.println("Server started on port: " + this.serverPort);
		
		while(!isStopped()) {
			Socket clientSocket = null;
			
			try {
				clientSocket = serverSocket.accept();
			}catch(SocketTimeoutException ex) {
				System.out.println("Client disconnected to server!");
			}catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(clientSocket != null) {
				ClientThread tmpClientThread = new ClientThread(clientSocket, clientThreads, this);
				tmpClientThread.start();
				this.clientThreads.add(tmpClientThread);
			}
		}
		
	}
	
	//disconnect -> TODO
	
	private synchronized boolean isStopped() {
        return this.isStopped;
    }
	
	public synchronized void stop(){
        this.isStopped = true;
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }

	public void log(String string) {
		System.out.println(string);
	}
}

class ClientThread extends Thread {
	private String clientName = null;
	private Socket clientSocket = null;
	private DataInputStream is = null;
	private PrintStream os = null;
	private BufferedReader reader = null;
	private MyServerApp server = null;
	
	private ArrayList<ClientThread> clientThreads;
	
	private int timeout = 10000;
	private int maxTimeout = 25000;
	
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
			
			Date date = new Date();
			this.clientName = "Client_from_" + new Timestamp(date.getTime());
			
			while(isRunning) {
				try {
					String line = reader.readLine();
					
					lastReadTime = System.currentTimeMillis();
			        if (line != null && line.startsWith("/quit")) {
			        	break;
			        }
			        
			        if(line != null)
				        synchronized (this) {
				        	for (int i = 0; i < clientThreads.size(); i++) {
				        		if (clientThreads.get(i) != null && clientThreads.get(i).clientName != null) {
				        			clientThreads.get(i).os.println("<" + this.clientName + "> " + line);
				        			this.server.log("Msg: " + line + " send to client " + clientThreads.get(i).clientName + " by " + this.clientName);
				        		}
				            }
				        }
				}catch(SocketTimeoutException e) {
					if (!isConnectionAlive()) {
				        server.log("CLIENT " + this.clientName + " TIMEOUT!");
				       
				        for (int i = 0; i < clientThreads.size(); i++) {
				        	if(clientThreads.get(i) != null && clientThreads.get(i).clientName == this.clientName) {
				        		clientThreads.get(i).terminateThread();
				        		clientThreads.remove(i);
				        	}
				        }
				        
				    } else {
				        sendHeartBeat(); //Send a heartbeat to the client
				    }
				}catch (SocketException e) {
					e.printStackTrace();
				}catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			this.clientSocket.close();
			is.close();
			os.close();
		}catch(IOException ex) {
			ex.printStackTrace();
		}
		
		this.server.log("Client " + this.clientName + " terminated");
	}
	
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
	
	public void sendHeartBeat() {
		this.os.println("CHECK_ALIVE");
	}
}
