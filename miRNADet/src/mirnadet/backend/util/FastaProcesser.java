package mirnadet.backend.util;

import java.util.ArrayList;
import java.util.HashMap;

import mirnadet.backend.evaluate.FastaEntry;
import mirnadet.security.ExceptionLogger;

public class FastaProcesser {

	private HashMap<String,FastaEntry> list;

	public FastaProcesser(String input, boolean translationNeeded) {
		list = processFileContentString(input, translationNeeded);
		
	}

	private HashMap<String,FastaEntry> processFileContentString(String wholeFileContent, boolean translation) {
		HashMap<String,FastaEntry> list = new HashMap<String,FastaEntry>();
		String[] splitByLine = wholeFileContent.split("\n");
		ArrayList<Integer> starts = getStartingPositions(">", splitByLine);
		try {
			for (int i = 0; i < starts.size(); i++) {
				FastaEntry fastaEntry = null;
				if (i != starts.size() - 1) {
					for (int j = starts.get(i); j < starts.get(i + 1); j++) {
						if (j != starts.get(i))
							fastaEntry.addToSequence(splitByLine[j]);
						else {
							fastaEntry = new FastaEntry(splitByLine[j],translation);}
					}
				} else {
					for (int j = starts.get(i); j < splitByLine.length; j++) {
						if (j != starts.get(i))
							fastaEntry.addToSequence(splitByLine[j]);
						else
							fastaEntry = new FastaEntry(splitByLine[j],translation);
					}
				}
				list.put(fastaEntry.getHeader(), fastaEntry);
			}

		} catch (Exception e) {
			ExceptionLogger.writeSevereError(e);
		}
		return list;
	}

	private ArrayList<Integer> getStartingPositions(String needle, String[] haystack) {
		ArrayList<Integer> positions = new ArrayList<Integer>();
		for (int i = 0; i < haystack.length; i++) {
			if (haystack[i].trim().startsWith(needle))
				positions.add(i);
		}
		return positions;
	}

	public HashMap<String,FastaEntry> getEntryList() {
		return list;
	}



	/**
	 * In case there are more than one line break within the sequence of one entry.
	 * Will reattach the separated parts
	 * 
	 * @param arr
	 * @return reattached parts
	 */
	private String buildSequence(String[] arr) {
		StringBuilder sb = new StringBuilder();
		for (int i = 1; i < arr.length; i++) {
			sb.append(arr[i].trim());
		}
		return sb.toString();
	}

}
