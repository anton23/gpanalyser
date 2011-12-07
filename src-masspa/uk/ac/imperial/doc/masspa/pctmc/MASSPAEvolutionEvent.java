package uk.ac.imperial.doc.masspa.pctmc;

import java.util.List;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.masspa.language.Messages;
import uk.ac.imperial.doc.pctmc.representation.EvolutionEvent;
import uk.ac.imperial.doc.pctmc.representation.State;

/**
 * This Evolution Event behaves like a normal evolution event. The only difference is the
 * ability to specify which part of the rate originates from the MAM-K matrix. This
 * information can be used to apply spatially motivated moment closures. 
 * 
 * @author Chris Guenther
 */
public class MASSPAEvolutionEvent extends EvolutionEvent
{
	private State m_kMatrixContribution=null;

	public MASSPAEvolutionEvent(List<State> _decreasing, List<State> _increasing, AbstractExpression _rate) 
	{
		this(_decreasing, _increasing, _rate,null);
	}

	public MASSPAEvolutionEvent(List<State> _decreasing, List<State> _increasing, AbstractExpression _rate, State _kMatrixContribution) 
	{
		super(_decreasing, _increasing, _rate);
		if (_increasing==null){throw new AssertionError(Messages.s_MASSPA_EVO_EVT_NULL_INCREASING);}
		if (_decreasing==null){throw new AssertionError(Messages.s_MASSPA_EVO_EVT_NULL_DECREASING);}
		if (_rate==null){throw new AssertionError(Messages.s_MASSPA_EVO_EVT_NULL_RATE);}
		m_kMatrixContribution = _kMatrixContribution;
	}
	
	public State getKMatrixContribution() {return m_kMatrixContribution;}
	
	//***************************************
	// Object overwrites
	//***************************************
	@Override
	public String toString()
	{
		return super.toString() + " KMatrixContribution: " + m_kMatrixContribution;
	}
}
