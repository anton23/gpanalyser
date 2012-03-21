package uk.ac.imperial.doc.masspa.tests.integration.compiler;

import uk.ac.imperial.doc.gpa.GPAPMain;
import uk.ac.imperial.doc.masspa.pctmc.MASSPAPCTMC;
import uk.ac.imperial.doc.pctmc.interpreter.PCTMCFileRepresentation;
import uk.ac.imperial.doc.pctmc.interpreter.PCTMCInterpreter;
import uk.ac.imperial.doc.pctmc.interpreter.ParseException;

public class BaseCompilerTest
{
	protected String inputFile;
	protected PCTMCFileRepresentation representation;
	protected MASSPAPCTMC pctmc;
	
	public BaseCompilerTest(String input) throws ParseException
	{
		this.inputFile = input;
		this.representation = getRepresentation();
		pctmc = (MASSPAPCTMC) representation.getPctmc();
	}
	
	public PCTMCFileRepresentation getRepresentation() throws ParseException
	{
		PCTMCInterpreter interpreter = GPAPMain.createMASSPAInterpreter();
		return interpreter.parsePCTMCFile(inputFile);		
	}
}
