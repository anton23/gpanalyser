package uk.ac.imperial.doc.gpa.forecasting.postprocessors.numerical;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import uk.ac.imperial.doc.gpa.forecasting.util.MathExtra;
import uk.ac.imperial.doc.gpa.plain.postprocessors.numerical.InhomogeneousSimulationAnalysisNumericalPostprocessor;
import uk.ac.imperial.doc.gpa.plain.representation.PlainPCTMC;
import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.analysis.PCTMCAnalysisPostprocessor;
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.PlotDescription;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.PopulationProduct;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.NumericalPostprocessor;
import uk.ac.imperial.doc.pctmc.representation.State;

public class ForecastingSimuAnalysisNumericalPostprocessor extends
		InhomogeneousSimulationAnalysisNumericalPostprocessor
{	
	private int mWarmup;
	private int mForecast;
	private int mIBF;
	private State mArrState;
	private List<State> mStartStates;
	private List<String> mDestMus;
	private List<String> mStartDeltas;
	private int mMAWindowSize;
	private String mMuTSFile;
	private String mDeltaTSFile;
	private List<String> mMixedMuTSFiles;
	private double mMixedMuRatio;
	private List<String> mArrTSFiles;
	private List<String> mDepTSFiles;
	List<double[]> mData = new LinkedList<double[]>();

	public ForecastingSimuAnalysisNumericalPostprocessor(double stepSize, int replications, int warmup, int forecast,
			   int ibf, State arrState, List<State> startStates, List<String> destMus, List<String> startDeltas,
			   int maWindowSize, String muTSFile, String deltaTSFile, List<String> mixedMuTSFile, double mixedMuRatio, List<String> arrTSFiles,
			   List<String> depTSFiles) {
		super(warmup+forecast+stepSize, stepSize, replications);
		mWarmup = warmup;
		mForecast = forecast;
		mIBF = ibf;
		mArrState = arrState;
		mStartStates = startStates;
		mDestMus = destMus;
		mStartDeltas = startDeltas;
		mMAWindowSize = maWindowSize;
		mMuTSFile = muTSFile;
		mDeltaTSFile = deltaTSFile;
		mMixedMuTSFiles = mixedMuTSFile;
		mMixedMuRatio = mixedMuRatio;
		mArrTSFiles = arrTSFiles;
		mDepTSFiles = depTSFiles;
	}

	public ForecastingSimuAnalysisNumericalPostprocessor(double stepSize, int replications, int warmup, int forecast,
			   int ibf, State arrState, List<State> startStates, List<String> destMus, List<String> startDeltas,
			   int maWindowSize, String muTSFile, String deltaTSFile, List<String> mixedMuTSFile, double mixedMuRatio,
			   List<String> arrTSFiles, List<String> depTSFiles, Map<String, Object> parameters) {
		super(warmup+forecast+stepSize, stepSize, replications, parameters);
		mWarmup = warmup;
		mForecast = forecast;
		mIBF = ibf;
		mArrState = arrState;
		mStartStates = startStates;
		mDestMus = destMus;
		mStartDeltas = startDeltas;
		mMAWindowSize = maWindowSize;
		mMuTSFile = muTSFile;
		mDeltaTSFile = deltaTSFile;
		mMixedMuTSFiles = mixedMuTSFile;
		mMixedMuRatio = mixedMuRatio;
		mArrTSFiles = arrTSFiles;
		mDepTSFiles = depTSFiles;
	}

	@Override
	public PCTMCAnalysisPostprocessor regenerate() {
		return new ForecastingSimuAnalysisNumericalPostprocessor(stepSize, replications, mWarmup, mForecast,
				   mIBF, mArrState, mStartStates, mDestMus, mStartDeltas, mMAWindowSize, mMuTSFile, mDeltaTSFile,
				   mMixedMuTSFiles, mMixedMuRatio, mArrTSFiles, mDepTSFiles);
	}
	
	@Override
	public NumericalPostprocessor getNewPreparedPostprocessor(Constants constants) {
		assert(prepared);
		ForecastingSimuAnalysisNumericalPostprocessor ret = new ForecastingSimuAnalysisNumericalPostprocessor(stepSize,
				replications, mWarmup, mForecast, mIBF, mArrState, mStartStates, mDestMus, mStartDeltas, mMAWindowSize,
				mMuTSFile, mDeltaTSFile, mMixedMuTSFiles, mMixedMuRatio, mArrTSFiles, mDepTSFiles);
		ret.fastPrepare(momentIndex, generalExpectationIndex,
				productUpdaterCode, accumulatorUpdaterCode, eventGeneratorCode,
				initialExpressions, eventGeneratorClassName);
		return ret;
	}
	
	@Override
	public void calculateDataPoints(Constants constants) {
		
		// Do analysis for all time series points
		PlainPCTMC pctmc = getPlainPCMTC(simulation);	

		// Time series preparation
		TimeSeriesForecast tsf = new TimeSeriesForecast(pctmc,mWarmup,mForecast,mIBF,
														mArrState,mStartStates,mDestMus,
														mStartDeltas, mMAWindowSize,
														mMuTSFile, mDeltaTSFile,
														mMixedMuTSFiles, mMixedMuRatio,
														mArrTSFiles, mDepTSFiles);

		PopulationProduct pp = PopulationProduct.getMeanProduct(mArrState);
		CombinedPopulationProduct cppArrMean = new CombinedPopulationProduct(pp);
		int cppArrMeanIndex = simulation.getMomentIndex().get(cppArrMean);
		CombinedPopulationProduct cppArrMeanSq = new CombinedPopulationProduct(PopulationProduct.getProduct(pp,pp));
		int cppArrMeanSqIndex = simulation.getMomentIndex().get(cppArrMeanSq);

		while (tsf.nextTSFile()) {
			while (true) {
				// Check if there is enough data for the forecast
				// period on the current day
				int actualArr = tsf.nextIntvl();
				if (actualArr < 0) {break;}
				
				// Do the calculation
				super.calculateDataPoints(constants);

				// Forecast vs Reality
				double forecastArr = MathExtra.twoDecim(dataPoints[dataPoints.length-1][cppArrMeanIndex]);
				double forecastArrSq = MathExtra.twoDecim(dataPoints[dataPoints.length-1][cppArrMeanSqIndex]);
				double forecastStdDev = MathExtra.twoDecim(Math.sqrt(forecastArrSq - forecastArr*forecastArr));
				double[] data = {forecastArr, forecastStdDev, actualArr};
				for (int i=0; i<mIBF*(1/stepSize); ++i) {
					mData.add(data);
				}

				// Compute what the normalised distance between forecast and actual number of arrivals
				double normActArr = Math.abs(actualArr - forecastArr)/forecastStdDev;
				System.out.println (forecastArr + ", stdDev " + forecastStdDev + " actual arrivals: " + actualArr + "\t Normalised Dist: "+normActArr);		
			}
			tsf.plotForecast(mData,stepSize,simulation.toString());
			mData.clear();
		}
	}

	@Override
	public void postprocessAnalysis(Constants constants,
			AbstractPCTMCAnalysis analysis,
			List<PlotDescription> plotDescriptions)
	{
		prepare(analysis, constants);
		calculateDataPoints(constants);
	}
}
