package uk.ac.imperial.doc.gpa.forecasting.postprocessors.numerical;

//import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jfree.data.xy.XYDataset;

import uk.ac.imperial.doc.gpa.forecasting.util.FileExtra;
import uk.ac.imperial.doc.gpa.plain.representation.PlainPCTMC;
import uk.ac.imperial.doc.gpa.plain.representation.timed.TimedEvents;
import uk.ac.imperial.doc.pctmc.analysis.AnalysisUtils;
import uk.ac.imperial.doc.pctmc.charts.PCTMCChartUtilities;
import uk.ac.imperial.doc.pctmc.representation.State;
import uk.ac.imperial.doc.pctmc.utils.PCTMCLogging;

public class TimeSeriesForecast {

  // These are the input fields
	private final PlainPCTMC mPctmc;
  private final String mFcastMode;
  private final int mFcastWarmup;
  private final int mFcastLen;
  private final int mFcastFreq;
  private final List<State> mClDepStates;
  private final List<String> mClDepTSFiles;
  private final List<State> mClArrStates;
  private final List<String> mClArrTSFiles;
  
  // These fields may change during analysis
  private int[][] mClDepTS;
  private int[][] mClArrTS;
  private int mTSStartIndex;
	
	public TimeSeriesForecast(
	  final PlainPCTMC pctmc,
	  final String fcastMode, final int fcastWarmup,
    final int fcastLen, final int fcastFreq,
    final List<State> clDepStates, final List<String> clDepTSFiles,
    final List<State> clArrStates, final List<String> clArrTSFiles
	) {
		mPctmc = pctmc;
    mFcastMode = fcastMode;
    mFcastWarmup = fcastWarmup;
    mFcastLen = fcastLen;
    mFcastFreq = fcastFreq;
    mClDepStates = clDepStates;
    mClDepTSFiles = clDepTSFiles;
    mClArrStates = clArrStates;
    mClArrTSFiles = clArrTSFiles;

		// Check departure and arrival time series files
		if (mClDepTSFiles.size() != mClArrTSFiles.size()) {
			PCTMCLogging.error(
			  "We require matching departure and arrival"+
			  " time series for all experiments!"
			);
			System.exit(0);
		}
	}

	/**
	 * Prepare departure and arrival time series data for the next
	 * time series
	 * @return true if we have another departure and arrival time series
	 */
	public boolean nextTSFile() {
	  // Are we done yet?
		if (mClDepTSFiles.size() == 0) {
		  return false;
		}
		
		// Expecting 1 observation per time unit per cluster
		final int numCl = mClDepStates.size();
    final String clDepTSRaw =
      FileExtra.readFromTextFile(mClDepTSFiles.remove(0));
		final String clArrTSRaw =
		  FileExtra.readFromTextFile(mClArrTSFiles.remove(0));
		final int numTSPoints = clDepTSRaw.split("\n")[0].split(" ").length;
		
		// Load observed cluster departures and arrivals for the day
		String[] clDep = clDepTSRaw.split("\n");
    String[] clArr = clArrTSRaw.split("\n");
		mClDepTS = new int[numCl][numTSPoints];
    mClArrTS = new int[numCl][numTSPoints];
		for (int cl = 0; cl < numCl; cl++) {
			String[] curClDep = clDep[cl].split(" ");
      String[] curClArr = clArr[cl].split(" ");
			for (int i=0; i < numTSPoints; ++i) {
			  mClDepTS[cl][i] = Integer.parseInt(curClDep[i]);
			  mClArrTS[cl][i] = Integer.parseInt(curClArr[i]);
			}
			
			// Sanity check
			if (numTSPoints != curClDep.length ||
			    numTSPoints != curClArr.length) {
				PCTMCLogging.error (
				  "There must be equally many departure and arrival observations"
				);
				System.exit(0);
			}
		}
		
		// We start every analysis from the first observation in the time series
		mTSStartIndex = 0;
		return true;
	}

	/**
	 * Run the forecast for a subset of our data at a time. From one forecast
	 * to another we leave an mFcastWarmup minute gap. The warmup period will be
	 * used to initialise the populations from where bicycles start.
	 * 
	 * @return number of arrivals per cluster during forecast window,
	 *         if there aren't enough time series points to make another
	 *         prediction the function returns null
	 */
	public int[] nextIntvl() {
	  final int numCl = mClDepStates.size();
	  final int numTSPoints = mClDepTS[0].length;
		// Not enough data in time series to do a forecast
    if (mTSStartIndex + mFcastWarmup + mFcastLen >= numTSPoints) {return null;}

		// Prepare pop changes in inhomogenous PCTMC
    final Map <String, double[][]> allRateEvents =
      new HashMap<String, double[][]>();
		final Map <State, double[][]> allJumpEvents =
		  new HashMap<State, double[][]>();
		final Map <State, double[][]> allResetEvents =
		  new HashMap<State, double[][]>();
		final int[] actualClArrivals = new int[numCl];
		
		// Time dependent departures and arrival count resets for each cluster
		final int tsEndPoint = mFcastWarmup + mFcastLen;
		for (int cl = 0; cl < numCl; cl++) {
		  // New arrivals
		  final double[][] jumpEvents =
		    genDepartures(cl, mTSStartIndex, tsEndPoint, mFcastMode);
			allJumpEvents.put(mClDepStates.get(cl), jumpEvents);
		  // And the reset for the clusters arrival location
	    final double[][] resets = {{mFcastWarmup, 0}};
	    allResetEvents.put(mClArrStates.get(cl), resets);
	    // Compute actual cluster arrivals during the time window
	    for (int i = mFcastWarmup; i < tsEndPoint; ++i) {
	      actualClArrivals[cl] += mClArrTS[cl][mTSStartIndex + i];
	    }
		}

		// Set events for TimedEvents in PCTMC
		TimedEvents te = mPctmc.getTimedEvents();
		te.setEvents(allRateEvents, allJumpEvents, allResetEvents);
		
		// Move time series window by (Interval Between Forecasts) mIBF minutes
		mTSStartIndex += mFcastFreq;
		return actualClArrivals;
	}
}
