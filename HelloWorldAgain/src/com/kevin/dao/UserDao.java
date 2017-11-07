package com.kevin.dao;

import com.kevin.entity.User;
import com.sun.org.apache.bcel.internal.generic.ReturnInstruction;

public class UserDao {

	private User user;
	public User findUser(){
		return new User("kevin","kevin'password");
		}
}
