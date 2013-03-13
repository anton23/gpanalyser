package uk.ac.imperial.doc.gpanalyser.testing.plain.odes;

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
public class TestPlainExpectedODEs extends BaseTestODEGeneratorExpectedODEs {
	

	@Override
	protected String getPath() {
		return "test-plain-inputs/";
	}

	@Override
	protected PCTMCInterpreter initialiseInterpreter() {
		return GPAPMain.createPlainPCTMCInterpreter();
	}

	protected PCTMCInterpreter interpreter;
	protected PCTMCFileRepresentation representation;
	protected String file;
	
	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
				 {"nonSmooth"}, {"mm1aircon"}, {"SIR"}, {"SIS"}, {"minVar"}});
	}
	
	public TestPlainExpectedODEs(String file) throws ParseException {
		super(file);
	}
	

}
