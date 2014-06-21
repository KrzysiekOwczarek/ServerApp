package main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

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

    /*public synchronized void writeUser(String phoneNum, int eventId) {
    	
    	String query = null;
    	
    	if(eventId != 0 && phoneNum != null) {
    		
    		if(!checkUser(phoneNum)) {
    			query = "INSERT INTO users(id, eventId, phoneNum) "
	    			+ "VALUES (NULL, '" + eventId + "', '" + phoneNum + "')";
    		}else {
    			query = "UPDATE users SET eventId = '"+eventId+"' WHERE phoneNum LIKE '"+phoneNum+"'";
    		}
	    	
	    	try {
	            Class.forName(DBDRIVER).newInstance();
	            connection = DriverManager.getConnection(DBURL, DBUSER, DBPASS);
	            statement = connection.createStatement();
	            statement.executeUpdate(query);
	 
	            statement.close();
	            connection.close();
	        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
	            e.printStackTrace();
	        }
    	}
    }*/

    public synchronized void writeLocation(String phoneNum, int id, String lat, String lon, String date) {
    	String query = "INSERT INTO locations(id, eventId, phoneNum, lat, lon, date) "
    			+ "VALUES (NULL, '" + id + "', '" + phoneNum + "', '" + lat + "', '" + lon + "', '" + date + "')";
    	
    	try {
            Class.forName(DBDRIVER).newInstance();
            connection = DriverManager.getConnection(DBURL, DBUSER, DBPASS);
            statement = connection.createStatement();
            statement.executeUpdate(query);
 
            statement.close();
            connection.close();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }
    
    public synchronized boolean writeEvent(String phoneNum, String eventName, String lat, String lon, String date) {
    	
    	if(this.checkEvent(phoneNum, eventName, lat, lon, date).getId() == 0) {
    		String query = "INSERT INTO events(id, phoneNum, name, lat, lon, date) "
    			+ "VALUES (NULL, '" + phoneNum + "', '" + eventName + "', '" + lat + "', '" + lon + "', '" + date + "')";
    	
    		try {
                Class.forName(DBDRIVER).newInstance();
                connection = DriverManager.getConnection(DBURL, DBUSER, DBPASS);
                statement = connection.createStatement();
                statement.executeUpdate(query);
     
                statement.close();
                connection.close();
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
                e.printStackTrace();
            }
    		
    		return true;
    	}
    	
    	return false;
    }
    
    public SQLEventResult checkEvent(String phoneNum, String eventName, String lat, String lon, String date) {
    	
    	String query = "SELECT * FROM events WHERE phoneNum LIKE '" + phoneNum + "' AND name LIKE '" + eventName + "' "
    			+ "AND lat LIKE '" + lat + "' AND lon LIKE '" + lon + "' AND date LIKE '" + date + "'";
    	
    	ResultSet result = null;
    	SQLEventResult eventResult = new SQLEventResult();
    	
    	try {
            Class.forName(DBDRIVER).newInstance();
            connection = DriverManager.getConnection(DBURL, DBUSER, DBPASS);
            statement = connection.createStatement();
            result = statement.executeQuery(query);
            
            while(result.next()) {
				eventResult.setId(result.getInt("id"));
				eventResult.setPhoneNum(result.getString("phoneNum"));
				eventResult.setName(result.getString("name"));
				eventResult.setLat(result.getString("lat"));
				eventResult.setLon(result.getString("lon"));
				eventResult.setDate(result.getString("date"));
			}

            statement.close();
            connection.close();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    	
    	return eventResult;
    		
    }
    
    public int checkEventById(int eventId) {
    	
    	query = "SELECT id FROM events WHERE id LIKE '"+eventId+"'";
    	
    	ResultSet result = null;
    	
    	try {
            Class.forName(DBDRIVER).newInstance();
            connection = DriverManager.getConnection(DBURL, DBUSER, DBPASS);
            statement = connection.createStatement();
            result = statement.executeQuery(query);
            
            while(result.next()) {
            	if(result.getInt("id") != 0)
            		return result.getInt("id");
            }
    		
            statement.close();
            connection.close();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    	
    	return 0;
    }   
    //OK
   
    public boolean checkUser(String phoneNum) {
    	String query = "SELECT * FROM users WHERE phoneNum LIKE '"+phoneNum+"'";
    	
    	try {
            Class.forName(DBDRIVER).newInstance();
            connection = DriverManager.getConnection(DBURL, DBUSER, DBPASS);
            statement = connection.createStatement();
            
            ResultSet result = statement.executeQuery(query);
 
            while(result.next()) {
            	if(result.getInt("id") != 0)
            		return true;
            }
            
            statement.close();
            connection.close();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    	
    	return false;
    }
    
    public SQLEventResult getEventById(int eventId) {
    	query = "SELECT * FROM events WHERE id LIKE '"+eventId+"'";
    	
    	SQLEventResult eventResult = new SQLEventResult();
    	
    	try {
            Class.forName(DBDRIVER).newInstance();
            connection = DriverManager.getConnection(DBURL, DBUSER, DBPASS);
            statement = connection.createStatement();
 
            ResultSet result = statement.executeQuery(query);
            
			while(result.next()) {
				eventResult.setId(result.getInt("id"));
				eventResult.setPhoneNum(result.getString("phoneNum"));
				eventResult.setName(result.getString("name"));
				eventResult.setLat(result.getString("lat"));
				eventResult.setLon(result.getString("lon"));
				eventResult.setDate(result.getString("date"));
			}
    		
            statement.close();
            connection.close();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    	
    	return eventResult;
    }
    
    //OK
    
    public ArrayList<SQLLocationResult> getLocations() {
    	ArrayList<SQLLocationResult> locationResults = new ArrayList<SQLLocationResult>();
    	query = "SELECT * FROM (SELECT * FROM locations ORDER BY date DESC ) as res GROUP BY phoneNum";
    	
    	try {
            Class.forName(DBDRIVER).newInstance();
            connection = DriverManager.getConnection(DBURL, DBUSER, DBPASS);
            statement = connection.createStatement();
 
            ResultSet result = statement.executeQuery(query);
            
			while(result.next()) {
				SQLLocationResult locationResult = new SQLLocationResult();
				locationResult.setId(result.getInt("id"));
				locationResult.setPhoneNum(result.getString("phoneNum"));
				locationResult.setEventId(result.getInt("eventId"));
				locationResult.setLat(result.getString("lat"));
				locationResult.setLon(result.getString("lon"));
				locationResult.setDate(result.getString("date"));
				
				locationResults.add(locationResult);
			}
    		
            statement.close();
            connection.close();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    	
    	return locationResults;
    }
}
