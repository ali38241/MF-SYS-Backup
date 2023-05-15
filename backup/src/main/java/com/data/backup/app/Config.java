package com.data.backup.app;

public class Config {
	private String user = "";
	private String pass = "";
	private String host = "";
	private String db = "";

	public String getDb() {
		return db;
	}
	public void setDb(String db) {
		this.db = db;
	}
	public String getUser() {
		return user;
	}
	public Config() {
		super();
	}
	public Config(String db, String user, String pass, String host) {
		super();
		this.user = user;
		this.pass = pass;
		this.host = host;
		this.db = db;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getPass() {
		return pass;
	}
	public void setPass(String pass) {
		this.pass = pass;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	
}
