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
	private String clientPhoneNum = null;
	private Socket clientSocket = null;
	private int clientEventId = 0;
	private DataInputStream is = null;
	private PrintStream os = null;
	private BufferedReader reader = null;
	private MyServerApp server = null;
	private ClientDAO databaseConnection = new ClientDAO();
	
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
			//this.clientSocket.setSoTimeout(timeout);
			is = new DataInputStream(clientSocket.getInputStream());
			os = new PrintStream(clientSocket.getOutputStream());
			reader = new BufferedReader(new InputStreamReader(is));
			
			//Date date = new Date();
			//this.clientName = "Client_from_" + new Timestamp(date.getTime());
			
			while(isRunning) {
				try {
					String[] parts = null;
					String line = reader.readLine();
					
					if(line != null)
						parts = line.split("\\|");
					
					if(parts != null)
						switch(parts[0]) {
							case "HELLO": //PHONE_NUM
								try{
									this.clientPhoneNum = parts[1];
									this.sendMsg("self", "HELLO");
								}catch(ArrayIndexOutOfBoundsException ex) {
									this.sendMsg("self", "RE|HELLO");
								}
								
								break;
								
							case "LOC": //PHONE_NUM|LAT|LON
								//PRZYJECIE LOKALIZACJI OD USERA
								Date date = new Date();
								SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
								try {
									this.databaseConnection.writeLocation(parts[1], this.clientEventId, parts[2], parts[3], ft.format(date));
								}catch(ArrayIndexOutOfBoundsException ex) {
									this.sendMsg("self", "WRG_COMM");
								}
								break;
								
							case "EVENT": //PHONE_NUM|EVENT_NAME|LAT|LON|DATE
								//REJESTRACJA EVENTU -> ODESŁANIE PRZYDZIELONEGO UNIQUE ID
								try {
									this.databaseConnection.writeEvent(parts[1], parts[2], parts[3], parts[4], parts[5]);
									int eventId = this.databaseConnection.checkEvent(parts[1], parts[2], parts[3], parts[4], parts[5]);
									this.clientEventId = eventId;
									this.sendMsg("self", "EVENT_OK|"+eventId);
									//ZWRACA NOWE ID JESLI STWORZY A STARE JESLI JEST
								}catch(ArrayIndexOutOfBoundsException ex) {
									this.sendMsg("self", "WRG_COMM");
								}
								
								break;
								
							case "REG": //PHONE_NUMBER|EVENTID
								//ZAPISANIE USERA NA WYDARZENIE
								//this.clientEventId = parts[2];
								//READ FROM EVENT DB
								break;
							
						}
					
				}catch(IOException ex) {
					ex.printStackTrace();
				}
					/*
						
					
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
				
				*/
			}
			
			this.clientSocket.close();
			is.close();
			os.close();
		}catch(IOException ex) {
			ex.printStackTrace();
		}
		
		this.server.log("Client " + this.clientPhoneNum + " terminated");
	}
	
	
	public void sendMsg(String phoneNum, String msg) {
		if(msg != null)
			if(phoneNum == "self")
				this.os.println(msg);
			else
	        	for (int i = 0; i < clientThreads.size(); i++) {
	        		if (clientThreads.get(i) != null && clientThreads.get(i).clientPhoneNum == phoneNum) {
	        			clientThreads.get(i).os.println(msg);
	        			this.server.log("Msg: " + msg + " send to client " + clientThreads.get(i).clientPhoneNum);
	        		}
	            }
	}
	
	public synchronized void broadcastMsg(String msg) {
		if(msg != null)
        	for (int i = 0; i < clientThreads.size(); i++) {
        		if (clientThreads.get(i) != null && clientThreads.get(i).clientPhoneNum != null) {
        			clientThreads.get(i).os.println(msg);
        			this.server.log("Msg: " + msg + " send to client " + clientThreads.get(i).clientPhoneNum);
        		}
            }
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

class ClientDAO {
	private final static String DBURL = "jdbc:mysql://127.0.0.1:8889/meet";
    private final static String DBUSER = "root";
    private final static String DBPASS = "root";
    private final static String DBDRIVER = "com.mysql.jdbc.Driver";

    //obiekt tworzący połączenie z bazą danych.
    private Connection connection;
    //obiekt pozwalający tworzyć nowe wyrażenia SQL
    private Statement statement;
    //zapytanie SQL
    private String query;
 
    public ClientDAO() { }

    public void writeLocation(String phoneNum, int id, String lat, String lon, String date) {
    	query = "INSERT INTO locations(id, eventId, phoneNum, lat, lon, date) "
    			+ "VALUES (NULL, '" + id + "', '" + phoneNum + "', '" + lat + "', '" + lon + "', '" + date + "')";
    	
    	if(id != 0)
    		this.executeQuery();
    }
    
    public boolean writeEvent(String phoneNum, String eventName, String lat, String lon, String date) {
    	
    	if(this.checkEvent(phoneNum, eventName, lat, lon, date) == 0) {
    		query = "INSERT INTO events(id, eventPhoneNum, eventName, eventLat, eventLon, eventDate) "
    			+ "VALUES (NULL, '" + phoneNum + "', '" + eventName + "', '" + lat + "', '" + lon + "', '" + date + "')";
    	
    		this.executeQuery();
    		
    		return true;
    	}
    	
    	return false;
    }
    
    private void executeQuery() {
    	try {
            Class.forName(DBDRIVER).newInstance();
            connection = DriverManager.getConnection(DBURL, DBUSER, DBPASS);
            statement = connection.createStatement();
            statement.executeUpdate(query);
 
            //zwolnienie zasobów i zamknięcie połączenia
            statement.close();
            connection.close();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }
    
    public int checkEvent(String phoneNum, String eventName, String lat, String lon, String date) {
    	query = "SELECT id FROM events WHERE eventPhoneNum LIKE '" + phoneNum + "' AND eventName LIKE '" + eventName + "' "
    			+ "AND eventLat LIKE '" + lat + "' AND eventLon LIKE '" + lon + "' AND eventDate LIKE '" + date + "'";
    	
    	ResultSet result = null;
    	
    	try {
            Class.forName(DBDRIVER).newInstance();
            connection = DriverManager.getConnection(DBURL, DBUSER, DBPASS);
            statement = connection.createStatement();
            //statement.executeUpdate(query);
 
            result = statement.executeQuery(query);
            //zwolnienie zasobów i zamknięcie połączenia
            
			if(result.first())
				return Integer.parseInt(result.getString("id"));
    		
            
            statement.close();
            connection.close();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    	
    	return 0;
    		
    }
}
