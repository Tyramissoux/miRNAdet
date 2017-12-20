package mirnadet.backend.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class UnixProcess {

	
	public static StringBuilder getCommandLineOutput(String[] command) {
		StringBuilder output = new StringBuilder();
		try {
			ProcessBuilder pb = new ProcessBuilder(command);
			pb.redirectErrorStream(true);
			Process p = pb.start();
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String out = "";
			while ((out = br.readLine()) != null) {
				output.append(out);
			}

		} catch (Exception e) {
			e.printStackTrace();

		}
		// System.out.println(output);
		return output;
	}
	
	public static boolean executeCommand(String[] command) {
	
		try {
			ProcessBuilder pb = new ProcessBuilder(command);
			pb.redirectErrorStream(true);
			Process p = pb.start();
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String out = "";
			while ((out=br.readLine() )!= null) {
				System.out.println(out);
			}

		} catch (Exception e) {
			e.printStackTrace();
			return false;

		}
		// System.out.println(output);
		return true;
	}
	
	public static boolean executeCommand(String[] command, File directory) {
		
		try {
			ProcessBuilder pb = new ProcessBuilder(command);
			pb.directory(directory);
			pb.redirectErrorStream(true);
			Process p = pb.start();
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String out = "";
			while ((out=br.readLine() )!= null) {
				//System.out.println(out);
			}

		} catch (Exception e) {
			e.printStackTrace();
			return false;

		}
		// System.out.println(output);
		return true;
	}

	/*private boolean checkForPostgreSQL() {
		String cmdLine = executeCommand(new String[] { "/bin/sh", "-c", "which psql" });
		if (!cmdLine.contains("psql")) {
			System.err.println("ERROR: no postgreSQL installed --- leaving program");
			return false;
		}
		return true;
	}*/

}
