package uk.ac.imperial.doc.gpa.forecasting.postprocessors.numerical;
import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;


public class PCTMCTSR extends AbstractPCTMCAnalysis {

  private final String sArrFcastMode;
  public PCTMCTSR(
    final PCTMC pctmc,
    final String arrFcastMode
  ) {
    super(pctmc);
    sArrFcastMode = arrFcastMode;
  }

  @Override
  public String toString() {
    return "R time series prediction with " + sArrFcastMode + " model";
  }

  @Override
  public AbstractPCTMCAnalysis regenerate(
    final PCTMC pctmc
  ) {
    return new PCTMCTSR(pctmc, sArrFcastMode);
  }

  @Override
  public void prepare(final Constants constants) {}

}
