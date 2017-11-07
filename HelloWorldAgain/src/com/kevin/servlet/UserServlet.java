package com.kevin.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.kevin.entity.User;
import com.kevin.service.UserService;
import com.sun.xml.internal.ws.developer.UsesJAXBContext;

@WebServlet("/UserServlet")
public class UserServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public UserServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		UserService userService = new UserService();
		User kevin=userService.find();
	
		request.setAttribute("kevin", kevin);
		request.getRequestDispatcher("/result.jsp").forward(request, response);
		
	}


}
