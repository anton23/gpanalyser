package uk.ac.imperial.doc.masspa.tests.unit.representation.components;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import uk.ac.imperial.doc.masspa.language.Labels;
import uk.ac.imperial.doc.masspa.representation.components.AnyComponent;

public class AnyComponentTest
{
	private AnyComponent m_any;

    @Before
    public void setUp()
    {
    	m_any=new AnyComponent();
    }

    @Test
    public void testGetName()
    {
    	assertEquals(Labels.s_ANY,m_any.getName());
    }
    
    @Test
    public void testHasDefinitino()
    {
    	assertFalse(m_any.hasDefinition());
    }
    
    @Test
    public void testMatchPattern()
    {
    	assertTrue(m_any.matchPattern(m_any));
    }
    
    @Test
    public void testGetDerivativeStates()
    {
    	assertEquals(0,m_any.getDerivativeStates().size());
    }
    
    @Test
    public void testGetDefinition()
    {
    	assertNull(m_any.getDefinition());
    }
    
	@Test
	public void testEquals()
	{
		assertTrue(m_any.equals(m_any));
		assertTrue(m_any.equals(new AnyComponent()));
		assertTrue(new AnyComponent().equals(m_any));
		assertFalse(m_any.equals(null));
		assertFalse(m_any.equals(new Object()));
	}
}
