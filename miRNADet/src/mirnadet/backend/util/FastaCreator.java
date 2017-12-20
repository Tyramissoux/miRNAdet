package mirnadet.backend.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import mirnadet.backend.evaluate.FastaEntry;
import mirnadet.security.ExceptionLogger;

public class FastaCreator {
	
	
	public static boolean createFile(FastaEntry en, String fileName) {
		StringBuilder sb = new StringBuilder();
		sb.append(en.getHeader());
		sb.append("\n");
		sb.append(en.getSequence());
			
		return writeToFile(sb.toString(), fileName);
	}
	
	public static boolean writeToFile(String content, String filePath)  {
		Path path = Paths.get(filePath);
		try (BufferedWriter writer = Files.newBufferedWriter(path)) {
		    writer.write(content);
		    writer.flush();
		    writer.close();
		    new File(filePath).canRead();
		    return true;
		}	
		catch(IOException e) {
			ExceptionLogger.writeSevereError(e);
			return false;
		}
	}

	
}
