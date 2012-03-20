package uk.ac.imperial.doc.pctmc.postprocessors.numerical;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.TDistribution;
import org.apache.commons.math.distribution.TDistributionImpl;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.constants.visitors.ExpressionEvaluatorWithConstants;
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
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

		int maxMomentOrder = 1;
		for (CombinedPopulationProduct cpp : momentIndex.keySet())
		{
			maxMomentOrder = Math.max(maxMomentOrder, cpp.getOrder());
		}
		
		// Repeat simulation until moments are sufficiently accurate
		int reps = 0;
		double[][] tmp;
		double[] curDataSamples = new double[dataPoints[0].length];
		double[][][] incMoment = new double[dataPoints.length][dataPoints[0].length][4];
		boolean accurate = false;

		while (!accurate)
		{
			for (int r = 0; r < mBatchSize; ++r)
			{
				if (reps > 0 && reps % (mBatchSize / 5 > 0 ? mBatchSize / 5 : 1) == 0)
				{
					PCTMCLogging.info(reps + " replications finished.");
				}
				tmp = GillespieSimulator.simulateAccumulated(mEventGenerator,initial, stopTime, stepSize, mAccUpdater);
				for (int t = 0; t < (int) Math.ceil(stopTime / stepSize); t++)
				{
					mUpdater.update(dataPoints[t], tmp[t]);
					mUpdaterNoAdd.update(curDataSamples, tmp[t]);
					
					double n0 = reps;
					double n1 = reps+1;
					for (int i=0; i<dataPoints[0].length; i++)
					{
						/** see @link{http://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Higher-order_statistics}*/
						double delta = curDataSamples[i] - incMoment[t][i][0];
						double delta_n = delta / n1;
						double delta_n2 = delta_n * delta_n;
						double term1 = delta * delta_n * n0;
						incMoment[t][i][0] += delta_n;
						incMoment[t][i][3] += term1 * delta_n2 * (n1*n1 - 3*n1 + 3) + 6 * delta_n2 * incMoment[t][i][1] - 4 * delta_n * incMoment[t][i][2];
						incMoment[t][i][2] += term1 * delta_n * (n1 - 2) - 3 * delta_n * incMoment[t][i][1];
						incMoment[t][i][1] += term1;
						curDataSamples[i]=0;
					}
				}
				reps++;
			}
			
			// Compute confidence interval for sample mean and variance
			TDistribution tDist = new TDistributionImpl(reps-1);
			double maxRelCIChange = 0;
			double ci = 1.96;
			try
			{
				ci = tDist.inverseCumulativeProbability(0.5+mCI/2);
			}
			catch (MathException e) {
				e.printStackTrace();
			}
			for (int t = 0; t < (int) Math.ceil(stopTime / stepSize); t++)
			{
				for (int i=0; i<dataPoints[0].length; i++)
				{
					/** see @link{ http://mathworld.wolfram.com/SampleVarianceDistribution.html}*/
					double sampleMean = incMoment[t][i][0];
					double sampleVar  = incMoment[t][i][1]/reps; // Actually this is just sum(X_i-\mu)^2/n
					double sampleKurt = incMoment[t][i][3]/reps; // Actually this is just sum(X_i-\mu)^4/n
					double reps1Div=(reps-1)/reps;
					double reps3Div=(reps-3)/reps;
					double test1 = sampleKurt*reps1Div*reps1Div/reps;
					double test2 = reps1Div*reps3Div*sampleVar*sampleVar/reps;
					double sampleVarVar = test1 - test2;
					
					// Mean confidence interval
					if (sampleMean > 1 && sampleVar > 1)
					{
						maxRelCIChange = Math.max(ci * Math.sqrt(sampleVar/reps)/ sampleMean,maxRelCIChange);
						if(maxMomentOrder > 1) {maxRelCIChange = Math.max(ci * Math.sqrt(sampleVarVar/reps)/ sampleVar,maxRelCIChange);}
					}
				}
			}
			
			PCTMCLogging.info("Max relative CI width: " + maxRelCIChange + " Max relative CI width tol: " + mMaxRelCIWidth);
			if (maxRelCIChange < mMaxRelCIWidth)
			{
				accurate=true;
			}
		}
		
		for (int t = 0; t < (int) Math.ceil(stopTime / stepSize); t++)
		{
			for (int i=0; i<dataPoints[0].length; i++)
			{
				dataPoints[t][i] /= reps;
			}
		}
		PCTMCLogging.decreaseIndent();
	}

}
