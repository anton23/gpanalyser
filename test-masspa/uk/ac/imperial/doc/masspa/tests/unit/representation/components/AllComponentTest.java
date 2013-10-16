package uk.ac.imperial.doc.masspa.tests.unit.representation.components;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import uk.ac.imperial.doc.masspa.language.Labels;
import uk.ac.imperial.doc.masspa.representation.components.AllComponent;

public class AllComponentTest
{
	private AllComponent m_all;

    @Before
    public void setUp()
    {
    	m_all=new AllComponent();
    }
    
    @Test
    public void testGetName()
    {
    	assertEquals(Labels.s_ALL,m_all.getName());
    }
    
    @Test
    public void testHasDefinitino()
    {
    	assertFalse(m_all.hasDefinition());
    }
    
    @Test
    public void testMatchPattern()
    {
    	assertTrue(m_all.matchPattern(m_all));
    }
    
    @Test
    public void testGetDerivativeStates()
    {
    	assertEquals(0, m_all.getDerivativeStates().size());
    }
    
    @Test
    public void testGetDefinition()
    {
    	assertNull(m_all.getDefinition());
    }
    
    @Test
    public void testEquals()
    {
    	assertTrue(m_all.equals(m_all));
    	assertTrue(m_all.equals(new AllComponent()));
    	assertTrue(new AllComponent().equals(m_all));
    	assertFalse(m_all.equals(null));
    	assertFalse(m_all.equals(new Object()));
    }
}
