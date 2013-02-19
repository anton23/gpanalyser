package uk.ac.imperial.doc.gpa.testing.quantitative;


import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.javaoutput.statements.AbstractExpressionEvaluator;
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.PlotDescription;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.NumericalPostprocessor;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.NumericalPostprocessorCI;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.ODEAnalysisNumericalPostprocessor;

public class ErrorEvaluator {
	
	// ODE analyses
	List<ODEAnalysisNumericalPostprocessor> postprocessors;	
	List<AbstractExpressionEvaluator> evaluators;

	// Simulation	
	NumericalPostprocessor simPostprocessor;
	AbstractExpressionEvaluator simEvaluator;
	
	public ErrorEvaluator(
			List<ODEAnalysisNumericalPostprocessor> postprocessors,
			List<AbstractExpressionEvaluator> evaluators,
			NumericalPostprocessor simPostprocessor,
			AbstractExpressionEvaluator simEvaluator) {
		super();
		this.postprocessors = postprocessors;
		this.evaluators = evaluators;
		this.simPostprocessor = simPostprocessor;
		this.simEvaluator = simEvaluator;
	}
	
	public ErrorEvaluator(List<ODEAnalysisNumericalPostprocessor> postprocessors,
						NumericalPostprocessor simPostprocessor,
						  List<AbstractExpression> expressions, Constants constants) {
		this(postprocessors, getEvaluators(postprocessors, expressions, constants),
				simPostprocessor, simPostprocessor.getExpressionEvaluator(expressions,
				constants));
	}
	
	public static List<AbstractExpressionEvaluator>
		getEvaluators(List<ODEAnalysisNumericalPostprocessor> postprocessors, List<AbstractExpression> expressions, Constants constants) {
		List<AbstractExpressionEvaluator> evaluators = new LinkedList<AbstractExpressionEvaluator>();
		for (int i = 0; i < postprocessors.size(); i++) {
			evaluators.add(postprocessors.get(i).getExpressionEvaluator(
					expressions, constants));
		}
		return evaluators;
	}

	ErrorSummary[][] accumulatedErrors;
	double[][][] transientErrors;
	
	public void calculateErrors(Constants constants) {		
		simPostprocessor.calculateDataPoints(constants);
		double[][] simValues = simPostprocessor.evaluateExpressions(simEvaluator, constants);
		double[][] simCI = null;
		if (simPostprocessor instanceof NumericalPostprocessorCI) {
			// Retreives the CI data. This is quite messy and relies on preserved ordering of PlotDescriptions
			// in the resultsCI map.
			Map<PlotDescription, double[][]> resultsCI = ((NumericalPostprocessorCI) simPostprocessor).getResultsCI();
			simCI = new double[simValues.length][simValues[0].length];
			int i = 0;
			for (Entry<PlotDescription, double[][]> e : resultsCI.entrySet()) {			
				for (int j = 0; j < e.getValue()[0].length; j++){
					for (int t = 0; t < e.getValue().length; t++) {
						simCI[t][i] = e.getValue()[t][j];
					}
					i++;
				}				
			}		
		}
		accumulatedErrors = new ErrorSummary[postprocessors.size()][simValues[0].length];
		transientErrors = new double[postprocessors.size()][simValues.length][simValues[0].length];

		for (int i = 0; i < postprocessors.size(); i++) {
			double[][] transientError = new double[simValues.length][simValues[0].length];
			ODEAnalysisNumericalPostprocessor postprocessor = postprocessors.get(i);
			postprocessor.calculateDataPoints(constants);
			AbstractExpressionEvaluator evaluator = evaluators.get(i);
			double[][] values = postprocessor.evaluateExpressions(evaluator, constants);
			for (int j = 0; j < simValues[0].length; j++ ) {
				double totalAccError = 0.0;
				double totalAccValue = 0.0;
				double maxRelativeAccError = 0.0;
				double averageAccError = 0.0; 
				for (int t = 0; t < simValues.length; t++) {
					totalAccValue += Math.abs(simValues[t][j]);
					double absError = Math.abs(values[t][j] - simValues[t][j]);
					if (simCI != null) {
						double ci = simCI[t][j];
						if (values[t][j] < simValues[t][j] - ci) {
							absError =  (simValues[t][j] - ci) - values[t][j];
						} else 
						if (values[t][j] > simValues[t][j] + ci) {
							absError = values[t][j] - (simValues[t][j] + ci);
						} else {
							absError = 0.0;
						}
					}
					totalAccError += absError;
					averageAccError += simValues[t][j] == 0 ? 0.0 : absError/Math.abs(simValues[t][j]);
					maxRelativeAccError = Math.max(simValues[t][j] == 0 ? 0.0 : absError/Math.abs(simValues[t][j]), maxRelativeAccError);

					transientError[t][j] = simValues[t][j] == 0 ? 0.0 : absError/Math.abs(simValues[t][j]);
				}
				accumulatedErrors[i][j] = new ErrorSummary(totalAccError/totalAccValue, maxRelativeAccError, averageAccError/simValues.length);
			}
			transientErrors[i] = transientError;
		}
	}
	
	public double[][][] getTransientErrors() {
		return transientErrors;
	}

	public ErrorSummary[][] getAccumulatedErrors() {
		return accumulatedErrors;
	}

	public static String printSummary(ErrorSummary[][] errors) {
		StringBuilder out = new StringBuilder();
		DecimalFormat df = new DecimalFormat("#.##");
		for (int i = 0; i < errors.length; i++) {
			out.append("Analysis " + i + "\n");
			for (int j = 0; j < errors[0].length; j++) {				
				out.append(j
								+ "\t acc: "
								+ df.format(errors[i][j]
										.getRelativeAccumulated() * 100.0)
								+ "\t max: "
								+ df
										.format(errors[i][j].getMaxRelative() * 100.0)
								+ "\t avg: "
								+ df
										.format(errors[i][j]
												.getAverageRelative() * 100.0)+"\n");
			}
		}
		return out.toString();
	}

}
