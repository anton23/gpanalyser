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

public class BikeArrivalODEPostprocessor extends
  InhomogeneousODEAnalysisNumericalPostprocessor
{	
  private final BikeModelRBridge mTSF;

	public BikeArrivalODEPostprocessor (
	  final double stepSize, final int density,
    final BikeModelRBridge tsf
	) {
    super(tsf.mFcastWarmup + tsf.mFcastLen + stepSize, stepSize, density);
    mTSF = tsf;
	}

	public BikeArrivalODEPostprocessor (
	  final double stepSize, final int density,
    final BikeModelRBridge tsf,
    Map<String, Object> params
	) {
    super(
      tsf.mFcastWarmup + tsf.mFcastLen + stepSize,
      stepSize, density, params
    );
    mTSF = tsf;
	}
	
	protected BikeArrivalODEPostprocessor(
	  final double stopTime,
	  final double stepSize,
	  final int density,
		final PCTMCODEAnalysis odeAnalysis,
		final JavaODEsPreprocessed preprocessedImplementation,
    final BikeModelRBridge tsf
	) {
		super(stopTime, stepSize, density, odeAnalysis, preprocessedImplementation);
		mTSF = tsf;
	}
	
	@Override
	public PCTMCAnalysisPostprocessor regenerate() {
		return new BikeArrivalODEPostprocessor(stepSize, density, mTSF);
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
	      mTSF
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

		while (mTSF.nextTSFile()) {
			while (true) {
        // Check if there is enough data for the forecast
        // period on the current day
        if (!mTSF.preparePCTMCForCurIntvlPCTMC(pctmc)) {break;}
        
        // Do the calculation
        super.calculateDataPoints(constants);

        // Forecast vs Reality output predict arrivals and actual arrivals
        // originating from each cluster
        mTSF.printFcastResult(
          clArrMomIndices, dataPoints[dataPoints.length - 1]
        );
        mTSF.nextIntvl();
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
