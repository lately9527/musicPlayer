package com.kevin.client;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
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
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.kevin.entity.User;
import com.music.AePlayWave.AePlayWave;

public class Client{

	private JFrame frame;
	private JList userList;
	private JTextArea textArea;
	private JTextField textField;
	private JTextField txt_port;
	private JTextField txt_hostIp;
	private JTextField txt_name;
	private JButton btn_start;
	private JButton btn_stop;
	private JButton btn_send;
	private JPanel northPanel;
	private JPanel southPanel;
	private JScrollPane rightScroll;
	private JPanel leftScroll;
	private JScrollPane leftUserScroll;
	private JSplitPane centerSplit;
	private JPanel buttonPanel;
	private JButton allUser_btn;
	private JButton singleUser_btn;
	private DefaultListModel listModel;
	private boolean isConnected = false;
	private String sendJudge;
	private Socket socket;
	private PrintWriter writer;
	private BufferedReader reader;
	private MessageThread messageThread;// 负责接收消息的线程
	private Map<String, User> onLineUsers = new HashMap<String, User>();// 所有在线用户
	private AePlayWave aePlayWave;
	
	// 主方法,程序入口
	public static void main(String[] args) {
		new Client();
	}

	// 执行发送
	public void send() {
		if (!isConnected) {
			JOptionPane.showMessageDialog(frame, "还没有连接服务器，无法发送消息！", "错误",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		String message = textField.getText().trim();
		if (message == null || message.equals("")) {
			JOptionPane.showMessageDialog(frame, "消息不能为空！", "错误",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		if (sendJudge=="all") {
			sendMessage(frame.getTitle() + "@" + "ALL" + "@" + message);
			//System.out.println(frame.getTitle() + "@" + "ALL" + "@" + message);
			textField.setText(null);
		}else{
			sendMessage(frame.getTitle() + "@" + sendJudge + "@" + message);
			textField.setText(null);
		}
		
	}

	// 构造方法
	public Client() {
		textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setForeground(Color.blue);
		textField = new JTextField();
		txt_port = new JTextField("6666");
		txt_hostIp = new JTextField("127.0.0.1");
		txt_name = new JTextField("bailing");
		btn_start = new JButton("连接");
		btn_stop = new JButton("断开");
		btn_send = new JButton("发送");
		allUser_btn = new JButton("stop");
		singleUser_btn = new JButton("play");
		listModel = new DefaultListModel();
		userList = new JList(listModel);

		
		
		northPanel = new JPanel();
		northPanel.setLayout(new GridLayout(1, 7));
		northPanel.add(new JLabel("端口")); 
		northPanel.add(txt_port);
		northPanel.add(new JLabel("服务器IP"));
		northPanel.add(txt_hostIp);
		northPanel.add(new JLabel("姓名"));
		northPanel.add(txt_name);
		northPanel.add(btn_start);
		northPanel.add(btn_stop);
		northPanel.setBorder(new TitledBorder("连接信息"));

		rightScroll = new JScrollPane(textArea);
		rightScroll.setBorder(new TitledBorder("消息显示区"));
		leftUserScroll = new JScrollPane(userList);
		
		leftScroll = new JPanel(new BorderLayout());
		leftScroll.add(leftUserScroll);
		buttonPanel = new JPanel(new GridLayout(1,2));
		buttonPanel.add(allUser_btn);
		buttonPanel.add(singleUser_btn);
		leftScroll.add(buttonPanel,"South");
		leftScroll.setBorder(new TitledBorder("在线用户"));
		southPanel = new JPanel(new BorderLayout());
		southPanel.add(textField, "Center");
		southPanel.add(btn_send, "East");
		southPanel.setBorder(new TitledBorder("写消息"));

		centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScroll,
				rightScroll);
		centerSplit.setDividerLocation(200);

		frame = new JFrame("客户机");
		frame.setLayout(new BorderLayout());
		frame.add(northPanel, "North");
		frame.add(centerSplit, "Center");
		frame.add(southPanel, "South");
		frame.setSize(1000, 600);
		ImageIcon icon=new ImageIcon("logo.png");
		frame.setIconImage(icon.getImage());
		int screen_width = Toolkit.getDefaultToolkit().getScreenSize().width;
		int screen_height = Toolkit.getDefaultToolkit().getScreenSize().height;
		frame.setLocation((screen_width - frame.getWidth()) / 2,
				(screen_height - frame.getHeight()) / 2);
		frame.setVisible(true);

		aePlayWave = new AePlayWave("小小虫.wav");
		
		// 写消息的文本框中按回车键时事件
		textField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				send();
			}
		});

		// 单击发送按钮时事件
		btn_send.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				send();
			}
		});

		// 单击连接按钮时事件
		btn_start.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int port;
				if (isConnected) {
					JOptionPane.showMessageDialog(frame, "已处于连接上状态，不要重复连接!",
							"错误", JOptionPane.ERROR_MESSAGE);
					return;
				}
				try {
					try {
						port = Integer.parseInt(txt_port.getText().trim());
					} catch (NumberFormatException e2) {
						throw new Exception("端口号不符合要求!端口为整数!");
					}
					String hostIp = txt_hostIp.getText().trim();
					String name = txt_name.getText().trim();
					if (name.equals("") || hostIp.equals("")) {
						throw new Exception("姓名、服务器IP不能为空!");
					}
					boolean flag = connectServer(port, hostIp, name);
					if (flag == false) {
						throw new Exception("与服务器连接失败!");
					}
					frame.setTitle(name);
					JOptionPane.showMessageDialog(frame, "成功连接!");
				} catch (Exception exc) {
					JOptionPane.showMessageDialog(frame, exc.getMessage(),
							"错误", JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		// 单击断开按钮时事件
		btn_stop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!isConnected) {
					JOptionPane.showMessageDialog(frame, "已处于断开状态，不要重复断开!",
							"错误", JOptionPane.ERROR_MESSAGE);
					return;
				}
				try {
					boolean flag = closeConnection();// 断开连接
					if (flag == false) {
						throw new Exception("断开连接发生异常！");
					}
					JOptionPane.showMessageDialog(frame, "成功断开!");
				} catch (Exception exc) {
					JOptionPane.showMessageDialog(frame, exc.getMessage(),
							"错误", JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		//jList事件
		userList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent evt) {
				if (!userList.getValueIsAdjusting()) {
					
				    sendJudge = (String) userList.getSelectedValue();
				    System.out.println("选中要聊天的对象是："+sendJudge);
				}
				
			}
		});
		
		
		// 关闭窗口时事件
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if (isConnected) {
					closeConnection();// 关闭连接
				}
				super.windowClosing(e);  
			}
		});
		
		//播放按钮事件
		singleUser_btn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				aePlayWave.start();
			}
		});
		
		//停止播放按钮
		allUser_btn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				aePlayWave.stop();
			}
		});
		
	}
	
	/**
	 * 连接服务器
	 * 
	 * @param port
	 * @param hostIp
	 * @param name
	 */
	public boolean connectServer(int port, String hostIp, String name) {
		// 连接服务器
		try {
			socket = new Socket(hostIp, port);// 根据端口号和服务器ip建立连接
			writer = new PrintWriter(socket.getOutputStream());
			reader = new BufferedReader(new InputStreamReader(socket
					.getInputStream()));
			// 发送客户端用户基本信息(用户名和ip地址)
			sendMessage(name + "@" + socket.getLocalAddress().toString());
			// 开启接收消息的线程
			messageThread = new MessageThread(reader, textArea, listModel, writer, socket, isConnected, onLineUsers, frame);
			messageThread.start();
			isConnected = true;// 已经连接上了
			return true;
		} catch (Exception e) {
			textArea.append("与端口号为：" + port + "    IP地址为：" + hostIp
					+ "   的服务器连接失败!" + "\r\n");
			isConnected = false;// 未连接上
			return false;
		}
	}

	/**
	 * 发送消息
	 * 
	 * @param message
	 */
	public void sendMessage(String message) {
		writer.println(message);
		writer.flush();
	}

	/**
	 * 客户端主动关闭连接
	 */
	@SuppressWarnings("deprecation")
	public synchronized boolean closeConnection() {
		try {
			sendMessage("CLOSE");// 发送断开连接命令给服务器
			messageThread.stop();// 停止接受消息线程
			// 释放资源
			if (reader != null) {
				reader.close();
			}
			if (writer != null) {
				writer.close();
			}
			if (socket != null) {
				socket.close();
			}
			isConnected = false;
			return true;
		} catch (IOException e1) {
			e1.printStackTrace();
			isConnected = true;
			return false;
		}
	}

	// 不断接收消息的线程
	
}
