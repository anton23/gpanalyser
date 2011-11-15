package uk.ac.imperial.doc.gpanalyser.testing.odes;

import java.io.IOException;

import org.junit.Test;

import uk.ac.imperial.doc.gpanalyser.testing.compiler.TestCompilerClientServer;
import uk.ac.imperial.doc.pctmc.interpreter.ParseException;

public class TestODEGeneratorClientServer extends TestODEGeneratorBase {

	public TestODEGeneratorClientServer() throws ParseException {
		super(new TestCompilerClientServer().getRepresentation());
	}

	
	@Test
	public void testFirstMoments() throws ParseException, IllegalArgumentException, SecurityException, IOException, IllegalAccessException, NoSuchFieldException {	
		checkExpectedODEs("test-gpa/expectedODEs/clientServer/firstOrder");
	}
	
	@Test
	public void testSecondMoments() throws ParseException, IllegalArgumentException, SecurityException, IOException, IllegalAccessException, NoSuchFieldException {	
		checkExpectedODEs("test-gpa/expectedODEs/clientServer/secondOrder");
	}
}
