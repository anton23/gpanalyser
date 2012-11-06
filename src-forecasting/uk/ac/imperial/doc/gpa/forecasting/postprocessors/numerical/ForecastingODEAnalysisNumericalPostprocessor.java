package uk.ac.imperial.doc.gpa.forecasting.postprocessors.numerical;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jfree.data.xy.XYSeriesCollection;

import uk.ac.imperial.doc.gpa.forecasting.util.MathExtra;
import uk.ac.imperial.doc.gpa.plain.postprocessors.numerical.InhomogeneousODEAnalysisNumericalPostprocessor;
import uk.ac.imperial.doc.gpa.plain.representation.PlainPCTMC;
import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.analysis.AnalysisUtils;
import uk.ac.imperial.doc.pctmc.analysis.PCTMCAnalysisPostprocessor;
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.PlotDescription;
import uk.ac.imperial.doc.pctmc.charts.PCTMCChartUtilities;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.PopulationProduct;
import uk.ac.imperial.doc.pctmc.javaoutput.JavaODEsPreprocessed;
import uk.ac.imperial.doc.pctmc.javaoutput.PCTMCJavaImplementationProvider;
import uk.ac.imperial.doc.pctmc.odeanalysis.PCTMCODEAnalysis;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.NumericalPostprocessor;
import uk.ac.imperial.doc.pctmc.representation.State;

public class ForecastingODEAnalysisNumericalPostprocessor extends
		InhomogeneousODEAnalysisNumericalPostprocessor
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
	private List<String> mArrTSFiles;
	private List<String> mDepTSFiles;
	private List<double[]> mData = new LinkedList<double[]>();

	public ForecastingODEAnalysisNumericalPostprocessor(double stepSize, int density, int warmup, int forecast,
			   int ibf, State arrState, List<State> startStates, List<String> destMus, List<String> startDeltas,
			   int tsStep, String muTSFile, List<String> arrTSFiles, List<String> depTSFiles) {
		super(warmup+forecast+TimeSeriesForecast.s_ADDON_LENGTH, stepSize, density);
		mWarmup = warmup;
		mForecast = forecast;
		mIBF = ibf;
		mArrState = arrState;
		mStartStates = startStates;
		mDestMus = destMus;
		mStartDeltas = startDeltas;
		mTSStep = tsStep;
		mMuTSFile = muTSFile;
		mArrTSFiles = arrTSFiles;
		mDepTSFiles = depTSFiles;
	}

	public ForecastingODEAnalysisNumericalPostprocessor(double stepSize, int density, int warmup, int forecast,
			   int ibf, State arrState, List<State> startStates, List<String> destMus, List<String> startDeltas,
			   int tsStep, String muTSFile, List<String> arrTSFiles, List<String> depTSFiles, Map<String, Object> parameters) {
		super(warmup+forecast+TimeSeriesForecast.s_ADDON_LENGTH, stepSize, density, parameters);
		mWarmup = warmup;
		mForecast = forecast;
		mIBF = ibf;
		mArrState = arrState;
		mStartStates = startStates;
		mDestMus = destMus;
		mStartDeltas = startDeltas;
		mTSStep = tsStep;
		mMuTSFile = muTSFile;
		mArrTSFiles = arrTSFiles;
		mDepTSFiles = depTSFiles;
	}
	
	protected ForecastingODEAnalysisNumericalPostprocessor(double stopTime, double stepSize, int density,
			PCTMCODEAnalysis odeAnalysis, JavaODEsPreprocessed preprocessedImplementation) {
		super(stopTime, stepSize, density, odeAnalysis, preprocessedImplementation);
	}
	
	@Override
	public PCTMCAnalysisPostprocessor regenerate() {
		return new ForecastingODEAnalysisNumericalPostprocessor(stepSize, density, mWarmup, mForecast,
				   mIBF, mArrState, mStartStates, mDestMus, mStartDeltas, mTSStep, mMuTSFile, mArrTSFiles, mDepTSFiles);
	}
	
	@Override
	public NumericalPostprocessor getNewPreparedPostprocessor(Constants constants) {
		assert(odeAnalysis!=null);
		PCTMCJavaImplementationProvider javaImplementation = new PCTMCJavaImplementationProvider();
		ForecastingODEAnalysisNumericalPostprocessor ret = new ForecastingODEAnalysisNumericalPostprocessor(stopTime, stepSize, density, odeAnalysis, javaImplementation
				.getPreprocessedODEImplementation(odeAnalysis.getOdeMethod(), constants, momentIndex));
		return ret;
	}
	
	@Override
	public void calculateDataPoints(Constants constants) {
		
		// Do analysis for all time series points
		PlainPCTMC pctmc = getPlainPCMTC(odeAnalysis);	

		// Time series preparation
		TimeSeriesForecast tsf = new TimeSeriesForecast(pctmc,mWarmup,mForecast,mIBF,
														mArrState,mStartStates,mDestMus,
														mStartDeltas,mTSStep,mMuTSFile,mArrTSFiles,
														mDepTSFiles);

		PopulationProduct pp = PopulationProduct.getMeanProduct(mArrState);
		CombinedPopulationProduct cppArrMean = new CombinedPopulationProduct(pp);
		int cppArrMeanIndex = odeAnalysis.getMomentIndex().get(cppArrMean);
		//CombinedPopulationProduct cppArrMeanSq = new CombinedPopulationProduct(PopulationProduct.getProduct(pp,pp));
		//int cppArrMeanSqIndex = odeAnalysis.getMomentIndex().get(cppArrMeanSq);
		
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
				//double forecastArrSq = MathExtra.twoDecim(dataPoints[(int) (dataPoints.length-(1/stepSize))][cppArrMeanSqIndex]);
				//double forecastStdDev = MathExtra.twoDecim(Math.sqrt(forecastArrSq - forecastArr*forecastArr));
				double[] data = {forecastArr, actualArr};
				for (int i=0; i<mIBF*(1/stepSize); ++i) {
					mData.add(data);
				}
				
				System.out.println (forecastArr + /*", stdDev " + forecastStdDev +*/ " actual arrivals: " + actualArr);
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
			String[] names =  {"E[ForecastArrival]","Observed Arrivals"};
			double[][] data = new double[mData.size()][2];
			for (int i=0; i < data.length; i++) {
				for (int j=0; j < data[i].length; j++)
				{
					data[i][j] = mData.get(i)[j];
				}
			}
			XYSeriesCollection dataset = AnalysisUtils.getDatasetFromArray(data,stepSize,names);
			PCTMCChartUtilities.drawChart(dataset, "time", "count", "",	analysis.toString());
		}
	}
}
