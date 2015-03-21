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

public class Receiver {

	File file = null;
	List<String> lines = new ArrayList<String>();
	String lastMessage = "";

	ServerSocket fileserverSocket;
	Socket filesocket;
	String serverIP = "?";
	boolean isReceivingFile = false;

	public Receiver() throws IOException {

		InputStreamReader inputstreamreader = new InputStreamReader(
				Main.socket.getInputStream());
		BufferedReader bufferedreader = new BufferedReader(inputstreamreader);

		// Captures incoming messages
		String lineread = "";
		while ((lineread = bufferedreader.readLine()) != null) {

			if (lineread.startsWith("-FILE"))
				System.out.println("* " + lineread);
			else if (!lineread.equals(lastMessage) && !isReceivingFile)
				System.out.println(serverIP + ": " + lineread);
			lastMessage = lineread;
			if (lineread.startsWith("-IP: "))
				serverIP = lineread.substring(5, lineread.length());

			if (isReceivingFile) {
				if (lineread.equals("-FILE_DONE_DOWNLOADING")) {
					isReceivingFile = false;

					if (!(file.exists())) {

						try {
							PrintWriter out = null;
							out = new PrintWriter(new FileWriter(file));
							String[] lineArray = (String[]) lines
									.toArray(new String[lines.size()]);
							for (int i = 0; i < lineArray.length; i++) {
								String str = lineArray[i];
								if (i % 20000 == 0)
									System.out
											.println(file.getName()
													+ "  "
													+ ((double) (i + 1)
															/ (double) lineArray.length * 100)
													+ "%  Line " + (i + 1)
													+ "/" + lineArray.length
													+ " downloaded");
								if (i == lineArray.length - 1)
									System.out
											.println(file.getName()
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
							Main.socket.getOutputStream(), true);
					printwriter.println("-DOWNLOAD_COMPLETE");
				} else {
					lines.add(lineread);
				}
			}

			if (lineread.equals("-SHUTDOWN"))
				System.exit(0);

			if (lineread.startsWith("-FILE_CREATE_FOLDER")) {
				String[] args = lineread.split(" ");
				new File(args[1].replace("+", " ")).mkdirs();
			}
			if (lineread.startsWith("-FILE_BEGIN_DOWNLOAD")) {
				String[] args = lineread.split(" ");

				// args are //-FILE_BEGIN_DOWNLOAD <file being downloaded>
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
					System.out.println("* ERROR DOWNLOADING " + file.getPath());
				}
				bos.flush();
				bos.close();
				filesocket.close();
				fileserverSocket.close();
				PrintWriter printwriter = new PrintWriter(
						Main.socket.getOutputStream(), true);
				printwriter.println("-FILE_DOWNLOAD_CONFIRMED");

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
