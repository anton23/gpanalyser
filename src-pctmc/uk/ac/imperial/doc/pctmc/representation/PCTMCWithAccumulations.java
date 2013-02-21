package uk.ac.imperial.doc.pctmc.representation;

import java.util.Map;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.pctmc.representation.accumulations.NamedAccumulation;
import uk.ac.imperial.doc.pctmc.representation.accumulations.NamedAccumulationUnfolder;

public class PCTMCWithAccumulations extends PCTMC {
	
	protected Map<NamedAccumulation, AbstractExpression> accODEs;
	protected Map<NamedAccumulation, AbstractExpression> accInit;

	public PCTMCWithAccumulations(PCTMC pctmc, Map<NamedAccumulation, AbstractExpression> accODEs,
			Map<NamedAccumulation, AbstractExpression> accInit) {
		super(pctmc.getInitMap(), pctmc.getEvolutionEvents());
		this.accODEs = accODEs;		
		this.accInit = accInit;
		setNamedAccumulations();
	}
	
	protected void setNamedAccumulations() {
		for (Map.Entry<NamedAccumulation, AbstractExpression> e : accODEs.entrySet()) {
			NamedAccumulationUnfolder unfolder = new NamedAccumulationUnfolder(accODEs);
			e.getValue().accept(unfolder);
		}
		for (Map.Entry<NamedAccumulation, AbstractExpression> e : accInit.entrySet()) {
			NamedAccumulationUnfolder unfolder = new NamedAccumulationUnfolder(accODEs);
			e.getValue().accept(unfolder);
		}
		for (EvolutionEvent e : this.evolutionEvents) {
			NamedAccumulationUnfolder unfolder = new NamedAccumulationUnfolder(accODEs);
			e.getRate().accept(unfolder);
		}
	}

	public Map<NamedAccumulation, AbstractExpression> getAccODEs() {
		return accODEs;
	}

	public Map<NamedAccumulation, AbstractExpression> getAccInit() {
		return accInit;
	}
	
	
	
}
