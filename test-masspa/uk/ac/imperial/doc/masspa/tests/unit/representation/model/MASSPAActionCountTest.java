package uk.ac.imperial.doc.masspa.tests.unit.representation.model;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import uk.ac.imperial.doc.masspa.representation.model.AllLocation;
import uk.ac.imperial.doc.masspa.representation.model.MASSPAActionCount;

public class MASSPAActionCountTest extends ModelTestUtil
{
	private MASSPAActionCount m_count1;
	private MASSPAActionCount m_count2;

    @Before
    public void setUp()
    {
    	m_count1 = new MASSPAActionCount("Count1", s_loc1);
    	m_count2 = new MASSPAActionCount("Count2", AllLocation.getInstance());
    }
  
    @Test(expected=AssertionError.class)
    public void testConstructorNullName()
    {
    	new MASSPAActionCount(null, s_loc2);
    }
    
    @Test
    public void testEquals()
    {
    	assertTrue(m_count1.equals(m_count1));
    	assertTrue(m_count1.equals(new MASSPAActionCount("Count1",s_loc1)));
    	assertTrue(new MASSPAActionCount("Count1",s_loc1).equals(m_count1));
    	assertTrue(m_count2.equals(m_count2));
    	assertTrue(m_count2.equals(new MASSPAActionCount("Count2",AllLocation.getInstance())));
    	assertTrue(new MASSPAActionCount("Count2",AllLocation.getInstance()).equals(m_count2));

    	assertFalse(m_count1.equals(new MASSPAActionCount("Count1",s_loc2)));
    	assertFalse(new MASSPAActionCount("Count1",AllLocation.getInstance()).equals(m_count1));
    	assertFalse(m_count2.equals(new MASSPAActionCount("Count2",s_loc3)));
    	assertFalse(new MASSPAActionCount("Count2",s_loc1).equals(m_count2));
    	
    	assertFalse(m_count1.equals(null));
    	assertFalse(m_count2.equals(null));
    	assertFalse(m_count1.equals(new Object()));
    	assertFalse(m_count2.equals(new Object()));
    	assertFalse(m_count1.equals(m_count2));
    	assertFalse(m_count2.equals(m_count1));
    }
}
