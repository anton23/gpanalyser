package uk.ac.imperial.doc.gpanalyser.testing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;

import uk.ac.imperial.doc.gpa.GPAPMain;
import uk.ac.imperial.doc.pctmc.interpreter.PCTMCFileRepresentation;
import uk.ac.imperial.doc.pctmc.interpreter.PCTMCInterpreter;
import uk.ac.imperial.doc.pctmc.interpreter.ParseException;


public class TestParsers {
	
	PCTMCInterpreter interpreter = GPAPMain.createGPEPAInterpreter();
	
	String simpleModel = 
		"ra = 1.0;" +
		"rb = 0.1;" +
		"n = 10.0;" +
		"" +
		"A = (a, ra).B;" +
		"B = (b, rb).A;" +
		"As{A[n]}";
	
	@Test
	public void testParseSimpleModel1() throws ParseException {				
		PCTMCFileRepresentation PCTMCrepresentation = interpreter.parsePCTMCFileInString(simpleModel);		
	}
	
	String simpleModelMissingSemi = 
		"ra = 1.0;\n" +
		"rb = 0.1;\n" +
		"n = 10.0\n" +
		"\n" +
		"A = (a, ra).B;\n" +
		"B = (b, rb).A;\n" +
		"As{A[n]}";
	
	@Test
	public void testReportsMissingSemi() {
		try {
			interpreter.parsePCTMCFileInString(simpleModelMissingSemi);
			fail("Interpreter should raise a parse exception!");
		} catch (ParseException e) {
			List<String> errors = e.getErrors();
			assertEquals(1, errors.size());
			assertEquals("line 5:0 missing SEMI at 'A'", errors.iterator().next().toString());
		}
	}	
	
	String simpleModelMissingModel = 
		"ra = 1.0;\n" +
		"rb = 0.1;\n" +
		"n = 10.0\n;" +
		"\n" +
		"A = (a, ra).B;\n" +
		"B = (b, rb).A;\n";
		
	
	@Test
	public void testReportsMissingModel() {
		try {
			interpreter.parsePCTMCFileInString(simpleModelMissingModel);
			fail("Interpreter should raise a parse exception!");
		} catch (ParseException e) {
			List<String> errors = e.getErrors();
			assertEquals(1, errors.size());
			assertEquals("line 7:0 no viable alternative at input '<EOF>':incomplete model definition:missing system equation", errors.iterator().next().toString());
		}
	}	
}
