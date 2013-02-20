package uk.ac.imperial.doc.pctmc.representation;

import java.util.Collection;
import java.util.Map;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.pctmc.representation.accumulations.AccumulationVariable;

public class PCTMCwithAccumulations extends PCTMC {
	
	protected Map<AccumulationVariable, AbstractExpression> accODEs;

	public PCTMCwithAccumulations(Map<State, AbstractExpression> initMap,
			Collection<EvolutionEvent> evolutionEvents, Map<AccumulationVariable, AbstractExpression> accODEs) {
		super(initMap, evolutionEvents);

	}

}
