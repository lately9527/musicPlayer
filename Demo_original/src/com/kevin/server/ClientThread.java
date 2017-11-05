package com.kevin.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.DefaultListModel;
import javax.swing.JTextArea;

import com.kevin.entity.User;

public // 为一个客户端服务的线程
class ClientThread extends Thread {
	Socket socket;
	BufferedReader reader;
	PrintWriter writer;
	User user;
	
	private ArrayList<ClientThread> clients;
	private JTextArea contentArea;
	private DefaultListModel listModel;

	public BufferedReader getReader() {
		return reader;
	}

	public PrintWriter getWriter() {
		return writer;
	}

	public User getUser() {
		return user;
	}

	// 客户端线程的构造方法
	public ClientThread(Socket socket,ArrayList<ClientThread> clients,JTextArea contentArea,DefaultListModel listModel) {
		try {
			this.socket = socket;
			this.clients = clients;
			this.listModel = listModel;
			this.contentArea = contentArea;
			reader = new BufferedReader(new InputStreamReader(socket
					.getInputStream()));
			writer = new PrintWriter(socket.getOutputStream());
			// 接收客户端的基本用户信息
			String inf = reader.readLine();
			StringTokenizer st = new StringTokenizer(inf, "@");
			user = new User(st.nextToken(), st.nextToken());
			// 反馈连接成功信息
			writer.println(user.getName() + user.getIp() + "与服务器连接成功!");
			writer.flush();
			// 反馈当前在线用户信息
			if (clients.size() > 0) {
				String temp = "";
				for (int i = clients.size() - 1; i >= 0; i--) {
					temp += (clients.get(i).getUser().getName() + "/" + clients
							.get(i).getUser().getIp())
							+ "@";
				}
				writer.println("USERLIST@" + clients.size() + "@" + temp);
				writer.flush();
			}
			// 向所有在线用户发送该用户上线命令
			for (int i = clients.size() - 1; i >= 0; i--) {
				clients.get(i).getWriter().println(
						"ADD@" + user.getName() + user.getIp());
				clients.get(i).getWriter().flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("deprecation")
	public void run() {// 不断接收客户端的消息，进行处理。
		String message = null;
		while (true) {
			try {
				message = reader.readLine();// 接收客户端消息
				if (message.equals("CLOSE"))// 下线命令
				{
					contentArea.append(this.getUser().getName()
							+ this.getUser().getIp() + "下线!\r\n");
					// 断开连接释放资源
					reader.close();
					writer.close();
					socket.close();

					// 向所有在线用户发送该用户的下线命令
					for (int i = clients.size() - 1; i >= 0; i--) {
						clients.get(i).getWriter().println(
								"DELETE@" + user.getName());
						clients.get(i).getWriter().flush();
					}

					listModel.removeElement(user.getName());// 更新在线列表

					// 删除此条客户端服务线程
					for (int i = clients.size() - 1; i >= 0; i--) {
						if (clients.get(i).getUser() == user) {
							ClientThread temp = clients.get(i);
							clients.remove(i);// 删除此用户的服务线程
							temp.stop();// 停止这条服务线程
							return;
						}
					}
				} else {
					dispatcherMessage(message);// 转发消息
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// 转发消息
	public void dispatcherMessage(String message) {
		StringTokenizer stringTokenizer = new StringTokenizer(message, "@");
		String source = stringTokenizer.nextToken();
		String owner = stringTokenizer.nextToken();
//		System.out.println();
//		System.out.println(source+" "+owner);
	String content = stringTokenizer.nextToken();
		message = source + "说：" + content;
		contentArea.append(message + "\r\n");
		if (owner.equals("ALL")) {// 群发
			System.out.println("群发");
			toAll(message);}
		else{
			System.out.println("单人发送");
			toSingle(message,source,owner);}
		
	}
	
	
	//群发消息
	public void toAll(String message){
		for (int i = clients.size() - 1; i >= 0; i--) {
			System.out.println("clients.get(i): "+clients.get(i).getName());
			clients.get(i).getWriter().println(message + "(多人发送)");
			clients.get(i).getWriter().flush();
		}
	}
	//单独发送消息
	public void toSingle(String message,String source,String owner){

		for (int i = clients.size() - 1; i >= 0; i--) {
			System.out.println(clients.get(i).getUser().getName());
			if(clients.get(i).getUser().getName().equals(owner)){
				System.out.println(owner+"将要转发消息给"+source);
				clients.get(i).getWriter().println(message + "(单人发送)"+source);
				clients.get(i).getWriter().flush();
			}
		}
	}
}
