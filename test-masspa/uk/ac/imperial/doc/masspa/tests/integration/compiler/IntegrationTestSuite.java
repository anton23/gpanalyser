package uk.ac.imperial.doc.masspa.tests.integration.compiler;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses
({
	uk.ac.imperial.doc.masspa.tests.integration.compiler.MASSPACompilerWSNSynchTest.class,
	uk.ac.imperial.doc.masspa.tests.integration.compiler.MASSPACompilerWSNASynchTest.class
})
public class IntegrationTestSuite
{
}