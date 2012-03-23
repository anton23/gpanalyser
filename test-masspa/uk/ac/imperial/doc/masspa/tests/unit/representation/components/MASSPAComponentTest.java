package uk.ac.imperial.doc.masspa.tests.unit.representation.components;

import static org.junit.Assert.*;

import org.junit.*;

import uk.ac.imperial.doc.masspa.representation.components.AnyComponent;
import uk.ac.imperial.doc.masspa.representation.components.StopComponent;

public class MASSPAComponentTest extends MASSPAComponentTestUtil
{
	private MASSPAComponentNonAbstract m_c1;
	private MASSPAComponentNonAbstract m_c2;
	private MASSPAComponentNonAbstract m_c3;
	
    @Before
    public void setUp()
    {
    	m_c1=new MASSPAComponentNonAbstract(s_name1);
    	m_c2=new MASSPAComponentNonAbstract(s_name2);
    	m_c3=new MASSPAComponentNonAbstract(s_name3);
    }
    	
	@Test(expected=AssertionError.class)
	public void testConstructorNullName()
	{
		new MASSPAComponentNonAbstract(null);
	}
    
    @Test
    public void testGetName()
    {
    	assertEquals(s_name1,m_c1.getName());
    	assertEquals(s_name2,m_c2.getName());
    	assertEquals(s_name3,m_c3.getName());
    }
    
    @Test
    public void hasDefinition()
    {
    	assertFalse(m_c1.hasDefinition());
    	assertFalse(m_c2.hasDefinition());
    	assertFalse(m_c3.hasDefinition());
    }
    
    @Test
    public void testMatchPattern()
    {
    	assertFalse(m_c1.matchPattern(m_c1));
    	assertFalse(m_c2.matchPattern(m_c2));
    	assertFalse(m_c3.matchPattern(m_c3));
    	assertFalse(m_c1.matchPattern(new AnyComponent()));
    }
    
    @Test
    public void testGetDerivativeStates()
    {
    	assertEquals(0,m_c1.getDerivativeStates().size());
    	assertEquals(0,m_c2.getDerivativeStates().size());
    	assertEquals(0,m_c3.getDerivativeStates().size());
    }
    
    @Test
    public void testGetDefinition()
    {
    	assertNull(m_c1.getDefinition());
    	assertNull(m_c2.getDefinition());
    	assertNull(m_c3.getDefinition());
    }
    
    @Test
    public void testEquals()
    {
    	assertTrue(m_c1.equals(m_c1));
    	assertTrue(m_c1.equals(new MASSPAComponentNonAbstract(s_name1)));
    	assertTrue(new MASSPAComponentNonAbstract(s_name1).equals(m_c1));
    	assertTrue(m_c2.equals(m_c2));
    	assertTrue(m_c2.equals(new MASSPAComponentNonAbstract(s_name2)));
    	assertTrue(new MASSPAComponentNonAbstract(s_name2).equals(m_c2));
    	assertTrue(m_c3.equals(m_c3));
    	assertTrue(m_c3.equals(new MASSPAComponentNonAbstract(s_name3)));
    	assertTrue(new MASSPAComponentNonAbstract(s_name3).equals(m_c3));
    	
    	assertFalse(m_c1.equals(null));
    	assertFalse(m_c2.equals(null));
    	assertFalse(m_c3.equals(null));
    	assertFalse(m_c1.equals(m_c2));
    	assertFalse(m_c2.equals(m_c1));
    	assertFalse(m_c1.equals(m_c3));
    	assertFalse(m_c3.equals(m_c1));
    	assertFalse(m_c2.equals(m_c3));
    	assertFalse(m_c3.equals(m_c2));
    	assertFalse(m_c1.equals(new Object()));
    	assertFalse(m_c1.equals(new AnyComponent()));
    	assertFalse(m_c1.equals(new StopComponent()));
    }
}
