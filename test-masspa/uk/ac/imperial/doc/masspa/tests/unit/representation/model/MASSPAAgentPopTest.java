package uk.ac.imperial.doc.masspa.tests.unit.representation.model;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import uk.ac.imperial.doc.jexpressions.constants.ConstantExpression;
import uk.ac.imperial.doc.jexpressions.expressions.IntegerExpression;
import uk.ac.imperial.doc.masspa.language.Labels;
import uk.ac.imperial.doc.masspa.representation.components.ConstComponent;
import uk.ac.imperial.doc.masspa.representation.components.MASSPAComponent;
import uk.ac.imperial.doc.masspa.representation.model.Location;
import uk.ac.imperial.doc.masspa.representation.model.MASSPAAgentPop;

public class MASSPAAgentPopTest extends ModelTestUtil
{
	// To test protected constructors & methods
	public class MASSPAAgentPopDeriv extends MASSPAAgentPop
	{
		public void setComponent(MASSPAComponent _c){super.setComponent(_c);}
		public void setLocation(Location _l){super.setLocation(_l);}
	}
	
	private MASSPAAgentPop m_pop1;
	private MASSPAAgentPop m_pop2;
	private MASSPAAgentPop m_pop3;
	
    @Before
    public void setUp()
    {
    	m_pop1 = new MASSPAAgentPop(s_comp1, s_loc1);
    	m_pop2 = new MASSPAAgentPop(s_comp1, s_loc2);
    	m_pop3 = new MASSPAAgentPop(s_comp2, s_loc3);
    }
    
    @Test public void testContructProtected()
    {
    	MASSPAAgentPopDeriv m = new MASSPAAgentPopDeriv();
    	assertNull(m.getComponent());
    	assertNull(m.getLocation());
    	assertNull(m.getInitialPopulation());
    }
    
    @Test(expected=AssertionError.class)
    public void testConstructorNullComponent()
    {
    	new MASSPAAgentPop(null, s_loc1);
    }
    
    @Test(expected=AssertionError.class)
    public void testConstructorNullLocation()
    {
    	new MASSPAAgentPop(s_comp1, null);
    }
    
    @Test public void testSetComponent()
    {
    	MASSPAAgentPopDeriv m = new MASSPAAgentPopDeriv();
    	MASSPAComponent c = new ConstComponent("Test");
    	m.setComponent(c);
    	assertEquals(c, m.getComponent());
    }
    
    @Test
    public void testGetComponent()
    {
    	assertTrue(s_comp1==m_pop1.getComponent());
    	assertTrue(s_comp1==m_pop2.getComponent());
    	assertTrue(s_comp2==m_pop3.getComponent());
    }
    
    @Test public void testSetLocation()
    {
    	MASSPAAgentPopDeriv m = new MASSPAAgentPopDeriv();
    	Location l = new Location(Arrays.asList(1,2));
    	m.setLocation(l);
    	assertEquals(l, m.getLocation());
    }
    
    @Test
    public void testGetLocation()
    {
    	assertTrue(s_loc1==m_pop1.getLocation());
    	assertTrue(s_loc2==m_pop2.getLocation());
    	assertTrue(s_loc3==m_pop3.getLocation());
    }
    
    @Test
    public void testHasInitialPopulation1()
    {
    	assertFalse(m_pop1.hasInitialPopulation());
    	assertFalse(m_pop2.hasInitialPopulation());
    	assertFalse(m_pop3.hasInitialPopulation());
    }
    
    @Test
    public void testHasInitialPopulation2()
    {
    	m_pop1.setInitialPopulation(null);
    	assertFalse(m_pop1.hasInitialPopulation());
    	m_pop1.setInitialPopulation(new IntegerExpression(1));
    	assertTrue(m_pop1.hasInitialPopulation());
    }
    
    @Test
    public void testGetInitialPopulation()
    {
    	assertNull(m_pop1.getInitialPopulation());
    	IntegerExpression iexpr = new IntegerExpression(1);
    	m_pop1.setInitialPopulation(iexpr);
    	assertTrue(iexpr==m_pop1.getInitialPopulation());
    }
    
    @Test
    public void testSetInitialPopulation()
    {
    	IntegerExpression iexpr = new IntegerExpression(1);
    	IntegerExpression iexpr2 = new IntegerExpression(2);
    	assertTrue(m_pop1.setInitialPopulation(iexpr));
    	assertTrue(iexpr==m_pop1.getInitialPopulation());
    	assertFalse(m_pop1.setInitialPopulation(iexpr2));
    	assertTrue(iexpr==m_pop1.getInitialPopulation());
    }
    
    @Test
    public void testGetComponentName()
    {
    	assertEquals(s_comp1.getName(), m_pop1.getComponentName());
    	assertEquals(s_comp1.getName(), m_pop2.getComponentName());
    	assertEquals(s_comp2.getName(), m_pop3.getComponentName());
    }

    @Test
    public void testGetName()
    {
    	assertEquals(s_comp1.getName() + s_loc1.toString(), m_pop1.getName());
    	assertEquals(s_comp1.getName() + s_loc2.toString(), m_pop2.getName());
    	assertEquals(s_comp2.getName() + s_loc3.toString(), m_pop3.getName());
    }

    @Test
    public void testGetNameAndInitPop()
    {
    	assertEquals(m_pop1.getName() + "=null", m_pop1.getNameAndInitPop());
    	assertEquals(m_pop2.getName() + "=null", m_pop2.getNameAndInitPop());
    	IntegerExpression iexpr = new IntegerExpression(2);
    	m_pop3.setInitialPopulation(iexpr);
    	assertEquals(m_pop3.getName() + "=" + iexpr, m_pop3.getNameAndInitPop());
    }
    
    @Test
    public void testHashCode()
    {
    	// Ensure that hash code is independent of initial pop
    	int hashCode1 = m_pop1.hashCode();
    	m_pop1.setInitialPopulation(new IntegerExpression(2));
    	assertEquals(hashCode1,m_pop1.hashCode());
    }
    
    @Test
    public void testEquals()
    {
    	assertTrue(m_pop1.equals(m_pop1));
    	assertTrue(new MASSPAAgentPop(s_comp1, s_loc1).equals(m_pop1));
    	assertTrue(m_pop1.equals(new MASSPAAgentPop(s_comp1, s_loc1)));
    	assertTrue(m_pop2.equals(m_pop2));
    	assertTrue(new MASSPAAgentPop(s_comp1, s_loc2).equals(m_pop2));
    	assertTrue(m_pop2.equals(new MASSPAAgentPop(s_comp1, s_loc2)));
    	assertTrue(m_pop3.equals(m_pop3));
    	assertTrue(new MASSPAAgentPop(s_comp2, s_loc3).equals(m_pop3));
    	assertTrue(m_pop3.equals(new MASSPAAgentPop(s_comp2, s_loc3)));
    	
    	// Ensure that equals is independent of initial pop
    	m_pop1.setInitialPopulation(new IntegerExpression(1));
    	m_pop2.setInitialPopulation(new IntegerExpression(2));
    	m_pop3.setInitialPopulation(new IntegerExpression(3));
    	assertTrue(m_pop1.equals(m_pop1));
    	assertTrue(new MASSPAAgentPop(s_comp1, s_loc1).equals(m_pop1));
    	assertTrue(m_pop1.equals(new MASSPAAgentPop(s_comp1, s_loc1)));
    	assertTrue(m_pop2.equals(m_pop2));
    	assertTrue(new MASSPAAgentPop(s_comp1, s_loc2).equals(m_pop2));
    	assertTrue(m_pop2.equals(new MASSPAAgentPop(s_comp1, s_loc2)));
    	assertTrue(m_pop3.equals(m_pop3));
    	assertTrue(new MASSPAAgentPop(s_comp2, s_loc3).equals(m_pop3));
    	assertTrue(m_pop3.equals(new MASSPAAgentPop(s_comp2, s_loc3)));
    	
    	assertFalse(m_pop1.equals(null));
    	assertFalse(m_pop2.equals(null));
    	assertFalse(m_pop3.equals(null));
    	assertFalse(m_pop1.equals(m_pop2));
    	assertFalse(m_pop1.equals(m_pop3));
    	assertFalse(m_pop2.equals(m_pop1));
    	assertFalse(m_pop2.equals(m_pop3));
    	assertFalse(m_pop3.equals(m_pop1));
    	assertFalse(m_pop3.equals(m_pop2));
    	assertFalse(m_pop1.equals(new Object()));
    	assertFalse(m_pop2.equals(new Object()));
    	assertFalse(m_pop3.equals(new Object()));
    }
    
    @Test
    public void testConstRecvAgentPop()
    {
    	assertTrue(new ConstantExpression(Labels.s_RECEIVING_AGENT_POP).equals(MASSPAAgentPop.s_constRecvAgentPop));
    }
}
