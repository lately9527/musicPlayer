package com.kevin.server;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import com.kevin.entity.User;

public class Server {

	private JFrame frame;
	private JTextArea contentArea;
	private JTextField txt_message;
	private JTextField txt_max;
	private JTextField txt_port;
	private JButton btn_start;
	private JButton btn_stop;
	private JButton btn_send;
	private JPanel northPanel;
	private JPanel southPanel;
	private JScrollPane rightPanel;
	private JScrollPane leftPanel;
	private JSplitPane centerSplit;
	private JList userList;
	private DefaultListModel listModel;

	private ServerSocket serverSocket;
	private ServerThread serverThread;
	private ArrayList<ClientThread> clients;

	private boolean isStart = false;

	// 主方法,程序执行入口
	public static void main(String[] args) {
		new Server();
	}

	// 执行消息发送
	public void send() {
		if (!isStart) {
			JOptionPane.showMessageDialog(frame, "服务器还未启动,不能发送消息！", "错误",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (clients.size() == 0) {
			JOptionPane.showMessageDialog(frame, "没有用户在线,不能发送消息！", "错误",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		String message = txt_message.getText().trim();
		if (message == null || message.equals("")) {
			JOptionPane.showMessageDialog(frame, "消息不能为空！", "错误",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		/**
		 * 服务器发送消息
		 * 判断是否群发消息
		 */
		if (message.contains("@ALL")) {
			System.out.println("群发消息");
			sendServerMessage(message);// 群发服务器消息
			contentArea.append("服务器说：" + txt_message.getText() + "\r\n");
			txt_message.setText(null);
		}else{
			System.out.println("单独发送消息");
			sendServerMessage(message);// 群发服务器消息
			contentArea.append("服务器说：" + txt_message.getText() + "\r\n");
			txt_message.setText(null);
		}
		
	}

	// 构造放法
	public Server() {
		frame = new JFrame("服务器");
		// 更改JFrame的图标：
		ImageIcon icon=new ImageIcon("logo.png");
		frame.setIconImage(icon.getImage());
		contentArea = new JTextArea();
		contentArea.setEditable(false);
		contentArea.setForeground(Color.blue);
		txt_message = new JTextField();
		txt_max = new JTextField("30");
		txt_port = new JTextField("6666");
		btn_start = new JButton("启动");
		btn_stop = new JButton("停止");
		btn_send = new JButton("发送");
		btn_stop.setEnabled(false);
		listModel = new DefaultListModel();
		userList = new JList(listModel);

		southPanel = new JPanel(new BorderLayout());
		southPanel.setBorder(new TitledBorder("写消息"));
		southPanel.add(txt_message, "Center");
		southPanel.add(btn_send, "East");
		leftPanel = new JScrollPane(userList);
		leftPanel.setBorder(new TitledBorder("在线用户"));

		rightPanel = new JScrollPane(contentArea);
		rightPanel.setBorder(new TitledBorder("消息显示区"));

		centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel,
				rightPanel);
		centerSplit.setDividerLocation(100);
		northPanel = new JPanel();
		northPanel.setLayout(new GridLayout(1, 6));
		northPanel.add(new JLabel("人数上限"));
		northPanel.add(txt_max);
		northPanel.add(new JLabel("端口"));
		northPanel.add(txt_port);
		northPanel.add(btn_start);
		northPanel.add(btn_stop);
		northPanel.setBorder(new TitledBorder("配置信息"));

		frame.setLayout(new BorderLayout());
		frame.add(northPanel, "North");
		frame.add(centerSplit, "Center");
		frame.add(southPanel, "South");
		frame.setSize(1000, 600);
		//frame.setSize(Toolkit.getDefaultToolkit().getScreenSize());//设置全屏
		int screen_width = Toolkit.getDefaultToolkit().getScreenSize().width;
		int screen_height = Toolkit.getDefaultToolkit().getScreenSize().height;
		frame.setLocation((screen_width - frame.getWidth()) / 2,
				(screen_height - frame.getHeight()) / 2);
		frame.setVisible(true);

		// 关闭窗口时事件
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if (isStart) {
					closeServer();// 关闭服务器
				}
				super.windowClosing(e);  // 退出程序
			}
		});

		// 文本框按回车键时事件
		txt_message.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				send();
			}
		});

		// 单击发送按钮时事件
		btn_send.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				send();
			}
		});

		// 单击启动服务器按钮时事件
		btn_start.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (isStart) {
					JOptionPane.showMessageDialog(frame, "服务器已处于启动状态，不要重复启动！",
							"错误", JOptionPane.ERROR_MESSAGE);
					return;
				}
				int max;
				int port;
				try {
					try {
						max = Integer.parseInt(txt_max.getText());
					} catch (Exception e1) {
						throw new Exception("人数上限为正整数！");
					}
					if (max <= 0) {
						throw new Exception("人数上限为正整数！");
					}
					try {
						port = Integer.parseInt(txt_port.getText());
					} catch (Exception e1) {
						throw new Exception("端口号为正整数！");
					}
					if (port <= 0) {
						throw new Exception("端口号 为正整数！");
					}
					serverStart(max, port);
					contentArea.append("服务器已成功启动!人数上限：" + max + ",端口：" + port
							+ "\r\n");
					JOptionPane.showMessageDialog(frame, "服务器成功启动!");
					btn_start.setEnabled(false);
					txt_max.setEnabled(false);
					txt_port.setEnabled(false);
					btn_stop.setEnabled(true);
				} catch (Exception exc) {
					JOptionPane.showMessageDialog(frame, exc.getMessage(),
							"错误", JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		// 单击停止服务器按钮时事件
		btn_stop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!isStart) {
					JOptionPane.showMessageDialog(frame, "服务器还未启动，无需停止！", "错误",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				try {
					closeServer();
					btn_start.setEnabled(true);
					txt_max.setEnabled(true);
					txt_port.setEnabled(true);
					btn_stop.setEnabled(false);
					contentArea.append("服务器成功停止!\r\n");
					JOptionPane.showMessageDialog(frame, "服务器成功停止！");
				} catch (Exception exc) {
					JOptionPane.showMessageDialog(frame, "停止服务器发生异常！", "错误",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});
	}

	// 启动服务器
	public void serverStart(int max, int port) throws java.net.BindException {
		try {
			clients = new ArrayList<ClientThread>();
			serverSocket = new ServerSocket(port);
			serverThread = new ServerThread(serverSocket, max, clients, listModel, contentArea);
			serverThread.start();
			isStart = true;
		} catch (BindException e) {
			isStart = false;
			throw new BindException("端口号已被占用，请换一个！");
		} catch (Exception e1) {
			e1.printStackTrace();
			isStart = false;
			throw new BindException("启动服务器异常！");
		}
	}

	// 关闭服务器
	@SuppressWarnings("deprecation")
	public void closeServer() {
		try {
			if (serverThread != null)
				serverThread.stop();// 停止服务器线程

			for (int i = clients.size() - 1; i >= 0; i--) {
				// 给所有在线用户发送关闭命令
				clients.get(i).getWriter().println("CLOSE");
				clients.get(i).getWriter().flush();
				// 释放资源
				clients.get(i).stop();// 停止此条为客户端服务的线程
				clients.get(i).reader.close();
				clients.get(i).writer.close();
				clients.get(i).socket.close();
				clients.remove(i);
			}
			if (serverSocket != null) {
				serverSocket.close();// 关闭服务器端连接
			}
			listModel.removeAllElements();// 清空用户列表
			isStart = false;
		} catch (IOException e) {
			e.printStackTrace();
			isStart = true;
		}
	}

	// 服务器群发消息
	public void sendServerMessage(String message) {
		for (int i = clients.size() - 1; i >= 0; i--) {
			clients.get(i).getWriter().println("服务器：" + message + "(多人发送)");
			clients.get(i).getWriter().flush();
		}
	}

	
	/*// 单一用户发送消息
	public void sendMessageToSingleUser(String message,Socket socket){
		
	}*/

	
}

