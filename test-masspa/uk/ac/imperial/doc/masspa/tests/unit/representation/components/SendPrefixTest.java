package uk.ac.imperial.doc.masspa.tests.unit.representation.components;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import uk.ac.imperial.doc.masspa.representation.components.SendPrefix;
import uk.ac.imperial.doc.masspa.representation.components.ReceivePrefix;

public class SendPrefixTest extends PrefixTestUtil
{
	private SendPrefix m_p1;
	private SendPrefix m_p2;
	private SendPrefix m_p3;

	@Before
    public void setUp()
    {
    	m_p1=new SendPrefix(s_action1,s_expr1,s_msg1,s_sendRate1,s_comp1);
    	m_p2=new SendPrefix(s_action2,s_expr2,s_msg2,s_sendRate2,s_comp2);
    	m_p3=new SendPrefix(s_action1,s_expr3,s_msg1,s_sendRate2,s_comp3);
    }
    
	@Test(expected=AssertionError.class)
	public void testConstructorNullNofMsgs()
	{
		new SendPrefix(s_action1,s_expr1,s_msg1,null,s_comp1);
	}
	
    @Test
    public void testGetNofMsgsSent()
    {
    	assertEquals(s_sendRate1,m_p1.getNofMsgsSent());
    	assertEquals(s_sendRate2,m_p2.getNofMsgsSent());
    	assertEquals(s_sendRate2,m_p3.getNofMsgsSent());
    }
    
    @Test
    public void testEquals()
    {
    	assertTrue(m_p1.equals(m_p1));
    	assertTrue(new SendPrefix(s_action1,s_expr1,s_msg1,s_sendRate1,s_comp1).equals(m_p1));
    	assertTrue(m_p1.equals(new SendPrefix(s_action1,s_expr1,s_msg1,s_sendRate1,s_comp1)));
    	assertTrue(m_p2.equals(m_p2));
    	assertTrue(new SendPrefix(s_action2,s_expr2,s_msg2,s_sendRate2,s_comp2).equals(m_p2));
    	assertTrue(m_p2.equals(new SendPrefix(s_action2,s_expr2,s_msg2,s_sendRate2,s_comp2)));
    	assertTrue(m_p3.equals(m_p3));
    	assertTrue(new SendPrefix(s_action1,s_expr3,s_msg1,s_sendRate2,s_comp3).equals(m_p3));
    	assertTrue(m_p3.equals(new SendPrefix(s_action1,s_expr3,s_msg1,s_sendRate2,s_comp3)));
		assertFalse(m_p1.equals(null));
		assertFalse(m_p2.equals(null));
		assertFalse(m_p3.equals(null));
    	assertFalse(m_p1.equals(m_p2));
    	assertFalse(m_p1.equals(m_p3));
    	assertFalse(m_p2.equals(m_p3));
    	assertFalse(m_p3.equals(m_p1));
    	assertFalse(m_p3.equals(m_p2));
    	assertFalse(m_p2.equals(m_p1));
    	assertFalse(m_p1.equals(new ReceivePrefix(s_action1,s_msg1,s_sendRate1,s_comp1)));
    	assertFalse(m_p2.equals(new ReceivePrefix(s_action2,s_msg2,s_sendRate2,s_comp2)));
    	assertFalse(m_p3.equals(new ReceivePrefix(s_action1,s_msg1,s_sendRate2,s_comp3)));
    	assertFalse(m_p1.equals(new SendPrefix(s_action1,s_expr1,s_msg1,s_sendRate1,s_comp2)));
    	assertFalse(m_p1.equals(new SendPrefix(s_action1,s_expr1,s_msg1,s_sendRate2,s_comp1)));
    	assertFalse(m_p1.equals(new SendPrefix(s_action1,s_expr1,s_msg2,s_sendRate1,s_comp1)));
    	assertFalse(m_p1.equals(new SendPrefix(s_action1,s_expr2,s_msg1,s_sendRate1,s_comp1)));
    	assertFalse(m_p1.equals(new SendPrefix(s_action2,s_expr1,s_msg1,s_sendRate1,s_comp1)));	
    }
}
