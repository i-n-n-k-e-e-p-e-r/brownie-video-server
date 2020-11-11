package org.brownie.server.core;

public class User {
	public static enum UserGroup {
		USER,
		ADMIN;
	}

	private long id;
	private String name;
	private UserGroup userGroup;
	
	public User(long id, String name, UserGroup userGroup) {
		setId(id);
		setName(name);
		setUserGroup(userGroup);
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public UserGroup getUserGroup() {
		return userGroup;
	}
	
	public void setUserGroup(UserGroup userGroup) {
		this.userGroup = userGroup;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
}
