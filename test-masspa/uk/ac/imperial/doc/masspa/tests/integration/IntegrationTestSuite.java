package uk.ac.imperial.doc.masspa.tests.integration;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses
({
	uk.ac.imperial.doc.masspa.tests.integration.parser.IntegrationTestSuite.class,
	uk.ac.imperial.doc.masspa.tests.integration.compiler.IntegrationTestSuite.class
})
public class IntegrationTestSuite
{
}