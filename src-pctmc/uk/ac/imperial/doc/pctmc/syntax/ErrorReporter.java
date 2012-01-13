package uk.ac.imperial.doc.pctmc.syntax;

import java.util.LinkedList;
import java.util.List;

public class ErrorReporter {
	
	public List<String> errors = new LinkedList<String>();
	
	public void addError(String message) {
		errors.add(message);
	}

}
