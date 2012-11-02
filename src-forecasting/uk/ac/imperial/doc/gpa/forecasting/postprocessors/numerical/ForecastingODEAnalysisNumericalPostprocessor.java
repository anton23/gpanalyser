package uk.ac.imperial.doc.gpa.forecasting.postprocessors.numerical;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jfree.data.xy.XYSeriesCollection;

import uk.ac.imperial.doc.gpa.forecasting.util.FileExtra;
import uk.ac.imperial.doc.gpa.forecasting.util.MathExtra;
import uk.ac.imperial.doc.gpa.plain.postprocessors.numerical.InhomogeneousODEAnalysisNumericalPostprocessor;
import uk.ac.imperial.doc.gpa.plain.representation.PlainPCTMC;
import uk.ac.imperial.doc.gpa.plain.representation.timed.TimedEvents;
import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.analysis.AnalysisUtils;
import uk.ac.imperial.doc.pctmc.analysis.PCTMCAnalysisPostprocessor;
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.MeanOfLinearCombinationExpression;
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.PlotDescription;
import uk.ac.imperial.doc.pctmc.charts.PCTMCChartUtilities;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;
import uk.ac.imperial.doc.pctmc.expressions.PopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.patterns.PatternPopulationExpression;
import uk.ac.imperial.doc.pctmc.javaoutput.JavaODEsPreprocessed;
import uk.ac.imperial.doc.pctmc.javaoutput.PCTMCJavaImplementationProvider;
import uk.ac.imperial.doc.pctmc.odeanalysis.PCTMCODEAnalysis;
import uk.ac.imperial.doc.pctmc.plain.PlainState;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.NumericalPostprocessor;
import uk.ac.imperial.doc.pctmc.representation.State;
import uk.ac.imperial.doc.pctmc.utils.FileUtils;
import uk.ac.imperial.doc.pctmc.utils.PCTMCLogging;

public class ForecastingODEAnalysisNumericalPostprocessor extends
		InhomogeneousODEAnalysisNumericalPostprocessor
{	
	private int mWarmup;
	private int mForecast;
	private int mIBF;
	private State mArrState;
	private List<State> mStartStates;
	private List<String> mStartDeltas;
	private int mTSStep;
	private List<String> mArrTS;
	private List<String> mDepTS;

	public ForecastingODEAnalysisNumericalPostprocessor(double stepSize, int density, int warmup, int forecast,
			   int ibf, State arrState, List<State> startStates, List<String> startDeltas,
			   int tsStep, List<String> arrTS, List<String> depTS) {
		super(warmup+forecast+1, stepSize, density);
		mWarmup = warmup;
		mForecast = forecast;
		mIBF = ibf;
		mArrState = arrState;
		mStartStates = startStates;
		mStartDeltas = startDeltas;
		mTSStep = tsStep;
		mArrTS = arrTS;
		mDepTS = depTS;
	}

	public ForecastingODEAnalysisNumericalPostprocessor(double stepSize, int density, int warmup, int forecast,
			   int ibf, State arrState, List<State> startStates, List<String> startDeltas,
			   int tsStep, List<String> arrTS, List<String> depTS, Map<String, Object> parameters) {
		super(warmup+forecast+1, stepSize, density,parameters);
		mWarmup = warmup;
		mForecast = forecast;
		mIBF = ibf;
		mArrState = arrState;
		mStartStates = startStates;
		mStartDeltas = startDeltas;
		mTSStep = tsStep;
		mArrTS = arrTS;
		mDepTS = depTS;
	}
	
	protected ForecastingODEAnalysisNumericalPostprocessor(double stopTime, double stepSize, int density,
			PCTMCODEAnalysis odeAnalysis, JavaODEsPreprocessed preprocessedImplementation) {
		super(stopTime, stepSize, density, odeAnalysis, preprocessedImplementation);
	}
	
	@Override
	public PCTMCAnalysisPostprocessor regenerate() {
		return new ForecastingODEAnalysisNumericalPostprocessor(stepSize, density, mWarmup, mForecast,
				   mIBF, mArrState, mStartStates, mStartDeltas, mTSStep, mArrTS, mDepTS);
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
		
		int nofDays = mArrTS.size();
		if (nofDays != mDepTS.size()) {
			PCTMCLogging.error("For each observation period we should have an arrival and a departures.");
			System.exit(0);
		}
		
		// Forecast vs Reality
		CombinedPopulationProduct cppArrMean = new CombinedPopulationProduct(PopulationProduct.getMeanProduct(mArrState));
		int cppArrMeanIndex = this.odeAnalysis.getMomentIndex().get(cppArrMean);
		
		// Let's look at our forecasts for each day
		for (int day=0; day < nofDays; ++day) {
			
			// Read the time series
			int numStartStates = mStartStates.size();
			String arrTSStr = FileExtra.readFromTextFile(mArrTS.get(day));
			String depTSStr = FileExtra.readFromTextFile(mDepTS.get(day));

			String[] arrToday = arrTSStr.split(" ");
			String[] depTodayPerStartState = depTSStr.split("\n");
			String[][] depToday = new String[numStartStates][];
			for (int state=0; state < numStartStates; ++state) {
				depToday[state] = depTodayPerStartState[state].split(" ");
				if(arrToday.length != depToday[state].length) {
					PCTMCLogging.error("All departure and arrival time series must have contain the same number of observations.");
					System.exit(0);
				}
			}
			
			// Now we to run the forecast for a subset of our data at a time
			// From one forecast to another we leave a mIBF minute gap. The
			// warmup period will be used to initialise the populations from
			// where bicycles start
			int numObs = arrToday.length;
			for (int startObsIndex = 0; startObsIndex + mWarmup + mForecast < numObs; startObsIndex += mIBF) {
				// Prepare deterministic rate/pop changes in inhomogenous PCTMC
				Map <String,double[][]> allRateEvents = new HashMap<String, double[][]>();
				Map <State,double[][]> allJumpEvents = new HashMap<State, double[][]>();
				Map <State,double[][]> allResetEvents = new HashMap<State, double[][]>();
				
				// Let's start with rates and jumps which affect the start states
				for (int state=0; state < numStartStates; ++state) {
					// Select departures from start state in the current windows
					// warmup period
					double[][] jumpEvents = new double[mWarmup][2];
					for (int i=0; i<mWarmup; i++) {
						int relativeTime = i*mTSStep;
						jumpEvents[i][0] = relativeTime;
						jumpEvents[i][1] = Double.parseDouble(depToday[state][startObsIndex+i]);
					}
					allJumpEvents.put(mStartStates.get(state), jumpEvents);
					
					// Approximate future departures rate from most recent departure rates
					double rate = 0.2*jumpEvents[mWarmup-1][1]+0.2*jumpEvents[mWarmup-2][1]+
								   0.2*jumpEvents[mWarmup-3][1]+0.2*jumpEvents[mWarmup-4][1]+
								   0.2*jumpEvents[mWarmup-5][1];
					double[][] rateEvents = { { mWarmup, rate }};
					allRateEvents.put(mStartDeltas.get(state), rateEvents);
				}
				
				// And the resets for the arrival location
				double[][] resets = {{mWarmup, 0}};
				allResetEvents.put(mArrState,resets);
			
				// Set events in TimeEvents series point in PCTMC
				TimedEvents te = pctmc.getTimedEvents();
				te.setEvents(allRateEvents, allJumpEvents, allResetEvents);
			
				// Do the calculation
				super.calculateDataPoints(constants);
			
				// Forecast vs Reality
				double forecastArr = MathExtra.twoDecim(dataPoints[(int) (dataPoints.length-(1/stepSize))][cppArrMeanIndex]);
				double actualArr = 0;
				for (int i=mWarmup; i<mWarmup+mForecast; ++i) {
					actualArr += Double.parseDouble(arrToday[startObsIndex+i]);
				}

				double[] data = {forecastArr, actualArr};
				for (int i=0; i<mIBF*(1/stepSize); ++i) {
					mData.add(data);
				}
				
				System.out.println (forecastArr + " "+ actualArr);
			}
		}
	}
	
	List<double[]> mData = new LinkedList<double[]>();
	
	@Override
	public void postprocessAnalysis(Constants constants,
			AbstractPCTMCAnalysis analysis,
			List<PlotDescription> plotDescriptions){
		prepare(analysis, constants);
		calculateDataPoints(constants); 
		if (mData!=null){
			results = new HashMap<PlotDescription, double[][]>();
			List<AbstractExpression> l = new LinkedList<AbstractExpression>();
			PatternPopulationExpression fcast = new PatternPopulationExpression(mArrState);
			fcast.setUnfolded(CombinedProductExpression.createMeanExpression(mArrState));
			PatternPopulationExpression actual = new PatternPopulationExpression(new PlainState("ActualArr"));
			actual.setUnfolded(CombinedProductExpression.createMeanExpression(new PlainState("ActualArr")));
			l.add(fcast);
			l.add(actual);
			PlotDescription pd = new PlotDescription(l);
			double[][] data = plotData(analysis.toString(), constants, pd.getExpressions(), pd.getFilename());
			results.put(pd, data);
		}
	}

	@Override
	public double[][] plotData(String analysisTitle,
			Constants constants, List<AbstractExpression> expressions,
			String filename) {
		String[] names = new String[expressions.size()];
		for (int i = 0; i < expressions.size(); i++) {
			names[i] = expressions.get(i).toString();
		}
		double[][] data = new double[mData.size()][expressions.size()];
		for (int i=0; i < data.length; i++) {
			for (int j=0; j < data[i].length; j++)
			{
				data[i][j] = mData.get(i)[j];
			}
		}
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
}
