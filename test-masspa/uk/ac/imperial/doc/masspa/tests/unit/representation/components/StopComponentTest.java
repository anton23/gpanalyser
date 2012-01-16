package uk.ac.imperial.doc.masspa.tests.unit.representation.components;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import uk.ac.imperial.doc.masspa.language.Labels;
import uk.ac.imperial.doc.masspa.representation.components.AllComponent;
import uk.ac.imperial.doc.masspa.representation.components.AnyComponent;
import uk.ac.imperial.doc.masspa.representation.components.ConstComponent;
import uk.ac.imperial.doc.masspa.representation.components.StopComponent;

public class StopComponentTest
{
	private StopComponent m_stop;

    @Before
    public void setUp()
    {
    	m_stop=new StopComponent();
    }

    @Test
    public void testGetName()
    {
    	assertEquals(Labels.s_STOP,m_stop.getName());
    }
    
    @Test
    public void testHasDefinitino()
    {
    	assertFalse(m_stop.hasDefinition());
    }
        
    @Test
    public void testMatchPattern()
    {
    	assertTrue(m_stop.matchPattern(new AnyComponent()));
    	assertTrue(m_stop.matchPattern(m_stop));
    	assertTrue(new StopComponent().matchPattern(m_stop));
    	assertFalse(m_stop.matchPattern(new AllComponent()));
    	assertFalse(m_stop.matchPattern(new ConstComponent(Labels.s_STOP)));
    }
    
    @Test
    public void testGetDerivativeStates()
    {
    	assertEquals(m_stop,m_stop.getDerivativeStates().iterator().next());
    }
    
    @Test
    public void testGetDefinition()
    {
    	assertNull(m_stop.getDefinition());
    }
    
	@Test
	public void testEquals()
	{
		assertTrue(m_stop.equals(m_stop));
		assertTrue(m_stop.equals(new StopComponent()));
		assertTrue(new StopComponent().equals(m_stop));
		assertFalse(m_stop.equals(null));
		assertFalse(m_stop.equals(new Object()));
		assertFalse(m_stop.equals(new AnyComponent()));
		assertFalse(m_stop.equals(new ConstComponent(Labels.s_STOP)));
	}
}
