package uk.ac.imperial.doc.pctmc.postprocessors.numerical;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.TDistribution;
import org.apache.commons.math.distribution.TDistributionImpl;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.constants.visitors.ExpressionEvaluatorWithConstants;
import uk.ac.imperial.doc.jexpressions.javaoutput.statements.AbstractExpressionEvaluator;
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.PlotDescription;
import uk.ac.imperial.doc.pctmc.javaoutput.utils.ClassCompiler;
import uk.ac.imperial.doc.pctmc.simulation.PCTMCSimulation;
import uk.ac.imperial.doc.pctmc.simulation.SimulationUpdater;
import uk.ac.imperial.doc.pctmc.simulation.utils.AccumulatorUpdater;
import uk.ac.imperial.doc.pctmc.simulation.utils.AggregatedStateNextEventGenerator;
import uk.ac.imperial.doc.pctmc.simulation.utils.GillespieSimulator;
import uk.ac.imperial.doc.pctmc.utils.FileUtils;
import uk.ac.imperial.doc.pctmc.utils.PCTMCLogging;

public class AccurateSimulationAnalysisNumericalPostprocessor extends
		NumericalPostprocessorCI {
	private PCTMCSimulation mSimulation;

	private SimulationUpdater mUpdater;
	private SimulationUpdater mUpdaterNoAdd;
	private AccumulatorUpdater mAccUpdater;
	private AggregatedStateNextEventGenerator mEventGenerator;

	private String mOverrideCode;
	private String mOverrideCodeClassName;

	protected double mMaxRelChangePerRep;
	protected int mBatchSize;
	protected double mCI;

	protected Map<String, Object> mParameters;

	@Override
	public String toString() {
		return "(stopTime = " + stopTime + ", stepSize = " + stepSize
				+ " maxRelChangePerRep = " + mMaxRelChangePerRep
				+ ", batchSize = " + mBatchSize + ", CI =" + mCI + ")";
	}

	@Override
	public NumericalPostprocessor getNewPreparedPostprocessor(
			Constants _constants) {
		assert (mSimulation != null);
		AccurateSimulationAnalysisNumericalPostprocessor ret = new AccurateSimulationAnalysisNumericalPostprocessor(
				stopTime, stepSize, mMaxRelChangePerRep, mBatchSize, mCI,
				mParameters);
		ret.prepare(mSimulation, _constants);
		return ret;
	}

	public AccurateSimulationAnalysisNumericalPostprocessor(double _stopTime,
			double _stepSize, double _maxRelChangePerRep, int _batchSize,
			double _ci) {
		super(_stopTime, _stepSize);
		mMaxRelChangePerRep = _maxRelChangePerRep;
		mBatchSize = _batchSize;
		mCI = _ci;
	}

	public AccurateSimulationAnalysisNumericalPostprocessor(double _stopTime,
			double _stepSize, double _maxRelChangePerRep, int _batchSize,
			double _ci, Map<String, Object> _parameters) {
		this(_stopTime, _stepSize, _maxRelChangePerRep, _batchSize, _ci);
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

	private void simulate(Constants constants) {
		mEventGenerator.setRates(constants.getFlatConstants());

		int n = mSimulation.getPCTMC().getStateIndex().size();
		initial = new double[n];
		for (int i = 0; i < n; i++) {
			ExpressionEvaluatorWithConstants evaluator = new ExpressionEvaluatorWithConstants(
					constants);
			mSimulation.getPCTMC().getInitCounts()[i].accept(evaluator);
			initial[i] = evaluator.getResult();
		}

		PCTMCLogging.info("Running Gillespie simulator.");
		PCTMCLogging.increaseIndent();

		mUpdater.setRates(constants.getFlatConstants());
		mAccUpdater.setRates(constants.getFlatConstants());

		// Repeat simulation until moments are sufficiently accurate
		int reps = 0;
		double[][] tmp;
		double[][][] lastBatchData = new double[plotDescriptions.size()][][];
		double[][] curBatchData = null;
		double[][] dataPointsTmp = new double[dataPoints.length][dataPoints[0].length];
		double[][] meanLast = new double[dataPoints.length][dataPoints[0].length];
		boolean accurate = false;
		List<AbstractExpressionEvaluator> evals = new ArrayList<AbstractExpressionEvaluator>();
		for (PlotDescription pd : plotDescriptions) {
			evals.add(getExpressionEvaluator(pd.getExpressions(), constants));
		}

		while (!accurate) {
			for (int r = 0; r < mBatchSize; ++r) {
				if (reps > 0
						&& reps % (mBatchSize / 5 > 0 ? mBatchSize / 5 : 1) == 0) {
					PCTMCLogging.info(reps + " replications finished.");
				}
				tmp = GillespieSimulator.simulateAccumulated(mEventGenerator,
						initial, stopTime, stepSize, mAccUpdater);
				for (int t = 0; t < (int) Math.ceil(stopTime / stepSize); t++) {
					mUpdater.update(dataPoints[t], tmp[t]);
				}
				reps++;
			}

			// Test smoothness
			for (int t = 0; t < dataPoints.length; ++t) {
				for (int i = 0; i < dataPoints[t].length; ++i) {
					dataPointsTmp[t][i] = dataPoints[t][i];
					dataPoints[t][i] /= reps;
				}
			}

			int index = 0;
			accurate = true;
			double maxRelChange = 0;
			double relChange = 0;
			for (AbstractExpressionEvaluator eval : evals) {
				curBatchData = evaluateExpressions(eval, constants);
				if (lastBatchData[index] != null) {
					for (int i = 0; i < curBatchData.length; i++) {
						for (int j = 0; j < curBatchData[i].length; j++) {
							if (lastBatchData[index][i][j] > 1) // Too avoid
																// problems with
																// very small
																// populations
							{
								relChange = Math.abs(lastBatchData[index][i][j]
										- curBatchData[i][j])
										/ lastBatchData[index][i][j]
										/ mBatchSize;
								maxRelChange = Math
										.max(maxRelChange, relChange);
							}
						}
					}
				} else {
					maxRelChange = relChange + 100;
				}
				lastBatchData[index++] = curBatchData;
			}

			accurate = maxRelChange < mMaxRelChangePerRep;
			if (!accurate) {
				PCTMCLogging.info("Found max rel change of " + maxRelChange
						+ " per Rep > " + mMaxRelChangePerRep);
			}

			for (int t = 0; t < dataPoints.length; ++t) {
				for (int i = 0; i < dataPoints[t].length; ++i) {
					dataPoints[t][i] = dataPointsTmp[t][i];
					meanLast[t][i] = dataPoints[t][i] / reps;
				}
			}

		}
		dataPoints = meanLast;

		if (mCI > 0) {
			// Initialise variables
			double[] tmp2 = new double[dataPoints[0].length];
			double[] tmp3 = null;
			double[][][] ciDataPointsVarAcc = new double[lastBatchData.length][][];
			confidenceIntervalWidth = new double[lastBatchData.length][][];
			for (int index = 0; index < lastBatchData.length; index++) {
				ciDataPointsVarAcc[index] = new double[lastBatchData[index].length][lastBatchData[index][0].length];
				confidenceIntervalWidth[index] = new double[lastBatchData[index].length][lastBatchData[index][0].length];
			}

			PCTMCLogging.info("Computing confidence interval for " + reps
					+ " replications");
			for (int r = 0; r < reps; ++r) {
				if (r > 0 && r % (reps / 5 > 0 ? reps / 5 : 1) == 0) {
					PCTMCLogging.info(r
							+ " replications finished for CI computation.");
				}
				tmp = GillespieSimulator.simulateAccumulated(mEventGenerator,
						initial, stopTime, stepSize, mAccUpdater);

				for (int t = 0; t < (int) Math.ceil(stopTime / stepSize); t++) {
					mUpdaterNoAdd.update(tmp2, tmp[t]);
					int index = 0;
					for (AbstractExpressionEvaluator eval : evals) {
						tmp3 = evaluateExpressions(eval, tmp2, t, constants);
						for (int i = 0; i < tmp3.length; i++) {
							// S^2*(reps-1) = \sum(X - \mu)^2
							double var = (tmp3[i] - lastBatchData[index][t][i]);
							ciDataPointsVarAcc[index][t][i] += var * var;
						}
						++index;
					}
				}
			}

			// Now compute the confidence interval
			// Confidence interval check for mean
			TDistribution studentsT = new TDistributionImpl(reps - 1);
			double ciMeanHalfWidth = 1.96;
			try {
				ciMeanHalfWidth = studentsT
						.inverseCumulativeProbability(0.5 + mCI / 2);
			} catch (MathException e1) {
				e1.printStackTrace();
			}

			for (int index = 0; index < ciDataPointsVarAcc.length; index++) {
				for (int t = 0; t < ciDataPointsVarAcc[index].length; t++) {
					for (int v = 0; v < ciDataPointsVarAcc[index][t].length; v++) {
						// double mean = lastBatchData[index][t][v];
						double stdDev = Math
								.sqrt(ciDataPointsVarAcc[index][t][v]
										/ (reps - 1));
						double stdDevDivRepsSqRoot = stdDev / Math.sqrt(reps);
						confidenceIntervalWidth[index][t][v] = ciMeanHalfWidth
								* stdDevDivRepsSqRoot;
					}
				}
			}
		}

		PCTMCLogging.decreaseIndent();
	}

}
