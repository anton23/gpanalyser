package uk.ac.imperial.doc.pctmc.representation;

import java.util.Map;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.pctmc.representation.accumulations.NamedAccumulation;

public class PCTMCWithAccumulations extends PCTMC {
	
	protected Map<NamedAccumulation, AbstractExpression> accODEs;
	protected Map<NamedAccumulation, AbstractExpression> accInit;

	public PCTMCWithAccumulations(PCTMC pctmc, Map<NamedAccumulation, AbstractExpression> accODEs,
			Map<NamedAccumulation, AbstractExpression> accInit) {
		super(pctmc.getInitMap(), pctmc.getEvolutionEvents());
		this.accODEs = accODEs;		
		this.accInit = accInit;
	}
	
	public Map<NamedAccumulation, AbstractExpression> getAccODEs() {
		return accODEs;
	}

	public Map<NamedAccumulation, AbstractExpression> getAccInit() {
		return accInit;
	}
	
	
	
}
