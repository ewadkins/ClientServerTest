import java.io.IOException;
import java.io.PrintWriter;
//import java.util.Scanner;

public class Sender {

	private Thread keepAlive;
	public boolean active = false;
	
	//@SuppressWarnings("resource")
	public Sender() throws IOException {
		active = true;
		
		keepAlive = new Thread(new Runnable(){
			@Override
			public void run() {
				while(active) {
					try {
						send("__KEEP_ALIVE");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		keepAlive.start();
		
		/*Scanner scanner = new Scanner(System.in);
		String input = "";
		while (true) {
			if (scanner.hasNextLine()) {
				input = scanner.nextLine();
			}

			send(input);

			if (input.equals("-SHUTDOWN"))
				System.exit(0);

			// System.out.println("You: "+input);
		}*/
	}

	public void send(String s) throws IOException {
		PrintWriter printwriter = new PrintWriter(Main.socket.getOutputStream(),
				true);
		printwriter.println(s);
	}

}
