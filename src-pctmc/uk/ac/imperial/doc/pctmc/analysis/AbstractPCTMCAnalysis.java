package uk.ac.imperial.doc.pctmc.analysis;

import java.util.Collection;
import java.util.HashSet;
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
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.PlotExpression;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;
import uk.ac.imperial.doc.pctmc.representation.State;
import uk.ac.imperial.doc.pctmc.statements.odeanalysis.EvaluatorMethod;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/*
 * Abstract class for transient analysis of PCTMCs. 
 */
public abstract class AbstractPCTMCAnalysis {

	public abstract String toString();

	protected Collection<CombinedPopulationProduct> usedCombinedProducts;
	protected Collection<AbstractExpression> usedGeneralExpressions;

	protected PCTMC pctmc;

	protected BiMap<State, Integer> stateIndex;
	protected BiMap<CombinedPopulationProduct, Integer> momentIndex;

	protected BiMap<AbstractExpression, Integer> generalExpectationIndex;

	protected double stopTime;
	protected double stepSize;

	protected double[][] dataPoints;
	
	
	
	public PCTMC getPCTMC() {
		return pctmc;
	}

	
	
	public BiMap<CombinedPopulationProduct, Integer> getMomentIndex() {
		return momentIndex;
	}



	public BiMap<AbstractExpression, Integer> getGeneralExpectationIndex() {
		return generalExpectationIndex;
	}



	/**
	 * Prepares the analysis with given constants. 
	 * @param constants
	 */
	public abstract void prepare(Constants constants);
	
	/**
	 * Returns the explicit step size for data keeping.
	 * @return
	 */
	public double getStepSize() {
		return stepSize;
	}
	
	

	public double getStopTime() {
		return stopTime;
	}

	public AbstractPCTMCAnalysis(PCTMC pctmc, double stopTime, double stepSize) {
		this.pctmc = pctmc;
		this.stopTime = stopTime;
		this.stepSize = stepSize;

		stateIndex = pctmc.getStateIndex();

		momentIndex = HashBiMap.<CombinedPopulationProduct, Integer> create();
		generalExpectationIndex = HashBiMap
				.<AbstractExpression, Integer> create();
		usedCombinedProducts = new HashSet<CombinedPopulationProduct>();
		usedGeneralExpressions = new HashSet<AbstractExpression>();
	}
	
	/**
	 * Sets the moments the analysis has to compute.
	 * @param combinedProducts
	 */
	public void setUsedMoments(
			Collection<CombinedPopulationProduct> combinedProducts) {
		momentIndex = HashBiMap.<CombinedPopulationProduct, Integer> create();
		usedCombinedProducts = new HashSet<CombinedPopulationProduct>();
		int i = 0;
		for (CombinedPopulationProduct p : combinedProducts) {
			usedCombinedProducts.add(p);
			momentIndex.put(p, i++);
		}
	}
	
	/**
	 * Runs the analysis with given constants.
	 * @param constants
	 */
	public abstract void analyse(Constants constants);

	/**
	 * Evaluates given expressions at a given time. 
	 * @param plotExpressions
	 * @param time
	 * @param constants
	 * @return
	 */
	public double[] evaluateExpressionsAtTime(final List<PlotExpression> plotExpressions,
			double time, Constants constants) {
		double[][] data = evaluateExpressions(plotExpressions, constants);
		return data[(int) Math.floor(time / stepSize)];
	}
	
	/**
	 * Returns an object providing updates to expressions from moment data. 
	 * @param plotExpressions
	 * @param constants
	 * @return
	 */
	public AbstractExpressionEvaluator getExpressionEvaluator(
			final List<PlotExpression> plotExpressions, Constants constants) {
		EvaluatorMethod updaterMethod = getEvaluatorMethod(plotExpressions, constants);
		AbstractExpressionEvaluator evaluator = PCTMCTools.getImplementationProvider()
				.getEvaluatorImplementation(updaterMethod, evaluatorClassName,
						constants, momentIndex, generalExpectationIndex);
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
	

	private static String evaluatorClassName = "GeneratedExpressionEvaluator";

	public EvaluatorMethod getEvaluatorMethod(List<PlotExpression> plotExpressions,
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((pctmc == null) ? 0 : pctmc.hashCode());
		long temp;
		temp = Double.doubleToLongBits(stepSize);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(stopTime);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractPCTMCAnalysis other = (AbstractPCTMCAnalysis) obj;
		if (pctmc == null) {
			if (other.pctmc != null)
				return false;
		} else if (!pctmc.equals(other.pctmc))
			return false;
		if (Double.doubleToLongBits(stepSize) != Double
				.doubleToLongBits(other.stepSize))
			return false;
		if (Double.doubleToLongBits(stopTime) != Double
				.doubleToLongBits(other.stopTime))
			return false;
		return true;
	}

}
