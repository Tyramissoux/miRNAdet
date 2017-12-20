package mirnadet.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.zkoss.zul.Messagebox;

public class FileControl {

	private final static String[] FORBIDDEN = { "<html>", "<?php", "![CDATA", "SQL_TCP", "<asp:", "<script type" };
	private String fileContent;

	public FileControl(String pathToUploadedFile) {
		control(pathToUploadedFile);
	}

	private void control(String in) {
		String tmp;
		try {
			tmp = readFile(in);
		} catch (IOException e) {
			Messagebox.show("Uploaded file couldn't be processed - stopping", "Warning", Messagebox.OK,
					Messagebox.EXCLAMATION);
			return;
		}
		if (!containsForbidden(tmp)) {
			if (checkForFasta(tmp)) {
				fileContent = tmp;
			}
			else {
				Messagebox.show("Uploaded file is not FASTA-formatted", "Warning", Messagebox.OK, Messagebox.EXCLAMATION);
				return;
			}
		}

	}

	public static boolean checkForFasta(String in) {
		if (in.split("\n").length == 1)
			return false;
		if (!in.contains(">"))
			return false;
		return true;
	}

	public static boolean containsForbidden(String in) {
		for (int i = 0; i < FORBIDDEN.length; i++) {
			if (in.contains(FORBIDDEN[i])) {
				Messagebox.show("Uploaded file contained forbidden text element '" + FORBIDDEN[i] + "' - stopping",
						"Warning", Messagebox.OK, Messagebox.EXCLAMATION);
				return true;
			}
		}
		return false;
	}

	private String readFile(String inputFilePath) throws IOException {
		return new String(Files.readAllBytes(Paths.get(inputFilePath)), StandardCharsets.UTF_8);
	}

	public String getFileContent() {
		return fileContent;
	}

}
