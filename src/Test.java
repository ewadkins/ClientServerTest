import java.io.File;
import java.io.IOException;

public class Test {

	public static void main(String[] args) {

		ProcessBuilder pb = new ProcessBuilder("./bin/Run.sh", "host");
		//pb.directory(new File("."));
		try {
			Process p = pb.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/*
		ProcessBuilder pb = new ProcessBuilder("./res/Run.sh", "host");
		try {
			Process p = pb.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
	}

}
