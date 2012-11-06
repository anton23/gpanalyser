package uk.ac.imperial.doc.pctmc.condor;

import java.util.Map;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
import uk.ac.imperial.doc.pctmc.experiments.iterate.PCTMCExperiment;
import uk.ac.imperial.doc.pctmc.experiments.iterate.PCTMCIterate;
import uk.ac.imperial.doc.pctmc.interpreter.PCTMCFileRepresentation;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;

public class CondorBase {
	protected Constants constants;
	protected PCTMC pctmc;
	protected Map<ExpressionVariable, AbstractExpression> unfoldedVariables;
	protected PCTMCIterate iterate;
	protected String file;
	protected String options;

	public CondorBase(PCTMCFileRepresentation fileRepresentation, String file, String options) {
		this.constants = fileRepresentation.getConstants();
		this.pctmc = fileRepresentation.getPctmc();
		
		this.unfoldedVariables = fileRepresentation.getUnfoldedVariables();
		this.file = file;
		for (PCTMCExperiment e : fileRepresentation.getExperiments()) {
			if (e instanceof PCTMCIterate) {
				iterate = (PCTMCIterate) e;
			}
		}
		if (iterate == null) {
			throw new AssertionError("At least one iterate has to be present!");
		}
		this.options = options;
}
}
