package uk.ac.imperial.doc.masspa.tests.unit.pctmc;

import java.util.HashMap;
import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.masspa.pctmc.MASSPAPCTMC;
import uk.ac.imperial.doc.masspa.representation.components.MASSPAAgents;
import uk.ac.imperial.doc.masspa.representation.model.MASSPAModel;
import uk.ac.imperial.doc.pctmc.representation.EvolutionEvent;
import uk.ac.imperial.doc.pctmc.representation.State;

public class MASSPAPCTMCTest
{
	private MASSPAModel m_model;
	
	@Before
    public void setUp()
    {
		m_model = new MASSPAModel(new MASSPAAgents());
    }
	
	@Test(expected=NullPointerException.class)
	public void testConstructorNullInitPop()
	{
		new MASSPAPCTMC(null, new LinkedList<EvolutionEvent>(), m_model);
	}
	
	@Test(expected=AssertionError.class)
	public void testConstructorNullEvoEvts()
	{
		new MASSPAPCTMC(new HashMap<State,AbstractExpression>(), null, m_model);
	}
	
	@Test(expected=NullPointerException.class)
	public void testConstructorNullModel()
	{
		new MASSPAPCTMC(new HashMap<State,AbstractExpression>(), new LinkedList<EvolutionEvent>(), null);
	}
}
