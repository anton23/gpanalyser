package uk.ac.imperial.doc.pctmc.postprocessors.numerical;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jfree.data.xy.XYSeriesCollection;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.IntegerExpression;
import uk.ac.imperial.doc.jexpressions.javaoutput.statements.AbstractExpressionEvaluator;
import uk.ac.imperial.doc.jexpressions.statements.AbstractStatement;
import uk.ac.imperial.doc.jexpressions.statements.ArrayDeclaration;
import uk.ac.imperial.doc.jexpressions.statements.ArrayElementAssignment;
import uk.ac.imperial.doc.jexpressions.statements.Comment;
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.analysis.AnalysisUtils;
import uk.ac.imperial.doc.pctmc.analysis.PCTMCAnalysisPostprocessor;
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.PlotDescription;
import uk.ac.imperial.doc.pctmc.charts.PCTMCChartUtilities;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.javaoutput.PCTMCJavaImplementationProvider;
import uk.ac.imperial.doc.pctmc.statements.odeanalysis.EvaluatorMethod;
import uk.ac.imperial.doc.pctmc.utils.FileUtils;

public abstract class NumericalPostprocessor implements PCTMCAnalysisPostprocessor {
	
	protected Map<CombinedPopulationProduct, Integer> momentIndex;

	protected Map<AbstractExpression, Integer> generalExpectationIndex;

	protected double stopTime;
	protected double stepSize;
	 
	public abstract NumericalPostprocessor getNewPreparedPostprocessor(Constants constants);
	
	@Override
	public String toString() {
		return "(stopTime = " + stopTime + ", stepSize = " + stepSize+")";
	}
	
	
	public double getStopTime() {
		return stopTime;
	}

	public double getStepSize() {
		return stepSize;
	}
	
	public int getNumberOfSteps() {
		return (int)Math.floor(stopTime/stepSize);
	}
	

	public void setStopTime(double stopTime) {
		this.stopTime = stopTime;
	}


	public void setStepSize(double stepSize) {
		this.stepSize = stepSize;
	}


	public NumericalPostprocessor(double stopTime, double stepSize) {
		super();
		this.stopTime = stopTime;
		this.stepSize = stepSize;
	}

	protected double[][] dataPoints;
	
	
	@Override
	public void postprocessAnalysis(Constants constants,
			AbstractPCTMCAnalysis analysis,
			List<PlotDescription> plotDescriptions){
		prepare(analysis, constants);
		calculateDataPoints(constants); 
		if (dataPoints!=null){
			results = new HashMap<PlotDescription, double[][]>();
			for (PlotDescription pd:plotDescriptions){
				double[][] data = plotData(analysis.toString(), constants, pd.getExpressions(), pd.getFilename());
				results.put(pd, data);
			}
		}
	}
	
	protected Map<PlotDescription, double[][]> results;
	
	public Map<PlotDescription, double[][]> getResults() {
		return results;
	}


	public double[][] plotData(String analysisTitle,
			Constants constants, List<AbstractExpression> expressions,
			String filename) {
		String[] names = new String[expressions.size()];
		for (int i = 0; i < expressions.size(); i++) {
			names[i] = expressions.get(i).toString();
		}
		double[][] data = evaluateExpressions(expressions, constants);
		XYSeriesCollection dataset = AnalysisUtils.getDatasetFromArray(data,
				stepSize, names);
		PCTMCChartUtilities.drawChart(dataset, "time", "count", "",
				analysisTitle+this.toString());
		if (filename != null && !filename.equals("")) {
			List<String> labels = new LinkedList<String>();
			for (AbstractExpression e : expressions) {
				labels.add(e.toString());
			}
			FileUtils.writeGnuplotFile(filename, "", labels, "time", "count");
			FileUtils.writeCSVfile(filename, dataset);
		}
		return data;
	}

	
	public void prepare(AbstractPCTMCAnalysis analysis, Constants constants){
		momentIndex = analysis.getMomentIndex();
		generalExpectationIndex = analysis.getGeneralExpectationIndex(); 
		dataPoints = null;
	}
	
	public abstract void calculateDataPoints(Constants constants);

	private static String evaluatorClassName = "GeneratedExpressionEvaluator";
	
	/**
	 * Returns an object providing updates to expressions from moment data. 
	 * @param plotExpressions
	 * @param constants
	 * @return
	 */
	public AbstractExpressionEvaluator getExpressionEvaluator(
			final List<AbstractExpression> plotExpressions, Constants constants) {
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
	public double[][] evaluateExpressions(final List<AbstractExpression> plotExpressions,
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
		//evaluator.setRates(constants.getFlatConstants());
		
		double[][] selectedData = new double[dataPoints.length][evaluator.getNumberOfExpressions()];

		for (int t = 0; t < selectedData.length; t++) {
			selectedData[t] = evaluator.update(constants.getFlatConstants(),dataPoints[t], t * stepSize);
		}

		return selectedData;
	}
	
	public int getTimeIndex(double time){
		return (int) Math.floor(time/stepSize);
	}
	
	public double[] evaluateExpressionsAtTimes(AbstractExpressionEvaluator evaluator, double[] times,Constants constants){
		//			evaluator.setRates(constants.getFlatConstants());
		
		double[] selectedData = new double[evaluator.getNumberOfExpressions()];

		for (int e = 0; e<evaluator.getNumberOfExpressions(); e++){
			double[] tmp = evaluator.update(constants.getFlatConstants(),dataPoints[getTimeIndex(times[e])], getTimeIndex(times[e]) * stepSize);
			selectedData[e] = tmp[e];
		}
		
		return selectedData;
	}
	
	public static EvaluatorMethod getEvaluatorMethod(List<AbstractExpression> plotExpressions,
			Constants constants) {
		List<AbstractStatement> body = new LinkedList<AbstractStatement>();
		String returnArray = "ret";
		body.add(new ArrayDeclaration("double", returnArray, new IntegerExpression(
				plotExpressions.size())));
		int iRet = 0;
		for (AbstractExpression plotExpression : plotExpressions) {
			body.add(new Comment(plotExpression.toString()));
			body.add(new ArrayElementAssignment(returnArray, new IntegerExpression(
					iRet), plotExpression));
			iRet++;
		}
		return new EvaluatorMethod(body,plotExpressions.size(),returnArray);
	}
}
