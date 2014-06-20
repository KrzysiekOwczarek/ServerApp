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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
		
		BroadcastThread broadcastThread = new BroadcastThread(clientThreads);
		broadcastThread.start();
		
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
				//dopiero po hello powinien dodać
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
