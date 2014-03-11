package uk.ac.imperial.doc.gpa.forecasting.postprocessors.numerical;

//import java.util.Arrays;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RFileInputStream;
import org.rosuda.REngine.Rserve.RserveException;

import uk.ac.imperial.doc.gpa.forecasting.util.FileExtra;
import uk.ac.imperial.doc.gpa.plain.representation.PlainPCTMC;
import uk.ac.imperial.doc.gpa.plain.representation.timed.TimedEvents;
import uk.ac.imperial.doc.pctmc.representation.State;
import uk.ac.imperial.doc.pctmc.utils.PCTMCLogging;

public class BikeModelConfig {

  // These are the input fields
  public final int mFcastWarmup;
  public final int mFcastLen;
  public final int mFcastFreq;
  public final List<State> mClDepStates;
  public final List<State> mClArrStates;
  private final String mDepFcastMode;
  private final List<String> mTrainClDepTSFiles;
  private final List<String> mTrainClDepToDestTSFiles;
  private final List<String> mClDepTSFiles;
  private final List<String> mClDepToDestTSFiles;
  private final List<String> mClArrTSFiles;
  
  // These fields may change during analysis
  private int[][] mClDepTS;
  private int[][] mClArrTS;
  private int mTSStartIndex;
	
	public BikeModelConfig(
    final int fcastWarmup, final int fcastLen, final int fcastFreq,
    final List<State> clDepStates, final List<State> clArrStates,
    final String depFcastMode, final List<String> trainClDepTSFiles,
    final List<String> trainClDepToDestTSFiles, final List<String> clDepTSFiles,
    final List<String> clDepToDestTSFiles, final List<String> clArrTSFiles
	) {
    mFcastWarmup = fcastWarmup;
    mFcastLen = fcastLen;
    mFcastFreq = fcastFreq;
    mClDepStates = clDepStates;
    mClArrStates = clArrStates;
    mDepFcastMode = depFcastMode;
    mTrainClDepTSFiles = trainClDepTSFiles;
    mTrainClDepToDestTSFiles = trainClDepToDestTSFiles;
    mClDepTSFiles = clDepTSFiles;
    mClDepToDestTSFiles = clDepToDestTSFiles;
    mClArrTSFiles = clArrTSFiles;

		// Check departure and arrival time series files
		if (mClDepTSFiles.size() != mClArrTSFiles.size()) {
			PCTMCLogging.error(
			  "We require matching departure and arrival"+
			  " time series for all experiments!"
			);
			System.exit(0);
		}

		// The working directory
		final String dir = System.getProperty("user.dir");
		
		// Train departure time series using R and rJava
		RConnection c;
    try {
      c = new RConnection();
      // Train the model departure time series model
      c.eval("setwd(\"" + dir + "/src-R/\")");
      c.eval("source(\"departureFcast.R\")");
      REXP x = c.eval("test('aaaa')");
      //REXP x = c.eval("R.version.string");
      System.out.println(x.asString());
    } catch (RserveException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (REXPMismatchException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
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
	public int[] nextIntvl(final PlainPCTMC pctmc) {
	  final int numCl = mClDepStates.size();
	  final int numTSPoints = mClDepTS[0].length;
		// Not enough data in time series to do a forecast
    if (mTSStartIndex + mFcastWarmup + mFcastLen >= numTSPoints) {return null;}

		// Prepare pop changes in inhomogenous PCTMC
    final Map <String, double[][]> allRateEvents =
      new HashMap<String, double[][]>();
		final Map <State, double[][]> allDepEvents =
		  new HashMap<State, double[][]>();
		final Map <State, double[][]> allResetEvents =
		  new HashMap<State, double[][]>();
		final int[] actualClArrivals = new int[numCl];
		
		// Time dependent departures and arrival count resets for each cluster
		final int tsEndPoint = mFcastWarmup + mFcastLen;
		for (int cl = 0; cl < numCl; cl++) {
		  // New cluster departures
		  allDepEvents.put(
			  mClDepStates.get(cl),
			  genDepartures(
			    cl, mTSStartIndex, mFcastWarmup, tsEndPoint, mDepFcastMode
			  )
			);
		  // Arrival cluster resets
	    allResetEvents.put(
	      mClArrStates.get(cl), new double[][] {{mFcastWarmup, 0}}
	    );
	    // Compute actual cluster arrivals during the time window
	    for (int i = mFcastWarmup; i < tsEndPoint; ++i) {
	      actualClArrivals[cl] += mClArrTS[cl][mTSStartIndex + i];
	    }
		}

		// Set events for TimedEvents in PCTMC
		TimedEvents te = pctmc.getTimedEvents();
		te.setEvents(allRateEvents, allDepEvents, allResetEvents);
		
		// Move time series window by (Interval Between Forecasts) mIBF minutes
		mTSStartIndex += mFcastFreq;
		return actualClArrivals;
	}
	
	private double[][] genDepartures(
	  int cl,
	  int tSStartIndex,
    int fcastWarmup,
    int tsEndPoint,
    String fcastMode
  ) {
    // TODO Call R code
	  // TODO For now we use dummy data
	  double[][] retVal = new double[tsEndPoint + 1][2];
	  for (int t = 0; t <= tsEndPoint; t++) {
	    retVal[t][0] = t;
	    retVal[t][1] = 2;
	  }
    return retVal;
  }

  /**
	 * Print predicted vs actual cluster arrivals
	 * @param clArrIds map arrival states to cluster ids
	 * @param clArrMomIndicies moment indices of arrival states in dataPoints
	 * @param fcastClArrivals cluster arrivals computed by forecast
	 * @param actualClArrivals cluster arrivals from real data
	 */
	public void printFcastResult(
	  Map<State, int[]> clArrMomIndices,
	  double[][] fcastClArrivals,
	  int[] actualClArrivals
	) {
	  PCTMCLogging.info("Prediction #"+ mTSStartIndex);
    PCTMCLogging.increaseIndent();
    for (int cl = 0; cl < mClArrStates.size(); cl++) {
      final int[] indices = clArrMomIndices.get(mClArrStates.get(cl));
      final double fcastClArr =
          fcastClArrivals[fcastClArrivals.length - 1][indices[0]];
      final double fcastClArrSq =
          fcastClArrivals[fcastClArrivals.length - 1][indices[1]];
      final double fcastClSD =
        Math.sqrt(fcastClArrSq - fcastClArr * fcastClArr);
      PCTMCLogging.info(String.format(
        "Cl:%d Mean:%.2f SD:%.2f Actual:%d",
        cl, fcastClArr, fcastClSD, actualClArrivals[cl]
      ));
    }
    PCTMCLogging.decreaseIndent();
	}
}
