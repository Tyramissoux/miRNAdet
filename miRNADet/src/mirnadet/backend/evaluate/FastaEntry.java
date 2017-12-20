package mirnadet.backend.evaluate;

import java.util.HashMap;
import java.util.regex.Pattern;

public class FastaEntry {

	private String header;
	private StringBuilder sequenceBuilder;
	private StringBuilder translateBuilder;
	private boolean translationNeeded;
	private HashMap<Integer,String> tempToSequences;

	public FastaEntry(boolean translate) {
		translationNeeded = translate;
		sequenceBuilder = new StringBuilder();
	}

	public FastaEntry(String header,boolean translate) {
		translationNeeded = translate;
		this.header = header;
		sequenceBuilder = new StringBuilder();
	}

	public FastaEntry(String head, String seq,boolean translate) {
		translationNeeded = translate;
		this.header = head;
		setSequence(seq);
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public StringBuilder getSequence() {
		 return sequenceBuilder;
	}

	public void setSequence(String sequence) {
		sequenceBuilder = new StringBuilder(sequence.toUpperCase());
	}

	public void addToSequence(String part) {
		sequenceBuilder.append(part);
	}

	public boolean containsAmbiguity() {
		return !Pattern.matches("[atgc]+", getSequence().toString());
	}

	public StringBuilder getTranslatedSequence() {
		if (translateBuilder == null) 
			translateSequence();
			return translateBuilder;
	}

	public boolean getTranslationNeeded() {
		return translationNeeded;
	}

	public void translateSequence() {

		translateBuilder = new StringBuilder();
		getSequence();
		for (int i = 0; i < getSequence().length(); i++) {
			translateBuilder.append(translate(getSequence().charAt(i)));
		}
	}

	public void setTempToSequences(HashMap<Integer,String> map) {
		tempToSequences = map;
	}
	
	public HashMap<Integer,String> getTempToSequences(){
		return tempToSequences;
	}
	
	public int getNumOfSeqsForTemp(int temp) {
		if(tempToSequences.containsKey(temp)) {
			return tempToSequences.get(temp).trim().split("\n").length-1;
		}
		return -1;
	}
	
	// http://reverse-complement.com/ambiguity.html
	private char translate(char in) {
		switch (in) {
		case 'a':
			return 'u';
		case 't':
			return 'a';
		case 'c':
			return 'g';
		case 'g':
			return 'c';
		case 'r':
			return 'y';
		case 'y':
			return 'r';
		case 's':
			return 's';
		case 'w':
			return 'w';
		case 'k':
			return 'm';
		case 'm':
			return 'k';
		case 'b':
			return 'v';
		case 'v':
			return 'b';
		case 'd':
			return 'h';
		case 'h':
			return 'd';
		case '-':
			return '-';
		default:
			return ' ';
		}
	}

}
