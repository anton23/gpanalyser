package uk.ac.imperial.doc.pctmc.experiments.distribution;

import java.util.List;
import java.util.Map;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.SimulationAnalysisNumericalPostprocessor;

public abstract class GroupOfDistributions {
	
	protected String filename;
	protected EmpiricalDistributions distributions;
	protected int nbins;

	
	public GroupOfDistributions(int nbins, String filename) {		
		this.filename = filename;
		this.nbins = nbins;
	}
	
	public abstract void newReplication(double [][] data);
	public abstract void simulationFinished();
	public abstract void prepare(Constants constants, SimulationAnalysisNumericalPostprocessor postprocessor);
	public abstract List<AbstractExpression> getUsedExpressions(Map<ExpressionVariable, AbstractExpression> unfoldedVariables);
	
}
