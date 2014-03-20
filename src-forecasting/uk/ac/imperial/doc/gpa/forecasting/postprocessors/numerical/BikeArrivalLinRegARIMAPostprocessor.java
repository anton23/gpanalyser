package uk.ac.imperial.doc.gpa.forecasting.postprocessors.numerical;

import java.util.HashMap;
import java.util.Map;

import uk.ac.imperial.doc.gpa.plain.representation.PlainPCTMC;
import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.analysis.PCTMCAnalysisPostprocessor;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.NumericalPostprocessor;
import uk.ac.imperial.doc.pctmc.representation.State;

public class BikeArrivalLinRegARIMAPostprocessor extends NumericalPostprocessor {

  private final String mArrFcastMode;
  private final int mMinXreg;
  private final BikeModelTSRBridge mTSF;
  //private PlainPCTMC mPCTMC;
  
  public BikeArrivalLinRegARIMAPostprocessor(
    final String arrFcastMode,
    final int minXreg,
    final BikeModelTSRBridge tsf
  ) {
    super(tsf.mFcastWarmup + tsf.mFcastLen, 1);
    mArrFcastMode = arrFcastMode;
    mMinXreg = minXreg;
    mTSF = tsf;
  }

  public BikeArrivalLinRegARIMAPostprocessor(  
    final String arrFcastMode,
    final int minXreg,
    final BikeModelTSRBridge tsf,
    Map<String, Object> params
   ) {
    super(tsf.mFcastWarmup + tsf.mFcastLen, 1);
    mArrFcastMode = arrFcastMode;
    mMinXreg = minXreg;
    mTSF = tsf;
  }
  
  protected PlainPCTMC getPlainPCMTC(AbstractPCTMCAnalysis analysis) {
    if (!(analysis.getPCTMC() instanceof PlainPCTMC)) {
      throw new AssertionError(
        "Expected a PlainPCTMC object but did not receive it"
      );
    }
    return (PlainPCTMC) analysis.getPCTMC();
  }
  
  @Override
  public void prepare(AbstractPCTMCAnalysis analysis, Constants constants){
    super.prepare(analysis, constants);
    //mPCTMC = getPlainPCMTC(analysis);
  }
  
  @Override
  public PCTMCAnalysisPostprocessor regenerate() {
    return
      new BikeArrivalLinRegARIMAPostprocessor(mArrFcastMode, mMinXreg, mTSF);
  }

  @Override
  public NumericalPostprocessor getNewPreparedPostprocessor(Constants constants) {
    return
      new BikeArrivalLinRegARIMAPostprocessor(mArrFcastMode, mMinXreg, mTSF);
  }

  @Override
  public void calculateDataPoints(Constants constants) {    
    // Find arrival populations for all clusters
    final Map<State, int[]> clArrMomIndices = new HashMap<State, int[]>();
    for (int clId = 0; clId < mTSF.mClArrStates.size(); clId++) {
      clArrMomIndices.put(mTSF.mClArrStates.get(clId), new int[] {clId, clId});
    }

    String arrModelVar =
      mTSF.genLinRegArimaArrivalFcastModel(mArrFcastMode, mMinXreg);
    double[] intvlArrFcast = new double[mTSF.mClArrStates.size()];
    while (mTSF.nextTSFile()) {
      dataPoints = mTSF.linRegARIMAArrForecast(arrModelVar);
      int intvl = 0;
      while (intvl < dataPoints[0].length) {
        for (int clId = 0; clId < mTSF.mClArrStates.size(); clId++) {
          intvlArrFcast[clId] = dataPoints[clId][intvl];
        }
        // Forecast vs Reality output predict arrivals and actual arrivals
        // originating from each cluster
        mTSF.printFcastResult(clArrMomIndices, intvlArrFcast);
        mTSF.nextIntvl();
        intvl++;
      }
    }
  }

}
