package uk.ac.imperial.doc.gpa.forecasting.postprocessors.numerical;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

import uk.ac.imperial.doc.gpa.plain.representation.PlainPCTMC;
import uk.ac.imperial.doc.pctmc.representation.State;
import uk.ac.imperial.doc.pctmc.utils.PCTMCLogging;

public class BikeModelRBridge {

  private RConnection mRConn;
  
  // These are the input fields
  public final int mFcastFreq;
  public final int mFcastWarmup;
  public final int mFcastLen;
  public final List<State> mClDepStates;
  public final List<State> mClArrStates;
  private final List<String> mTrainClDepTSFiles;
  private final List<String> mTrainClDepToDestTSFiles;
  private final List<String> mTrainClArrTSFiles;
  private final List<String> mClDepTSFiles;
  private final List<String> mClDepToDestTSFiles;
  private final List<String> mClArrTSFiles;
  
  // These fields may change during analysis
  private boolean mLogging = true;
  private String mCurClDepTSFile;
  private String mCurClDepToDestTSFile;
  private String mCurArrTSFile;
  private double[][] mCurClArrTS;
  private double[] mCurClArrTtlTS;
  private int mCurTSFileIdx;
  private int mCurTSStartIndex;
  
	public BikeModelRBridge(
	  final int fcastFreq, final int fcastWarmup, final int fcastLen,
    final List<State> clDepStates, final List<State> clArrStates,
    final List<String> trainClDepTSFiles,
    final List<String> trainClDepToDestTSFiles,
    final List<String> trainClArrTSFiles, final List<String> clDepTSFiles,
    final List<String> clDepToDestTSFiles, final List<String> clArrTSFiles
	) {
    mFcastFreq = fcastFreq;
    mFcastWarmup = fcastWarmup;
    mFcastLen = fcastLen;
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

    // Initialise the series
    mCurTSFileIdx = -1;
    mCurTSStartIndex = 0;
	}
	
	public void setLogging(boolean logging) {
	  mLogging = logging;
	}
	
	/**
	 * @return a duplicate instance with its on R connection
	 */
	public BikeModelRBridge newInstance() {
	  return new BikeModelRBridge(
	    mFcastFreq, mFcastWarmup, mFcastLen,
	    mClDepStates, mClArrStates,
	    mTrainClDepTSFiles, mTrainClDepToDestTSFiles, mTrainClArrTSFiles,
	    mClDepTSFiles, mClDepToDestTSFiles, mClArrTSFiles
	  );  
	}
	
  /**
   * @return a duplicate instance with its on R connection
   */
  public BikeModelRBridge newInstance(
    final int fcastLen    
  ) {
    return new BikeModelRBridge(
      mFcastFreq, mFcastWarmup, fcastLen,
      mClDepStates, mClArrStates,
      mTrainClDepTSFiles, mTrainClDepToDestTSFiles, mTrainClArrTSFiles,
      mClDepTSFiles, mClDepToDestTSFiles, mClArrTSFiles
    );  
  }
	
	 /**
   * @return a duplicate instance with its on R connection where training and
   *   forecast are done on the same data
   */
	public BikeModelRBridge trainingForecast(
	    final int fcastWarmup,
	    final int fcastLen
	  ) {
	  return new BikeModelRBridge(
	    mFcastFreq, fcastWarmup, fcastLen,
	    mClDepStates, mClArrStates,
	    mTrainClDepTSFiles, mTrainClDepToDestTSFiles, mTrainClArrTSFiles,
	    mTrainClDepTSFiles, mTrainClDepToDestTSFiles, mTrainClArrTSFiles
	  );  
	}
	
	/**
	 * Close the R connection
	 */
	public void closeConnection() {
	  mRConn.close();
	}
	
	/**
	 * Train departure time series model for each cluster on the training data
	 * 
	 * @param depFcastMode name of method we use to forecast future departures
	 */
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
	
	 /**
   * Train arrival time series model for each cluster on the training data
   * 
   * @param arrFcastMode name of method we use to forecast future arrivals
   * @param minXreg regress on departures that happen in interval
   *        [fcastTime - minXreg, fcastTime]
   */
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

  public void genErrorARIMA(
    final LinkedList<LinkedList<double[]>> clErrorRepTS,
    final int fcastLen
  ) {
    // Train models for cluster error time series
    double[][][] clError = new double[clErrorRepTS.getLast().getLast().length]
      [clErrorRepTS.size()][clErrorRepTS.getLast().size()];
    for (int cl = 0; cl < clError.length; cl++) {
      for (int rep = 0; rep < clError[0].length; rep++) {
        for (int obs = 0; obs < clError[0][0].length; obs++) {
          clError[cl][rep][obs] = clErrorRepTS.get(rep).get(obs)[cl];
        }
      }
    }
    try {
      // Load replicated error time series into R
      mRConn.voidEval(String.format(
        "trainClErrorRepTS <- array(dim = c(%d, %d, %d))",
        clError.length, clError[0].length, clError[0][0].length
      ));
      for (int clId = 0; clId < clError.length; clId++) {
        mRConn.assign("errorTSTmp", REXP.createDoubleMatrix(clError[clId]));
        mRConn.voidEval(String.format(
          "trainClErrorRepTS[%d,,] <- errorTSTmp", clId + 1
        ));
      }
      mRConn.voidEval(
        "trainClErrorRepTS <- aperm(trainClErrorRepTS, c(2,1,3))"
      );
      mRConn.voidEval(String.format(
        "errModel <- genARIMARepError(%d, %d, %d, trainClErrorRepTS)",
        mFcastFreq, mFcastWarmup, fcastLen
      ));
    } catch (RserveException e) {
      e.printStackTrace();
    }
  }
  
  public double[] calcErrorCorrection(LinkedList<double[]> clErrors) {
    double[] retVal = null;
    try {
      // Load replicated error time series into R
      mRConn.voidEval(String.format(
        "clErrorTS <- matrix(nrow = %d, ncol = 0)", mClDepStates.size()
      ));
      if (!clErrors.isEmpty()) {
        double[][] clErr = new double[clErrors.get(0).length][clErrors.size()];
        for (int clId = 0; clId < clErr.length; clId++) {
          for (int obs = 0; obs < clErr[0].length; obs++) {
            clErr[clId][obs] = clErrors.get(obs)[clId];
          }
        }
        mRConn.assign("clErrorTS", REXP.createDoubleMatrix(clErr));
      }
      REXP ret = mRConn.eval(String.format(
        "fcastError(errModel, clErrorTS)"
      ));
      retVal = ret.asDoubles();
    } catch (RserveException e) {
      e.printStackTrace();
    } catch (REXPMismatchException e) {
      e.printStackTrace();
    }
    return retVal;
  }
	
	/**
	 * Set departure and reset events for {@code pctmc} for current interval
	 * 
	 * @pctmc forecast model for which we want to set the events
	 * 
	 * @return true iff there it is possible to make a forecast for the interval
	 */
	public boolean loadPCTMCEvents(final PlainPCTMC pctmc) {
		// Prepare pop changes in inhomogenous PCTMC
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
	    final double[][] depEvts = new double[data[cl].length][2];
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
		pctmc.getTimedEvents().setEvents(
		  new HashMap<String, double[][]>(), allDepEvents, allResetEvents
		);

		return true;
	}

	/**
	 * @return time series arrival forecast for current interval
	 */
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
  * Prepare departure and arrival time series data for next time series file
  * 
  * @return true iff we have another departure and arrival time series
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
    if (mLogging) {
      PCTMCLogging.info(String.format("Interval: %s", mCurArrTSFile));
    }
    mCurTSStartIndex = 0;
    return true;
  }
  
  /**
   * Advance to next forecast
   * 
   * @return forecast interval start index in minutes
   */
  public int nextFcast() {
    mCurTSStartIndex += mFcastFreq;
    return mCurTSStartIndex;
  }
	
  /**
   * @param clArrMomIndices indicies of cluster distribution moments in
   *   {@code fcastClArrivals}
   * @param fcastClArrivals results
   * @param print iff true log results
   * @return forecast results
   */
	public double[] processFcastResult(
	  Map<State, int[]> clArrMomIndices,
	  double[] fcastClArrivals,
	  double[] clFcastError
	) {
	  // Compare actual arrivals with the prediction
	  if (mLogging) {
	    PCTMCLogging.info("Prediction #" + mCurTSStartIndex);
	    PCTMCLogging.increaseIndent();
	  }
    double ttlAvg = 0;
    double ttlSD = 0;
    int actArr = 0;
    double[] curError = new double[mClArrStates.size()];
    for (int clId = 0; clId < mClArrStates.size(); clId++) {
      final int[] indices = clArrMomIndices.get(mClArrStates.get(clId));
      // Get moments
      double fcastClAvg = fcastClArrivals[indices[0]];
      final double fcastClAvgSq = fcastClArrivals[indices[1]];
      final double fcastClSD =
        Math.sqrt(fcastClAvgSq - fcastClAvg * fcastClAvg);
      
      // Correct mean error
      if (clFcastError != null) {
        fcastClAvg = Math.max(fcastClAvg + clFcastError[clId], 0);
      }
      
      // Error analysis
      final int actualClArrivals =
        (int) mCurClArrTS[clId][mCurTSStartIndex / mFcastFreq];
      curError[clId] = fcastClAvg - actualClArrivals;
      
      // Global cluster stats
      ttlAvg += fcastClAvg;
      ttlSD += fcastClSD;
      actArr += actualClArrivals;
      if (mLogging) {
        PCTMCLogging.info(String.format(
          "Cl:%d Mean:%.2f SD:%.2f Actual:%d",
          clId, fcastClAvg, fcastClSD, actualClArrivals
        ));
      }
    }
    // Compute actual cluster arrivals during current time window
    final int ttlArr = (int) mCurClArrTtlTS[mCurTSStartIndex / mFcastFreq];
    if (mLogging) {
      PCTMCLogging.info(String.format(
        "Ttl: Mean:%.2f SD:%.2f Actual:%d All:%d", ttlAvg, ttlSD, actArr, ttlArr
      ));
      PCTMCLogging.decreaseIndent();
    }
    return curError;
	}
}
