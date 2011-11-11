package uk.ac.imperial.doc.gpanalyser.testing;

import java.util.LinkedList;

import org.junit.Test;

import uk.ac.imperial.doc.gpa.GPAPMain;
import uk.ac.imperial.doc.pctmc.interpreter.ParseException;
import uk.ac.imperial.doc.pctmc.testing.TestPCTMCParserBase;

import com.google.common.collect.Lists;

public class TestGPAParser extends TestPCTMCParserBase {
	
	
	public TestGPAParser() {
		interpreter = GPAPMain.createGPEPAInterpreter();
	}
	

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
	public void testReportsWrongComponents1() {
		testReportsMoreParseErrors(
				"ra = 1.0;\n" +
				"rb = 0.1;\n" +
				"n = 10;\n" +
				"\n" +
				"A = (a, ra).B.C;\n" +
				"B = (b, rb).A;\n" +
				"As{A[n]}"
				,
				Lists.newArrayList(
				  "[line 5:13] unknown component 'B' (invalid definition of component 'A')",
				  "[line 5:13] mismatched input '.' expecting ';' (definition of 'A' must end with a semicolon)",
				  "[line 5:15] mismatched input ';' expecting '{' (group components must be enclosed inside '{' and '}')"));		
	}
	
	@Test
	public void testReportsWrongComponents2() {
		testReportsMoreParseErrors(
				"ra = 1.0;\n" +
				"rb = 0.1;\n" +
				"n = 10;\n" +
				"\n" +
				"A = (a, ra).B + ;\n" +
				"B = (b, rb).A;\n" +
				"As{A[n]}"
				,
				Lists.newArrayList(
				  "[line 5:14] extraneous input '+' expecting ';' (definition of 'A' must end with a semicolon)"));		
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
	

	@Test
	public void testReportsUndefinedComponents() {
		testReportsMoreParseErrors(
				"ra = 1.0;\n" +
				"rb = 0.1;\n" +
				"n = 10;\n" +
				"\n" +
				"A = (a, ra).B;\n" +
				"B = (b, rb).D;\n" +
				"As{A[n]}"
				,
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
				Lists.newArrayList(
				  "[line 2:9] mismatched input '+' expecting ';' (constant definition has to be of the form <constant> = <number> ;)",
				  "[line 6:10] constant 'rb' unknown (invalid definition of component 'B')"));		
	}
	
	@Test
	public void testReportsMissingConstantDefinition() {
		testReportsMoreParseErrors(
				"ra = 1.0;\n" +
				"n = 10;\n" +
				"\n" +
				"A = (a, ra).B;\n" +
				"B = (b, rb).A;\n" +
				"As{A[n]}"
				,
				Lists.newArrayList(
				  "[line 5:10] constant 'rb' unknown (invalid definition of component 'B')"));		
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
	public void testWrongSystemEquation1() {
		testReportsOneParseError(
				"ra = 1.0;\n" +
				"rb = 0.1;\n" +
				"n = 10\n;" +
				"\n" +
				"A = (a, ra).B;\n" +
				"B = (b, rb).A;\n" + 
				"As{A[n]|}"
				,
				"[line 7:8] no viable alternative at '}' (group definition has to be of the form G{A[n]|B[m]|...|Z[k]})");		
	}	
	
	
	@Test
	public void testCorrectAnalysis3() throws ParseException {				
		testReportsMoreParseErrors(
				"ra = 1.0;\n" +
				"rb = 0.1;\n" +
				"n = 10;\n" +
				"\n" +
				"A = (a, ra).B + (c,rb).A;\n" +
				"B = (b, rb).A;\n" +
				"As{A[n]}<>Bs{B[n]}\n" + 
				"ODEs(stopTime=10.0, stepSize=0.1, density=10){\n"+
				"E[As:A];" +
				"Var[As:A],E[acc(As:A)], E[As:A + Bs:B];}"
				, 
				new LinkedList<String>());
	}

	@Test
	public void testWrongAnalysis1() throws ParseException {				
		testReportsMoreParseErrors(
				"ra = 1.0;\n" +
				"rb = 0.1;\n" +
				"n = 10;\n" +
				"\n" +
				"A = (a, ra).B + (c,rb).A;\n" +
				"B = (b, rb).A;\n" +
				"As{A[n]}\n" + 
				"ODEs(){}"
				, 
				Lists.newArrayList(
				  "[line 8:5] mismatched input ')' expecting 'stopTime' (ODE analysis has to be of the form ODEs(stopTime=<number>, stepSize=<number>, density=<integer>){}')"));
	}
	
	@Test
	public void testWrongAnalysis2() throws ParseException {				
		testReportsMoreParseErrors(
				"ra = 1.0;\n" +
				"rb = 0.1;\n" +
				"n = 10;\n" +
				"\n" +
				"A = (a, ra).B + (c,rb).A;\n" +
				"B = (b, rb).A;\n" +
				"As{A[n]}\n" + 
				"ODEs(stopTime=10.0, stepSize=0.1, density=10.0){}"
				, 
				Lists.newArrayList(
				  "[line 8:42] mismatched input '10.0' expecting an integer (ODE analysis has to be of the form ODEs(stopTime=<number>, stepSize=<number>, density=<integer>){}')"));
	}
	
	@Test
	public void testWrongAnalysis3() throws ParseException {				
		testReportsMoreParseErrors(
				"ra = 1.0;\n" +
				"rb = 0.1;\n" +
				"n = 10;\n" +
				"\n" +
				"A = (a, ra).B + (c,rb).A;\n" +
				"B = (b, rb).A;\n" +
				"As{A[n]}\n" + 
				"ODEs(stopTime=10.0, stepSize=0.1, density=10){\n"+
				"E[As:A]}"
				, 
				Lists.newArrayList(
				  "[line 9:7] missing ';' at '}' (each plot description has to be of the 'e1,...,en (optional ->\"filename\"); where e1,...,en are expectation based expressions)"));
	}
	
	@Test
	public void testPopulationNotInExpectation() throws ParseException {				
		testReportsMoreParseErrors(
				"ra = 1.0;\n" +
				"rb = 0.1;\n" +
				"n = 10;\n" +
				"\n" +
				"A = (a, ra).B + (c,rb).A;\n" +
				"B = (b, rb).A;\n" +
				"As{A[n]}\n" + 
				"ODEs(stopTime=10.0, stepSize=0.1, density=10){\n"+
				"As:A;}"
				, 
				Lists.newArrayList(
				   "[line 9:4] population As:A has to be inside an expectation (each plot description has to be of the 'e1,...,en (optional ->\"filename\"); where e1,...,en are expectation based expressions)"));
	}
	

	@Test
	public void testPopulationBadGroupComponentPair() throws ParseException {				
		testReportsMoreParseErrors(
				"ra = 1.0;\n" +
				"rb = 0.1;\n" +
				"n = 10;\n" +
				"\n" +
				"A = (a, ra).B + (c,rb).A;\n" +
				"B = (b, rb).A;\n" +
				"As{A[n]}\n" + 
				"ODEs(stopTime=10.0, stepSize=0.1, density=10){\n"+
				"E[As:];}"
				, 
				Lists.newArrayList(
				   "[line 9:5] no viable alternative at ']' (populations have to be of the form 'Group:Component')"));
	}
	
	@Test
	public void testUnknownAnalysis() throws ParseException {				
		testReportsMoreParseErrors(
				"ra = 1.0;\n" +
				"rb = 0.1;\n" +
				"n = 10;\n" +
				"\n" +
				"A = (a, ra).B + (c,rb).A;\n" +
				"B = (b, rb).A;\n" +
				"As{A[n]}\n" + 
				"odes(stopTime=10.0, stepSize=0.1, density=10){\n"+
				"E[As:A];}"
				, 
				Lists.newArrayList(
				   "[line 8:0] unknown command 'odes' (allowed analyses are 'ODEs', 'Simulation', 'Compare' and experiments 'Iterate' and 'Minimise')"));
	}
	
	@Test
	public void testWrongFilename() throws ParseException {				
		testReportsMoreParseErrors(
				"ra = 1.0;\n" +
				"rb = 0.1;\n" +
				"n = 10;\n" +
				"\n" +
				"A = (a, ra).B + (c,rb).A;\n" +
				"B = (b, rb).A;\n" +
				"As{A[n]}\n" + 
				"ODEs(stopTime=10.0, stepSize=0.1, density=10){\n"+
				"E[As:A] -> bla.out;}"
				, 
				Lists.newArrayList(
				   "[line 9:11] mismatched input 'bla' expecting filename of the form \"filename\" (filename description has to be of the form '-> \"filename\"')"));
	}
	
	
}