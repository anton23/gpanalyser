package uk.ac.imperial.doc.masspa.tests.unit.representation.components;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.*;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.IntegerExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ProductExpression;
import uk.ac.imperial.doc.jexpressions.expressions.SumExpression;
import uk.ac.imperial.doc.masspa.representation.components.AnyComponent;
import uk.ac.imperial.doc.masspa.representation.components.ConstComponent;
import uk.ac.imperial.doc.masspa.representation.components.MASSPAComponent;
import uk.ac.imperial.doc.masspa.representation.components.StopComponent;

public class ConstComponentTest extends MASSPAComponentTestUtil
{
	private ConstComponent m_c1;
	private ConstComponent m_c2;
	private ConstComponent m_c3;
	
    @Before
    public void setUp()
    {
    	m_c1=new ConstComponent(s_name1);
    	m_c2=new ConstComponent(s_name2);
    	m_c3=new ConstComponent(s_name3);
    }
    
	@Test(expected=AssertionError.class)
	public void testConstructorNullName()
	{
		new ConstComponent(null);
	}
    
    @Test
    public void testHasDefinition()
    {
    	assertFalse(m_c1.hasDefinition());
    	assertFalse(m_c2.hasDefinition());
    	assertFalse(m_c3.hasDefinition());
    	
    	m_c1.define(m_c2, -1);
    	m_c2.define(m_c2, 0);
    	m_c3.define(new StopComponent(), 3);
    	
    	assertTrue(m_c1.hasDefinition());
    	assertTrue(m_c2.hasDefinition());
    	assertTrue(m_c3.hasDefinition());
    }
    
    @Test
    public void testGetDefinition()
    {
    	assertNull(m_c1.getDefinition());
    	assertNull(m_c2.getDefinition());
    	assertNull(m_c3.getDefinition());
    	
    	m_c1.define(m_c2, -1);
    	m_c2.define(m_c2, 0);
    	m_c3.define(new StopComponent(), 3);
    	
    	assertEquals(m_c2,m_c1.getDefinition());
    	assertEquals(m_c2,m_c2.getDefinition());
    	assertEquals(new StopComponent(),m_c3.getDefinition());
    }
    
    @Test
    public void testDefineOk()
    {
    	assertTrue(m_c1.define(m_c2, 3));
    }

    @Test
    public void testDefineReDefinition()
    {
    	assertTrue(m_c1.define(m_c2, 3));
    	assertFalse(m_c1.define(m_c2, 3));
    	assertFalse(m_c1.define(m_c1, 3));
    	assertFalse(m_c1.define(m_c3, 3));
    }
    
    @Test(expected=AssertionError.class)
    public void testDefineNull()
    {
    	m_c1.define(null, 3);
    }
    
    @Test
    public void testCanSend()
    {
    	m_c1.define(s_choice1, 10);
    	assertTrue(m_c1.canSend(s_msg2));
    	assertFalse(m_c1.canSend(s_msg1));
    	assertFalse(m_c1.canSend(null));
    }
    
    @Test
    public void testGetSendingRate1()
    {
    	m_c1.define(s_choice1, 10);
    	assertEquals(new IntegerExpression(0),m_c1.getSendingRate(s_msg1));
    }
    
    @Test
    public void testGetSendingRate2()
    {
    	m_c1.define(s_choice1, 10);
    	AbstractExpression s = SumExpression.create(ProductExpression.create(s_expr1,s_sendRate1),ProductExpression.create(s_expr2,s_sendRate2));
    	assertEquals(s,m_c1.getSendingRate(s_msg2));
    }
    
    @Test
    public void testCanReceive()
    {
    	m_c1.define(s_choice1, 10);
    	assertTrue(m_c1.canReceive(s_msg1));
    	assertFalse(m_c1.canReceive(s_msg2));
    	assertFalse(m_c1.canReceive(null));
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
    	assertTrue(m_c3.matchPattern(new AnyComponent()));
    	assertTrue(m_c3.matchPattern(m_c3));
    	assertFalse(m_c3.matchPattern(new StopComponent()));   	
    	assertFalse(m_c1.matchPattern(m_c2));
    	assertFalse(m_c1.matchPattern(m_c3));
    	assertFalse(m_c2.matchPattern(m_c1));
    	assertFalse(m_c2.matchPattern(m_c3));
    	assertFalse(m_c3.matchPattern(m_c1));
    	assertFalse(m_c3.matchPattern(m_c2));
    }
    
	@Test
    public void testGetDerivativeStates()
    {
    	m_c1.define(s_choice1, 10);
    	Set<MASSPAComponent> s = new HashSet<MASSPAComponent>();

    	s.add(s_comp1);
    	s.add(s_comp2);
    	s.add(s_comp3);
    	assertEquals(s,m_c1.getDerivativeStates());
    	
    	s.clear();
    	assertEquals(s,m_c2.getDerivativeStates());
    	
    	s.clear();
    	assertEquals(s,m_c3.getDerivativeStates());
    }
	
	@Test
	public void testEquals()
	{
		assertTrue(m_c1.equals(m_c1));
		assertTrue(new ConstComponent(s_name1).equals(m_c1));
		assertTrue(m_c1.equals(new ConstComponent(s_name1)));
    	m_c1.define(s_choice1, 10);
		assertTrue(m_c1.equals(new ConstComponent(s_name1)));
		assertTrue(m_c2.equals(m_c2));
		assertTrue(new ConstComponent(s_name2).equals(m_c2));
		assertTrue(m_c2.equals(new ConstComponent(s_name2)));
    	m_c2.define(m_c1, 10);
		assertTrue(m_c2.equals(new ConstComponent(s_name2)));
		assertTrue(m_c3.equals(m_c3));
		assertTrue(new ConstComponent(s_name3).equals(m_c3));
		assertTrue(m_c3.equals(new ConstComponent(s_name3)));

    	assertFalse(m_c1.equals(m_c2));
    	assertFalse(m_c2.equals(m_c1));
    	assertFalse(m_c1.equals(m_c3));
    	assertFalse(m_c3.equals(m_c1));
    	assertFalse(m_c2.equals(m_c3));
    	assertFalse(m_c3.equals(m_c2));
    	assertFalse(m_c1.equals(new Object()));
    	assertFalse(m_c1.equals(new AnyComponent()));
    	assertFalse(m_c1.equals(new StopComponent()));
    	assertFalse(m_c1.equals(null));
    	assertFalse(m_c2.equals(null));
    	assertFalse(m_c3.equals(null));
    	assertFalse(m_c1.equals(new MASSPAComponentNonAbstract(s_name1)));
    	assertFalse(m_c2.equals(new MASSPAComponentNonAbstract(s_name2)));
    	assertFalse(m_c3.equals(new MASSPAComponentNonAbstract(s_name3)));
	}
}
