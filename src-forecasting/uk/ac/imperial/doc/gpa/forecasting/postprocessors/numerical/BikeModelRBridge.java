package uk.ac.imperial.doc.gpa.forecasting.postprocessors.numerical;

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

public class BikeModelRBridge {

  private RConnection mRConn;
  
  // These are the input fields
  public final int mFcastWarmup;
  public final int mFcastLen;
  public final int mFcastFreq;
  public final List<State> mClDepStates;
  public final List<State> mClArrStates;
  private final List<String> mTrainClDepTSFiles;
  private final List<String> mTrainClDepToDestTSFiles;
  private final List<String> mTrainClArrTSFiles;
  private final List<String> mClDepTSFiles;
  private final List<String> mClDepToDestTSFiles;
  private final List<String> mClArrTSFiles;
  
  // These fields may change during analysis
  private String mCurClDepTSFile;
  private String mCurClDepToDestTSFile;
  private String mCurArrTSFile;
  private double[][] mCurClArrTS;
  private double[] mCurClArrTtlTS;
  private int mCurTSFileIdx;
  private int mCurTSStartIndex;
  
	public BikeModelRBridge(
    final int fcastWarmup, final int fcastLen, final int fcastFreq,
    final List<State> clDepStates, final List<State> clArrStates,
    final List<String> trainClDepTSFiles,
    final List<String> trainClDepToDestTSFiles,
    final List<String> trainClArrTSFiles, final List<String> clDepTSFiles,
    final List<String> clDepToDestTSFiles, final List<String> clArrTSFiles
	) {
    mFcastWarmup = fcastWarmup;
    mFcastLen = fcastLen;
    mFcastFreq = fcastFreq;
    mClDepStates = clDepStates;
    mClArrStates = clArrStates;
    mTrainClDepTSFiles = trainClDepTSFiles;
    mTrainClDepToDestTSFiles = trainClDepToDestTSFiles;
    mTrainClArrTSFiles = trainClArrTSFiles;
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

		// Create Rserve connection
    try {
      // Open connection an load functions
      mRConn = new RConnection();
      final String dir = System.getProperty("user.dir");
      mRConn.voidEval(String.format("setwd(\"%s/src-R/\")", dir));
      mRConn.voidEval("source(\"arrivalFcast.R\")");
      mRConn.voidEval(String.format("setwd(\"%s\")", dir));
      
      // Create variables for training data files
      final int size = mTrainClDepTSFiles.size();
      mRConn.assign("trainClDepRepTSF", mTrainClDepTSFiles.toArray(new String[size]));
      mRConn.assign("trainClDepToDestRepTSF", mTrainClDepToDestTSFiles.toArray(new String[size]));
      mRConn.assign("trainClArrRepTSF", mTrainClArrTSFiles.toArray(new String[size]));
    } catch (RserveException e) {
      e.printStackTrace();
    } catch (REngineException e) {
      e.printStackTrace();
    }

    // Initialise the model
    mCurTSFileIdx = -1;
    mCurTSStartIndex = 0;
	}
	
	 public BikeModelRBridge newInstance() {
	    return new BikeModelRBridge(
	      mFcastWarmup, mFcastLen, mFcastFreq,
	      mClDepStates, mClArrStates,
	      mTrainClDepTSFiles, mTrainClDepToDestTSFiles, mTrainClArrTSFiles,
	      mClDepTSFiles, mClDepToDestTSFiles, mClArrTSFiles
	    );  
	  }
	
	public BikeModelRBridge trainingForecast() {
	  return new BikeModelRBridge(
	    mFcastWarmup, mFcastLen, mFcastFreq,
	    mClDepStates, mClArrStates,
	    mTrainClDepTSFiles, mTrainClDepToDestTSFiles, mTrainClArrTSFiles,
	    mTrainClDepTSFiles, mTrainClDepToDestTSFiles, mTrainClArrTSFiles
	  );  
	}
	
	public void closeConnection() {
	  mRConn.close();
	}
	
	public void genTSDepModel(
	  final String depFcastMode
	) {
	  // Train departure time series
	  try {
      mRConn.voidEval(String.format(
        "depModel <- genDepFcastModel(\"%s\", %d, %d, %d, "+
        "trainClDepRepTSF, trainClDepToDestRepTSF)",
        depFcastMode, mFcastFreq, mFcastWarmup, mFcastLen
      ));
    } catch (RserveException e) {
      e.printStackTrace();
    }
	}
	
	public void genTSArrivalFcastModel(
	  final String arrFcastMode,
	  final int minXreg
	) {
    // Train time series model for arrival forecasts
	  try {
      mRConn.voidEval(String.format(
        "arrModel <- genArrFcastModel(\"%s\", %d, %d, %d, %d,"+
        "trainClDepRepTSF, trainClDepToDestRepTSF, trainClArrRepTSF)",
        arrFcastMode, mFcastFreq, mFcastWarmup, mFcastLen, minXreg
      ));
    } catch (RserveException e) {
      e.printStackTrace();
    }
	}
	
  public double[][] tsArrivalForecast() {
    double data[][] = null;
    try {
      REXP res = mRConn.eval(String.format(
        "fcastArrivalTS(depModel, arrModel, \"%s\", \"%s\", \"%s\")",
        mCurClDepTSFile, mCurClDepToDestTSFile, mCurArrTSFile
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
        "fcastDepartureTS(depModel, \"%s\", \"%s\", %d)",
        mCurClDepTSFile, mCurClDepToDestTSFile, mCurTSStartIndex + 1
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
		for (int cl = 0; cl < mClDepStates.size(); cl++) {
	    double[][] depEvts = new double[data[cl].length][2];
	    for (int t = 0; t < data[cl].length; t++) {
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
  * Prepare departure and arrival time series data for the next
  * time series
  * @return true if we have another departure and arrival time series
  */
  public boolean nextTSFile() {
    // Are we done yet?
    if (mClDepTSFiles.size() <= ++mCurTSFileIdx) {
      return false;
    }
   
    // Get actual cluster arrivals in target area and total arrivals
    mCurClDepTSFile = mClDepTSFiles.get(mCurTSFileIdx);
    mCurClDepToDestTSFile = mClDepToDestTSFiles.get(mCurTSFileIdx);
    mCurArrTSFile = mClArrTSFiles.get(mCurTSFileIdx);
    try {
      mCurClArrTS = mRConn.eval(String.format(
        "fcastOracleArrivalTS(%d, %d, %d, \"%s\") ",
        mFcastFreq, mFcastWarmup, mFcastLen, mCurArrTSFile
      )).asDoubleMatrix();
      mCurClArrTtlTS = mRConn.eval(String.format(
        "fcastOracleArrivalTS(%d, %d, %d, \"%s\") ",
        mFcastFreq, mFcastWarmup, mFcastLen,
        mCurArrTSFile.replace("Arrivals", "ArrivalsTtl")
      )).asDoubles();
    } catch (RserveException e) {
      e.printStackTrace();
    } catch (REXPMismatchException e) {
      e.printStackTrace();
    }

    // We start every analysis from the first observation in the time series
    PCTMCLogging.info(String.format("Interval: %s", mCurArrTSFile));
    mCurTSStartIndex = 0;
    return true;
  }
  
  public int nextIntvl() {
    mCurTSStartIndex += mFcastFreq;
    return mCurTSStartIndex;
  }
	
	public void printFcastResult(
	  Map<State, int[]> clArrMomIndices,
	  double[] fcastClArrivals
	) {
	  processFcastResult(clArrMomIndices, fcastClArrivals, true);
	}
	
  /**
   *
	 */
	public double[][] processFcastResult(
	  Map<State, int[]> clArrMomIndices,
	  double[] fcastClArrivals,
	  boolean print
	) {
	  // Compare actual arrivals with the prediction
	  PCTMCLogging.info("Prediction #" + mCurTSStartIndex);
    PCTMCLogging.increaseIndent();
    double ttlAvg = 0;
    double ttlSD = 0;
    int actArr = 0;
    double[][] retVal = new double[mClArrStates.size()][3];
    for (int clId = 0; clId < mClArrStates.size(); clId++) {
      final int actualClArrivals =
        (int) mCurClArrTS[clId][mCurTSStartIndex / mFcastFreq];
      final int[] indices = clArrMomIndices.get(mClArrStates.get(clId));
      final double fcastClAvg = retVal[clId][0] = fcastClArrivals[indices[0]];
      final double fcastClAvgSq = retVal[clId][2] = fcastClArrivals[indices[1]];
      final double fcastClSD = retVal[clId][1] = 
        Math.sqrt(fcastClAvgSq - fcastClAvg * fcastClAvg);
      ttlAvg += fcastClAvg;
      ttlSD += fcastClSD;
      actArr += actualClArrivals;
      if (print) {
        PCTMCLogging.info(String.format(
          "Cl:%d Mean:%.2f SD:%.2f Actual:%d",
          clId, fcastClAvg, fcastClSD, actualClArrivals
        ));
      }
    }
    // Compute actual cluster arrivals during current time window
    final int ttlArr = (int) mCurClArrTtlTS[mCurTSStartIndex / mFcastFreq];
    if (print) {
      PCTMCLogging.info(String.format(
        "Ttl: Mean:%.2f SD:%.2f Actual:%d All:%d", ttlAvg, ttlSD, actArr, ttlArr
      ));
    }
    PCTMCLogging.decreaseIndent();
    return retVal;
	}
}
