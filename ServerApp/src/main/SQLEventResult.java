package main;

class SQLEventResult {
	private int id;
	private String phoneNum;
	private String name;
	private String lat;
	private String lon;
	private String date;
	
	public SQLEventResult() {
		
	}
	
	public SQLEventResult(int id, String phone, String name, String lon, String lat, String date) {
		this.id = id;
		this.phoneNum = phone;
		this.name = name;
		this.lon = lon;
		this.lat = lat;
		this.date = date;
	}
	
	public int getId() {
		return id;
	}

	public String getPhoneNum() {
		return phoneNum;
	}

	public String getName() {
		return name;
	}

	public String getLat() {
		return lat;
	}

	public String getLon() {
		return lon;
	}

	public String getDate() {
		return date;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setPhoneNum(String phone) {
		this.phoneNum = phone;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setLat(String lat) {
		this.lat = lat;
	}

	public void setLon(String lon) {
		this.lon = lon;
	}

	public void setDate(String date) {
		this.date = date;
	}
	
	
}

