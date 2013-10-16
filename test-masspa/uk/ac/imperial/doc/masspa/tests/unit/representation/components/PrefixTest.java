package uk.ac.imperial.doc.masspa.tests.unit.representation.components;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.masspa.representation.components.Prefix;

public class PrefixTest extends PrefixTestUtil
{
	private Prefix m_p1;
	private Prefix m_p2;
	private Prefix m_p3;
	private Prefix m_p4;

    @Before
    public void setUp()
    {
    	m_p1=new Prefix(s_action1,s_expr1,s_comp1);
    	m_p2=new Prefix(s_action2,s_expr2,s_comp2);
    	m_p3=new Prefix(s_action1,"1",s_comp3);
    	m_p4=new Prefix("",s_expr1,s_comp1);
    }
    
	@Test(expected=AssertionError.class)
	public void testConstructorNullActionName()
	{
		new Prefix(null,s_expr1,s_comp1);
	}
	
	@Test(expected=AssertionError.class)
	public void testConstructorNullRate1()
	{
		new Prefix(s_action1,(String)null,s_comp1);
	}
	
	@Test(expected=AssertionError.class)
	public void testConstructorNullRate2()
	{
		new Prefix(s_action1,(AbstractExpression)null,s_comp1);
	}
	
	@Test(expected=AssertionError.class)
	public void testConstructorNullContinuation()
	{
		new Prefix(s_action1,s_expr1,null);
	}
    
    @Test
    public void testGetAction()
    {
    	assertEquals(s_action1,m_p1.getAction());
    	assertEquals(s_action2,m_p2.getAction());
    	assertEquals(s_action1,m_p3.getAction());
    	assertEquals("",m_p4.getAction());
    }
    
    @Test
    public void testGetRate()
    {
    	assertEquals(s_expr1.toString(),m_p1.getRate().toString());
    	assertEquals(s_expr2.toString(),m_p2.getRate().toString());
    	assertEquals("1",m_p3.getRate().toString());
    	assertEquals(s_expr1.toString(),m_p4.getRate().toString());
    }
    
    @Test
    public void testGetContinuation()
    {
    	assertEquals(s_comp1,m_p1.getContinuation());
    	assertEquals(s_comp2,m_p2.getContinuation());
    	assertEquals(s_comp3,m_p3.getContinuation());
    	assertEquals(s_comp1,m_p4.getContinuation());
    }

    @Test
    public void testEquals()
    {
    	assertTrue(m_p1.equals(m_p1));
    	assertTrue(m_p2.equals(m_p2));
    	assertTrue(m_p3.equals(m_p3));
    	assertTrue(m_p4.equals(m_p4));
    	assertTrue(m_p1.equals(new Prefix(s_action1,s_expr1,s_comp1)));
    	assertTrue(new Prefix(s_action1,s_expr1,s_comp1).equals(m_p1));
    	assertTrue(m_p2.equals(new Prefix(s_action2,s_expr2,s_comp2)));
    	assertTrue(new Prefix(s_action2,s_expr2,s_comp2).equals(m_p2));
    	assertTrue(m_p3.equals(new Prefix(s_action1,"1",s_comp3)));
    	assertTrue(new Prefix(s_action1,"1",s_comp3).equals(m_p3));
    	assertTrue(m_p4.equals(new Prefix("",s_expr1,s_comp1)));
    	assertTrue(new Prefix("",s_expr1,s_comp1).equals(m_p4));
    	assertFalse(m_p1.equals(m_p2));
    	assertFalse(m_p2.equals(m_p1));
    	assertFalse(m_p3.equals(m_p4));
    	assertFalse(m_p4.equals(m_p3));
    	assertFalse(m_p1.equals(null));
    	assertFalse(m_p2.equals(null));
    	assertFalse(m_p3.equals(null));
    }
}
