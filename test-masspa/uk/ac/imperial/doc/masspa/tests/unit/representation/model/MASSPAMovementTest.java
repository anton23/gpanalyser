package uk.ac.imperial.doc.masspa.tests.unit.representation.model;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import uk.ac.imperial.doc.masspa.representation.model.MASSPAMovement;

public class MASSPAMovementTest extends ModelTestUtil
{
	private MASSPAMovement m_move1;
	private MASSPAMovement m_move2;

    @Before
    public void setUp()
    {
    	m_move1 = new MASSPAMovement("leave",s_pop1,"enter",s_pop2,s_expr1);
    	m_move2 = new MASSPAMovement("leave2",s_pop2,"enter2",s_pop1,s_expr2);
    }
  
	@Test(expected=AssertionError.class)
	public void testConstructorNullLeaveAction()
	{
		new MASSPAMovement(null,s_pop1,"enter",s_pop2,s_expr1);
	}
    
	@Test(expected=AssertionError.class)
	public void testConstructorNullFromPopulation()
	{
		new MASSPAMovement("leave",null,"enter",s_pop2,s_expr1);
	}
    
	@Test(expected=AssertionError.class)
	public void testConstructorNullEnterAction()
	{
		new MASSPAMovement("leave",s_pop1,null,s_pop2,s_expr1);
	}
    
	@Test(expected=AssertionError.class)
	public void testConstructorNullToPopulation()
	{
		new MASSPAMovement("leave",s_pop1,"enter",null,s_expr1);
	}
	
	@Test(expected=AssertionError.class)
	public void testConstructorNullRate()
	{
		new MASSPAMovement("leave",s_pop1,"enter",s_pop2,null);
	}

    @Test
    public void testGetLeaveAction()
    {
    	assertTrue(m_move1.getLeaveAction().equals("leave"));
    	assertTrue(m_move2.getLeaveAction().equals("leave2"));
    }
	
    @Test
    public void testGetFrom()
    {
    	assertTrue(m_move1.getFrom()==s_pop1);
    	assertTrue(m_move2.getFrom()==s_pop2);
    }
	
    @Test
    public void testGetEnterAction()
    {
    	assertTrue(m_move1.getLeaveAction().equals("leave"));
    	assertTrue(m_move2.getLeaveAction().equals("leave2"));
    }
    
    @Test
    public void testGetTo()
    {
    	assertTrue(m_move1.getTo()==s_pop2);
    	assertTrue(m_move2.getTo()==s_pop1);
    }

    
    @Test
    public void testGetRate()
    {
    	assertTrue(m_move1.getRate()==s_expr1);
    	assertTrue(m_move2.getRate()==s_expr2);
    }
    
    @Test
    public void testSetIntensity()
    {
    	m_move1.setRate(s_expr2);
    	assertTrue(m_move1.getRate()==s_expr2);
    	assertTrue(m_move2.getRate()==s_expr2);
    	m_move1.setRate(s_expr1);
    	assertTrue(m_move1.getRate()==s_expr1);
    	assertTrue(m_move2.getRate()==s_expr2);
    }
    
    @Test
    public void testHashCode()
    {
    	// Ensure that hash code is independent of intensity
    	int hashCode = m_move1.hashCode();
    	m_move1.setRate(s_expr2);
    	assertEquals(hashCode,m_move1.hashCode());
    }
    
    @Test
    public void testEquals()
    {
    	assertTrue(m_move1.equals(m_move1));
    	assertTrue(m_move1.equals(new MASSPAMovement("leave",s_pop1,"enter",s_pop2,s_expr1)));
    	assertTrue(m_move1.equals(new MASSPAMovement("leaveX",s_pop1,"enterX",s_pop2,s_expr1)));
    	assertTrue(new MASSPAMovement("leave",s_pop1,"enter",s_pop2,s_expr1).equals(m_move1));
    	assertTrue(m_move2.equals(m_move2));
    	assertTrue(m_move2.equals(new MASSPAMovement("leave",s_pop2,"enter",s_pop1,s_expr2)));
    	assertTrue(m_move2.equals(new MASSPAMovement("leaveX",s_pop2,"enterX",s_pop1,s_expr2)));
    	assertTrue(new MASSPAMovement("leave",s_pop2,"enter",s_pop1,s_expr2).equals(m_move2));
    	
    	// Ensure that equals is independent of intensity
    	m_move1.setRate(s_expr2);
    	m_move2.setRate(s_expr1);
    	assertTrue(m_move1.equals(m_move1));
    	assertTrue(m_move1.equals(new MASSPAMovement("leave",s_pop1,"enter",s_pop2,s_expr1)));
    	assertTrue(m_move1.equals(new MASSPAMovement("leaveX",s_pop1,"enterX",s_pop2,s_expr1)));
    	assertTrue(new MASSPAMovement("leave",s_pop1,"enter",s_pop2,s_expr1).equals(m_move1));
    	assertTrue(m_move2.equals(m_move2));
    	assertTrue(m_move2.equals(new MASSPAMovement("leave",s_pop2,"enter",s_pop1,s_expr2)));
    	assertTrue(m_move2.equals(new MASSPAMovement("leaveX",s_pop2,"enterX",s_pop1,s_expr2)));
    	assertTrue(new MASSPAMovement("leave",s_pop2,"enter",s_pop1,s_expr2).equals(m_move2));
    	
    	assertFalse(m_move1.equals(null));
    	assertFalse(m_move2.equals(null));
    	assertFalse(m_move1.equals(new Object()));
    	assertFalse(m_move2.equals(new Object()));
    	assertFalse(m_move1.equals(m_move2));
    	assertFalse(m_move2.equals(m_move1));
    }
}
