package uk.ac.imperial.doc.masspa.tests.unit.representation.components;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import uk.ac.imperial.doc.masspa.tests.unit.representation.components.MASSPAMessageTest;

@RunWith(Suite.class)
@Suite.SuiteClasses
({
	MASSPAMessageTest.class,
	AllMessageTest.class,
	PrefixTest.class,
	MessagePrefixTest.class,
	ReceivePrefixTest.class,
	SendPrefixTest.class,
	MASSPAComponentTest.class,
	AllComponentTest.class,
	AnyComponentTest.class,
	StopComponentTest.class,
	ChoiceComponentTest.class,
	ConstComponentTest.class,
	MASSPAAgentsTest.class
})
public class UnitTestSuite
{
}