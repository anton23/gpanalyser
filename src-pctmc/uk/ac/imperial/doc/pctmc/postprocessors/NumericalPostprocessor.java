package uk.ac.imperial.doc.pctmc.postprocessors;

import java.util.LinkedList;
import java.util.List;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.IntegerExpression;
import uk.ac.imperial.doc.jexpressions.javaoutput.statements.AbstractExpressionEvaluator;
import uk.ac.imperial.doc.jexpressions.statements.AbstractStatement;
import uk.ac.imperial.doc.jexpressions.statements.ArrayDeclaration;
import uk.ac.imperial.doc.jexpressions.statements.ArrayElementAssignment;
import uk.ac.imperial.doc.jexpressions.statements.Comment;
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.analysis.PCTMCAnalysisPostprocessor;
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.PlotDescription;
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.PlotExpression;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.javaoutput.PCTMCJavaImplementationProvider;
import uk.ac.imperial.doc.pctmc.statements.odeanalysis.EvaluatorMethod;

import com.google.common.collect.BiMap;

public class NumericalPostprocessor implements PCTMCAnalysisPostprocessor {
	
	protected BiMap<CombinedPopulationProduct, Integer> momentIndex;

	protected BiMap<AbstractExpression, Integer> generalExpectationIndex;

	protected double stopTime;
	protected double stepSize;

	protected double[][] dataPoints;


	@Override
	public void postprocessAnalysis(Constants constants,
			AbstractPCTMCAnalysis analysis,
			List<PlotDescription> plotDescriptions){
		momentIndex = analysis.getMomentIndex();
		generalExpectationIndex = analysis.getGeneralExpectationIndex(); 
		stopTime = analysis.getStopTime(); 
		stepSize = analysis.getStepSize(); 		
	}

	private static String evaluatorClassName = "GeneratedExpressionEvaluator";
	
	/**
	 * Returns an object providing updates to expressions from moment data. 
	 * @param plotExpressions
	 * @param constants
	 * @return
	 */
	public AbstractExpressionEvaluator getExpressionEvaluator(
			final List<PlotExpression> plotExpressions, Constants constants) {
		EvaluatorMethod updaterMethod = getEvaluatorMethod(plotExpressions, constants);
		AbstractExpressionEvaluator evaluator = new PCTMCJavaImplementationProvider()
				.getEvaluatorImplementation(updaterMethod, evaluatorClassName,
						constants, momentIndex,generalExpectationIndex);
		return evaluator;
	}

	/**
	 * Evaluates given expressions at each time point in range.
	 * @param plotExpressions
	 * @param constants
	 * @return
	 */
	public double[][] evaluateExpressions(final List<PlotExpression> plotExpressions,
			Constants constants) {
		AbstractExpressionEvaluator evaluator = getExpressionEvaluator(plotExpressions, constants); 
		return evaluateExpressions(evaluator,constants); 
	}
	
	/**
	 * Evaluate expressions with a specified evaluator.
	 * @param evaluator
	 * @param plotExpressions
	 * @param constants
	 * @return
	 */
	public double[][] evaluateExpressions(AbstractExpressionEvaluator evaluator, Constants constants){
		evaluator.setRates(constants.getFlatConstants());
		
		double[][] selectedData = new double[dataPoints.length][evaluator.getNumberOfExpressions()];

		for (int t = 0; t < selectedData.length; t++) {
			selectedData[t] = evaluator.update(dataPoints[t], t * stepSize);
		}

		return selectedData;
	}
	
	public int getTimeIndex(double time){
		return (int) Math.floor(time/stepSize);
	}
	
	public double[] evaluateExpressionsAtTimes(AbstractExpressionEvaluator evaluator, double[] times,Constants constants){
		evaluator.setRates(constants.getFlatConstants());
		
		double[] selectedData = new double[evaluator.getNumberOfExpressions()];

		for (int e = 0; e<evaluator.getNumberOfExpressions(); e++){
			double[] tmp = evaluator.update(dataPoints[getTimeIndex(times[e])], getTimeIndex(times[e]) * stepSize);
			selectedData[e] = tmp[e];
		}
		
		return selectedData;
	}
	
	public static EvaluatorMethod getEvaluatorMethod(List<PlotExpression> plotExpressions,
			Constants constants) {
		List<AbstractStatement> body = new LinkedList<AbstractStatement>();
		String returnArray = "ret";
		body.add(new ArrayDeclaration("double", returnArray, new IntegerExpression(
				plotExpressions.size())));
		int iRet = 0;
		for (PlotExpression plotExpression : plotExpressions) {
			body.add(new Comment(plotExpression.toString()));
			body.add(new ArrayElementAssignment(returnArray, new IntegerExpression(
					iRet), plotExpression.getExpression()));
			iRet++;
		}
		return new EvaluatorMethod(body,plotExpressions.size(),returnArray);
	}

	
}
