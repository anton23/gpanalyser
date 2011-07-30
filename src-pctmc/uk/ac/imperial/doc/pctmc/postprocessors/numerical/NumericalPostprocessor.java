package uk.ac.imperial.doc.pctmc.postprocessors.numerical;

import java.util.LinkedList;
import java.util.List;

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

import com.google.common.collect.BiMap;

public abstract class NumericalPostprocessor implements PCTMCAnalysisPostprocessor {
	
	protected BiMap<CombinedPopulationProduct, Integer> momentIndex;

	protected BiMap<AbstractExpression, Integer> generalExpectationIndex;

	protected double stopTime;
	protected double stepSize;

	
	
	public double getStopTime() {
		return stopTime;
	}

	public double getStepSize() {
		return stepSize;
	}

	public NumericalPostprocessor(double stopTime, double stepSize) {
		super();
		this.stopTime = stopTime;
		this.stepSize = stepSize;
	}

	protected double[][] dataPoints;

	@Override
	public final void postprocessAnalysis(Constants constants,
			AbstractPCTMCAnalysis analysis,
			List<PlotDescription> plotDescriptions){
		prepare(analysis, constants);
		calculateDataPoints(constants); 
		if (dataPoints!=null){
			for (PlotDescription pd:plotDescriptions){
				plotData(analysis, constants, pd.getExpressions(), pd.getFilename());
			}
		}
	}
	
	public void plotData(AbstractPCTMCAnalysis analysis,
			Constants variables, List<AbstractExpression> expressions,
			String filename) {
		String[] names = new String[expressions.size()];
		for (int i = 0; i < expressions.size(); i++) {
			names[i] = expressions.get(i).toString();
		}
		double[][] data = evaluateExpressions(expressions, variables);
		XYSeriesCollection dataset = AnalysisUtils.getDataset(data,
				stepSize, names);
		PCTMCChartUtilities.drawChart(dataset, "time", "count", "",
				analysis.toString());
		if (!filename.equals("")) {
			List<String> labels = new LinkedList<String>();
			for (AbstractExpression e : expressions) {
				labels.add(e.toString());
			}
			FileUtils.writeGnuplotFile(filename, "", labels, "time", "count");
			FileUtils.writeCSVfile(filename, dataset);
		}
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
