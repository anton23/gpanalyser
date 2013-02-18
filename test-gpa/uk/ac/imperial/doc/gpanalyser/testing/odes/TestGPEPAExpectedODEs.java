package uk.ac.imperial.doc.gpanalyser.testing.odes;

import java.util.Arrays;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import uk.ac.imperial.doc.gpa.GPAPMain;
import uk.ac.imperial.doc.pctmc.interpreter.PCTMCFileRepresentation;
import uk.ac.imperial.doc.pctmc.interpreter.PCTMCInterpreter;
import uk.ac.imperial.doc.pctmc.interpreter.ParseException;
import uk.ac.imperial.doc.pctmc.testing.odes.BaseTestODEGeneratorExpectedODEs;


@RunWith(Parameterized.class)
public class TestGPEPAExpectedODEs extends BaseTestODEGeneratorExpectedODEs {
	

	@Override
	protected String getPath() {
		return "test-gpa-inputs/";
	}

	@Override
	protected PCTMCInterpreter initialiseInterpreter() {
		return GPAPMain.createGPEPAInterpreter();
	}

	protected PCTMCInterpreter interpreter;
	protected PCTMCFileRepresentation representation;
	protected String file;
	
	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
				{"simpleModel"}, {"clientServer"},
				{"clientServerProbed"}});
	}
	
	public TestGPEPAExpectedODEs(String file) throws ParseException {
		super(file);
	}
	

}
