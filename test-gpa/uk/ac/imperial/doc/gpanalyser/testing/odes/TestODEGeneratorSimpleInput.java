package uk.ac.imperial.doc.gpanalyser.testing.odes;

import java.io.IOException;

import org.junit.Test;

import uk.ac.imperial.doc.gpanalyser.testing.compiler.TestCompilerSimpleInput;
import uk.ac.imperial.doc.pctmc.interpreter.ParseException;

public class TestODEGeneratorSimpleInput extends TestODEGeneratorBase {
	
	public TestODEGeneratorSimpleInput() throws ParseException {
		super(new TestCompilerSimpleInput().getRepresentation());
	}
	
	@Test
	public void testFirstMoments() throws ParseException, IllegalArgumentException, SecurityException, IOException, IllegalAccessException, NoSuchFieldException {
		checkExpectedODEs("test-gpa/expectedODEs/simpleInput/firstOrder");
	}
	
	@Test
	public void testSecondMoments() throws ParseException, IllegalArgumentException, SecurityException, IOException, IllegalAccessException, NoSuchFieldException {
		checkExpectedODEs("test-gpa/expectedODEs/simpleInput/secondOrder");		
	}
	
	
	@Test
	public void testFirstAccumulatedMoments() throws ParseException, IllegalArgumentException, SecurityException, IOException, IllegalAccessException, NoSuchFieldException {
		checkExpectedODEs("test-gpa/expectedODEs/simpleInput/accFirstOrder");
	}
	
	@Test
	public void testSecondAccumulatedMoments() throws ParseException, IllegalArgumentException, SecurityException, IOException, IllegalAccessException, NoSuchFieldException {
		checkExpectedODEs("test-gpa/expectedODEs/simpleInput/accSecondOrder");
	}
}
