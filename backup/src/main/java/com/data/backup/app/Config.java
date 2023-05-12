package com.data.backup.app;

public class Config {
	private String user = "";
	private String pass = "";
	private String host = "";

	public String getUser() {
		return user;
	}
	public Config() {
		super();
	}
	public Config(String user, String pass, String host) {
		super();
		this.user = user;
		this.pass = pass;
		this.host = host;
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
