package scripts.closurecomparison;

import java.util.List;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.javaoutput.statements.AbstractExpressionEvaluator;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.ODEAnalysisNumericalPostprocessor;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.SimulationAnalysisNumericalPostprocessor;

public class ErrorEvaluator {
	
	// ODE analyses
	List<ODEAnalysisNumericalPostprocessor> postprocessors;	
	List<AbstractExpressionEvaluator> evaluators;

	// Simulation	
	SimulationAnalysisNumericalPostprocessor simPostprocessor;
	AbstractExpressionEvaluator simEvaluator;
	
	public ErrorEvaluator(
			List<ODEAnalysisNumericalPostprocessor> postprocessors,
			List<AbstractExpressionEvaluator> evaluators,
			SimulationAnalysisNumericalPostprocessor simPostprocessor,
			AbstractExpressionEvaluator simEvaluator) {
		super();
		this.postprocessors = postprocessors;
		this.evaluators = evaluators;
		this.simPostprocessor = simPostprocessor;
		this.simEvaluator = simEvaluator;
	}

	public ErrorSummary[][] calculateErrors(Constants constants) {		
		simPostprocessor.calculateDataPoints(constants);
		double[][] simValues = simPostprocessor.evaluateExpressions(simEvaluator, constants);
		
		ErrorSummary[][] ret = new ErrorSummary[postprocessors.size()][simValues[0].length];
		
		for (int i = 0; i < postprocessors.size(); i++) {
			ODEAnalysisNumericalPostprocessor postprocessor = postprocessors.get(i);
			postprocessor.calculateDataPoints(constants);
			AbstractExpressionEvaluator evaluator = evaluators.get(i);
			double[][] values = postprocessor.evaluateExpressions(evaluator, constants);
			for (int j = 0; j < simValues[0].length; j++ ) {
				double totalError = 0.0;
				double totalValue = 0.0;
				double maxRelativeError = 0.0;
				double averageError = 0.0; 
				for (int t = 0; t < simValues.length; t++) {
					totalValue += Math.abs(simValues[t][j]);
					totalError += Math.abs(values[t][j] - simValues[t][j]);
					averageError += simValues[t][j] == 0 ? 0.0 : Math.abs(values[t][j] - simValues[t][j])/Math.abs(simValues[t][j]);
					maxRelativeError = Math.max(simValues[t][j] == 0 ? 0.0 : Math.abs(values[t][j] - simValues[t][j])/Math.abs(simValues[t][j]), maxRelativeError);
				}
				ret[i][j] = new ErrorSummary(totalError/totalValue, maxRelativeError, averageError/simValues.length);
			}
		}
		return ret;
	}

}
