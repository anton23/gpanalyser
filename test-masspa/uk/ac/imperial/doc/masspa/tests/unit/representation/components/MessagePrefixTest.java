package uk.ac.imperial.doc.masspa.tests.unit.representation.components;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class MessagePrefixTest extends PrefixTestUtil
{
	private MessagePrefixNonAbstract m_p1;
	private MessagePrefixNonAbstract m_p2;
	private MessagePrefixNonAbstract m_p3;

	@Before
    public void setUp()
    {
    	m_p1=new MessagePrefixNonAbstract(s_action1,s_expr1,s_msg1,s_comp1);
    	m_p2=new MessagePrefixNonAbstract(s_action2,s_expr2,s_msg2,s_comp2);
    	m_p3=new MessagePrefixNonAbstract(s_action1,s_expr3,s_msg1,s_comp3);
    }
    
	@Test(expected=AssertionError.class)
	public void testConstructorNullMessageName()
	{
		new MessagePrefixNonAbstract(s_action1,s_expr1,null,s_comp1);
	}
	
    @Test
    public void testGetMsg()
    {
    	assertEquals(s_msg1,m_p1.getMsg());
    	assertEquals(s_msg2,m_p2.getMsg());
    	assertEquals(s_msg1,m_p3.getMsg());
    }
}
