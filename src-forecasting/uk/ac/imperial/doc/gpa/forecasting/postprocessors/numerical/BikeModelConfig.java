package uk.ac.imperial.doc.gpa.forecasting.postprocessors.numerical;

//import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

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
  private RConnection mRConn;
  private final String mDir;
  private final String[] mTrainClDepTSFiles;
  private final String[] mTrainClDepToDestRepTSFiles;
  private final String[] mTrainClArrRepTSFiles;
  private final List<String> mClDepTSFiles;
  private final List<String> mClDepToDestTSFiles;
  private final List<String> mClArrTSFiles;
  private final String mDepModelVar;
  private final int numCl;
  private final int mTSEndPoint;
  
  // These fields may change during analysis
  private String curClDepTSFile;
  private String curClDepToDestTSFile;
  private String curArrTSFile;
  private double[][] mClArrTS;
  private double[] mClArrTtlTS;
  private int mTSFileIdx;
  private int mTSStartIndex;
  
	public BikeModelConfig(
    final int fcastWarmup, final int fcastLen, final int fcastFreq,
    final List<State> clDepStates, final List<State> clArrStates,
    final String depFcastMode, final List<String> trainClDepTSFiles,
    final List<String> trainClDepToDestTSFiles,
    final List<String> trainClArrTSFiles, final List<String> clDepTSFiles,
    final List<String> clDepToDestTSFiles, final List<String> clArrTSFiles
	) {
    mFcastWarmup = fcastWarmup;
    mFcastLen = fcastLen;
    mFcastFreq = fcastFreq;
    mClDepStates = clDepStates;
    mClArrStates = clArrStates;
    mClDepTSFiles = clDepTSFiles;
    mClDepToDestTSFiles = clDepToDestTSFiles;
    mClArrTSFiles = clArrTSFiles;
    numCl = mClDepStates.size();
    mTSEndPoint = mFcastWarmup + mFcastLen;

		// Check departure and arrival time series files
		if (mClDepTSFiles.size() != mClArrTSFiles.size()) {
			PCTMCLogging.error(
			  "We require matching departure and arrival"+
			  " time series for all experiments!"
			);
			System.exit(0);
		}

		// The working directory
		mDir = System.getProperty("user.dir");
		
		// Train departure time series using R and rJava
		int size = trainClDepTSFiles.size();
		mTrainClDepTSFiles =
      trainClDepTSFiles.toArray(new String[size]);
    mTrainClDepToDestRepTSFiles =
      trainClDepToDestTSFiles.toArray(new String[size]);
    mTrainClArrRepTSFiles =
      trainClArrTSFiles.toArray(new String[size]);
    mDepModelVar = "depModel" + System.currentTimeMillis();
    try {
      mRConn = new RConnection();
      // Train the model departure time series model
      mRConn.voidEval(String.format("setwd(\"%s/src-R/\")", mDir));
      mRConn.voidEval("source(\"arrivalFcast.R\")");
      mRConn.voidEval(String.format("setwd(\"%s\")", mDir));
      mRConn.assign("trainClDepRepTSF", mTrainClDepTSFiles);
      mRConn.assign("trainClDepToDestRepTSF", mTrainClDepToDestRepTSFiles);
      mRConn.assign("trainClArrRepTSF", mTrainClArrRepTSFiles);
      mRConn.voidEval(String.format(
        "%s <- genDepFcastModel(\"%s\", %d, %d, %d, "+
        "trainClDepRepTSF, trainClDepToDestRepTSF)",
        mDepModelVar, depFcastMode, mFcastFreq, mFcastWarmup, mFcastLen
      ));
    } catch (RserveException e) {
      e.printStackTrace();
    } catch (REngineException e) {
      e.printStackTrace();
    }

    // Initialise the model
    mTSFileIdx = -1;
    mTSStartIndex = 0;
	}
	
	public String genLinRegArimaArrivalFcastModel(
	  final String arrFcastMode,
	  final int minXreg
	) {
	  final String arrModelVar = "arrModel" + System.currentTimeMillis();
	  try {
      // Train the model departure time series model
      mRConn.assign("trainClDepRepTSF", mTrainClDepTSFiles);
      mRConn.assign("trainClDepToDestRepTSF", mTrainClDepToDestRepTSFiles);
      mRConn.assign("trainClArrRepTSF", mTrainClArrRepTSFiles);
      mRConn.voidEval(String.format(
        "%s <- genArrFcastModel(\"%s\", %d, %d, %d, %d,"+
        "trainClDepRepTSF, trainClDepToDestRepTSF, trainClArrRepTSF)",
        arrModelVar, arrFcastMode, mFcastFreq, mFcastWarmup, mFcastLen, minXreg
      ));
    } catch (RserveException e) {
      e.printStackTrace();
    } catch (REngineException e) {
      e.printStackTrace();
    }
	  return arrModelVar;
	}
	
	/**
	 * Prepare departure and arrival time series data for the next
	 * time series
	 * @return true if we have another departure and arrival time series
	 */
	public boolean nextTSFile() {
	  // Are we done yet?
		if (mClDepTSFiles.size() <= ++mTSFileIdx) {
		  return false;
		}
		
		// Get actual cluster arrivals in target area and total arrivals
    curClDepTSFile = mClDepTSFiles.get(mTSFileIdx);
		curClDepToDestTSFile = mClDepToDestTSFiles.get(mTSFileIdx);
		curArrTSFile = mClArrTSFiles.get(mTSFileIdx);
		try {
		  mClArrTS = mRConn.eval(String.format(
        "fcastOracleArrivalTS(%d, %d, %d, \"%s\") ",
        mFcastFreq, mFcastWarmup, mFcastLen, curArrTSFile
      )).asDoubleMatrix();
		  mClArrTtlTS = mRConn.eval(String.format(
        "fcastOracleArrivalTS(%d, %d, %d, \"%s\") ",
        mFcastFreq, mFcastWarmup, mFcastLen,
        curArrTSFile.replace("Arrivals", "ArrivalsTtl")
      )).asDoubles();
    } catch (RserveException e) {
      e.printStackTrace();
    } catch (REXPMismatchException e) {
      e.printStackTrace();
    }

		// We start every analysis from the first observation in the time series
    PCTMCLogging.info(String.format("Interval: %s", curArrTSFile));
    mTSStartIndex = 0;
		return true;
	}
	
  public double[][] linRegARIMAArrForecast(final String arrModelVar) {
    double data[][] = null;
    try {
      REXP res = mRConn.eval(String.format(
        "fcastArrivalTS(%s, %s, \"%s\", \"%s\", \"%s\")", mDepModelVar,
        arrModelVar, curClDepTSFile, curClDepToDestTSFile, curArrTSFile
      ));
      if (!res.isNull()) {
        data = res.asDoubleMatrix();
      }
    } catch (RserveException e) {
      e.printStackTrace();
    } catch (REXPMismatchException e) {
      e.printStackTrace();
    }
    
    return data;
  }

	public int nextIntvl() {
	  mTSStartIndex += mFcastFreq;
	  return mTSStartIndex;
	}
	
	/**
	 * Run the forecast for a subset of our data at a time. From one forecast
	 * to another we leave an mFcastWarmup minute gap. The warmup period will be
	 * used to initialise the populations from where bicycles start.
	 * 
	 * @return true iff there it is possible to make a forecast for the interval
	 */
	public boolean preparePCTMCForCurIntvlPCTMC(final PlainPCTMC pctmc) {
		// Prepare pop changes in inhomogenous PCTMC
    final Map <String, double[][]> allRateEvents =
      new HashMap<String, double[][]>();
		final Map <State, double[][]> allDepEvents =
		  new HashMap<State, double[][]>();
		final Map <State, double[][]> allResetEvents =
		  new HashMap<State, double[][]>();
		
		double[][] data = null;
	  try {
      REXP res = mRConn.eval(String.format(
        "fcastDepartureTS(%s, \"%s\", \"%s\", %d)",
        mDepModelVar, curClDepTSFile, curClDepToDestTSFile, mTSStartIndex + 1
      ));
      if (!res.isNull()) {
        data = res.asDoubleMatrix();
      }
    } catch (RserveException e) {
      e.printStackTrace();
    } catch (REXPMismatchException e) {
      e.printStackTrace();
    }
	  // Not enough data in time series to do a forecast
	  if (data == null) {return false;}
		
		// Time dependent departures and arrival count resets for each cluster
		for (int cl = 0; cl < numCl; cl++) {
	    double[][] depEvts = new double[mTSEndPoint + 1][2];
	    for (int t = 0; t < mTSEndPoint; t++) {
	      depEvts[t][0] = t;
	      depEvts[t][1] = data[cl][t];
	    }
		  // New cluster departures
		  allDepEvents.put(mClDepStates.get(cl), depEvts);
		  // Arrival cluster resets
	    allResetEvents.put(
	      mClArrStates.get(cl), new double[][] {{mFcastWarmup, 0}}
	    );

		}

		// Set events for TimedEvents in PCTMC
		TimedEvents te = pctmc.getTimedEvents();
		te.setEvents(allRateEvents, allDepEvents, allResetEvents);

		return true;
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
	  double[] fcastClArrivals
	) {
	  // Compute actual cluster arrivals during current time window
	  final int mNumCl = mClDepStates.size();
	  final int[] actualClArrivals = new int[mNumCl];
    for (int cl = 0; cl < mNumCl; cl++) {
      actualClArrivals[cl] += mClArrTS[cl][mTSStartIndex / mFcastFreq];
    }
    int ttlArr = (int) mClArrTtlTS[mTSStartIndex / mFcastFreq];
	  
	  // Compare actual arrivals with the prediction
	  PCTMCLogging.info("Prediction #" + mTSStartIndex);
    PCTMCLogging.increaseIndent();
    double ttlAvg = 0;
    double ttlSD = 0;
    int actArr = 0;
    for (int cl = 0; cl < mClArrStates.size(); cl++) {
      final int[] indices = clArrMomIndices.get(mClArrStates.get(cl));
      final double fcastClAvg = fcastClArrivals[indices[0]];
      final double fcastClAvgSq = fcastClArrivals[indices[1]];
      final double fcastClSD =
        Math.sqrt(fcastClAvgSq - fcastClAvg * fcastClAvg);
      ttlAvg += fcastClAvg;
      ttlSD += fcastClSD;
      actArr += actualClArrivals[cl];
      PCTMCLogging.info(String.format(
        "Cl:%d Mean:%.2f SD:%.2f Actual:%d",
        cl, fcastClAvg, fcastClSD, actualClArrivals[cl]
      ));
    }
    PCTMCLogging.info(String.format(
      "Ttl: Mean:%.2f SD:%.2f Actual:%d All:%d", ttlAvg, ttlSD, actArr, ttlArr
    ));
    PCTMCLogging.decreaseIndent();
	}
}
