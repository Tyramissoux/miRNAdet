package mirnadet.frontend.output;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import javax.activation.MimetypesFileTypeMap;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Selectbox;

import mirnadet.backend.evaluate.FastaEntry;
import mirnadet.security.ExceptionLogger;


public class OutputVM {

	@Wire("#selBox")
	private Selectbox select;

	//private List<ORF> orfs;
	ArrayList<FastaEntry> allEntries;
	private int minSeqLen;
	private boolean multipleStartCodons;
	private ListModelList<String> model;
	private String header;
	private int selected;
	private FastaEntry currentEntry;
	private String average;
	DecimalFormat df; 

	public OutputVM() {
		getSessionGlobals();
		fillSelectBox();
		df = new DecimalFormat("#.00"); 
		// if(allEntries.size()==1)select.setVisible(false);
		entryToOrfList(0);
		currentEntry = allEntries.get(0);
		/*if (orfs.size() == 0) {
			orfs.add(new ORF(0, '0'));
			Messagebox.show("No ORFs were found for '" + header
					+ "' with a min. sequence length of " + minSeqLen,
					"Information", Messagebox.OK, Messagebox.EXCLAMATION);
		}*/
	}

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
		Selectors.wireComponents(view, this, false);
	}

	@Command
	@NotifyChange("{header , orfs}")
	public void switchEntry() {
		Clients.showBusy("Preparing data...");
		int index = select.getSelectedIndex();
		currentEntry = allEntries.get(index);
		entryToOrfList(index);
		setHeader();
		BindUtils.postNotifyChange(null, null, OutputVM.this, "orfs");
		Clients.clearBusy();
	}
	
	private String calculateAverage(){
		int val = 0;
	/*	for(ORF o : orfs)
			val += o.getSeqLength();
		double av= (double)val/(double)orfs.size();*/
		return df.format(val);
		
	}

	public String getAverage(){
		return average;
	}
	
	public String getHeader() {
		return header;
	}

	private void setHeader() {
		header = allEntries.get(select.getSelectedIndex()).getHeader();
		if (header.equals(""))
			header = "Entry " + (select.getSelectedIndex() + 1);
	}

	private void fillSelectBox() {
		ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < allEntries.size(); i++) {
			String header = allEntries.get(i).getHeader();
			if (header.equals(""))
				header = "Entry " + (i + 1);
			list.add(header);
		}
		model = new ListModelList<String>(list);
	}

	@NotifyChange({"orfs","average"})
	private void entryToOrfList(int entryNum) {
		StringBuilder currentEntry = allEntries.get(entryNum).getSequence();
		/*if (!currentEntry.equals(""))
			orfs = new ORFanalyzer(currentEntry, minSeqLen, multipleStartCodons)
					.getORFlist();
		else {
			Messagebox.show("No nucleotide sequence was found", "Information",
					Messagebox.OK, Messagebox.EXCLAMATION);
		}*/
		average = calculateAverage() +" nt ";
		BindUtils.postNotifyChange(null, null, OutputVM.this, "average");
	}

	@Command
	public void saveAsExcel() {
		String filePath = (String) Sessions.getCurrent().getAttribute(
				"uploadedFile");
		filePath = filePath.substring(0, filePath.lastIndexOf(".")) + ".xlsx";
		Clients.showBusy("Preparing download...");
		//new ExcelSaver(filePath, allEntries, minSeqLen, multipleStartCodons);

		Clients.clearBusy();

		if (!tryDownload(filePath))
			Messagebox.show("Excel file couldn't be downloaded", "Information",
					Messagebox.OK, Messagebox.EXCLAMATION);
	}

	/*
	 * private boolean checkFile(String filePath) { File f = new File(filePath);
	 * 
	 * if (!f.exists()) { try {
	 * 
	 * } catch (InterruptedException e) { ExceptionLogger.writeSevereError(e); }
	 * } else return true;
	 * 
	 * return false; }
	 */

	private boolean tryDownload(String filePath) {
		try {
			File f = new File(filePath);
			if (f.exists()) {
				FileInputStream fs = new FileInputStream(f);
				Filedownload.save(fs, new MimetypesFileTypeMap().getContentType(f),f.getName());
				return true;
			}
			else return false;
		} catch (FileNotFoundException e) {
			ExceptionLogger.writeSevereError(e);
			return false;
		}
	}

	@Command
	public void openNCBI(
			@ContextParam(ContextType.COMPONENT) Component component) {
		Listitem li = (Listitem) component.getParent().getParent();
		//String seq = orfs.get(li.getIndex()).getNucSequence();
		// https://blast.ncbi.nlm.nih.gov/Blast.cgi?QUERY=atgaaaaacccaaaaaagaaatccggaggattccggattgtcaatatgctaaaacgcggagtagcccgtgtgagcccctttgggggcttgaagaggctgccagccggacttctgctgggtcatgggcccatcaggatggtcttggcgattctagccttttt&DATABASE=nt&PROGRAM=blastn&CMD=Put
		/*Executions.getCurrent().sendRedirect(
				"https://blast.ncbi.nlm.nih.gov/Blast.cgi?QUERY=" + seq
						+ "&DATABASE=nt&PROGRAM=blastn&CMD=Put", "_blank");*/

	}

	@Command
	public void showDiags() {
		//Sessions.getCurrent().setAttribute("orfList", orfs);
		Sessions.getCurrent().setAttribute("entry", currentEntry);
		Executions.getCurrent().sendRedirect("diagrams.zul", "_blank");
	}

	@SuppressWarnings("unchecked")
	private void getSessionGlobals() {
		allEntries = (ArrayList<FastaEntry>) Sessions.getCurrent().getAttribute(
				"listOfEntries");
		multipleStartCodons = (boolean) Sessions.getCurrent().getAttribute(
				"multiStart");
		minSeqLen = (int) Sessions.getCurrent().getAttribute("minSeqLength");
	}

	public void setSelected(int selected) {
		this.selected = selected;
	}

	public int getSelected() {
		return selected;
	}

	public ListModelList<String> getModel() {
		return model;
	}

	/*public List<ORF> getOrfs() {
		return orfs;
	}*/

}
