package uk.ac.imperial.doc.gpanalyser.testing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

import uk.ac.imperial.doc.gpa.GPAPMain;
import uk.ac.imperial.doc.pctmc.interpreter.PCTMCFileRepresentation;
import uk.ac.imperial.doc.pctmc.interpreter.PCTMCInterpreter;
import uk.ac.imperial.doc.pctmc.interpreter.ParseException;


public class TestParsers {
	
	PCTMCInterpreter interpreter = GPAPMain.createGPEPAInterpreter();
	
	@Test
	public void testParseSimpleModel1() throws ParseException {				
		testReportsMoreParseErrors(
				"ra = 1.0;" +
				"rb = 0.1;" +
				"n = 10;" +
				"" +
				"A = (a, ra).B + (c,rb).A;" +
				"B = (b, rb).A;" +
				"As{A[n]}"
				, new LinkedList<String>());
	}
		
	
	@Test
	public void testReportsMissingSemiComponent() {
		testReportsOneParseError(
				"ra = 1.0;\n" +
				"rb = 0.1;\n" +
				"n = 10;\n" +
				"\n" +
				"A = (a, ra).B\n" +
				"B = (b, rb).A;\n" +
				"As{A[n]}"
				,
				"[line 6:0] missing ';' at 'B' (definition of 'A' must end with a semicolon)");
	}	
	
	@Test
	public void testReportsMissingModel() {
		testReportsOneParseError(
				"ra = 1.0;\n" +
				"rb = 0.1;\n" +
				"n = 10\n;" +
				"\n" +
				"A = (a, ra).B;\n" +
				"B = (b, rb).A;\n"
				,
				"[line 7:0] no viable alternative at the end of file (missing system equation)");		
	}	
	
	@Test
	public void testReportsWrongComponents() {
		testReportsMoreParseErrors(
				"ra = 1.0;\n" +
				"rb = 0.1;\n" +
				"n = 10;\n" +
				"\n" +
				"A = (a, ra).B.C;\n" +
				"B = (b, rb).A;\n" +
				"As{A[n]}"
				,
				Lists.newArrayList("[line 5:13] unknown component 'B' (invalid definition of component 'A')",
								   "[line 5:13] mismatched input '.' expecting ';' (definition of 'A' must end with a semicolon)",
						           "[line 5:15] mismatched input ';' expecting '{' (group components must be enclosed inside '{' and '}')"));		
	}
	
	@Test
	public void testReportsNoComponents() {
		testReportsOneParseError(
				"ra = 1.0;\n" +
				"rb = 0.1;\n" +
				"n = 10;\n" +
				"\n" +
				"As{A[n]}"
				,
				"[line 5:0] required list did not match anything at input 'As' (at least one PEPA component definition required)");		
	}
	
	private void testReportsOneParseError(String fileContents, String errorMessage) {
		testReportsMoreParseErrors(fileContents, Lists.newArrayList(errorMessage));
	}
	
	private void testReportsMoreParseErrors(String fileContents, List<String> errorMessages) {
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
	
	String simpleModelUndefinedComponents = 
		"ra = 1.0;\n" +
		"rb = 0.1;\n" +
		"n = 10;\n" +
		"\n" +
		"A = (a, ra).B;\n" +
		"B = (b, rb).D;\n" +
		"As{A[n]}";
	
	@Test
	public void testReportsUndefinedComponents() {
		testReportsMoreParseErrors(simpleModelUndefinedComponents,
				Lists.newArrayList("[line 6:13] unknown component 'D' (invalid definition of component 'B')"));		
	}
	
	@Test
	public void testReportsBadConstantDefinition() {
		testReportsMoreParseErrors(
				"ra = 1.0;\n" +
				"rb = 0.1 + 0.2;\n" +
				"n = 10;\n" +
				"\n" +
				"A = (a, ra).B;\n" +
				"B = (b, rb).A;\n" +
				"As{A[n]}"
				,
				Lists.newArrayList("[line 2:9] constant definition has to be of the form <constant> = <number> ;"));		
	}
	
}
