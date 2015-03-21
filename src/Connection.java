import java.net.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;

public class Connection {

	protected static enum ConnectionState {
		CLIENT, HOST,
	}

	protected static final long connectionTimeout = 5000;

	private Connection connection = this;
	protected ConnectionState state = null;

	protected ServerSocket serverSocket;
	protected Socket socket;

	protected Sender sender;
	protected Receiver receiver;
	private Thread receivingThread;
	protected boolean alive = true;

	private ArrayList<String> listData = new ArrayList<String>();

	protected boolean usingGUI = false;

	private JFrame f;
	private JPanel p;
	private JPanel p2;
	private JLabel l;
	private DefaultListModel<String> listModel;
	private JList<String> list;
	private JScrollPane listScroller;
	private JTextField t;
	private JButton enter;
	private JButton close;

	public static void main(String[] args) throws IOException {
		
		// Ran from script
		if(args.length == 1) {
			if (args[0].equalsIgnoreCase("host"))
				new Connection(true);
			else
				new Connection(args[0], true);
		}
		
		
		
		// Testing in eclipse
		else {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub

				new Connection(true); // HOST
			}
		}).start();

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub

				//new Connection("18.111.121.190", true); // CLIENT
			}
		}).start();
		}

	}

	public Connection(final String ip, final boolean usingGUI) {
		this.usingGUI = usingGUI;
		if (usingGUI)
			createGUI("Client Test");
		startClient(ip);
	}

	public Connection(final boolean usingGUI) {
		this.usingGUI = usingGUI;
		if (usingGUI)
			createGUI("Server Test");
		startHost();

	}

	public void startClient(String ip) {

		state = ConnectionState.CLIENT;

		boolean connected = false;
		print("Waiting for host...");
		long start = System.currentTimeMillis();
		// Tries to connect to host for ten seconds, then quits
		while (isAlive()
				&& System.currentTimeMillis() - start < connectionTimeout) {
			try {
				socket = new Socket(ip, 45678);
				connected = true;
				break;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}
		}

		if (!connected) {
			JOptionPane
					.showMessageDialog(
							null,
							"Host was unable to accept the connection \nMake sure host server is running before attempting to connect!");
			kill();
		}

		if (usingGUI) {
			enter.setEnabled(true);
			close.setText("Disconnect");
		}
		print("Connection to host has been made!");
		print("Host IP:Port is " + socket.getRemoteSocketAddress().toString());
		print("");
		print("----");

		initSender();
		initReceiver();

	}

	public void startHost() {

		state = ConnectionState.HOST;

		print("Waiting for client...");

		try {
			serverSocket = new ServerSocket(45678);
			socket = serverSocket.accept();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (usingGUI) {
			enter.setEnabled(true);
			close.setText("Disconnect");
		}
		print("Connection to client has been made!");
		print("Client IP:Port is " + socket.getRemoteSocketAddress().toString());
		print("");
		print("----");

		initSender();
		initReceiver();

	}

	public void initSender() {
		sender = new Sender(this);
	}

	public void initReceiver() {
		receivingThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					receiver = new Receiver(connection);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					JOptionPane.showMessageDialog(null, "The " + (state == ConnectionState.CLIENT ? "host" : "client") +" seems to have lost connection!");
					kill();
				}
			}
		});
		receivingThread.start();
	}

	public void print(String str) {
		if (usingGUI) {
			listData.add(str);
			updateList(listData);
		} else {
			System.out.println(str);
		}
	}

	public void updateList(ArrayList<String> lines) {
		listModel.clear();
		for (int i = 0; i < lines.size(); i++) {
			listModel.addElement(lines.get(i));
		}
		for(int i = 0; i < 10; i++)
			listModel.addElement("");
		//list.setSelectedIndex(listData.size());
		list.setEnabled(false);
		//listScroller.getVerticalScrollBar().setValue(listScroller.getVerticalScrollBar().getMaximum());
		listScroller.getVerticalScrollBar().setValue(list.getHeight());
	}

	public void kill() {
		alive = false;
		if (sender != null)
			sender.kill();
		System.exit(0);
	}

	public boolean isAlive() {
		return alive;
	}

	public void createGUI(String title) {
		usingGUI = true;

		f = new JFrame(title);
		f.setResizable(false);
		f.setType(Window.Type.NORMAL); // POPUP //UTILITY
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		p = new JPanel(new GridBagLayout());
		p.setBackground(Color.gray);
		p2 = new JPanel(new GridBagLayout());
		p2.setBackground(Color.gray);
		l = new JLabel("Enter your message:");
		l.setForeground(Color.white);
		t = new JTextField();
		t.setBackground(new Color(200, 200, 255));
		t.setColumns(15);
		enter = new JButton("Enter");
		enter.setEnabled(false);

		enter.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String text = t.getText();
				if (!text.isEmpty()) {
					sender.send(text);
					t.setText("");
				}
			}
		});
		close = new JButton("Close");
		close.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				kill();
			}
		});

		listModel = new DefaultListModel<String>();
		list = new JList<String>(listModel);
		list.setVisible(true);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setLayoutOrientation(JList.VERTICAL);
		list.setVisibleRowCount(10);

		listScroller = new JScrollPane(list);
		listScroller.setVisible(true);
		listScroller.setPreferredSize(new Dimension(400, 160));
		listScroller
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		listScroller
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;

		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 0;
		c.insets = new Insets(10, 15, 5, 15);
		p.add(l, c);
		c.gridy = 1;
		c.gridwidth = 1;
		c.insets = new Insets(0, 15, 0, 0);
		p.add(t, c);
		c.gridx = 1;
		c.insets = new Insets(0, 0, 0, 15);
		p.add(enter, c);
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 0;
		c.insets = new Insets(0, 10, 5, 10);
		p.add(close, c);
		c.gridy = 0;
		c.insets = new Insets(0, 15, 10, 15);
		p2.add(listScroller, c);
		f.add(p);
		f.add(p2, BorderLayout.SOUTH);

		f.pack();

		f.setVisible(true);
		f.toFront();
	}

	/*
	 * public void run() { try {
	 * 
	 * @SuppressWarnings("unused") Receiver receiver = new Receiver(); } catch
	 * (IOException e) { if (e instanceof SocketException) {
	 * e.printStackTrace();
	 * System.out.println("~~~Connection has been lost!~~~"); System.exit(0); }
	 * e.printStackTrace(); } }
	 */
}