package uk.ac.imperial.doc.gpa.forecasting.postprocessors.numerical;
import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;


public class PCTMCLinRegARIMA extends AbstractPCTMCAnalysis {

  private final String sArrFcastMode;
  public PCTMCLinRegARIMA(
    final PCTMC pctmc,
    final String arrFcastMode
  ) {
    super(pctmc);
    sArrFcastMode = arrFcastMode;
  }

  @Override
  public String toString() {
    return "Arrival time series prediction with " + sArrFcastMode + " model";
  }

  @Override
  public AbstractPCTMCAnalysis regenerate(
    final PCTMC pctmc
  ) {
    return new PCTMCLinRegARIMA(pctmc, sArrFcastMode);
  }

  @Override
  public void prepare(final Constants constants) {}

}
