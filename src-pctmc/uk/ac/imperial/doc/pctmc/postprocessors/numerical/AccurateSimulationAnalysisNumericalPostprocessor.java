package uk.ac.imperial.doc.pctmc.postprocessors.numerical;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.TDistribution;
import org.apache.commons.math.distribution.TDistributionImpl;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.constants.visitors.ExpressionEvaluatorWithConstants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.javaoutput.statements.AbstractExpressionEvaluator;
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.CentralMomentOfLinearCombinationExpression;
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.MeanOfLinearCombinationExpression;
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.PlotDescription;
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.StandardisedCentralMomentOfLinearCombinationExpression;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;
import uk.ac.imperial.doc.pctmc.javaoutput.utils.ClassCompiler;
import uk.ac.imperial.doc.pctmc.simulation.PCTMCSimulation;
import uk.ac.imperial.doc.pctmc.simulation.SimulationUpdater;
import uk.ac.imperial.doc.pctmc.simulation.utils.AccumulatorUpdater;
import uk.ac.imperial.doc.pctmc.simulation.utils.AggregatedStateNextEventGenerator;
import uk.ac.imperial.doc.pctmc.simulation.utils.GillespieSimulator;
import uk.ac.imperial.doc.pctmc.utils.FileUtils;
import uk.ac.imperial.doc.pctmc.utils.PCTMCLogging;

public class AccurateSimulationAnalysisNumericalPostprocessor extends NumericalPostprocessorCI {
	private PCTMCSimulation mSimulation;

	private SimulationUpdater mUpdater;
	private SimulationUpdater mUpdaterNoAdd;
	private AccumulatorUpdater mAccUpdater;
	private AggregatedStateNextEventGenerator mEventGenerator;

	private String mOverrideCode;
	private String mOverrideCodeClassName;

	protected double mCI;
	protected double mMaxRelCIWidth;
	protected int mBatchSize;
	
	protected Map<String, Object> mParameters;

	@Override
	public String toString() {
		return "(stopTime = " + stopTime + ", stepSize = " + stepSize
				+ " CI = " + mCI
				+ ", maxRelCIWidth = " + mMaxRelCIWidth + ", batchSize =" + mBatchSize + ")";
	}

	@Override
	public NumericalPostprocessor getNewPreparedPostprocessor(
			Constants _constants) {
		assert (mSimulation != null);
		AccurateSimulationAnalysisNumericalPostprocessor ret = new AccurateSimulationAnalysisNumericalPostprocessor(
				stopTime, stepSize, mCI, mMaxRelCIWidth, mBatchSize, mParameters);
		ret.prepare(mSimulation, _constants);
		return ret;
	}

	public AccurateSimulationAnalysisNumericalPostprocessor(double _stopTime,
			double _stepSize, double _ci, double _maxRelCIWidth, int _batchSize) {
		super(_stopTime, _stepSize);
		mCI = _ci;
		mMaxRelCIWidth = _maxRelCIWidth;
		mBatchSize = _batchSize;
	}

	public AccurateSimulationAnalysisNumericalPostprocessor(double _stopTime,
			double _stepSize, double _ci, double _maxRelCIWidth, int _batchSize, Map<String, Object> _parameters) {
		this(_stopTime, _stepSize, _ci, _maxRelCIWidth, _batchSize);
		mParameters = _parameters;
		if (mParameters != null && mParameters.containsKey("overrideCode")) {
			Object value = mParameters.get("overrideCode");
			if (value instanceof String) {
				String asString = ((String) value);
				try {
					mOverrideCode = FileUtils.readFile(asString);
					String[] split = asString.split("/");
					mOverrideCodeClassName = split[split.length - 1].replace(
							".java", "");
				} catch (IOException e) {
					throw new AssertionError("File + " + asString
							+ " cannot be open!");
				}

			} else {
				throw new AssertionError(
						"Given value of 'overrideCode' has to be a filename!");
			}
		}
	}

	@Override
	public void prepare(AbstractPCTMCAnalysis _analysis, Constants _constants) {
		super.prepare(_analysis, _constants);
		mSimulation = null;
		if (_analysis instanceof PCTMCSimulation) {
			mSimulation = (PCTMCSimulation) _analysis;
			dataPoints = new double[(int) Math.ceil(stopTime / stepSize)][momentIndex
					.size() + generalExpectationIndex.size()];
			mUpdater = (SimulationUpdater) ClassCompiler
					.getInstance(SimulationAnalysisNumericalPostprocessor.getProductUpdaterCode(mSimulation, _constants, false),
							SimulationAnalysisNumericalPostprocessor.updaterClassName);
			mUpdaterNoAdd = (SimulationUpdater) ClassCompiler.getInstance(
					SimulationAnalysisNumericalPostprocessor.getProductUpdaterCode(mSimulation, _constants, true),
					SimulationAnalysisNumericalPostprocessor.mUpdaterNoAddClassName);
			mAccUpdater = (AccumulatorUpdater) ClassCompiler.getInstance(
					SimulationAnalysisNumericalPostprocessor.getAccumulatorUpdaterCode(mSimulation, _constants),
					SimulationAnalysisNumericalPostprocessor.accumulatorUpdaterName);

			PCTMCLogging.info("Generating one step generator.");
			PCTMCLogging.increaseIndent();
			mEventGenerator = getEventGenerator(_constants);
			PCTMCLogging.decreaseIndent();
		}
	}

	protected AggregatedStateNextEventGenerator getEventGenerator(
			Constants constants) {
		String code;
		String className;
		if (mOverrideCode == null) {
			code = SimulationAnalysisNumericalPostprocessor.getEventGeneratorCode(mSimulation, constants);
			className = SimulationAnalysisNumericalPostprocessor.generatorName;
		} else {
			code = mOverrideCode;
			className = mOverrideCodeClassName;
		}

		return (AggregatedStateNextEventGenerator) ClassCompiler.getInstance(
				code, className);
	}

	@Override
	public void calculateDataPoints(Constants constants) {
		if (mSimulation != null) {
			simulate(constants);
			setResults(constants, plotDescriptions);
		}
	}

	private double[] initial;

	private void simulate(Constants constants)
	{
		mEventGenerator.setRates(constants.getFlatConstants());

		int n = mSimulation.getPCTMC().getStateIndex().size();
		initial = new double[n];
		for (int i = 0; i < n; i++)
		{
			ExpressionEvaluatorWithConstants evaluator = new ExpressionEvaluatorWithConstants(constants);
			mSimulation.getPCTMC().getInitCounts()[i].accept(evaluator);
			initial[i] = evaluator.getResult();
		}

		PCTMCLogging.info("Running Gillespie simulator.");
		PCTMCLogging.increaseIndent();

		mUpdater.setRates(constants.getFlatConstants());
		mAccUpdater.setRates(constants.getFlatConstants());
		mUpdaterNoAdd.setRates(constants.getFlatConstants());

		Map<Integer,CombinedPopulationProduct> momentIndexInverse = new HashMap<Integer,CombinedPopulationProduct>();
		int ind=0;
		for (CombinedPopulationProduct cpp : momentIndex.keySet())
		{
			momentIndexInverse.put(ind++, cpp);
		}
		
		// Repeat simulation until moments are sufficiently accurate
		int reps = 0;
		double[][] tmp;
		double[] curDataSamples = new double[dataPoints[0].length];
		double[][][] incCentralMoment = new double[dataPoints.length][dataPoints[0].length][4];
		boolean accurate = false;
		final int timeSteps = (int) Math.ceil(stopTime / stepSize);
		double normalisedHalfCIWidth = 1.96;
		
		while (!accurate)
		{
			for (int r = 0; r < mBatchSize; ++r)
			{
				if (reps > 0 && reps % (mBatchSize / 5 > 0 ? mBatchSize / 5 : 1) == 0)
				{
					PCTMCLogging.info(reps + " replications finished.");
				}
				tmp = GillespieSimulator.simulateAccumulated(mEventGenerator,initial, stopTime, stepSize, mAccUpdater);
				for (int t = 0; t < timeSteps; t++)
				{
					mUpdater.update(dataPoints[t], tmp[t]);
					mUpdaterNoAdd.update(curDataSamples, tmp[t]);

					double n0 = reps;
					double n1 = reps+1;
					for (int i=0; i<dataPoints[0].length; i++)
					{
						/** Online approximations of the first 4 central moments from the population samples
						 *  see @link{http://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Higher-order_statistics}*/
						double delta = curDataSamples[i] - incCentralMoment[t][i][0];
						double delta_n = delta / n1;
						double delta_n2 = delta_n * delta_n;
						double term1 = delta * delta_n * n0;
						incCentralMoment[t][i][0] += delta_n;
						incCentralMoment[t][i][3] += term1 * delta_n2 * (n1*n1 - 3*n1 + 3) + 6 * delta_n2 * incCentralMoment[t][i][1] - 4 * delta_n * incCentralMoment[t][i][2];
						incCentralMoment[t][i][2] += term1 * delta_n * (n1 - 2) - 3 * delta_n * incCentralMoment[t][i][1];
						incCentralMoment[t][i][1] += term1;
					}
				}
				reps++;
			}
			
			// Compute confidence interval for sample mean and variance
			TDistribution tDist = new TDistributionImpl(reps-1);
			double maxRelHalfCIChange = 0;
			normalisedHalfCIWidth = 1.96;
			try
			{
				normalisedHalfCIWidth = tDist.inverseCumulativeProbability(0.5+mCI/2);
			}
			catch (MathException e) {
				e.printStackTrace();
			}
			for (int t = 0; t < timeSteps; t++)
			{
				for (int i=0; i<dataPoints[0].length; i++)
				{
					CombinedPopulationProduct cpp = momentIndexInverse.get(i);
					if (cpp.getOrder() == 1)
					{
						cpp=CombinedPopulationProduct.getProductOf(cpp, cpp);
						Integer index = momentIndex.get(cpp);
						if (index == null)
						{
							maxRelHalfCIChange = Math.max(maxRelHalfCIChange,getRelCIWidthMean(incCentralMoment[t][i], normalisedHalfCIWidth, reps));
						}
						else
						{
							maxRelHalfCIChange = Math.max(maxRelHalfCIChange,getRelCIWidthVar(incCentralMoment[t][i], normalisedHalfCIWidth, reps));
						}
					}
				}
			}
			
			PCTMCLogging.info("Max half relative "+mCI*100+"%-CI width: " + maxRelHalfCIChange + ", convergence threshold <" + mMaxRelCIWidth);
			if (maxRelHalfCIChange < mMaxRelCIWidth)
			{
				accurate=true;
			}
		}
		
		// Moment expectations
		for (int t = 0; t < timeSteps; t++)
		{
			for (int i=0; i<dataPoints[0].length; i++)
			{
				dataPoints[t][i] /= reps;
			}
		}
		
		// Compute Confidence intervals for expressions (if possible)
		// Currently we only support mean, var and stddev expressions
		absHalfCIWidth = new double[plotDescriptions.size()][timeSteps][];
		int i1 = 0;
		for (PlotDescription pd : plotDescriptions)
		{
			AbstractExpressionEvaluator evals = getExpressionEvaluator(pd.getExpressions(), constants);
			for (int t=0; t < timeSteps; t++)
			{
				absHalfCIWidth[i1][t] = new double[pd.getExpressions().size()];
				double[] values = this.evaluateExpressions(evals,dataPoints[t],t,constants);
				int i2 = 0;
				for (AbstractExpression ae : pd.getExpressions())
				{
					absHalfCIWidth[i1][t][i2] = 0;
					// TODO this work should be done by an expression visitor
					//      rather than by a number of if, else if blocks 
					if (ae instanceof MeanOfLinearCombinationExpression)
					{
						MeanOfLinearCombinationExpression m = (MeanOfLinearCombinationExpression)ae;
						if (m.getCoefficients().size() == 1 && m.getCombinedProducts().size() == 1)
						{
							CombinedPopulationProduct cpp = m.getCombinedProducts().get(0);
							int index = momentIndex.get(cpp);
							double relCIWidth = getRelCIWidthMean(incCentralMoment[t][index], normalisedHalfCIWidth, reps);
							absHalfCIWidth[i1][t][i2] = values[i2]*(relCIWidth);
						}
					}
					else if (ae instanceof CentralMomentOfLinearCombinationExpression)
					{
						CentralMomentOfLinearCombinationExpression cm = (CentralMomentOfLinearCombinationExpression)ae;
						if (cm.getOrder() == 2 && cm.getOriginalExpression() instanceof CombinedProductExpression)
						{
							CombinedPopulationProduct cpp = ((CombinedProductExpression)cm.getOriginalExpression()).getProduct();
							int index = momentIndex.get(cpp);
							double relCIWidth = getRelCIWidthVar(incCentralMoment[t][index], normalisedHalfCIWidth, reps);
							absHalfCIWidth[i1][t][i2] = values[i2]*(relCIWidth);
						}
					}
					else if (ae instanceof StandardisedCentralMomentOfLinearCombinationExpression)
					{
						StandardisedCentralMomentOfLinearCombinationExpression scm = (StandardisedCentralMomentOfLinearCombinationExpression)ae;
						if (scm.getOrder() == 2 && scm.getOriginalExpression() instanceof CombinedProductExpression)
						{
							CombinedPopulationProduct cpp = ((CombinedProductExpression)scm.getOriginalExpression()).getProduct();
							int index = momentIndex.get(cpp);
							double relCIWidth = Math.sqrt(getRelCIWidthVar(incCentralMoment[t][index], normalisedHalfCIWidth, reps)+1)-1;
							absHalfCIWidth[i1][t][i2] = values[i2]*(relCIWidth);
						}
					}
					i2++;
				}
			}
			i1++;
		}
		PCTMCLogging.decreaseIndent();
	}

	private double getRelCIWidthMean(final double[] _moments, final double _ci, final double _n)
	{
		double sampleMean = _moments[0];
		double sampleVar  = _moments[1]/(_n-1);
		
		// Mean confidence interval
		if (sampleMean > 1 && sampleVar > 1)
		{		
			return _ci * Math.sqrt(sampleVar/_n)/ sampleMean;
		}
			
		return 0;
	}
	
	private double getRelCIWidthVar(final double[] _moments, final double _ci, final double _n)
	{
		double sampleMean = _moments[0];
		double sampleVar  = _moments[1]/(_n-1);
		double sample4thCM = _moments[3]/(_n-3);
		double reps1Div=(_n-1)/_n;
		double reps3Div=(_n-3)/_n;
		double term1 = reps1Div*reps1Div*sample4thCM;
		double term2 = reps1Div*reps3Div*sampleVar*sampleVar;
		double distSqSampleVar = (term1 - term2);
		
		// Mean confidence interval
		// Squared Dist confidence interval
		if (sampleMean > 1 && sampleVar > 1)
		{
			return Math.max(_ci * Math.sqrt(sampleVar/_n)/ sampleMean,
							_ci * Math.sqrt(distSqSampleVar/_n)/ sampleVar);
		}
		return 0;
	}
}
