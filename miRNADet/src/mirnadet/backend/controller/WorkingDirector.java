package mirnadet.backend.controller;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import mirnadet.backend.evaluate.FastaEntry;
import mirnadet.backend.util.FastaCreator;
import mirnadet.backend.util.UnixProcess;
import mirnadet.security.ExceptionLogger;

public class WorkingDirector {

	HashMap<String, FastaEntry> entries;
	ArrayList<File> generatedFiles;
	File uploadedFile;
	// File workingfolder;
	boolean cuttingNeeded;
	int slidingWindowSize;
	final String tmpFolderName = "tempiTempTemp";
	final static String perlPREFIX = "/usr/bin/perl";
	final static String UNAFoldPATH = "/usr/local/bin/UNAFold.pl";
	
	/*
	 *#!/bin/bash
perl /usr/local/bin/melt.pl $1 | tee $2
perl /usr/local/bin/UNAFold.pl $1
perl /usr/local/bin/Ct2B.pl $1.ct | tee -a $2 
	 */
	final static String BASHSCRIPT_CONTENT = "#!/bin/bash\n" +"perl /usr/local/bin/melt.pl $1 | tee $2"+ "perl /usr/local/bin/UNAFold.pl $1\n"
			+ "perl /usr/local/bin/Ct2B.pl $1.ct | tee -a $2";
	final static String BASHSCRIPT_NAME = "UNAFoldSucks.sh";
	final static int[] TEMPS = new int[] { 37};

	public WorkingDirector(HashMap<String, FastaEntry> entryList, File uploadedFile, File workingFolder, boolean isDNA,
			int slidingSize) {
		this.entries = entryList;
		this.uploadedFile = uploadedFile;
		// this.workingfolder = workingFolder;
		this.cuttingNeeded = isDNA;
		this.slidingWindowSize = slidingSize;
		controlLoop(workingFolder);
		writeStuff();

	}

	private void controlLoop(File folder) {
		File tmpFolder = new File(folder.getPath() + File.separator + tmpFolderName);
		if (!createFolder(tmpFolder)) {
			System.err.println("ERROR");
			return;
		}
		File bashFile = new File(tmpFolder.getPath() + File.separator + BASHSCRIPT_NAME);

		if (!createBashScript(bashFile.getPath())) {
			System.err.println("Error: Bash script not written");
			return;
		}
		bashFile.setExecutable(true, false);
		bashFile.setReadable(true, false);
		bashFile.setWritable(true, false);

		for (Map.Entry<String, FastaEntry> en : entries.entrySet()) {

			HashMap<Integer, String> tempMap = new HashMap<Integer, String>();
			File inputFile = new File(tmpFolder.getPath() + File.separator + "test.fa");
			inputFile.setReadable(true, false);
			File outputFile = new File(tmpFolder.getPath() + File.separator + "nonsense.txt");

			FastaCreator.createFile(en.getValue(), inputFile.getPath());

			for (int t : TEMPS) {
					System.out.println(t);
				if (redirectToUNAFold(bashFile.getAbsolutePath(), inputFile.getAbsolutePath(),
						outputFile.getAbsolutePath(), t + "", tmpFolder) == false) {
					System.err.println("Error: process broken");
					return;
				}
				String fileContent = readFile(outputFile.getAbsolutePath());
				if (fileContent == null || fileContent.length() == 0) {
					System.err.println("Error: file jagged");
					return;
				}
				tempMap.put(t, fileContent);

				if (deleteTrashFiles(tmpFolder) == false) {
					System.err.println("Error: files couldn't be deleted");
					return;
				}

			}
			en.getValue().setTempToSequences(tempMap);
			inputFile.delete();
			
			 //for (Map.Entry<Integer, String> is : tempMap.entrySet()) {
			  //System.out.println(is.getKey()); System.out.println(is.getValue()); }
			 
		}
		cleanUp(tmpFolder);
	}

	private String readFile(String path) {
		try {
			return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// ExceptionLogger.writeSevereError(e);
			return null;
		}
	}

	private boolean redirectToUNAFold(String scriptFilePath, String inputFileName, String outputFileName, String temp,
			File workingDir) {

		return UnixProcess.executeCommand(new String[] { "/bin/sh", "-c",
				scriptFilePath + " " + inputFileName + " " + outputFileName + " " + temp }, workingDir);
	}

	private boolean deleteTrashFiles(File folder) {
		File[] files = folder.listFiles();
		try {
			for (File f : files) {
				if (f.getName().endsWith(".sh")==false && f.getName().endsWith(".fa")==false)
					f.delete();
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private void cleanUp(File f) {
		try {
			FileUtils.deleteDirectory(f);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private boolean createFolder(File folder) {
		try {
			if (!folder.exists())
				FileUtils.forceMkdir(folder);
			else {

				FileUtils.deleteDirectory(folder);
				FileUtils.forceMkdir(folder);
			}
			folder.setWritable(true, false);
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			ExceptionLogger.writeSevereError(e);
			return false;
		}
	}

	
	private void writeStuff() {
		StringBuilder sb = new StringBuilder("name;length;37\n");
		
		for(Map.Entry<String, FastaEntry> fe : entries.entrySet() ) {
			sb.append(fe.getKey());
			sb.append(";");
			sb.append(fe.getValue().getSequence().length());
			sb.append(";");
			for(int i = 0; i < TEMPS.length; i++) {
				sb.append(fe.getValue().getNumOfSeqsForTemp(TEMPS[i]));
				sb.append(";");
			}
			sb.append("\n");
		}
		FastaCreator.writeToFile(sb.toString(), "out_temp.csv");
	}
	
	private boolean createBashScript(String scriptPath) {
		return FastaCreator.writeToFile(BASHSCRIPT_CONTENT, scriptPath);
	}

	public static void main(String[] args) {
		HashMap<String, FastaEntry> map = new HashMap<String, FastaEntry>();
		map.put(">hsa-mir-1289-1 MI0006350 Homo sapiens miR-1289-1 stem-loop", new FastaEntry(
				">hsa-mir-1289-1 MI0006350 Homo sapiens miR-1289-1 stem-loop",
				"UUCUCAAUUUUUAGUAGGAAUUAAAAACAAAACUGGUAAAUGCAGACUCUUGGUUUCCACCCCCAGAGAAUCCCUAAACCGGGGGUGGAGUCCAGGAAUCUGCAUUUUAGAAAGUACCCAGGGUGAUUCUGAUAAUUGGGAACA",
				false));
		new WorkingDirector(map, null, new File("WebContent/Data"), false, 50);
	}
	/// home/ik/git/Master_InsaKruse/miRNADet/WebContent/Data/tempiTempTemp

	// -P 100

}
