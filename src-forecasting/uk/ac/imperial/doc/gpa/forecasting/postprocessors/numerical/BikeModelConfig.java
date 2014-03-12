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
  private RConnection mRConn;
  private final List<String> mClDepTSFiles;
  private final List<String> mClDepToDestTSFiles;
  private final List<String> mClArrTSFiles;
  
  // These fields may change during analysis
  private String curClDepTSFile;
  private String curClDepToDestTSFile;
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
    String[] depTSTrainFiles = new String[trainClDepTSFiles.size()];
    trainClDepTSFiles.toArray(depTSTrainFiles);
    String[] depToDestTSTrainFiles = new String[trainClDepToDestTSFiles.size()];
    trainClDepToDestTSFiles.toArray(depToDestTSTrainFiles);
    try {
      mRConn = new RConnection();
      // Train the model departure time series model
      mRConn.eval(String.format("setwd(\"%s/src-R/\")", dir));
      mRConn.eval("source(\"departureFcast.R\")");
      mRConn.assign("trainDepTSFiles", depTSTrainFiles);
      mRConn.assign("trainDepToDestTSFiles", depToDestTSTrainFiles);
      mRConn.eval(String.format(
        "model <- genDepFcastModel("+
          "\"%s\", %d, %d, %d, trainDepTSFiles, trainDepToDestTSFiles)",
        depFcastMode, mFcastFreq, mFcastWarmup, mFcastLen
      ));
    } catch (RserveException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (REngineException e) {
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
    curClDepTSFile = mClDepTSFiles.remove(0);
		curClDepToDestTSFile = mClDepToDestTSFiles.remove(0);
		final String clArrTSRaw =
		  FileExtra.readFromTextFile(mClArrTSFiles.remove(0));
		final int numTSPoints = clArrTSRaw.split("\n")[0].split(" ").length;
		
		// Load observed cluster departures and arrivals for the day
    String[] clArr = clArrTSRaw.split("\n");
    mClArrTS = new int[numCl][numTSPoints];
		for (int cl = 0; cl < numCl; cl++) {
      String[] curClArr = clArr[cl].split(" ");
			for (int i=0; i < numTSPoints; ++i) {
			  mClArrTS[cl][i] = Integer.parseInt(curClArr[i]);
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

		// Prepare pop changes in inhomogenous PCTMC
    final Map <String, double[][]> allRateEvents =
      new HashMap<String, double[][]>();
		final Map <State, double[][]> allDepEvents =
		  new HashMap<State, double[][]>();
		final Map <State, double[][]> allResetEvents =
		  new HashMap<State, double[][]>();
		final int[] actualClArrivals = new int[numCl];
		
		double[][] data = null;
	  try {
      REXP res = mRConn.eval(String.format(
        "fcastDepartureTS(model, \"../%s\", \"../%s\", %d)",
        curClDepTSFile, curClDepToDestTSFile, mTSStartIndex + 1
      ));
      if (!res.isNull()) {
        data = res.asDoubleMatrix();
      }
    } catch (RserveException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (REXPMismatchException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
	  // Not enough data in time series to do a forecast
	  if (data == null) {return null;}
		
		// Time dependent departures and arrival count resets for each cluster
		final int tsEndPoint = mFcastWarmup + mFcastLen;
		for (int cl = 0; cl < numCl; cl++) {
	    double[][] depEvts = new double[tsEndPoint + 1][2];
	    for (int t = 0; t < tsEndPoint; t++) {
	      depEvts[t][0] = t;
	      depEvts[t][1] = data[cl][t];
	    }
		  // New cluster departures
		  allDepEvents.put(mClDepStates.get(cl), depEvts);
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
    double ttlAvg = 0;
    double ttlSD = 0;
    int actArr = 0;
    for (int cl = 0; cl < mClArrStates.size(); cl++) {
      final int[] indices = clArrMomIndices.get(mClArrStates.get(cl));
      final double fcastClAvg =
          fcastClArrivals[fcastClArrivals.length - 1][indices[0]];
      final double fcastClAvgSq =
          fcastClArrivals[fcastClArrivals.length - 1][indices[1]];
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
      "Ttl: Mean:%.2f SD:%.2f Actual:%d", ttlAvg, ttlSD, actArr
    ));
    PCTMCLogging.decreaseIndent();
	}
}
