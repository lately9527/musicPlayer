package com.kevin.clinet;

import java.awt.Frame;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import com.kevin.entity.User;

public class MessageThread extends Thread {
	private BufferedReader reader;
	private PrintWriter writer;
	private JTextArea textArea;

	private DefaultListModel listModel;
	private Map<String, User> onLineUsers = new HashMap<String, User>();// 所有在线用户
	private boolean isConnected;
	private Frame frame;

	private Socket socket;

	// 接收消息线程的构造方法
	public MessageThread(BufferedReader reader, JTextArea textArea, Socket socket, Boolean isConnected,
			Map<String, User> onLineUsers, Frame frame) {
		this.reader = reader;
		this.textArea = textArea;
		this.socket = socket;
		this.isConnected = isConnected;
		this.onLineUsers = onLineUsers;
		this.frame = frame;
	}

	// 被动的关闭连接
	public synchronized void closeCon() throws Exception {
		// 清空用户列表
		listModel.removeAllElements();
		// 被动的关闭连接释放资源
		if (reader != null) {
			reader.close();
		}
		if (writer != null) {
			writer.close();
		}
		if (socket != null) {
			socket.close();
		}
		isConnected = false;// 修改状态为断开
	}

	@Override
	public void run() {
		String message = "";
		System.out.println("kevin");
		while (true) {
			try {
				message = reader.readLine();
				StringTokenizer stringTokenizer = new StringTokenizer(message, "/@");
				String command = stringTokenizer.nextToken();// 命令
				if (command.equals("CLOSE"))// 服务器已关闭命令
				{
					textArea.append("服务器已关闭!\r\n");
					closeCon();// 被动的关闭连接
					return;// 结束线程
				} else if (command.equals("ADD")) {// 有用户上线更新在线列表
					String username = "";
					String userIp = "";
					if ((username = stringTokenizer.nextToken()) != null
							&& (userIp = stringTokenizer.nextToken()) != null) {
						User user = new User(username, userIp);
						onLineUsers.put(username, user);
						listModel.addElement(username);
					}
				} else if (command.equals("DELETE")) {// 有用户下线更新在线列表
					String username = stringTokenizer.nextToken();
					User user = (User) onLineUsers.get(username);
					onLineUsers.remove(user);
					listModel.removeElement(username);
				} else if (command.equals("USERLIST")) {// 加载在线用户列表
					int size = Integer.parseInt(stringTokenizer.nextToken());
					String username = null;
					String userIp = null;
					for (int i = 0; i < size; i++) {
						username = stringTokenizer.nextToken();
						userIp = stringTokenizer.nextToken();
						User user = new User(username, userIp);
						onLineUsers.put(username, user);
						listModel.addElement(username);
					}
				} else if (command.equals("MAX")) {// 人数已达上限
					textArea.append(stringTokenizer.nextToken() + stringTokenizer.nextToken() + "\r\n");
					closeCon();// 被动的关闭连接
					JOptionPane.showMessageDialog(frame, "服务器缓冲区已满！", "错误", JOptionPane.ERROR_MESSAGE);
					return;// 结束线程
				} else {// 普通消息
					textArea.append(message + "\r\n");
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
