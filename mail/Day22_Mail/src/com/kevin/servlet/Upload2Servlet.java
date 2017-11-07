package com.kevin.servlet;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.tomcat.util.http.fileupload.IOUtils;


/**
 * Servlet implementation class Upload1Servlet
 */
@WebServlet("/Upload2Servlet")
public class Upload2Servlet extends HttpServlet {

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		response.setContentType("txt/html;charset=utf-8");
		
		DiskFileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload servletFileUpload = new ServletFileUpload(factory);
		try {
			List<FileItem> fileItems = servletFileUpload.parseRequest(request);
			FileItem f1 = fileItems.get(0);
			FileItem f2 = fileItems.get(1);
			System.out.println("普通表单项演示："+f1.getFieldName()+"="+f1.getString("UTF-8"));
			System.out.println("文件表单项演示");
			System.out.println("ContentType:"+f2.getContentType());
			System.out.println("size:"+f2.getSize());
			System.out.println("fileName"+f2.getName());
			
			File destFile = new File("d:/"+f1.getString("UTF-8")+".jpg"); 
			try {
				f2.write(destFile);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (FileUploadException e) {
			e.printStackTrace();
		}
	}

}
