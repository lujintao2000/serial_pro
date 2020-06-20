package com.tuling.domain;

import org.msgpack.annotation.Message;

import java.io.Serializable;

@Message
public class Company implements Serializable{

	private String name;

	private User user;

	public Company(){
		
	}
	
	public Company(String name){
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@Override
	public int hashCode(){
		int result = 1;
		if(this.name != null){
			result *= this.name.hashCode();
		}
		return result;
	}
}
