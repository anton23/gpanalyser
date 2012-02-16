package uk.ac.imperial.doc.pctmc.testing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Iterator;
import java.util.List;

import uk.ac.imperial.doc.gpa.GPAPMain;
import uk.ac.imperial.doc.pctmc.interpreter.PCTMCInterpreter;
import uk.ac.imperial.doc.pctmc.interpreter.ParseException;

import com.google.common.collect.Lists;


public class BaseTestPCTMCParser {
	
	protected BaseTestPCTMCParser() {
		interpreter = GPAPMain.createGPEPAInterpreter();
	}
	
	protected PCTMCInterpreter interpreter;
	
	
	protected void testReportsOneParseError(String fileContents, String errorMessage) {
		testReportsMoreParseErrors(fileContents, Lists.newArrayList(errorMessage));
	}
	
	protected void testReportsMoreParseErrors(String fileContents, List<String> errorMessages) {
		try {
			interpreter.parsePCTMCFileInString(fileContents);
			if (!errorMessages.isEmpty()) {
				fail("Interpreter should raise a parse exception!");
			}
		} catch (ParseException e) {
			List<String> errors = e.getErrors();
			assertEquals(errorMessages.size(), errors.size());
			Iterator<String> i = errors.iterator();
			Iterator<String> j = errorMessages.iterator();
			do {
				assertEquals(j.next(),i.next());
			} while (j.hasNext());
		}
	}
	
		
}
