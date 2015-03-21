import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

public class Receiver {

	private final Connection connection;
	
	private File file = null;
	protected List<String> lines = new ArrayList<String>();
	
	protected Thread keepAlive;
	protected long lastKeepAlive = System.currentTimeMillis();

	ServerSocket fileserverSocket;
	Socket filesocket;
	boolean isReceivingFile = false;

	protected Receiver(final Connection connection) throws IOException {
		
		this.connection = connection;
		
		InputStreamReader inputstreamreader = new InputStreamReader(
				connection.socket.getInputStream());
		BufferedReader bufferedreader = new BufferedReader(inputstreamreader);

		keepAlive = new Thread(new Runnable(){
			@Override
			public void run() {
				while(connection.isAlive()){
					long timeElapsed = System.currentTimeMillis() - lastKeepAlive;
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(timeElapsed > Connection.connectionTimeout) {
						JOptionPane.showMessageDialog(null, "The " + (connection.state == Connection.ConnectionState.CLIENT ? "host" : "client") +" seems to have lost connection!");
						connection.kill();
					}
				}
			}
		});
		keepAlive.start();
		
		String lineread = "";
		while (connection.isAlive() && (lineread = bufferedreader.readLine()) != null) {
			
			if(lineread.startsWith("__KEEP_ALIVE")){
				//System.out.println((connection.state == Connection.ConnectionState.CLIENT ? "host" : "client") + " received keep alive ("+(System.currentTimeMillis()-lastKeepAlive)+")");
				lastKeepAlive = System.currentTimeMillis();
			}
			
			else if (lineread.startsWith("__FILE"))
				connection.print("* " + lineread);

			else if (!isReceivingFile){
				/*connection.print((connection.state == Connection.ConnectionState.CLIENT ? connection.socket
								.getRemoteSocketAddress().toString()
								: connection.socket.getLocalSocketAddress()
										.toString()) + ": " + lineread);*/
				connection.print((connection.state == Connection.ConnectionState.CLIENT ? "Host" : "Client") + ": " + lineread);
			}

			if (isReceivingFile) {
				if (lineread.equals("__FILE_DONE_DOWNLOADING")) {
					isReceivingFile = false;

					if (!(file.exists())) {

						try {
							PrintWriter out = null;
							out = new PrintWriter(new FileWriter(file));
							String[] lineArray = lines
									.toArray(new String[0]);
							for (int i = 0; i < lineArray.length; i++) {
								String str = lineArray[i];
								if (i % 20000 == 0)
									connection.print(file.getName()
													+ "  "
													+ ((double) (i + 1)
															/ (double) lineArray.length * 100)
													+ "%  Line " + (i + 1)
													+ "/" + lineArray.length
													+ " downloaded");
								if (i == lineArray.length - 1)
									connection.print(file.getName()
													+ "  "
													+ ((double) (i + 1)
															/ (double) lineArray.length * 100)
													+ "%  Line " + (i + 1)
													+ "/" + lineArray.length
													+ " downloaded");
								if (i != lineArray.length - 1)
									out.println(str);
								else
									out.print(str);
							}
							out.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					lines.clear();
					PrintWriter printwriter = new PrintWriter(
							connection.socket.getOutputStream(), true);
					printwriter.println("__FILE_DOWNLOAD_COMPLETE");
				} else {
					lines.add(lineread);
				}
			}

			if (lineread.equals("_KILL"))
				System.exit(0);

			if (lineread.startsWith("__FILE_CREATE_FOLDER")) {
				String[] args = lineread.split(" ");
				new File(args[1].replace("+", " ")).mkdirs();
			}
			if (lineread.startsWith("__FILE_BEGIN_DOWNLOAD")) {
				String[] args = lineread.split(" ");

				// args are //-FILE_BEGIN_DOWNLOAD <file>
				int bytesRead = 0;
				int currentTot = 0;
				byte[] bytearray = new byte[2022386];

				file = new File(args[1].replace("+", " "));
				isReceivingFile = true;
				fileserverSocket = new ServerSocket(45679);
				filesocket = fileserverSocket.accept();

				InputStream is = filesocket.getInputStream();
				if (!(file.exists())) {
					// file.getParentFile().mkdirs();
					PrintWriter out = new PrintWriter(new FileWriter(file));
					out.close();
				}
				FileOutputStream fos = new FileOutputStream(file.getPath());
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				bytesRead = is.read(bytearray, 0, bytearray.length);
				currentTot = bytesRead;

				do {
					bytesRead = is.read(bytearray, currentTot,
							(bytearray.length - currentTot));

					if (bytesRead >= 0) {
						currentTot += bytesRead;
					}
				} while (bytesRead > -1);
				try {
					bos.write(bytearray, 0, currentTot);
				} catch (ArrayIndexOutOfBoundsException e) {
					connection.print("* ERROR DOWNLOADING " + file.getPath());
				}
				bos.flush();
				bos.close();
				filesocket.close();
				fileserverSocket.close();
				PrintWriter printwriter = new PrintWriter(
						connection.socket.getOutputStream(), true);
				printwriter.println("__FILE_DOWNLOAD_CONFIRMED");

				// File Downloader
				/*
				 * int bytesRead = 0; int currentTot = 0; byte[] bytearray = new
				 * byte[2022386];
				 * 
				 * 
				 * InputStream is = socket.getInputStream(); FileOutputStream
				 * fos = new FileOutputStream(args[2]); BufferedOutputStream bos
				 * = new BufferedOutputStream(fos); bytesRead =
				 * is.read(bytearray, 0, bytearray.length); currentTot =
				 * bytesRead;
				 * 
				 * do{ bytesRead = is.read(bytearray, currentTot,
				 * (bytearray.length-currentTot));
				 * 
				 * System.out.println(bytesRead+"");
				 * 
				 * if(bytesRead >= 0){ currentTot += bytesRead; } }
				 * while(bytesRead > -1); System.out.println(new
				 * String(bytearray)); bos.write(bytearray, 0, currentTot);
				 * bos.flush(); bos.close();
				 */
				// ////
			}

		}
	}

}
