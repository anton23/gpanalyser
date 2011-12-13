package uk.ac.imperial.doc.masspa.tests.unit.representation.model;

import static org.junit.Assert.*;

import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;

import uk.ac.imperial.doc.masspa.language.Labels;
import uk.ac.imperial.doc.masspa.language.Messages;
import uk.ac.imperial.doc.masspa.representation.components.ConstComponent;
import uk.ac.imperial.doc.masspa.representation.components.MASSPAAgents;
import uk.ac.imperial.doc.masspa.representation.components.MASSPAMessage;
import uk.ac.imperial.doc.masspa.representation.model.Location;
import uk.ac.imperial.doc.masspa.representation.model.MASSPAActionCount;
import uk.ac.imperial.doc.masspa.representation.model.MASSPAAgentPop;
import uk.ac.imperial.doc.masspa.representation.model.MASSPAChannel;
import uk.ac.imperial.doc.masspa.representation.model.MASSPAModel;
import uk.ac.imperial.doc.masspa.representation.model.MASSPAMovement;
import uk.ac.imperial.doc.masspa.representation.model.VarLocation;

public class MASSPAModelTest extends ModelTestUtil
{
	private MASSPAAgents m_agents;
	private MASSPAModel m_model;

	@Before
    public void setUp()
    {
		m_agents = new MASSPAAgents();
		m_model = new MASSPAModel(m_agents);
    }

	@Test(expected=AssertionError.class)
	public void testConstructorNullComponentFactory()
	{
		new MASSPAModel(null);
	}
	
	// *****************************************************************
	// Test Getters
	// *****************************************************************
	
	@Test
	public void testGetComponentFactory()
	{
		assertTrue(m_agents == m_model.getMASSPAAgents());
	}
	
	@Test
	public void getAgentPop()
	{
		m_agents.getConstComponent("Scope1","Test1", 0);
		m_agents.getConstComponent("Scope2","Test2", 0);
		m_model.addLocation(s_loc1, 2);
		m_model.addLocation(s_loc2, 2);
		MASSPAAgentPop p1 = new MASSPAAgentPop(new ConstComponent("Test1"),s_loc1);
		assertNull(m_model.getAgentPop(p1));
		MASSPAAgentPop p2 = m_model.getAgentPop("Test1", s_loc1, 3);
		assertTrue(m_model.getAgentPop(p1)==p2);
		assertTrue(m_model.getAgentPop(p2)==p2);
		assertTrue(m_model.getAgentPop("Test1", s_loc1, 4)==p2);
		p2 = m_model.getAgentPop("Test2",s_loc2,5);
		assertFalse(m_model.getAgentPop(p1)==p2);
		assertTrue(m_model.getAgentPop(p2)==p2);
		assertTrue(m_model.getAgentPop("Test2", s_loc2, 3)==p2);
	}
	
	@Test(expected=AssertionError.class)
	public void getAgentPopNullComponent1()
	{
		m_model.addLocation(s_loc1, 2);
		m_model.getAgentPop(null, s_loc1, 3);
	}
		
	@Test(expected=AssertionError.class)
	public void getAgentPopNullComponent2()
	{
		m_model.addLocation(s_loc1, 2);
		m_model.getAgentPop("Test1", s_loc1, 3);
	}
	
	@Test(expected=AssertionError.class)
	public void getAgentPopNullLocation1()
	{
		m_agents.getConstComponent("Scope2","Test1", 0);
		m_model.getAgentPop("Test1", null, 3);
	}
	
	@Test(expected=AssertionError.class)
	public void getAgentPopNullLocation2()
	{
		m_agents.getConstComponent("","Test1", 0);
		m_model.getAgentPop("Test1", s_loc1, 3);
	}
	
	
	@Test
	public void getActionCount()
	{
		m_agents.addAction("submit");
		m_agents.addAction("clear");
		m_model.addLocation(s_loc1, 2);
		m_model.addLocation(s_loc2, 2);
		MASSPAActionCount c1 = new MASSPAActionCount("submit",s_loc1);
		assertNull(m_model.getActionCount(c1));
		MASSPAActionCount c2 = new MASSPAActionCount("clear",s_loc2);
		assertNull(m_model.getActionCount(c2));
		MASSPAActionCount c3 = m_model.getActionCount("submit", s_loc1, 3);
		MASSPAActionCount c4 = m_model.getActionCount("clear", s_loc2, 3);
		assertTrue(m_model.getActionCount(c1)==c3);
		assertTrue(m_model.getActionCount(c2)==c4);
		assertTrue(m_model.getActionCount(c3).equals(c1));
		assertTrue(m_model.getActionCount(c4).equals(c2));
	}
	
	@Test(expected=AssertionError.class)
	public void getActionCountNullAction()
	{
		m_model.addLocation(s_loc1, 2);
		m_model.getActionCount(null, s_loc1, 3);
	}
	
	@Test(expected=AssertionError.class)
	public void getActionCountNullAction2()
	{
		m_model.addLocation(s_loc1, 2);
		m_model.getActionCount("unkownaction", s_loc1, 3);
	}
	
	@Test(expected=AssertionError.class)
	public void getActionCountNullLocation1()
	{
		m_agents.addAction("submit");
		m_model.getActionCount("submit", null, 3);
	}
	
	@Test(expected=AssertionError.class)
	public void getActionCountNullLocation2()
	{
		m_agents.addAction("submit");
		m_model.getActionCount("Test1", s_loc1, 3);
	}
	
	public void getChannelType()
	{
		m_model.setChannelType(Messages.s_COMPILER_KEYWORD_MASSACTION, 2);
		assertEquals(m_model.getChannelType(),MASSPAChannel.RateType.MASSACTION);
		m_model.setChannelType(Messages.s_COMPILER_KEYWORD_MULTISERVER, 1);
		assertEquals(m_model.getChannelType(),MASSPAChannel.RateType.MULTISERVER);
	}
	
	@Test(expected=AssertionError.class)
	public void getChannelTypeFailure()
	{
		m_model.setChannelType("blaaa", 2);
	}

	// *****************************************************************
	// Test "All" Getters
	// *****************************************************************
	
	@Test
	public void testGetAllAgents()
	{
		assertEquals(m_agents.getAgents(), m_model.getAllAgents());
	}

	@Test
	public void testGetAllMessages()
	{
		m_agents.addMessage(s_msg1);
		m_agents.addMessage(s_msg2);
		HashSet<MASSPAMessage> s = new HashSet<MASSPAMessage>();
		s.add(s_msg1);
		s.add(s_msg2);
		assertEquals(s,m_model.getAllMessages());
	}
	
	@Test(expected=UnsupportedOperationException.class)
	public void testGetAllMessagesModifyFail()
	{
		m_model.getAllMessages().add(s_msg1);
	}
	
	@Test
	public void testGetAllLocations1()
	{
		assertEquals(1,m_model.getAllLocations().size());
	}
	
	@Test
	public void testGetAllLocations2()
	{
		HashSet<Location> locs = new HashSet<Location>();
		locs.add(s_loc1);
		locs.add(s_loc2);
		locs.add(s_loc3);
		locs.add(VarLocation.getInstance());
		
		m_model.addLocation(s_loc3, 10);
		m_model.addLocation(s_loc1, 10);
		m_model.addLocation(s_loc2, 10);
		assertEquals(locs,m_model.getAllLocations());
	}
	
	@Test(expected=UnsupportedOperationException.class)
	public void testGetAllLocationsModifyFail()
	{
		m_model.getAllLocations().add(s_loc1);
	}
	
	@Test
	public void testGetAllAgentPopulations()
	{
		m_agents.getConstComponent("Scope1","Test1", 0);
		m_agents.getConstComponent("Scope1","Test2", 0);
		m_model.addLocation(s_loc1, 2);
		m_model.addLocation(s_loc2, 2);
		HashSet<MASSPAAgentPop> s = new HashSet<MASSPAAgentPop>();
		s.add(new MASSPAAgentPop(m_agents.getComponent("Test1"),s_loc1));
		s.add(new MASSPAAgentPop(m_agents.getComponent("Test1"),s_loc2));
		s.add(new MASSPAAgentPop(m_agents.getComponent("Test1"),VarLocation.getInstance()));
		s.add(new MASSPAAgentPop(m_agents.getComponent("Test2"),s_loc1));
		s.add(new MASSPAAgentPop(m_agents.getComponent("Test2"),s_loc2));
		s.add(new MASSPAAgentPop(m_agents.getComponent("Test2"),VarLocation.getInstance()));
		s.add(new MASSPAAgentPop(m_agents.getComponent(Labels.s_STOP),s_loc1));
		s.add(new MASSPAAgentPop(m_agents.getComponent(Labels.s_STOP),s_loc2));
		s.add(new MASSPAAgentPop(m_agents.getComponent(Labels.s_STOP),VarLocation.getInstance()));
		s.add(new MASSPAAgentPop(m_agents.getComponent(Labels.s_ANY),s_loc1));
		s.add(new MASSPAAgentPop(m_agents.getComponent(Labels.s_ANY),s_loc2));
		s.add(new MASSPAAgentPop(m_agents.getComponent(Labels.s_ANY),VarLocation.getInstance()));
		assertEquals(s,m_model.getAllAgentPopulations());
	}
	
	@Test(expected=UnsupportedOperationException.class)
	public void testGetAllAgentPopulationsModifyFail()
	{
		m_model.getAllAgentPopulations().add(new MASSPAAgentPop(s_comp1,s_loc1));
	}
	
	@Test
	public void testGetAllChannels()
	{
		
		m_model.addLocation(s_loc1, 2);
		m_model.addLocation(s_loc2, 2);
		m_model.addLocation(s_loc3, 2);
		m_agents.addMessage(s_msg1);
		m_agents.addMessage(s_msg2);
		m_agents.getConstComponent("Scope1",s_comp1.getName(), 0);
		m_agents.getConstComponent("Scope2",s_comp2.getName(), 0);
		m_agents.getConstComponent("Scope1",s_comp3.getName(), 0);
		m_model.addChannel(s_pop1,s_pop2,s_msg1,s_expr1,0);
		m_model.addChannel(s_pop3,s_pop2,s_msg1,s_expr1,0);
		m_model.addChannel(s_pop2,s_pop1,s_msg2,s_expr2,0);
		m_model.addChannel(s_pop2,s_pop1,new MASSPAMessage("Unknown"),s_expr2,0);
		
		assertEquals(0,m_model.getAllChannels(s_pop1, s_msg1).size());
		assertEquals(1,m_model.getAllChannels(s_pop1, s_msg2).size());
		assertEquals(2,m_model.getAllChannels(s_pop2, s_msg1).size());
		assertEquals(0,m_model.getAllChannels(s_pop2, s_msg2).size());
		assertEquals(0,m_model.getAllChannels(s_pop3, s_msg1).size());
		assertEquals(0,m_model.getAllChannels(s_pop3, s_msg2).size());
		assertEquals(0,m_model.getAllChannels(null, s_msg2).size());
		assertEquals(0,m_model.getAllChannels(s_pop3, null).size());
		assertEquals(0,m_model.getAllChannels(null, null).size());
	}
	
	@Test
	public void testGetAllMovements()
	{
		m_model.addLocation(s_loc1, 2);
		m_model.addLocation(s_loc2, 2);
		m_model.addLocation(s_loc3, 2);
		m_agents.getConstComponent("Scope1",s_comp1.getName(), 0);
		m_agents.getConstComponent("Scope2",s_comp2.getName(), 0);
		m_agents.getConstComponent("Scope1",s_comp3.getName(), 0);
		m_model.addMovement("leave", s_pop1, "enter", s_pop2, s_expr1, 0);
		m_model.addMovement("leave", s_pop1, "enter", s_pop3, s_expr1, 0);
		m_model.addMovement("leave", s_pop2, "enter", s_pop3, s_expr1, 0);
		
		assertEquals(2,m_model.getAllMovements(s_pop1).size());
		assertEquals(1,m_model.getAllMovements(s_pop2).size());
		assertEquals(0,m_model.getAllMovements(s_pop3).size());
		assertEquals(0,m_model.getAllMovements(null).size());
		
		HashSet<MASSPAMovement> moves = new HashSet<MASSPAMovement>();
		moves.add(new MASSPAMovement("test",s_pop1,"test2",s_pop2,s_expr1));
		moves.add(new MASSPAMovement("test",s_pop1,"test2",s_pop3,s_expr1));
		assertEquals(moves,m_model.getAllMovements(s_pop1));
	}
	
	@Test(expected=UnsupportedOperationException.class)
	public void testGetAllMovementsModifyFail()
	{
		m_model.getAllMovements(s_pop1).add(new MASSPAMovement("test",s_pop1,"test2",s_pop2,s_expr1));
	}
	
	// *****************************************************************
	// Fabric - Generate Locations/InitialPopulations/Channels
	// *****************************************************************
	@Test
	public void testAddLocation()
	{
		m_model.addLocation(s_loc1, 2);
		assertTrue(s_loc1 == m_model.getAllLocations().iterator().next());
	}
	
	@Test
	public void testAddLocationNull()
	{
		m_model.addLocation(null, 2);
		assertEquals(1,m_model.getAllLocations().size());
	}
	
	@Test
	public void testAddLocationDuplicate()
	{
		m_model.addLocation(s_loc1, 2);
		assertTrue(s_loc1 == m_model.getAllLocations().iterator().next());
		m_model.addLocation(new Location(s_loc1), 2);
		assertTrue(s_loc1 == m_model.getAllLocations().iterator().next());
	}
	
	@Test
	public void testAddInitialAgentPopulation()
	{
		m_model.addLocation(s_loc1, 2);
		m_agents.getConstComponent("Scope",s_comp1.getName(), 0);
		MASSPAAgentPop p = m_model.getAgentPop(s_comp1.getName(), s_loc1, 2);
		assertNull(p.getInitialPopulation());
		m_model.addInitialAgentPopulation(p, s_expr1, 2);
		assertEquals(s_expr1,p.getInitialPopulation());
		
		m_agents.getConstComponent("Scope",s_comp2.getName(), 0);
		m_model.addInitialAgentPopulation(new MASSPAAgentPop(s_comp2,s_loc1), s_expr2, 2);
		p = m_model.getAgentPop(new MASSPAAgentPop(s_comp2,s_loc1));
		assertEquals(s_expr2,p.getInitialPopulation());
	}
	
	@Test(expected=AssertionError.class)
	public void testAddInitialAgentPopulationMissingLocation()
	{
		m_agents.getConstComponent("Scope",s_comp2.getName(), 0);
		m_model.addInitialAgentPopulation(new MASSPAAgentPop(s_comp2,s_loc1), s_expr2, 2);
	}
	
	@Test(expected=AssertionError.class)
	public void testAddInitialAgentPopulationMissingComponent()
	{
		m_model.addLocation(s_loc1, 2);
		m_model.addInitialAgentPopulation(new MASSPAAgentPop(s_comp2,s_loc1), s_expr2, 2);
	}
	
	@Test(expected=AssertionError.class)
	public void testAddChannelMissingLoc()
	{
		m_model.addLocation(s_loc2, 2);
		m_agents.getConstComponent("",s_comp1.getName(), 0);
		m_agents.getConstComponent("Scope1",s_comp2.getName(), 0);
		m_model.addChannel(s_pop1,s_pop2,s_msg1,s_expr1,0);
	}

	@Test(expected=AssertionError.class)
	public void testAddChannelMissingComponent()
	{
		m_model.addLocation(s_loc1, 2);
		m_model.addLocation(s_loc2, 2);
		m_agents.getConstComponent("",s_comp1.getName(), 0);
		m_model.addChannel(s_pop1,s_pop2,s_msg1,s_expr1,0);
	}
	
	@Test(expected=AssertionError.class)
	public void testAddChannelNullPop()
	{
		m_model.addLocation(s_loc1, 2);
		m_agents.getConstComponent("MyScope",s_comp1.getName(), 0);
		m_model.addChannel(s_pop1,null,s_msg1,s_expr1,0);
	}
	
	@Test(expected=AssertionError.class)
	public void testAddChannelNullMsg()
	{
		m_model.addLocation(s_loc1, 2);
		m_model.addLocation(s_loc2, 2);
		m_agents.getConstComponent("TheScope",s_comp1.getName(), 0);
		m_agents.getConstComponent("AScope",s_comp2.getName(), 0);
		m_model.addChannel(s_pop1,s_pop2,null,s_expr1,0);
	}
	
	@Test(expected=AssertionError.class)
	public void testAddChannelNullIntensity()
	{
		m_model.addLocation(s_loc1, 2);
		m_model.addLocation(s_loc2, 2);
		m_agents.getConstComponent("Scope",s_comp1.getName(), 0);
		m_agents.getConstComponent("",s_comp2.getName(), 0);
		m_model.addChannel(s_pop1,s_pop2,s_msg1,null,0);
	}
	
	@Test
	public void testAddChannel()
	{
		m_model.addLocation(s_loc1, 2);
		m_model.addLocation(s_loc2, 2);
		m_model.addLocation(s_loc3, 2);
		m_agents.addMessage(s_msg1);
		m_agents.addMessage(s_msg2);
		m_agents.getConstComponent("Scope1",s_comp1.getName(), 0);
		m_agents.getConstComponent("Scope2",s_comp2.getName(), 0);
		m_agents.getConstComponent("Scope3",s_comp3.getName(), 0);
		m_model.setChannelType(Messages.s_COMPILER_KEYWORD_MASSACTION, 2);
		m_model.addChannel(s_pop1,s_pop2,s_msg1,s_expr1,0);
		MASSPAChannel chan = m_model.getAllChannels(s_pop2, s_msg1).iterator().next();
		assertEquals(chan.getRateType(),MASSPAChannel.RateType.MASSACTION);
		m_model.setChannelType(Messages.s_COMPILER_KEYWORD_MULTISERVER, 2);
		m_model.addChannel(s_pop1,s_pop3,s_msg1,s_expr1,0);
		chan = m_model.getAllChannels(s_pop3, s_msg1).iterator().next();
		assertEquals(chan.getRateType(),MASSPAChannel.RateType.MULTISERVER);
	}
		
	@Test(expected=AssertionError.class)
	public void testAddMovementMissingLoc()
	{
		m_model.addLocation(s_loc2, 2);
		m_agents.getConstComponent("",s_comp1.getName(), 0);
		m_agents.getConstComponent("Scope1",s_comp2.getName(), 0);
		m_model.addMovement("leave",s_pop1,"enter",s_pop2,s_expr1,0);
	}

	@Test(expected=AssertionError.class)
	public void testAddMovementMissingComponent()
	{
		m_model.addLocation(s_loc1, 2);
		m_model.addLocation(s_loc2, 2);
		m_agents.getConstComponent("",s_comp1.getName(), 0);
		m_model.addMovement("leave",s_pop1,"enter",s_pop2,s_expr1,0);
	}
	
	@Test(expected=AssertionError.class)
	public void testAddMovementNullPop()
	{
		m_model.addLocation(s_loc1, 2);
		m_agents.getConstComponent("MyScope",s_comp1.getName(), 0);
		m_model.addMovement("leave",null,"enter",s_pop2,s_expr1,0);
	}
	
	@Test(expected=AssertionError.class)
	public void testAddMovementNullAction()
	{
		m_model.addLocation(s_loc1, 2);
		m_model.addLocation(s_loc2, 2);
		m_agents.getConstComponent("TheScope",s_comp1.getName(), 0);
		m_agents.getConstComponent("AScope",s_comp2.getName(), 0);
		m_model.addMovement("leave",s_pop1,null,s_pop2,s_expr1,0);
	}
	
	@Test(expected=AssertionError.class)
	public void testAddMovementNullRate()
	{
		m_model.addLocation(s_loc1, 2);
		m_model.addLocation(s_loc2, 2);
		m_agents.getConstComponent("Scope",s_comp1.getName(), 0);
		m_agents.getConstComponent("",s_comp2.getName(), 0);
		m_model.addMovement("leave",s_pop1,"enter",s_pop2,null,0);
	}
	
	// *****************************************************************
	// Model relationship information
	// *****************************************************************
	@Test
	public void testGetNeighbours()
	{
		m_model.addLocation(s_loc1, 2);
		m_model.addLocation(s_loc2, 2);
		m_model.addLocation(s_loc3, 2);
		m_agents.addMessage(s_msg1);
		m_agents.addMessage(s_msg2);
		m_agents.getConstComponent("Scope1",s_comp1.getName(), 0);
		m_agents.getConstComponent("Scope2",s_comp2.getName(), 0);
		m_agents.getConstComponent("Scope3",s_comp3.getName(), 0);
		m_model.addChannel(s_pop1,s_pop2,s_msg1,s_expr1,0);
		m_model.addChannel(s_pop3,s_pop2,s_msg1,s_expr1,0);
		m_model.addChannel(s_pop2,s_pop1,s_msg2,s_expr2,0);
		m_model.generateNeighbours();
		
		HashSet<MASSPAAgentPop> s = new HashSet<MASSPAAgentPop>();
		s.add(s_pop2);
		assertEquals(s,m_model.getNeighbours(s_pop1));
		
		s.clear();
		s.add(s_pop1);
		s.add(s_pop3);
		assertEquals(s,m_model.getNeighbours(s_pop2));
		
		s.clear();
		s.add(s_pop2);
		assertEquals(s,m_model.getNeighbours(s_pop3));
	}
	
}
