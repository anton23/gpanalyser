package uk.ac.imperial.doc.masspa.tests.unit.representation.components;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import uk.ac.imperial.doc.masspa.representation.components.AnyComponent;
import uk.ac.imperial.doc.masspa.representation.components.ConstComponent;
import uk.ac.imperial.doc.masspa.representation.components.MASSPAComponent;
import uk.ac.imperial.doc.masspa.representation.components.MASSPAAgents;
import uk.ac.imperial.doc.masspa.representation.components.MASSPAMessage;
import uk.ac.imperial.doc.masspa.representation.components.Prefix;
import uk.ac.imperial.doc.masspa.representation.components.StopComponent;

public class MASSPAAgentsTest extends MASSPAComponentTestUtil
{
	private MASSPAAgents m_fact;
	
    @Before
    public void setUp()
    {
    	m_fact=new MASSPAAgents();
    }
	
	// *****************************************************************
	// Actions
	// *****************************************************************
    @Test
    public void testGetActions()
    {
    	assertEquals(0,m_fact.getActions().size());
    }
    
    @Test
    public void testGetActions2()
    {
    	m_fact.addAction("action1");
    	m_fact.addAction("action2");
    	m_fact.addAction(null);
    	assertEquals(2,m_fact.getActions().size());
    }
    
    @Test(expected=UnsupportedOperationException.class)
    public void testGetActionsModifyFail()
    {
    	Set<String> s = m_fact.getActions();
    	s.add("test");
    }
    
	// *****************************************************************
	// Messages
	// *****************************************************************
    @Test
    public void testGetMessages1()
    {
    	assertEquals(0,m_fact.getMessages().size());
    }
    
    @Test
    public void testGetMessages2()
    {
    	m_fact.addMessage(new MASSPAMessage("msg1"));
    	m_fact.addMessage(new MASSPAMessage("msg1"));
    	m_fact.addMessage(new MASSPAMessage("msg2"));
    	m_fact.addMessage((MASSPAMessage)null);
    	assertEquals(2,m_fact.getMessages().size());
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testGetMessagesModifyFail()
    {
    	Set<MASSPAMessage> s = m_fact.getMessages();
    	s.add(new MASSPAMessage("test"));
    }
    
    @Test
    public void testGetMessage()
    {
    	MASSPAMessage m1 = new MASSPAMessage("msg1");
    	MASSPAMessage m2 = new MASSPAMessage("msg2");
    	m_fact.addMessage(m1);
    	m_fact.addMessage(m2);
    	
    	// Cache m1
    	assertNotNull(m_fact.getMessage(m1));
    	assertTrue(m_fact.getMessage(m1.getMsg()) == m1);
    	
    	// Cache new msg with name "msg2"
    	assertNotNull(m_fact.getMessage(m2.getMsg()));
    	assertTrue(m_fact.getMessage(m2) == m2);
    }
    
    @Test
    public void testGetMessageNull()
    {
    	assertNull(m_fact.getMessage((String)null));
    	assertNull(m_fact.getMessage((MASSPAMessage)null));
    }
    
	// *****************************************************************
	// Components
	// *****************************************************************
    @Test
    public void testGetComponentsEmpty()
    {
    	assertEquals(2,m_fact.getComponents().size());
    }
    
    @Test(expected=UnsupportedOperationException.class)
    public void testGetComponentsModifyFail()
    {
    	Collection<MASSPAComponent> c = m_fact.getComponents();
    	c.add(new StopComponent());
    }
    
    @Test
    public void testGetComponent()
    {
    	assertNotNull(m_fact.getComponent(new StopComponent().getName()));
    	assertNotNull(m_fact.getComponent(new AnyComponent().getName()));
    	assertNull(m_fact.getComponent("IdontExist"));
    	assertNull(m_fact.getComponent(null));
    }
    
    @Test
    public void testGetConstComponent1()
    {
    	ConstComponent c = (ConstComponent) m_fact.getConstComponent("Scope1","TestComp", -1);
    	assertTrue(c ==  m_fact.getConstComponent("Scope1","TestComp", -1));
    }
    
    @Test(expected=AssertionError.class)
    public void testGetConstComponent2()
    {
    	m_fact.getConstComponent("Scope1","TestComp", -1);
    	m_fact.getConstComponent("Scope2","TestComp", -1);
    }
    
    @Test
    public void testGetConstComponentNullScope()
    {
    	assertNull(m_fact.getConstComponent(null,"TestComp", -1));
    }
    
    @Test
    public void testGetConstComponentNullName()
    {
    	assertNull(m_fact.getConstComponent("Scope2",null, -1));
    }
    
    @Test(expected=AssertionError.class)
    public void testGetConstComponentStopComponent1()
    {
    	m_fact.getConstComponent("",new StopComponent().getName(), -1);
    }
    
    @Test(expected=AssertionError.class)
    public void testGetConstComponentStopComponent2()
    {
    	m_fact.getConstComponent("Scope1",new StopComponent().getName(), -1);
    }
    
    @Test(expected=AssertionError.class)
    public void testGetConstComponentAnyComponent1()
    {
    	m_fact.getConstComponent("",new AnyComponent().getName(), -1);
    }
    
    @Test(expected=AssertionError.class)
    public void testGetConstComponentAnyComponent2()
    {
    	m_fact.getConstComponent("Scope2",new AnyComponent().getName(), -1);
    }
    
    @Test
    public void testGetChoiceComponent()
    {
    	assertEquals(s_choice1,m_fact.getChoiceComponent(new ArrayList<Prefix>(Arrays.asList(s_prefixes1))));
    	assertEquals(s_choice2,m_fact.getChoiceComponent(new ArrayList<Prefix>(Arrays.asList(s_prefixes2))));
    }
    
    @Test
    public void testGetChoiceComponentNull()
    {
    	assertNull(m_fact.getChoiceComponent(null));
    }
    
    @Test
    public void testGetAnyComponent()
    {
    	assertNotNull(m_fact.getAnyComponent());
    	assertTrue(m_fact.getAnyComponent()==m_fact.getAnyComponent());
    }
    
    @Test
    public void testGetStopComponent()
    {
    	assertNotNull(m_fact.getStopComponent());
    	assertTrue(m_fact.getStopComponent()==m_fact.getStopComponent());
    }
    
    @Test
    public void testGetAgents()
    {
       	m_fact.getConstComponent("Scope2",s_comp1.getName(),2);
       	m_fact.getConstComponent("Scope2",s_comp2.getName(),2);
    	m_fact.getConstComponent("Scope1",s_comp4.getName(),3);
    	
    	Multimap<String, MASSPAComponent> mm = HashMultimap.create();
    	mm.put("Scope2", s_comp1);
    	mm.put("Scope2", s_comp2);
    	mm.put("Scope1", s_comp4);
    	assertEquals(mm,m_fact.getAgents());    	
    }
}
