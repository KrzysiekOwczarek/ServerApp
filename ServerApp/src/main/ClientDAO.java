package main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
    		query = "INSERT INTO events(id, phoneNum, name, lat, lon, date) "
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
    	query = "SELECT id FROM events WHERE phoneNum LIKE '" + phoneNum + "' AND name LIKE '" + eventName + "' "
    			+ "AND lat LIKE '" + lat + "' AND lon LIKE '" + lon + "' AND date LIKE '" + date + "'";
    	
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
    
    public int checkEventById(int eventId) {
    	
    	query = "SELECT id FROM events WHERE id LIKE '"+eventId+"'";
    	
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
}
