package uk.ac.imperial.doc.gpa.forecasting.postprocessors.numerical;

import java.util.HashMap;
import java.util.LinkedList;
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

public class BikeArrivalODEPostprocessor extends
  InhomogeneousODEAnalysisNumericalPostprocessor
{	
  private final String mDepFcastMode;
  private final BikeModelRBridge mTSF;

	public BikeArrivalODEPostprocessor (
	  final double stepSize,
	  final int density,
	  final String depFcastMode,
    final BikeModelRBridge tsf
	) {
    super(tsf.mFcastWarmup + tsf.mFcastLen + stepSize, stepSize, density);
    mDepFcastMode = depFcastMode;
    mTSF = tsf;
	}

	public BikeArrivalODEPostprocessor (
	  final double stepSize,
	  final int density,
	  final String depFcastMode,
    final BikeModelRBridge tsf,
    Map<String, Object> params
	) {
    super(
      tsf.mFcastWarmup + tsf.mFcastLen + stepSize,
      stepSize, density, params
    );
    mDepFcastMode = depFcastMode;
    mTSF = tsf;
	}
	
	protected BikeArrivalODEPostprocessor(
	  final double stopTime,
	  final double stepSize,
	  final int density,
		final PCTMCODEAnalysis odeAnalysis,
		final JavaODEsPreprocessed preprocessedImplementation,
    final String depFcastMode,
    final BikeModelRBridge tsf
	) {
		super(stopTime, stepSize, density, odeAnalysis, preprocessedImplementation);
    mDepFcastMode = depFcastMode;
		mTSF = tsf;
	}
	
	@Override
	public PCTMCAnalysisPostprocessor regenerate() {
		return new BikeArrivalODEPostprocessor(
		  stepSize, density, mDepFcastMode, mTSF.newInstance()
		);
	}

	@Override
	public NumericalPostprocessor getNewPreparedPostprocessor(Constants constants) {
		assert(odeAnalysis != null);
		PCTMCJavaImplementationProvider javaImplementation =
		  new PCTMCJavaImplementationProvider();
		BikeArrivalODEPostprocessor ret =
		  new BikeArrivalODEPostprocessor(
		    stopTime, stepSize, density, odeAnalysis,
		    javaImplementation.getPreprocessedODEImplementation(
		      odeAnalysis.getOdeMethod(), constants, momentIndex
		    ),
		    mDepFcastMode,
	      mTSF.newInstance()
		  );
		return ret;
	}
	
	@Override
	public void calculateDataPoints(Constants constants) {
		// Do analysis for all time series points
		PlainPCTMC pctmc = getPlainPCMTC(odeAnalysis);
		
		// Find arrival populations for all clusters
		final Map<State, int[]> clArrMomIndices = new HashMap<State, int[]>();
		for (final State arrState : mTSF.mClArrStates) {
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

		// Train the error model with oracle departure knowledge.
		// Also we only predict next frequency step - if we predicted the actual
		// fcastLen our error model would need to look into the future to work on the
		// unseen data. Clearly this would mean that the forecast would no longer be
		// out of sample so we can't do this.
    final int stopTimeIdx =
      (int) ((mTSF.mFcastWarmup + mTSF.mFcastFreq + stepSize) / stepSize);
		final BikeModelRBridge trainTSF =
		  mTSF.trainingForecast(mTSF.mFcastWarmup, mTSF.mFcastFreq);
		trainTSF.genTSDepModel("oracle");
		LinkedList<LinkedList<double[]>> resError =
		  new LinkedList<LinkedList<double[]>>();
    while (trainTSF.nextTSFile(false)) {
      resError.add(new LinkedList<double[]>());
      while (trainTSF.loadPCTMCEvents(pctmc)) {
        // Forecast
        super.calculateDataPoints(constants);
        resError.getLast().add(trainTSF.processFcastResult(
          clArrMomIndices, dataPoints[stopTimeIdx], null, false
        ));
        trainTSF.nextFcast();
      }
    }
    double[][][] error = new double[resError.getLast().getLast().length]
      [resError.size()][resError.getLast().size()];
    for (int cl = 0; cl < error.length; cl++) {
      for (int rep = 0; rep < error[0].length; rep++) {
        for (int obs = 0; obs < error[0][0].length; obs++) {
          error[cl][rep][obs] = resError.get(rep).get(obs)[cl];
        }
      }
    }
    trainTSF.genErrorARIMA(error, mTSF.mFcastLen);

    // Forecast
    mTSF.genTSDepModel(mDepFcastMode);
    final BikeModelRBridge mTSFShort = mTSF.newInstance(mTSF.mFcastFreq);
		while (mTSF.nextTSFile(true) && mTSFShort.nextTSFile(false)) {
		  LinkedList<double[]> clErrors = new LinkedList<double[]>();
			while (mTSF.loadPCTMCEvents(pctmc)) {
			  // Predict error
        double[] clFcastError = trainTSF.calcErrorCorrection(clErrors);
			  
        // Forecast
        super.calculateDataPoints(constants);

        // Correct current result using predicted error
        // Note: We are not using current error!
        mTSF.processFcastResult(    
          clArrMomIndices, dataPoints[dataPoints.length - 1], clFcastError, true
        );
        
        // Store current fcast error for next prediction
        clErrors.add(mTSFShort.processFcastResult(
          clArrMomIndices, dataPoints[stopTimeIdx], null, false
        ));

        mTSF.nextFcast();
        mTSFShort.nextFcast();
			}
		}
		mTSF.closeConnection();
    trainTSF.closeConnection();
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
