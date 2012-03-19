package uk.ac.imperial.doc.masspa.tests.unit.representation.components;

import java.util.TreeSet;

import org.junit.*;

import uk.ac.imperial.doc.masspa.representation.components.MASSPAMessage;
import static org.junit.Assert.*;

public class MASSPAMessageTest 
{
	private static final String s_testMsg1 = "Msg1"; 
	private static final String s_testMsg2 = "Msg2"; 
	private MASSPAMessage m_msg1;
	private MASSPAMessage m_msg2;

    @Before
    public void setUp()
    {
    	m_msg1=new MASSPAMessage(s_testMsg1);
    	m_msg2=new MASSPAMessage(s_testMsg2);
    }
	
	@Test(expected=AssertionError.class)
	public void testConstructorNullMsgName()
	{
		new MASSPAMessage(null);
	}    
    
    @Test
    public void testGetMsg()
    {
        assertEquals(s_testMsg1,m_msg1.getMsg());
        assertEquals(s_testMsg2,m_msg2.getMsg());
    }
    
    @Test
    public void testEquals()
    {
    	assertTrue(m_msg1.equals(m_msg1));
    	assertTrue(m_msg2.equals(m_msg2));
    	assertTrue(m_msg1.equals(new MASSPAMessage(s_testMsg1)));
    	assertTrue(new MASSPAMessage(s_testMsg1).equals(m_msg1));
    	assertTrue(m_msg2.equals(new MASSPAMessage(s_testMsg2)));
    	assertTrue(new MASSPAMessage(s_testMsg2).equals(m_msg2));
    	assertFalse(m_msg1.equals(m_msg2));
    	assertFalse(m_msg2.equals(m_msg1));
    	assertFalse(m_msg1.equals(null));
    	assertFalse(m_msg2.equals(null));
    	assertFalse(m_msg1.equals(new MASSPAMessage("")));
    	assertFalse(m_msg1.equals(s_testMsg1));
    	assertFalse(m_msg2.equals(s_testMsg2));
    }
    
    @Test
    public void testCompareTo()
    {
    	assertEquals(0,m_msg1.compareTo(m_msg1));
    	assertEquals(0,m_msg2.compareTo(m_msg2));
    	assertEquals(s_testMsg1.compareTo(s_testMsg2),m_msg1.compareTo(m_msg2));
    	assertEquals(s_testMsg2.compareTo(s_testMsg1),m_msg2.compareTo(m_msg1));
    	assertNotSame(s_testMsg2.compareTo(s_testMsg1),m_msg1.compareTo(m_msg2));
    	assertNotSame(s_testMsg1.compareTo(s_testMsg2),m_msg2.compareTo(m_msg1));
    }
    
    @Test
    public void testMsgNamesToString()
    {
    	TreeSet<MASSPAMessage> m_msgs = new TreeSet<MASSPAMessage>();
    	m_msgs.add(m_msg1);
    	m_msgs.add(m_msg2);
    	assertEquals("["+s_testMsg1+","+s_testMsg2+"]",MASSPAMessage.MsgNamesToString(m_msgs));
    }
}
