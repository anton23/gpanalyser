package uk.ac.imperial.doc.masspa.pctmc;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.masspa.language.Messages;
import uk.ac.imperial.doc.masspa.representation.model.MASSPAAgentPop;
import uk.ac.imperial.doc.masspa.representation.model.MASSPAModel;
import uk.ac.imperial.doc.pctmc.representation.EvolutionEvent;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;
import uk.ac.imperial.doc.pctmc.representation.State;

/***
 * This class represents a SPCTMC derived from a MASSPA model.
 * 
 * @author Chris Guenther
 */
public class MASSPAPCTMC extends PCTMC
{
	private MASSPAModel m_model = null;
	
	public MASSPAPCTMC(Map<State, AbstractExpression> _initMap, Collection<EvolutionEvent> _evolutionEvents, MASSPAModel _model)
	{
		super(_initMap, _evolutionEvents);
		if (_evolutionEvents==null) {throw new AssertionError(Messages.s_COMPILER_MASSPA_PCTMC_NULL_EVO_EVTS);}
		m_model = _model;
		m_model.generateNeighbours();
	}

	public MASSPAModel getModel()
	{
		return m_model;
	}

	@SuppressWarnings("unchecked")
	public Set<State> getNeighbours(State _s)
	{
		return (Set<State>) m_model.getNeighbours((MASSPAAgentPop)_s);
	}

	@SuppressWarnings("unchecked")
	public Set<State> getDerivativeStates(State _s)
	{
		return (Set<State>) m_model.getSuccessorPopulations((MASSPAAgentPop)_s);
	}
}
