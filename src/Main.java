import java.net.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class Main implements Runnable {

	public static enum ConnectionState {
		CLIENT, HOST,
	}

	public static ConnectionState state = null;

	public static ServerSocket serverSocket;
	public static Socket socket;

	public static Sender sender;

	public static JFrame f;
	public static JPanel p;
	public static JPanel p2;
	public static JLabel l;
	public static JLabel l2;
	public static JTextField t;
	public static JButton b;
	public static JButton close;

	public static void main(String[] args) throws IOException {

		Main.startHost();

		//Main.startClient("18.111.121.190");

		Main.createGUI();

	}

	public static void startClient(String ip) {

		if(state!=null)
			System.exit(-1);
		
		state = ConnectionState.CLIENT;

		System.out.println("Waiting for host...");
		try {
			socket = new Socket(ip, 45678);
		} catch (IOException e) {
			System.out.println("Host was unable to accept the connection");
			System.out
					.println("Make sure host server is running before attempting to connect!");
			System.exit(0);
		}

		b.setEnabled(true);
		System.out.println("Connection to host has been made!");
		System.out.println("");
		System.out.println("Enter a message at any time to send it:");
		System.out.println("");

		initSender();

	}

	public static void startHost() {

		if(state!=null)
			System.exit(-1);
		
		state = ConnectionState.HOST;

		System.out.println("Waiting for client...");

		try {
			serverSocket = new ServerSocket(45678);
			socket = serverSocket.accept();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		b.setEnabled(true);
		System.out.println("Connection to client has been made!");
		System.out.println("");
		System.out.println("Enter a message at any time to send it:");
		System.out.println("");

		initSender();

	}

	// (new Thread(new Client())).start();

	public static void initSender() {
		try {
			sender = new Sender();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void createGUI() {
		f = new JFrame("Client Server Test");
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
		b = new JButton("Enter");
		b.setEnabled(false);

		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String text = t.getText();
				if (!text.isEmpty()) {
					try {
						sender.send(text);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});
		close = new JButton("Close");
		close.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});

		l2 = new JLabel("");
		l2.setForeground(Color.yellow);

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
		p.add(b, c);
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 0;
		c.insets = new Insets(0, 10, 5, 10);
		p.add(close, c);
		// c.gridy = 3;
		// c.insets = new Insets(0, 15, 10, 15);
		// p.add(l2, c);
		p2.add(l2, c);
		f.add(p);
		f.add(p2, BorderLayout.SOUTH);

		f.pack();

		f.setVisible(true);
		f.toFront();
	}

	public void run() {
		try {
			@SuppressWarnings("unused")
			Receiver receiver = new Receiver();
		} catch (IOException e) {
			if (e instanceof SocketException) {
				e.printStackTrace();
				System.out.println("~~~Connection has been lost!~~~");
				System.exit(0);
			}
			e.printStackTrace();
		}
	}
}