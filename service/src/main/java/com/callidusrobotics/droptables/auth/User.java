package com.callidusrobotics.droptables.auth;

public class User {
	private final String name;
	
	public User() {
		name = "";
	}
	
	public User(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
}