package uk.ac.imperial.doc.masspa.tests.unit.representation.model;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import uk.ac.imperial.doc.masspa.representation.model.MASSPAChannel;

public class MASSPAChannelTest extends ModelTestUtil
{
	private MASSPAChannel m_channel1;
	private MASSPAChannel m_channel2;

    @Before
    public void setUp()
    {
    	m_channel1 = new MASSPAChannel(s_pop1,s_pop2,s_msg1,s_expr1,MASSPAChannel.s_defaultRate);
    	m_channel2 = new MASSPAChannel(s_pop2,s_pop1,s_msg2,s_expr2,MASSPAChannel.s_defaultRate);
    }
  
	@Test(expected=AssertionError.class)
	public void testConstructorNullSender()
	{
		new MASSPAChannel(null,s_pop2,s_msg1,s_expr1,MASSPAChannel.s_defaultRate);
	}
    
	@Test(expected=AssertionError.class)
	public void testConstructorNullReceiver()
	{
		new MASSPAChannel(s_pop1,null,s_msg1,s_expr1,MASSPAChannel.s_defaultRate);
	}
	
	@Test(expected=AssertionError.class)
	public void testConstructorNullMessage()
	{
		new MASSPAChannel(s_pop1,s_pop2,null,s_expr1,MASSPAChannel.s_defaultRate);
	}
	
	@Test(expected=AssertionError.class)
	public void testConstructorNullIntensity()
	{
		new MASSPAChannel(s_pop1,s_pop2,s_msg1,null,MASSPAChannel.s_defaultRate);
	}
		
    @Test
    public void testGetSender()
    {
    	assertTrue(m_channel1.getSender()==s_pop1);
    	assertTrue(m_channel2.getSender()==s_pop2);
    }
    
    @Test
    public void testGetReceiver()
    {
    	assertTrue(m_channel1.getReceiver()==s_pop2);
    	assertTrue(m_channel2.getReceiver()==s_pop1);
    }
    
    @Test
    public void testGetMsg()
    {
    	assertTrue(m_channel1.getMsg()==s_msg1);
    	assertTrue(m_channel2.getMsg()==s_msg2);
    }
    
    @Test
    public void testGetIntensity()
    {
    	assertTrue(m_channel1.getIntensity()==s_expr1);
    	assertTrue(m_channel2.getIntensity()==s_expr2);
    }
    
    @Test
    public void testSetIntensity()
    {
    	m_channel1.setIntensity(s_expr2);
    	assertTrue(m_channel1.getIntensity()==s_expr2);
    	assertTrue(m_channel2.getIntensity()==s_expr2);
    	m_channel1.setIntensity(s_expr1);
    	assertTrue(m_channel1.getIntensity()==s_expr1);
    	assertTrue(m_channel2.getIntensity()==s_expr2);
    }
    
    @Test
    public void testRateType()
    {
    	m_channel1.setRateType(MASSPAChannel.RateType.MASSACTION);
    	assertEquals(m_channel1.getRateType(),MASSPAChannel.RateType.MASSACTION);
    	m_channel1.setRateType(MASSPAChannel.RateType.MULTISERVER);
    	assertEquals(m_channel1.getRateType(),MASSPAChannel.RateType.MULTISERVER);
    }
    
    @Test
    public void testHashCode()
    {
    	// Ensure that hash code is independent of intensity
    	int hashCode = m_channel1.hashCode();
    	m_channel1.setIntensity(s_expr2);
    	assertEquals(hashCode,m_channel1.hashCode());
    }
    
    @Test
    public void testEquals()
    {
    	assertTrue(m_channel1.equals(m_channel1));
    	assertTrue(m_channel1.equals(new MASSPAChannel(s_pop1,s_pop2,s_msg1,s_expr1,MASSPAChannel.s_defaultRate)));
    	assertTrue(new MASSPAChannel(s_pop1,s_pop2,s_msg1,s_expr1,MASSPAChannel.s_defaultRate).equals(m_channel1));
    	assertTrue(m_channel2.equals(m_channel2));
    	assertTrue(m_channel2.equals(new MASSPAChannel(s_pop2,s_pop1,s_msg2,s_expr2,MASSPAChannel.s_defaultRate)));
    	assertTrue(new MASSPAChannel(s_pop2,s_pop1,s_msg2,s_expr2,MASSPAChannel.s_defaultRate).equals(m_channel2));
    	
    	// Ensure that equals is independent of intensity
    	m_channel1.setIntensity(s_expr1);
    	m_channel2.setIntensity(s_expr2);
    	assertTrue(m_channel1.equals(m_channel1));
    	assertTrue(m_channel1.equals(new MASSPAChannel(s_pop1,s_pop2,s_msg1,s_expr1,MASSPAChannel.RateType.MULTISERVER)));
    	assertTrue(new MASSPAChannel(s_pop1,s_pop2,s_msg1,s_expr1,MASSPAChannel.RateType.MASSACTION).equals(m_channel1));
    	assertTrue(m_channel2.equals(m_channel2));
    	assertTrue(m_channel2.equals(new MASSPAChannel(s_pop2,s_pop1,s_msg2,s_expr2,MASSPAChannel.RateType.MASSACTION)));
    	assertTrue(new MASSPAChannel(s_pop2,s_pop1,s_msg2,s_expr2,MASSPAChannel.RateType.MASSACTION).equals(m_channel2));
    	
    	assertFalse(m_channel1.equals(null));
    	assertFalse(m_channel2.equals(null));
    	assertFalse(m_channel1.equals(new Object()));
    	assertFalse(m_channel2.equals(new Object()));
    	assertFalse(m_channel1.equals(m_channel2));
    	assertFalse(m_channel2.equals(m_channel1));
    }
}
