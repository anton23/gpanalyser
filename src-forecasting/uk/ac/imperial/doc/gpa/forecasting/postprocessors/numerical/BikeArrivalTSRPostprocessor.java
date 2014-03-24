package uk.ac.imperial.doc.gpa.forecasting.postprocessors.numerical;

import java.util.HashMap;
import java.util.Map;

import uk.ac.imperial.doc.gpa.plain.representation.PlainPCTMC;
import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.analysis.PCTMCAnalysisPostprocessor;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.NumericalPostprocessor;
import uk.ac.imperial.doc.pctmc.representation.State;

public class BikeArrivalTSRPostprocessor extends NumericalPostprocessor {

  private final String mDepFcastMode;
  private final String mArrFcastMode;
  private final int mMinXreg;
  private final BikeModelRBridge mTSF;
  
  public BikeArrivalTSRPostprocessor(
    final String depFcastMode,
    final String arrFcastMode,
    final int minXreg,
    final BikeModelRBridge tsf
  ) {
    super(tsf.mFcastWarmup + tsf.mFcastLen, 1);
    mDepFcastMode = depFcastMode;
    mArrFcastMode = arrFcastMode;
    mMinXreg = minXreg;
    mTSF = tsf;
  }

  public BikeArrivalTSRPostprocessor(
    final String depFcastMode,
    final String arrFcastMode,
    final int minXreg,
    final BikeModelRBridge tsf,
    Map<String, Object> params
   ) {
    super(tsf.mFcastWarmup + tsf.mFcastLen, 1);
    mDepFcastMode = depFcastMode;
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
    return new BikeArrivalTSRPostprocessor(
      mDepFcastMode, mArrFcastMode, mMinXreg, mTSF.newInstance()
    );
  }

  @Override
  public NumericalPostprocessor getNewPreparedPostprocessor(Constants constants) {
    return new BikeArrivalTSRPostprocessor(
      mDepFcastMode, mArrFcastMode, mMinXreg, mTSF.newInstance()
    );
  }

  @Override
  public void calculateDataPoints(Constants constants) {    
    // Find arrival populations for all clusters
    final Map<State, int[]> clArrMomIndices = new HashMap<State, int[]>();
    for (int clId = 0; clId < mTSF.mClArrStates.size(); clId++) {
      clArrMomIndices.put(mTSF.mClArrStates.get(clId), new int[] {clId, clId});
    }

    mTSF.genTSDepModel(mDepFcastMode);
    mTSF.genTSArrivalFcastModel(mArrFcastMode, mMinXreg);
    double[] intvlArrFcast = new double[mTSF.mClArrStates.size()];
    while (mTSF.nextTSFile(true)) {
      dataPoints = mTSF.tsArrivalForecast();
      int intvl = 0;
      while (intvl < dataPoints[0].length) {
        for (int clId = 0; clId < mTSF.mClArrStates.size(); clId++) {
          intvlArrFcast[clId] = dataPoints[clId][intvl];
        }
        // Forecast vs Reality output predict arrivals and actual arrivals
        // originating from each cluster
        mTSF.processFcastResult(clArrMomIndices, intvlArrFcast, null, true);
        mTSF.nextFcast();
        intvl++;
      }
    }
    mTSF.closeConnection();
  }

}
