package uk.ac.imperial.doc.pctmc.syntax;

import org.antlr.runtime.IntStream;
import org.antlr.runtime.RecognitionException;

public class CustomRecognitionException extends RecognitionException {
	private String message;

	public CustomRecognitionException(IntStream input, String message) {
		super(input);
		this.message = message;
	}

	public String getMessage() {
		return message;
	}	
}
