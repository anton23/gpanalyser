package uk.ac.imperial.doc.masspa.tests.unit.representation.model;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import uk.ac.imperial.doc.masspa.representation.model.AllLocation;
import uk.ac.imperial.doc.masspa.representation.model.Location;
import uk.ac.imperial.doc.masspa.representation.model.VarLocation;

public class LocationTest
{
	List<Integer> m_coords0;
	List<Integer> m_coords1;
	List<Integer> m_coords2;
	List<Integer> m_coords3;

    @Before
    public void setUp()
    {
    	m_coords0 = new LinkedList<Integer>();
    	m_coords1 = new LinkedList<Integer>();
    	m_coords1.add(2);
    	m_coords1.add(1);
    	m_coords2 = new LinkedList<Integer>();
    	m_coords2.add(0);
    	m_coords2.add(3);
    	m_coords3 = new LinkedList<Integer>();
    	m_coords3.add(4);
    	m_coords3.add(3);
    	m_coords3.add(6);
    }
  
    @Test(expected=AssertionError.class)
    public void testConstructorNullLocation()
    {
    	new Location((List<Integer>)null);
    }
    
    @Test
    public void testConstructor1()
    {
    	new Location(m_coords0);
    	new Location(m_coords1);
    	new Location(m_coords2);
    	new Location(m_coords3);
    }
    
    @Test
    public void testConstructor2()
    {
    	new Location(new Location(m_coords0));
    	new Location(new Location(m_coords1));
    	new Location(new Location(m_coords2));
    	new Location(new Location(m_coords3));
    }
    
    @Test
    public void testGetCoords()
    {
    	Location l = new Location(m_coords0);
    	assertTrue(l.getCoords().equals(m_coords0));
    	l = new Location(m_coords1);
    	assertTrue(l.getCoords().equals(m_coords1));
    	l = new Location(m_coords2);
    	assertTrue(l.getCoords().equals(m_coords2));
    	l = new Location(m_coords3);
    	assertTrue(l.getCoords().equals(m_coords3));
    }
    
	@Test(expected=UnsupportedOperationException.class)
	public void testGetCoordsModifyFail()
	{
		new Location(m_coords3).getCoords().add(2);
	}
    
	@Test
	public void testGetDistanceTo()
	{
		Location l1 = new Location(m_coords1);
    	Location l2 = new Location(m_coords2);
    	assertTrue(l1.getDistanceTo(l2)==l2.getDistanceTo(l1));
    	assertTrue(l1.getDistanceTo(l2)==Math.sqrt(8.0));
	}
	
    @Test
    public void testEquals()
    {		
    	Location l0 = new Location(m_coords0);
    	Location l0b = new Location(l0);
		Location l1 = new Location(m_coords1);
		Location l1b = new Location(l1);
		Location l2 = new Location(m_coords2);
    	Location l2b = new Location(l2);
		Location l3 = new Location(m_coords3);
    	Location l3b = new Location(l3);
    	
    	assertTrue(AllLocation.getInstance().equals(AllLocation.getInstance()));
    	assertTrue(VarLocation.getInstance().equals(VarLocation.getInstance()));
    	assertFalse(VarLocation.getInstance().equals(AllLocation.getInstance()));
    	assertFalse(AllLocation.getInstance().equals(VarLocation.getInstance()));
    	
    	assertTrue(l0.equals(l0));
    	assertTrue(l0.equals(l0b));
    	assertTrue(l0b.equals(l0));
    	assertTrue(l0b.equals(new Location(m_coords0)));
    	assertTrue(l1.equals(l1));
    	assertTrue(l1.equals(l1b));
    	assertTrue(l1b.equals(l1));
    	assertTrue(l1.equals(new Location(m_coords1)));
    	assertTrue(l2.equals(l2));
    	assertTrue(l2.equals(l2b));
    	assertTrue(l2b.equals(l2));
    	assertTrue(l2.equals(new Location(m_coords2)));
    	assertTrue(l3.equals(l3));
    	assertTrue(l3.equals(l3b));
    	assertTrue(l3b.equals(l3));
    	assertTrue(l3b.equals(new Location(m_coords3)));
    	
    	assertFalse(l0.equals(l1));
    	assertFalse(l0.equals(l2));
    	assertFalse(l0.equals(l3));
    	assertFalse(l1.equals(l0b));
    	assertFalse(l1.equals(l2b));
    	assertFalse(l1.equals(l3b));
    	assertFalse(l2.equals(l0));
    	assertFalse(l2.equals(l1));
    	assertFalse(l2.equals(l3));
    	assertFalse(l3b.equals(l0));
    	assertFalse(l3b.equals(l1b));
    	assertFalse(l3b.equals(l2));

    	assertFalse(l0.equals(null));
    	assertFalse(l1.equals(null));
    	assertFalse(l2b.equals(null));
    	assertFalse(l3b.equals(null));
    	assertFalse(l0b.equals(new Object()));
    	assertFalse(l1.equals(new Object()));
    	assertFalse(l2b.equals(new Object()));
    	assertFalse(l3.equals(new Object()));
    	
    	assertFalse(l0.equals(AllLocation.getInstance()));
    	assertFalse(l0.equals(VarLocation.getInstance()));
    	assertFalse(AllLocation.getInstance().equals(l0));
    	assertFalse(VarLocation.getInstance().equals(l0));
    	assertFalse(l1.equals(AllLocation.getInstance()));
    	assertFalse(l1.equals(VarLocation.getInstance()));
    	assertFalse(AllLocation.getInstance().equals(l1));
    	assertFalse(VarLocation.getInstance().equals(l1));
    	assertFalse(l3.equals(AllLocation.getInstance()));
    	assertFalse(l3.equals(VarLocation.getInstance()));
    	assertFalse(AllLocation.getInstance().equals(l3));
    	assertFalse(VarLocation.getInstance().equals(l3));
    }
    
    @Test
    public void testCompare()
    {
    	Location l0 = new Location(m_coords0);
    	Location l0b = new Location(l0);
		Location l1 = new Location(m_coords1);
		Location l1b = new Location(l1);
		Location l2 = new Location(m_coords2);
    	Location l2b = new Location(l2);
		Location l3 = new Location(m_coords3);
    	Location l3b = new Location(l3);
    	
    	assertTrue(l0.compareTo(l0b) == 0);
    	assertTrue(l0b.compareTo(l0) == 0);
    	assertTrue(l1.compareTo(l1b) == 0);
    	assertTrue(l1b.compareTo(l1) == 0);
    	assertTrue(l2.compareTo(l2b) == 0);
    	assertTrue(l2b.compareTo(l2) == 0);
    	assertTrue(l3.compareTo(l3b) == 0);
    	assertTrue(l3b.compareTo(l3) == 0);
    	
    	assertTrue(l0.compareTo(l1) == -1);
    	assertTrue(l0.compareTo(l2) == -1);
    	assertTrue(l0.compareTo(l3) == -1);
    	assertTrue(l1.compareTo(l0) == 1);
    	assertTrue(l1.compareTo(l2) == 1);
    	assertTrue(l1.compareTo(l3) == -1);
    	assertTrue(l2.compareTo(l0) == 1);
    	assertTrue(l2.compareTo(l1) == -1);
    	assertTrue(l2.compareTo(l3) == -1);
    	assertTrue(l3.compareTo(l0) == 1);
    	assertTrue(l3.compareTo(l1) == 1);
    	assertTrue(l3.compareTo(l2) == 1);
    	
    	assertTrue(l0.compareTo(AllLocation.getInstance()) == 0);
    	assertTrue(l0.compareTo(VarLocation.getInstance()) == 0);
    	assertTrue(l1.compareTo(AllLocation.getInstance()) == 1);
    	assertTrue(l1.compareTo(VarLocation.getInstance()) == 1);
    	assertTrue(l3.compareTo(AllLocation.getInstance()) == 1);
    	assertTrue(l3.compareTo(VarLocation.getInstance()) == 1);
    }
}
