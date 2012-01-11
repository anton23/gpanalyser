package uk.ac.imperial.doc.masspa.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses
({
	uk.ac.imperial.doc.masspa.tests.unit.UnitTestSuite.class, // Run unit tests
	uk.ac.imperial.doc.masspa.tests.integration.IntegrationTestSuite.class // Run integration tests
})
public class TestSuite
{
}