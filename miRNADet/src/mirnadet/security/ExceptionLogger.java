package mirnadet.security;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.zkoss.zk.ui.Executions;

public final class ExceptionLogger {

	private static Logger logger;
	private static Handler handler;
	private static String filepath;

	
	private static void setUpLogger() throws SecurityException, IOException{
		logger = Logger.getLogger(Thread.currentThread().getStackTrace()[0]
				.getClassName());
		filepath = createServerPath("exceptions.log");
		setLoggerPermissions();

		handler = new FileHandler(filepath, 1024 * 1024 * 1024, 1, false);
		handler.setEncoding("UTF-8");
		logger.addHandler(handler);
	}
	
	private static String createServerPath(String name) {
		String webAppPath = Executions.getCurrent().getDesktop().getWebApp()
				.getRealPath("/");
		webAppPath += "Files" + File.separator;
		System.out.println(webAppPath);
		return webAppPath + name;

	}

	private static void setLoggerPermissions() {
		File f = new File(filepath);
		if (!f.exists())
			try {
				f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				
			}
		f.setWritable(true);
		f.setReadable(true);
	}

	public static void writeSevereError(Exception e) {
	if(logger == null)
		try {
			setUpLogger();
		} catch (SecurityException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		logger.log(Level.SEVERE, e.toString(), e);
	}
	
	
	
	
}