package uk.ac.imperial.doc.gpa.forecasting.postprocessors.numerical;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import uk.ac.imperial.doc.gpa.forecasting.util.FileExtra;
import uk.ac.imperial.doc.gpa.forecasting.util.MathExtra;
import uk.ac.imperial.doc.gpa.plain.postprocessors.numerical.InhomogeneousODEAnalysisNumericalPostprocessor;
import uk.ac.imperial.doc.gpa.plain.representation.PlainPCTMC;
import uk.ac.imperial.doc.gpa.plain.representation.Transaction;
import uk.ac.imperial.doc.gpa.plain.representation.timed.TimedEvents;
import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.pctmc.analysis.PCTMCAnalysisPostprocessor;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
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
		
		/*
		// Load departure time series
		String arrivalState = "Arrival";
		String arrivals = FileExtra.readFromTextFile("../SpatialysisSVN/data/ts/Arrivals01-05-2012.dat");
		String[] arrTs = arrivals.split(" ");
		double[] arrivalTS = new double[arrTs.length];
		for (int i=0; i<arrTs.length; ++i) {arrivalTS[i] = Double.parseDouble(arrTs[i]);}
		
		String[] departureStates = {"StartCluster1","StartCluster2","StartCluster3","StartCluster4",
									"StartCluster5","StartCluster6","StartCluster7"};
		String departures = FileExtra.readFromTextFile("../SpatialysisSVN/data/ts/Departures01-05-2012.dat");
		double[][] departuresTS = new double[departureStates.length][arrivalTS.length];
		String[] allDepTs = departures.split("\n");
		for (int i=0; i < allDepTs.length; i++) {
			String[] depTs = allDepTs[i].split(" ");
			for (int j=0; j<depTs.length; ++j) {departuresTS[i][j] = Double.parseDouble(depTs[j]);}
		}
		
		//
		int indexOfArr=-1;
		for (Entry<CombinedPopulationProduct, Integer>  e: this.odeAnalysis.getMomentIndex().entrySet()) {
			if (e.getKey().toString().contains(arrivalState)) {
				indexOfArr = e.getValue();
			}
		}
		
		// Forecast
		int warmup = 40;
		int forecast = 5;
		this.stopTime = warmup+forecast;
		int intervalBetweenForecasts = 5;
		for (int index = 0; index+warmup+forecast < arrivalTS.length; index += intervalBetweenForecasts){
	
			Map <String,double[][]> rates = new HashMap<String, double[][]>();
			Map <State,double[][]> jumps = new HashMap<State, double[][]>();
			double[][][] depRates = new double[departureStates.length][1][2];
			double[][][] depJumps = new double[departureStates.length][warmup][2];
			for (int s=0; s < departureStates.length; s++) {
				// depRates from time 0 until forecast
				depRates[s][0][0] = warmup;
				depRates[s][0][1] += 0.8*departuresTS[s][index+warmup-1]+ 0.1*departuresTS[s][index+warmup-2] + 0.1*departuresTS[s][index+warmup-3];
				rates.put(departureStates[s].toString(), depRates[s]);
				List<String> l = new LinkedList<String>(); l.add(departureStates[s]);
	
				for (int t = 0; t < warmup; ++t){
					depJumps[s][t][0] = t;
					depJumps[s][t][1] = departuresTS[s][index+t];
				}
				jumps.put(new Transaction(l), depJumps[s]);
			}
			
			double actualArr = 0;
			for (int i=index+warmup; i < index+warmup+forecast; i++) {
				actualArr += arrivalTS[i];
			}
			
			// Resets
			double[][] arrReset = new double[1][2];
			arrReset[0][0] = warmup;
			arrReset[0][1] = 0;
			Map <State,double[][]> resets = new HashMap<State, double[][]>();
			List<String> l = new LinkedList<String>(); l.add(arrivalState);
			resets.put(new Transaction(l), arrReset);
	
			// Set time series point in PCTMC
			TimedEvents te = pctmc.getTimedEvents();
			//TimedEventsMem teTSData = new TimedEventsMem(te);
			//teTSData.setTimeSeries(rates, jumps, resets);
			//pctmc.setTimedEvents(teTSData);
	
			// Compute data points
			 
			 */
			super.calculateDataPoints(constants);
			//System.out.println (MathExtra.twoDecim(dataPoints[(int) (dataPoints.length-(1/stepSize))][indexOfArr]) + " "+ actualArr);
		//}
	}
}
