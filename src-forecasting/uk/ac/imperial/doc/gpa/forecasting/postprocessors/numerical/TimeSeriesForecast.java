package uk.ac.imperial.doc.gpa.forecasting.postprocessors.numerical;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import uk.ac.imperial.doc.gpa.forecasting.util.FileExtra;
import uk.ac.imperial.doc.gpa.plain.representation.PlainPCTMC;
import uk.ac.imperial.doc.gpa.plain.representation.timed.TimedEvents;
import uk.ac.imperial.doc.pctmc.representation.State;
import uk.ac.imperial.doc.pctmc.utils.PCTMCLogging;

public class TimeSeriesForecast {

	public final static double s_ADDON_LENGTH = 0.1;	
	private PlainPCTMC mPctmc;
	private int mWarmup;
	private int mForecast;
	private int mIBF;
	private State mArrState;
	private List<State> mStartStates;
	private List<String> mDestMus;
	private List<String> mStartDeltas;
	private int mTSStep;
	private double[][] mMuTS;
	private List<String> mArrTSFiles;
	private double[] mArrTS;
	private List<String> mDepTSFiles;
	private double[][] mDepTS;
	private int mTSStartIndex;
	
	public TimeSeriesForecast(PlainPCTMC pctmc, int warmup, int forecast,
			int ibf, State arrState, List<State> startStates,
			List<String> destMus, List<String> startDeltas, int tsStep,
			String muTSFile, List<String> arrTSFiles, List<String> depTSFiles)
	{
		mPctmc = pctmc;
		mWarmup = warmup;
		mForecast = forecast;
		mIBF = ibf;
		mArrState = arrState;
		mStartStates = startStates;
		mDestMus = destMus;
		mStartDeltas = startDeltas;
		mTSStep = tsStep;
		mArrTSFiles =  new LinkedList<String>(arrTSFiles);
		mDepTSFiles = new LinkedList<String>(depTSFiles);
		mTSStartIndex = 0;
		
		// Check departure and arrival time series files
		if (mArrTSFiles.size() != mDepTSFiles.size()) {
			PCTMCLogging.error("For each observation period we should have an arrival and a departures time series.");
			System.exit(0);
		}
				
		// Read Mu parameter time series, which govern the percentage of bikes that
		// go from a particular cluster to the destination partition at a particular
		// moment in time
		String muTSStr = FileExtra.readFromTextFile(muTSFile);
		mMuTS = new double[mDestMus.size()][];
		for (int i=0; i<mDestMus.size(); i++) {
			String[] muTSMuI = muTSStr.split("\n")[i].trim().split(" ");
			mMuTS[i] = new double[muTSMuI.length];
			for (int j=0; j < muTSMuI.length; j++) {
				mMuTS[i][j] = Double.parseDouble(muTSMuI[j]);
			}
		}
	}

	/**
	 * Prepare departure and arrival time series data for the next
	 * time series
	 * @return true if we have another departure and arrival time series
	 */
	public boolean nextTSFile() {
		if (mArrTSFiles.size()==0) {return false;}
		
		// Expecting 1 observation per time unit
		int numStartStates = mStartStates.size();
		String arrTSStr = FileExtra.readFromTextFile(mArrTSFiles.remove(0));
		String depTSStr = FileExtra.readFromTextFile(mDepTSFiles.remove(0));

		// Load observed arrivals for the timeseries
		String[] arrNow = arrTSStr.trim().split(" ");
		int numObs = arrNow.length;
		mArrTS = new double[numObs];
		for (int i=0; i < numObs; ++i) {
			mArrTS[i] = Double.parseDouble(arrNow[i]);
		}
		
		// Load observed departures for the day
		String[] depNowPerStartState = depTSStr.split("\n");
		mDepTS = new double[numStartStates][numObs];
		for (int state=0; state < numStartStates; ++state) {
			String[] depStateToday = depNowPerStartState[state].split(" ");
			for (int i=0; i < numObs; ++i) {
				mDepTS[state][i] = Double.parseDouble(depStateToday[i]);
			}
		}
		
		// We start every time series from the first observation in the time series
		mTSStartIndex = 0;
		
		return true;
	}
	
	/**
	 * Run the forecast for a subset of our data at a time. From one forecast
	 * to another we leave an mIBF minute gap. The warmup period will be used
	 * to initialise the populations from where bicycles start.
	 * 
	 * @return number of arrivals during forecast window if there is enough data
	 *         for doing another analysis, otherwise the function returns -1
	 */
	public int nextIntvl() {
		int numObs = mArrTS.length;
		int numStartStates = mStartStates.size();
		// Not enough data in time series to do a forecast
		if (mTSStartIndex + mWarmup + mForecast >= numObs) {return -1;}

		// Prepare deterministic rate/pop changes in inhomogenous PCTMC
		Map <String,double[][]> allRateEvents = new HashMap<String, double[][]>();
		Map <State,double[][]> allJumpEvents = new HashMap<State, double[][]>();
		Map <State,double[][]> allResetEvents = new HashMap<State, double[][]>();
		
		// Let's start with rates and jumps which affect the start cluster states
		for (int state=0; state < numStartStates; ++state) {
			// Select departures from start state in the current windows warmup period
			double[][] jumpEvents = new double[mWarmup][2];
			double[][] destRateEvents = new double[mWarmup+mForecast][2];
			for (int t=0; t<mWarmup; ++t) {
				int relativeTime = t*mTSStep;
				jumpEvents[t][0] = relativeTime;
				jumpEvents[t][1] = mDepTS[state][mTSStartIndex+t];
				destRateEvents[t][0] = relativeTime;
				destRateEvents[t][1] = mMuTS[state][mTSStartIndex+t];
			}
			
			// We assume mu rate changes to be inhomogeneous but deterministic
			// Thus we can also anticipate changes in mu during the forecast
			// period
			for (int t=mWarmup; t<mWarmup+mForecast; ++t) {
				int relativeTime = t*mTSStep;
				destRateEvents[t][0] = relativeTime;
				destRateEvents[t][1] = mMuTS[state][mTSStartIndex+t];
			}
			
			// This maps will be used to create update events
			allJumpEvents.put(mStartStates.get(state), jumpEvents);
			allRateEvents.put(mDestMus.get(state), destRateEvents);
			
			// Approximate future departures rate from most recent departure rates
			// To do that we find the gradient of change from the previous windowSize
			// minute window to the current one and then extrapolate the rate of the
			// current forecast window
			double depRateCurForecast = 0;
			double depRatePrevWindow = 0;
			double windowSize = 8;
			for (int t=1; t <= windowSize; ++t) {
				depRateCurForecast += (1/windowSize)*jumpEvents[mWarmup-t][1];
				depRatePrevWindow  += (1/windowSize)*jumpEvents[(int) (mWarmup-windowSize-t)][1];
			}
			// Rate = depRateCurForecast + (depRateCurForecast - depRatePrevWindow)
			double[][] depRateEvents = { { mWarmup, Math.max(2*depRateCurForecast-depRatePrevWindow, 0) }};
			allRateEvents.put(mStartDeltas.get(state), depRateEvents);
		}
		
		// And the resets for the arrival location
		double[][] resets = {{mWarmup, 0}};
		allResetEvents.put(mArrState,resets);
	
		// Set events in TimeEvents series point in PCTMC
		TimedEvents te = mPctmc.getTimedEvents();
		te.setEvents(allRateEvents, allJumpEvents, allResetEvents);
		
		// Compute arrivals during the time window
		int actualArr = 0;
		for (int i=mWarmup; i<mWarmup+mForecast; ++i) {
			actualArr += mArrTS[mTSStartIndex+i];
		}
		
		// Move time series window by (Interval Between Forecasts) mIBF minutes
		mTSStartIndex += mIBF;
		return actualArr;
	}

}