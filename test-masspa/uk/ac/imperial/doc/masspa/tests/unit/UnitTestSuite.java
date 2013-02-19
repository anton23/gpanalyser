package uk.ac.imperial.doc.masspa.tests.unit;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses
({
	uk.ac.imperial.doc.masspa.tests.unit.representation.components.UnitTestSuite.class,
	uk.ac.imperial.doc.masspa.tests.unit.representation.model.UnitTestSuite.class,
	uk.ac.imperial.doc.masspa.tests.unit.pctmc.UnitTestSuite.class
})
public class UnitTestSuite
{
}