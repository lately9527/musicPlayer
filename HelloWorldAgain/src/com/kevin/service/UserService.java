package com.kevin.service;

import com.kevin.dao.UserDao;
import com.kevin.entity.User;

public class UserService {

	UserDao userDao = new UserDao();
	public User find(){
		return userDao.findUser();
	}

}
