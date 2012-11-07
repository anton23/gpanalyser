package uk.ac.imperial.doc.gpa.forecasting.postprocessors.numerical;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jfree.data.xy.XYSeriesCollection;

import uk.ac.imperial.doc.gpa.forecasting.util.MathExtra;
import uk.ac.imperial.doc.gpa.plain.postprocessors.numerical.InhomogeneousSimulationAnalysisNumericalPostprocessor;
import uk.ac.imperial.doc.gpa.plain.representation.PlainPCTMC;
import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.analysis.AnalysisUtils;
import uk.ac.imperial.doc.pctmc.analysis.PCTMCAnalysisPostprocessor;
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.PlotDescription;
import uk.ac.imperial.doc.pctmc.charts.PCTMCChartUtilities;
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
	private int mTSStep;
	private String mMuTSFile;
	private List<String> mArrTSFile;
	private List<String> mDepTSFile;
	List<double[]> mData = new LinkedList<double[]>();

	public ForecastingSimuAnalysisNumericalPostprocessor(double stepSize, int replications, int warmup, int forecast,
			   int ibf, State arrState, List<State> startStates, List<String> destMus, List<String> startDeltas,
			   int tsStep, String muTSFile, List<String> arrTSFiles, List<String> depTSFiles) {
		super(warmup+forecast+TimeSeriesForecast.s_ADDON_LENGTH, stepSize, replications);
		mWarmup = warmup;
		mForecast = forecast;
		mIBF = ibf;
		mArrState = arrState;
		mStartStates = startStates;
		mDestMus = destMus;
		mStartDeltas = startDeltas;
		mTSStep = tsStep;
		mMuTSFile = muTSFile;
		mArrTSFile = arrTSFiles;
		mDepTSFile = depTSFiles;
	}

	public ForecastingSimuAnalysisNumericalPostprocessor(double stepSize, int replications, int warmup, int forecast,
			   int ibf, State arrState, List<State> startStates, List<String> destMus, List<String> startDeltas,
			   int tsStep, String muTSFile, List<String> arrTSFiles, List<String> depTSFiles, Map<String, Object> parameters) {
		super(warmup+forecast+TimeSeriesForecast.s_ADDON_LENGTH, stepSize, replications, parameters);
		mWarmup = warmup;
		mForecast = forecast;
		mIBF = ibf;
		mArrState = arrState;
		mStartStates = startStates;
		mDestMus = destMus;
		mStartDeltas = startDeltas;
		mTSStep = tsStep;
		mMuTSFile = muTSFile;
		mArrTSFile = arrTSFiles;
		mDepTSFile = depTSFiles;
	}

	@Override
	public PCTMCAnalysisPostprocessor regenerate() {
		return new ForecastingSimuAnalysisNumericalPostprocessor(stepSize, replications, mWarmup, mForecast,
				   mIBF, mArrState, mStartStates, mDestMus, mStartDeltas, mTSStep, mMuTSFile, mArrTSFile, mDepTSFile);
	}
	
	@Override
	public NumericalPostprocessor getNewPreparedPostprocessor(Constants constants) {
		assert(prepared);
		ForecastingSimuAnalysisNumericalPostprocessor ret = new ForecastingSimuAnalysisNumericalPostprocessor(stepSize, replications, mWarmup, mForecast,
				   mIBF, mArrState, mStartStates, mDestMus, mStartDeltas, mTSStep, mMuTSFile, mArrTSFile, mDepTSFile);
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
														mStartDeltas,mTSStep,mMuTSFile,mArrTSFile,
														mDepTSFile);

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
				double[] data = {forecastArr, actualArr};
				for (int i=0; i<mIBF*(1/stepSize); ++i) {
					mData.add(data);
				}
				
				System.out.println (forecastArr + ", stdDev " + forecastStdDev + " actual arrivals: " + actualArr);
			}
		}
	}

	@Override
	public void postprocessAnalysis(Constants constants,
			AbstractPCTMCAnalysis analysis,
			List<PlotDescription> plotDescriptions)
	{
		prepare(analysis, constants);
		calculateDataPoints(constants);
		if (mData!=null){
			int numDataPts = mData.size();
			int lag = mForecast * ((int)(1/stepSize));
			String[] names =  {"E[Forecast t+"+mForecast+" mins]","Empirical"};
			double[][] data = new double[numDataPts+lag][2];
			double[][] dataLag = new double[numDataPts+lag][2];
			
			for (int i=0; i < numDataPts; ++i) {
				// Forecast vs real process
				data[i][0] = mData.get(i)[0];
				data[i][1] = mData.get(i)[1];
				
				// Forecast vs real process shifted
				dataLag[i][0] = mData.get(i)[0];
				dataLag[i+lag][1] = mData.get(i)[1];
			}
			// Fill in end of prediction graph / beginning of real graph
			for (int i=0; i < lag; i++) {
				// Forecast vs real process
				data[numDataPts+i][0] = data[numDataPts-1][0];
				data[numDataPts+i][1] = data[numDataPts-1][1];
				
				// Forecast vs real process shifted
				dataLag[numDataPts+i][0] = dataLag[numDataPts-1][0];
				dataLag[i][1] = 0;
			}

			XYSeriesCollection dataset = AnalysisUtils.getDatasetFromArray(data,stepSize,names);
			PCTMCChartUtilities.drawChart(dataset, "time", "#arrivals", "Forecast Vs Oracle", analysis.toString());
			XYSeriesCollection datasetLag = AnalysisUtils.getDatasetFromArray(dataLag,stepSize,names);
			PCTMCChartUtilities.drawChart(datasetLag, "time", "#arrivals", "Forecast Vs Live", analysis.toString());
		}
	}
}
