package uk.ac.imperial.doc.gpa.forecasting.postprocessors.numerical;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.imperial.doc.gpa.forecasting.util.MathExtra;
import uk.ac.imperial.doc.gpa.plain.postprocessors.numerical.InhomogeneousODEAnalysisNumericalPostprocessor;
import uk.ac.imperial.doc.gpa.plain.representation.PlainPCTMC;
import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.analysis.PCTMCAnalysisPostprocessor;
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.PlotDescription;
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
	private final String mFcastMode;
	private final int mFcastWarmup;
	private final int mFcastLen;
	private final int mFcastFreq;
	private final List<State> mClDepStates;
  private final List<String> mClDepTSFiles;
	private final List<State> mClArrStates;
  private final List<String> mClArrTSFiles;

	public ForecastingODEAnalysisNumericalPostprocessor (
	  final double stepSize, final int density,
	  final String fcastMode, final int fcastWarmup,
	  final int fcastLen, final int fcastFreq,
	  final List<State> clDepStates, final List<String> clDepTSFiles,
	  final List<State> clArrStates, final List<String> clArrTSFiles
	) {
		super(fcastWarmup + fcastLen + stepSize, stepSize, density);
		mFcastMode = fcastMode;
		mFcastWarmup = fcastWarmup;
		mFcastLen = fcastLen;
		mFcastFreq = fcastFreq;
		mClDepStates = clDepStates;
		mClDepTSFiles = clDepTSFiles;
		mClArrStates = clArrStates;
		mClArrTSFiles = clArrTSFiles;
	}

	public ForecastingODEAnalysisNumericalPostprocessor (
	  final double stepSize, final int density,
	  final String fcastMode, final int fcastWarmup,
	  final int fcastLen, final int fcastFreq,
	  final List<State> clDepStates, final List<String> clDepTSFiles,
	  final List<State> clArrStates, final List<String> clArrTSFiles,
	  final Map<String, Object> params
	) {
    super(fcastWarmup + fcastLen + stepSize, stepSize, density, params);
    mFcastMode = fcastMode;
    mFcastWarmup = fcastWarmup;
    mFcastLen = fcastLen;
    mFcastFreq = fcastFreq;
    mClDepStates = clDepStates;
    mClDepTSFiles = clDepTSFiles;
    mClArrStates = clArrStates;
    mClArrTSFiles = clArrTSFiles;
	}
	
	protected ForecastingODEAnalysisNumericalPostprocessor(
	  final double stopTime,
	  final double stepSize,
	  final int density,
		final PCTMCODEAnalysis odeAnalysis,
		final JavaODEsPreprocessed preprocessedImplementation,
    final String fcastMode, final int fcastWarmup,
    final int fcastLen, final int fcastFreq,
    final List<State> clDepStates, final List<String> clDepTSFiles,
    final List<State> clArrStates, final List<String> clArrTSFiles
	) {
		super(stopTime, stepSize, density, odeAnalysis, preprocessedImplementation);
    mFcastMode = fcastMode;
    mFcastWarmup = fcastWarmup;
    mFcastLen = fcastLen;
    mFcastFreq = fcastFreq;
    mClDepStates = clDepStates;
    mClDepTSFiles = clDepTSFiles;
    mClArrStates = clArrStates;
    mClArrTSFiles = clArrTSFiles;
	}
	
	@Override
	public PCTMCAnalysisPostprocessor regenerate() {
		return new ForecastingODEAnalysisNumericalPostprocessor(
		  stepSize, density,
		  mFcastMode, mFcastWarmup, mFcastLen, mFcastFreq,
		  mClDepStates, mClDepTSFiles, mClArrStates, mClArrTSFiles
		);
	}

	@Override
	public NumericalPostprocessor getNewPreparedPostprocessor(Constants constants) {
		assert(odeAnalysis != null);
		PCTMCJavaImplementationProvider javaImplementation =
		  new PCTMCJavaImplementationProvider();
		ForecastingODEAnalysisNumericalPostprocessor ret =
		  new ForecastingODEAnalysisNumericalPostprocessor(
		    stopTime, stepSize, density, odeAnalysis,
		    javaImplementation.getPreprocessedODEImplementation(
		      odeAnalysis.getOdeMethod(), constants, momentIndex
		    ),
	      mFcastMode, mFcastWarmup, mFcastLen, mFcastFreq,
	      mClDepStates, mClDepTSFiles, mClArrStates, mClArrTSFiles
		  );
		return ret;
	}
	
	@Override
	public void calculateDataPoints(Constants constants) {
		
		// Do analysis for all time series points
		PlainPCTMC pctmc = getPlainPCMTC(odeAnalysis);	

		// Time series preparation
		TimeSeriesForecast tsf = new TimeSeriesForecast(
		  pctmc, mFcastMode, mFcastWarmup, mFcastLen, mFcastFreq,
      mClDepStates, mClDepTSFiles, mClArrStates, mClArrTSFiles
		);
		
		// Find arrival populations for all clusters
		Map<State, int[]> clArrMomIndicies = new HashMap<State, int[]>();
		for (State arrState : mClArrStates) {
		  PopulationProduct pp = PopulationProduct.getMeanProduct(arrState);
		  clArrMomIndicies.put(
		    arrState,
		    new int[] {
		      odeAnalysis.getMomentIndex().get(
		        new CombinedPopulationProduct(pp)
		      ),
		      odeAnalysis.getMomentIndex().get(
		        new CombinedPopulationProduct(PopulationProduct.getProduct(pp,pp))
		      )
		    } 
		  );
		}

		while (tsf.nextTSFile()) {
			while (true) {
				// Check if there is enough data for the forecast
				// period on the current day
				int[] actualClArrivals = tsf.nextIntvl();
				if (actualClArrivals == null) {break;}
				
				// Do the calculation
				super.calculateDataPoints(constants);
			
				// Forecast vs Reality
				/*
				double forecastArr = MathExtra.twoDecim(dataPoints[dataPoints.length-1][cppArrMeanIndex]);
				double forecastArrSq = MathExtra.twoDecim(dataPoints[dataPoints.length-1][cppArrMeanSqIndex]);
				double forecastStdDev = MathExtra.twoDecim(Math.sqrt(forecastArrSq - forecastArr*forecastArr));
				double[] data = {forecastArr, forecastStdDev, actualArr};
				
				// Compute what the normalised distance between forecast and actual number of arrivals
				double normActArr = Math.abs(actualArr - forecastArr)/forecastStdDev;
				System.out.println (forecastArr + ", stdDev " + forecastStdDev + " actual arrivals: " + actualArr + "\t Normalised Dist: "+normActArr);
				*/
			}
		}
	}
	
	@Override
	public void postprocessAnalysis(
	  Constants constants,
		AbstractPCTMCAnalysis analysis,
		List<PlotDescription> plotDescriptions)
	{
		prepare(analysis, constants);
		calculateDataPoints(constants);
	}
}
