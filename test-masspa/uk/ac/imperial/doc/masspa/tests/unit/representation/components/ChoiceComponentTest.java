package uk.ac.imperial.doc.masspa.tests.unit.representation.components;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.*;

import uk.ac.imperial.doc.masspa.representation.components.AnyComponent;
import uk.ac.imperial.doc.masspa.representation.components.ChoiceComponent;
import uk.ac.imperial.doc.masspa.representation.components.MASSPAComponent;
import uk.ac.imperial.doc.masspa.representation.components.Prefix;
import uk.ac.imperial.doc.masspa.representation.components.StopComponent;

public class ChoiceComponentTest extends MASSPAComponentTestUtil
{
	private ChoiceComponent m_c1;
	private ChoiceComponent m_c2;
	
    @Before
    public void setUp()
    {
    	m_c1=new ChoiceComponent(new ArrayList<Prefix>(Arrays.asList(s_prefixes1)));
    	m_c2=new ChoiceComponent(new ArrayList<Prefix>(Arrays.asList(s_prefixes2)));
    }

	@Test(expected=AssertionError.class)
	public void testConstructorNullChoices()
	{
		new ChoiceComponent(null);
	}
    
    @Test
    public void testGetChoices()
    {
    	List<Prefix> c1 = m_c1.getChoices();
    	List<Prefix> c2 = m_c2.getChoices();
    	assertEquals(new ArrayList<Prefix>(Arrays.asList(s_prefixes1)),c1);
    	assertEquals(new ArrayList<Prefix>(Arrays.asList(s_prefixes2)),c2);
    }
    
    @Test(expected=UnsupportedOperationException.class)
    public void testGetChoicesIllegalModification()
    {
    	List<Prefix> c1 = m_c1.getChoices();
    	assertEquals(new ArrayList<Prefix>(Arrays.asList(s_prefixes1)),c1);
    	c1.addAll(Arrays.asList(s_prefixes1));
    }
    
    @Test
    public void testHasDefinition()
    {
    	assertFalse(m_c1.hasDefinition());
    	assertFalse(m_c2.hasDefinition());
    }
    
    @Test
    public void testGetDefinition()
    {
    	assertNull(m_c1.getDefinition());
    	assertNull(m_c2.getDefinition());
    }
    
    @Test
    public void testMatchPattern()
    {
    	assertTrue(m_c1.matchPattern(new AnyComponent()));
    	assertTrue(m_c1.matchPattern(m_c1));
    	assertFalse(m_c1.matchPattern(new StopComponent()));
    	assertTrue(m_c2.matchPattern(new AnyComponent()));
    	assertTrue(m_c2.matchPattern(m_c2));
    	assertFalse(m_c2.matchPattern(new StopComponent()));  	
    	assertFalse(m_c1.matchPattern(m_c2));
    	assertFalse(m_c2.matchPattern(m_c1));
    }
    
	@Test
    public void testGetDerivativeStates()
    {
    	Set<MASSPAComponent> s = new HashSet<MASSPAComponent>();

    	s.add(s_comp1);
    	s.add(s_comp2);
    	s.add(s_comp3);
    	assertEquals(s,m_c1.getDerivativeStates());
    	assertEquals(s,m_c2.getDerivativeStates());
    }

	@Test
	public void testEquals()
	{
		assertTrue(m_c1.equals(m_c1));
		assertTrue(new ChoiceComponent(new ArrayList<Prefix>(Arrays.asList(s_prefixes1))).equals(m_c1));
		assertTrue(m_c1.equals(new ChoiceComponent(new ArrayList<Prefix>(Arrays.asList(s_prefixes1)))));
		assertTrue(m_c2.equals(m_c2));
		assertTrue(new ChoiceComponent(new ArrayList<Prefix>(Arrays.asList(s_prefixes2))).equals(m_c2));
		assertTrue(m_c2.equals(new ChoiceComponent(new ArrayList<Prefix>(Arrays.asList(s_prefixes2)))));

    	assertFalse(m_c1.equals(null));
    	assertFalse(m_c2.equals(null));
    	assertFalse(m_c1.equals(m_c2));
    	assertFalse(m_c2.equals(m_c1));
    	assertFalse(m_c1.equals(new Object()));
    	assertFalse(m_c1.equals(new AnyComponent()));
    	assertFalse(m_c1.equals(new StopComponent()));
    	assertFalse(m_c1.equals(new MASSPAComponentNonAbstract(s_name1)));
    	assertFalse(m_c2.equals(new MASSPAComponentNonAbstract(s_name2)));
	}
}
