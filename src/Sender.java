import java.io.IOException;
import java.io.PrintWriter;
//import java.util.Scanner;
import java.util.Scanner;

public class Sender {

	private final Connection connection;

	private Thread keepAlive;

	private Scanner scanner;
	private PrintWriter printwriter;

	// @SuppressWarnings("resource")
	public Sender(final Connection connection) {
		this.connection = connection;

		try {
			printwriter = new PrintWriter(connection.socket.getOutputStream(),
					true);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		keepAlive = new Thread(new Runnable() {
			@Override
			public void run() {
				while (connection.isAlive()) {
					send("__KEEP_ALIVE");
					try {
						// System.out.println((connection.state ==
						// Connection.ConnectionState.CLIENT ? "host" :
						// "client") + " sent keep alive");
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		keepAlive.start();

		if (!connection.usingGUI) {
			scanner = new Scanner(System.in);
			String input = "";
			while (true) {
				if (scanner.hasNextLine()) {
					input = scanner.nextLine();
				}
				send(input);
			}
		}
	}

	public void send(String s) {
		// Special functions
		if (s.equals("__KILL"))
			System.exit(0);
		else if (s.equals("__FREEZE"))
			keepAlive.stop();

		String[] dontPrint = new String[] { "__KEEP_ALIVE", "_KILL", "__FREEZE" };
		String[] dontSend = new String[] { "__FREEZE" };

		boolean print = true;
		for (int i = 0; i < dontPrint.length; i++)
			if (s.equals(dontPrint[i]))
				print = false;

		if (print)
			connection
					.print("You "
							+ (connection.state == Connection.ConnectionState.CLIENT ? "(Client)"
									: "(Host)") + ": " + s);

		boolean send = true;
		for (int i = 0; i < dontSend.length; i++)
			if (s.equals(dontSend[i]))
				send = false;

		if (send)
			printwriter.println(s);
	}

	protected void kill() {
		if (scanner != null)
			scanner.close();
	}

}
