package mirnadet.backend.util;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import mirnadet.backend.evaluate.FastaEntry;
import mirnadet.security.ExceptionLogger;


public class ExcelSaver {

	private ArrayList<FastaEntry> entryList;
	private String filePath;
	private CellStyleStorage css;
	private XSSFWorkbook wb;
	private boolean success;

	public ExcelSaver(String filePath, ArrayList<FastaEntry> inputList, int minLen, boolean multiple) {
		this.filePath = filePath;
		this.entryList = inputList;
		setUpWorkbook();
		createFile(minLen, multiple);
	}

	public boolean getSuccess(){
		return success;
	}
	
	private void createFile(int minLen, boolean multiple) {
		for (int i = 0; i < entryList.size(); i++) {
			String header = entryList.get(i).getHeader();
			if (header.equals(""))
				header = "Entry " + (i + 1);
			else if (header.length() > 25)
				header = header.substring(0, 20) + "...";
			header = header.replaceAll("[^a-zA-Z0-9]","");
			//createXLSXOutput(new ORFanalyzer(entryList.get(i).getSequence(),minLen,multiple).getORFlist(), header);
		}
		success = writeXLSXFile();
	}

	private void setUpWorkbook() {
		wb = new XSSFWorkbook();
		css = new CellStyleStorage(wb);
	}

	private XSSFSheet createXLSXHeaderRow(String sheetName) {

		String[] headerElements = new String[] { "#", "start position", "end position",
				"frame number", "sense", "nuc. seq. length", "GC content",
				"nucleotide sequence", "amino acid sequence" };

		XSSFSheet sheet = wb.createSheet(sheetName);
		Row row = sheet.createRow(0);

		for (int i = 0; i < headerElements.length; i++) {
			Cell cell = row.createCell(i);
			cell.setCellType(Cell.CELL_TYPE_STRING);
			cell.setCellValue(headerElements[i]);
			cell.setCellStyle(css.getHeaderGreyStyle());
		}
		return sheet;
	}

	/*private void createXLSXOutput(ArrayList<ORF> orfList, String sheetName) {
		// http://www.java67.com/2014/09/how-to-read-write-xlsx-file-in-java-apache-poi-example.html

		XSSFSheet sheet = createXLSXHeaderRow(sheetName);
	

		for (int i = 0; i < orfList.size(); i++) {
			ORF orf = orfList.get(i);
			Row row = sheet.createRow((i+1));

			Cell c1 = row.createCell(0, Cell.CELL_TYPE_NUMERIC);
			c1.setCellStyle(css.getBasicStyle());
			c1.setCellValue((i + 1));

			Cell c2 = row.createCell(1, Cell.CELL_TYPE_NUMERIC);
			c2.setCellStyle(css.getBasicStyle());
			c2.setCellValue(orf.getStartPos());

			Cell c3 = row.createCell(2, Cell.CELL_TYPE_NUMERIC);
			c3.setCellStyle(css.getBasicStyle());
			c3.setCellValue(orf.getEndPos());

			Cell c4 = row.createCell(3, Cell.CELL_TYPE_NUMERIC);
			c4.setCellStyle(css.getBasicStyle());
			c4.setCellValue(orf.getFrameNum());

			Cell c5 = row.createCell(4, Cell.CELL_TYPE_STRING);
			c4.setCellStyle(css.getBasicStyle());
			c5.setCellValue(orf.getSense()+"");

			Cell c6 = row.createCell(5, Cell.CELL_TYPE_NUMERIC);
			c4.setCellStyle(css.getBasicStyle());
			c6.setCellValue(orf.getSeqLength());

			Cell c7 = row.createCell(6, Cell.CELL_TYPE_NUMERIC);
			c7.setCellStyle(css.getBasicStyle());
			c7.setCellValue(orf.calculateGCContent());

			Cell c8 = row.createCell(7, Cell.CELL_TYPE_STRING);
			c8.setCellValue(orf.getNucSequence());
			c8.setCellStyle(css.getBasicStyle());

			Cell c9 = row.createCell(8, Cell.CELL_TYPE_STRING);
			c9.setCellValue(orf.getAaSequence());
			c9.setCellStyle(css.getBasicStyle());

			
		}

	}*/

	private boolean writeXLSXFile() {
		File file = new File(filePath);
		try {
			if (file.exists())
				file.delete();
			FileOutputStream fileOut = new FileOutputStream(filePath);
			wb.write(fileOut);
			fileOut.close();
			file.setReadOnly();
			wb.close();
			
			wb = null;
			return true;
		} catch (Exception e) {
			ExceptionLogger.writeSevereError(e);

			return false;
		}
	}
}
