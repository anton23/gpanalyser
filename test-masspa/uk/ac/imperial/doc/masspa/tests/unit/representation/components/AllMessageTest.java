package uk.ac.imperial.doc.masspa.tests.unit.representation.components;

import org.junit.*;

import uk.ac.imperial.doc.masspa.language.Labels;
import uk.ac.imperial.doc.masspa.representation.components.AllMessage;
import uk.ac.imperial.doc.masspa.representation.components.MASSPAMessage;
import static org.junit.Assert.*;

public class AllMessageTest
{
	private AllMessage m_all;

    @Before
    public void setUp()
    {
    	m_all=new AllMessage();
    }
	
    @Test
    public void testEquals()
    {
    	assertTrue(m_all.equals(m_all));
    	assertTrue(m_all.equals(new AllMessage()));
    	assertTrue(new AllMessage().equals(m_all));    	
    	assertTrue(m_all.equals(new MASSPAMessage(Labels.s_ALL)));
    	assertTrue(new MASSPAMessage(Labels.s_ALL).equals(m_all));
    	assertFalse(m_all.equals(null));
    	assertFalse(m_all.equals(new Object()));
    }
}
