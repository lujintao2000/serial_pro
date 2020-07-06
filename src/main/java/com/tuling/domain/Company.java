package com.tuling.domain;

import org.msgpack.annotation.Message;

import java.io.Serializable;

@Message
public class Company implements Serializable{

	private String name;

	private User user;

//	public Company(){
//
//	}
	
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
	public boolean equals(Object obj){
		if(obj != null && obj instanceof  Company){
			if(((this.name == null && ((Company)obj).getName() == null) || (this.name != null && this.name.equals(((Company)obj).getName())))
					&& ((this.user == null && ((Company)obj).getUser() == null)) || (this.user != null && this.user.equals(((Company)obj).getUser()))){
				return true;
			}
		}
		return false;
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
