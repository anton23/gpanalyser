package uk.ac.imperial.doc.gpanalyser.testing.compiler;

import uk.ac.imperial.doc.gpa.GPAPMain;
import uk.ac.imperial.doc.gpa.pctmc.GPEPAPCTMC;
import uk.ac.imperial.doc.pctmc.interpreter.PCTMCFileRepresentation;
import uk.ac.imperial.doc.pctmc.interpreter.PCTMCInterpreter;
import uk.ac.imperial.doc.pctmc.interpreter.ParseException;

public class TestCompilerBase {
	
	protected String input;
	protected PCTMCFileRepresentation representation;
	protected GPEPAPCTMC pctmc;
	
	public TestCompilerBase(String input) throws ParseException{
		this.input = input;
		this.representation = getRepresentation();
		pctmc = (GPEPAPCTMC) representation.getPctmc();
	}
	
	public PCTMCFileRepresentation getRepresentation() throws ParseException{
		PCTMCInterpreter interpreter = GPAPMain.createGPEPAInterpreter();
		return interpreter.parsePCTMCFileInString(input);		
	}

}
