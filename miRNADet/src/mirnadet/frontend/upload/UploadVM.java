package mirnadet.frontend.upload;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import org.zkoss.bind.BindContext;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.Init;
import org.zkoss.io.Files;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Selectbox;
import org.zkoss.zul.Textbox;

import mirnadet.backend.controller.WorkingDirector;
import mirnadet.backend.evaluate.FastaEntry;
import mirnadet.backend.util.FastaProcesser;
import mirnadet.security.ExceptionLogger;
import mirnadet.security.FileControl;
import mirnadet.security.SecuritySetup;

public class UploadVM {

	@Wire("#txtUpload")
	private Textbox txt;
	@Wire("#btnGo")
	private Button btn;
	@Wire("#rPre")
	private Radio rPre;
	@Wire("#rDNA")
	private Radio rDNA;
	@Wire("#selBox")
	private Selectbox select;
	@Wire("#btnProcess")
	private Button btnProcess;
	@Wire("#txtUploadedFile")
	private Textbox tempFile;

	private ListModelList<String> model;
	private HashMap<String,FastaEntry> listOfEntries;
	private Media uploaded;
	private int slidingSize;
	private SecuritySetup ss;
	private boolean translate;
	private File workingFolder;
	private File uploadedFile;

	public UploadVM() {
		ss = new SecuritySetup();
		ss.disableSystemExit();
		//System.out.println(getUserSessionForFileName());
	}

	public ListModelList<String> getModel() {
		return model;
	}

	public static List<String> getModelType() {
		return Arrays.asList(new String[] { "30", "50", "75", "100", "150", "200" });
	}

	@Init
	public void init() {
		model = new ListModelList<String>(getModelType());
	}

	@Command
	public void startTextBoxProcessing() {

		String textBoxContent = txt.getValue();
		if (textBoxContent == null || textBoxContent.length() == 0) {
			Messagebox.show("No textbox input", "Warning", Messagebox.OK, Messagebox.EXCLAMATION);
			return;
		}
		if (FileControl.containsForbidden(textBoxContent) == true) {
			return;
		}

		setVariablesGlobal(null);

		if (FileControl.checkForFasta(textBoxContent)) {
			listOfEntries = new FastaProcesser(textBoxContent, translate).getEntryList();
		} else {

			if (textBoxContent.contains("\n")) {
				Messagebox.show(
						"Textbox input malformed - non-FASTA-formatted sequences aren't allowed to have linebreaks",
						"Warning", Messagebox.OK, Messagebox.EXCLAMATION);
				return;
			}
			
			if (Pattern.matches("[atgcuryswkmbdhvn.-]+",textBoxContent.toLowerCase())==false){
				Messagebox.show("No nucleotide sequence found - try again, please", "Warning", Messagebox.OK,
						Messagebox.EXCLAMATION);
				return;
			}
			listOfEntries = new HashMap<String, FastaEntry>();
			String header = ">"+getUserSessionForFileName();
			listOfEntries.put(header,new FastaEntry(header,textBoxContent, translate));
		}

			workingFolder = createFolder();

			redirectToEvaluation();
	}

	
	
	//needed?
	private void setVariablesGlobal(String filePath) {
		translate = !rPre.isChecked();
		Sessions.getCurrent().setAttribute("translateDNA", translate);
		int chosenLen = 0;
		if (!rPre.isChecked()) {

			int index = select.getSelectedIndex();
			// "30", "50", "75", "100", "150","200"
			switch (index) {
			case 0: {
				chosenLen = 30;
				break;
			}
			case 1: {
				chosenLen = 50;
				break;
			}
			case 2: {
				chosenLen = 75;
				break;
			}
			case 3: {
				chosenLen = 100;
				break;
			}
			case 4: {
				chosenLen = 150;
				break;
			}
			case 5: {
				chosenLen = 200;
				break;
			}
			default: {
				chosenLen = 50;
				break;
			}
			}

		}
		slidingSize = chosenLen;
		Sessions.getCurrent().setAttribute("slidingWindowSize", chosenLen);
		Sessions.getCurrent().setAttribute("uploadedFile", filePath);
		Sessions.getCurrent().setAttribute("listOfEntries", listOfEntries);
	}

	private File createFolder() {

		Calendar now = Calendar.getInstance();
		int year = now.get(Calendar.YEAR);
		int month = now.get(Calendar.MONTH);
		int day = now.get(Calendar.DAY_OF_MONTH);
		int hour = now.get(Calendar.HOUR);
		int min = now.get(Calendar.MINUTE);
		int sec = now.get(Calendar.SECOND);
		int mil = now.get(Calendar.MILLISECOND);
		String tmpPath = getTemp();
		if (!tmpPath.endsWith(File.separatorChar + ""))
			tmpPath = tmpPath + File.separatorChar;

		String date = day + "." + month + "." + year + "-" + hour + ":" + min + ":" + sec;
		Sessions.getCurrent().setAttribute("uploadDate", date);
		String filePath = tmpPath + year + "_" + month + "_" + day + "_" + hour + "_" + min + "_" + sec + "_" + mil
				+ File.separatorChar;

		File f = new File(filePath);
		f.mkdir();

		return f;
	}

	@AfterCompose
	public void initSetup(@ContextParam(ContextType.VIEW) Component view) {
		Selectors.wireComponents(view, this, false);
		// Executions.getCurrent().getDesktop().getWebApp().getConfiguration()
		// .setMaxUploadSize(1024 * 1024);// for larger files
	}

	@Command
	public void uploadFile(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) throws IOException {
		
		UploadEvent upEvent = null;
		Object objUploadEvent = ctx.getTriggerEvent();
		if (objUploadEvent != null && (objUploadEvent instanceof UploadEvent)) {
			upEvent = (UploadEvent) objUploadEvent;
		}
		if (upEvent == null) {
			Messagebox.show("Upload failed", "Warning", Messagebox.OK, Messagebox.EXCLAMATION);
			return;
		}
		// Clients.showBusy("Preparing data...");

		uploaded = upEvent.getMedia();
		tempFile.setValue(uploaded.getName());

		btnProcess.setVisible(checkUploadedFile(uploaded));

	}

	private String getUserSessionForFileName() {
		return String.valueOf(Sessions.getCurrent().hashCode());
	}

	@Command
	public void startProcessing() {
		workingFolder = createFolder();
		String filePath = workingFolder.getAbsolutePath() + File.separatorChar + getUserSessionForFileName();

		try {
			if (!copyFile(filePath, uploaded)) {
				BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));

				Files.copy(writer, uploaded.getReaderData());
			}
			String upload = new FileControl(filePath).getFileContent();
			if (upload == null || upload.length() == 0) {
				return;
			}
			listOfEntries = new FastaProcesser(upload, translate).getEntryList();
			setVariablesGlobal(filePath);

			redirectToEvaluation();
		} catch (Exception e) {

			Messagebox.show("Upload failed - try again, please", "Warning", Messagebox.OK, Messagebox.EXCLAMATION);
			ExceptionLogger.writeSevereError(e);
			return;
		}
	}

	private boolean copyFile(String path, Media media) {
		try {
			Files.copy(new File(path), media.getStreamData());
			return true;
		} catch (Exception e) {
			ExceptionLogger.writeSevereError(e);
			return false;
		}
	}

	private void redirectToEvaluation() {
		new WorkingDirector(listOfEntries, uploadedFile, workingFolder, translate, slidingSize);
	}

	private boolean checkUploadedFile(Media media) {
		String originalName = media.getName();
		String reduced = originalName.replaceAll("\\.", "");
		if (reduced.length() < originalName.length() - 1) {
			Messagebox.show("Chosen file has an unallowed double file extension", "Warning", Messagebox.OK,
					Messagebox.EXCLAMATION);
			return false;
		}
		if (!media.getFormat().equals("octet-stream") || !checkIfValidFile(originalName)) {
			Messagebox.show("Chosen file is not a FASTA file", "Warning", Messagebox.OK, Messagebox.EXCLAMATION);
			return false;
		}
		return true;

		/*
		 * if (media.isBinary()) { Clients.clearBusy();
		 * Messagebox.show("Chosen file is not a text based FASTA file", "Warning",
		 * Messagebox.OK, Messagebox.EXCLAMATION); return null; }
		 */

	}

	/*
	 * private String createUploadedFilePath() { return
	 * createFolder().getAbsolutePath() + File.separatorChar + uploaded.getName();
	 * 
	 * }
	 */

	private String getTemp() {
		return System.getProperty("java.io.tmpdir");
	}

	private boolean checkIfValidFile(String pathToFile) {
		return (pathToFile.endsWith("fa") || pathToFile.endsWith("fasta") || pathToFile.endsWith("fna")
				|| pathToFile.endsWith("faa") || pathToFile.endsWith("fas"));
	}

}
