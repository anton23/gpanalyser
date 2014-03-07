package uk.ac.imperial.doc.gpa.forecasting.postprocessors.numerical;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	private final int mFcastWarmup;
	private final int mFcastLen;
	private final int mFcastFreq;
	private final List<State> mClDepStates;
	private final List<State> mClArrStates;
	private final String mDepFcastMode;
  private final List<String> mTrainClDepTSFiles;
  private final List<String> mTrainClMuTSFiles;
  private final List<String> mClDepTSFiles;
  private final List<String> mClMuTSFiles;
  private final List<String> mClArrTSFiles;

	public ForecastingODEAnalysisNumericalPostprocessor (
	  final double stepSize, final int density,
	  final int fcastWarmup, final int fcastLen, final int fcastFreq,
	  final List<State> clDepStates, final List<State> clArrStates,
	  final String depFcastMode, final List<String> trainClDepTSFiles,
	  final List<String> trainClMuTSFiles, final List<String> clDepTSFiles,
    final List<String> clMuTSFiles, final List<String> clArrTSFiles
	) {
		super(fcastWarmup + fcastLen + stepSize, stepSize, density);
		mFcastWarmup = fcastWarmup;
		mFcastLen = fcastLen;
		mFcastFreq = fcastFreq;
		mClDepStates = clDepStates;
    mClArrStates = clArrStates;
    mDepFcastMode = depFcastMode;
    mTrainClDepTSFiles = trainClDepTSFiles;
    mTrainClMuTSFiles = trainClMuTSFiles;
    mClDepTSFiles = clDepTSFiles;
		mClMuTSFiles = clMuTSFiles;
		mClArrTSFiles = clArrTSFiles;
	}

	public ForecastingODEAnalysisNumericalPostprocessor (
	  final double stepSize, final int density,
    final int fcastWarmup, final int fcastLen, final int fcastFreq,
    final List<State> clDepStates, final List<State> clArrStates,
    final String depFcastMode, final List<String> trainClDepTSFiles,
    final List<String> trainClMuTSFiles, final List<String> clDepTSFiles,
    final List<String> clMuTSFiles, final List<String> clArrTSFiles,
	  final Map<String, Object> params
	) {
    super(fcastWarmup + fcastLen + stepSize, stepSize, density, params);
    mFcastWarmup = fcastWarmup;
    mFcastLen = fcastLen;
    mFcastFreq = fcastFreq;
    mClDepStates = clDepStates;
    mClArrStates = clArrStates;
    mDepFcastMode = depFcastMode;
    mTrainClDepTSFiles = trainClDepTSFiles;
    mTrainClMuTSFiles = trainClMuTSFiles;
    mClDepTSFiles = clDepTSFiles;
    mClMuTSFiles = clMuTSFiles;
    mClArrTSFiles = clArrTSFiles;
	}
	
	protected ForecastingODEAnalysisNumericalPostprocessor(
	  final double stopTime,
	  final double stepSize,
	  final int density,
		final PCTMCODEAnalysis odeAnalysis,
		final JavaODEsPreprocessed preprocessedImplementation,
    final int fcastWarmup, final int fcastLen, final int fcastFreq,
    final List<State> clDepStates, final List<State> clArrStates,
    final String depFcastMode, final List<String> trainClDepTSFiles,
    final List<String> trainClMuTSFiles, final List<String> clDepTSFiles,
    final List<String> clMuTSFiles, final List<String> clArrTSFiles
	) {
		super(stopTime, stepSize, density, odeAnalysis, preprocessedImplementation);
    mFcastWarmup = fcastWarmup;
    mFcastLen = fcastLen;
    mFcastFreq = fcastFreq;
    mClDepStates = clDepStates;
    mClArrStates = clArrStates;
    mDepFcastMode = depFcastMode;
    mTrainClDepTSFiles = trainClDepTSFiles;
    mTrainClMuTSFiles = trainClMuTSFiles;
    mClDepTSFiles = clDepTSFiles;
    mClMuTSFiles = clMuTSFiles;
    mClArrTSFiles = clArrTSFiles;
	}
	
	@Override
	public PCTMCAnalysisPostprocessor regenerate() {
		return new ForecastingODEAnalysisNumericalPostprocessor(
		  stepSize, density,
		  mFcastWarmup, mFcastLen, mFcastFreq,
		  mClDepStates, mClArrStates,
		  mDepFcastMode, mTrainClDepTSFiles, mTrainClMuTSFiles,
		  mClDepTSFiles, mClMuTSFiles, mClArrTSFiles
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
	      mFcastWarmup, mFcastLen, mFcastFreq,
	      mClDepStates, mClArrStates,
	      mDepFcastMode, mTrainClDepTSFiles, mTrainClMuTSFiles,
	      mClDepTSFiles, mClMuTSFiles, mClArrTSFiles
		  );
		return ret;
	}
	
	@Override
	public void calculateDataPoints(Constants constants) {
		// Do analysis for all time series points
		PlainPCTMC pctmc = getPlainPCMTC(odeAnalysis);	

		// Time series preparation
		TimeSeriesForecast tsf = new TimeSeriesForecast(
	    pctmc, mFcastWarmup, mFcastLen, mFcastFreq,
	    mClDepStates, mClArrStates,
	    mDepFcastMode, mTrainClDepTSFiles, mTrainClMuTSFiles,
	    mClDepTSFiles, mClMuTSFiles, mClArrTSFiles
		);
		
		// Find arrival populations for all clusters
		final Map<State, int[]> clArrMomIndices = new HashMap<State, int[]>();
		for (final State arrState : mClArrStates) {
		  final PopulationProduct pp = PopulationProduct.getMeanProduct(arrState);
		  clArrMomIndices.put(
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
				final int[] actualClArrivals = tsf.nextIntvl();
				if (actualClArrivals == null) {break;}
				
				// Make a prediction
				super.calculateDataPoints(constants);

				// Forecast vs Reality output predict arrivals and actual arrivals
				// originating from each cluster
				tsf.printFcastResult(
				  clArrMomIndices,
				  dataPoints,
				  actualClArrivals
				);
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
