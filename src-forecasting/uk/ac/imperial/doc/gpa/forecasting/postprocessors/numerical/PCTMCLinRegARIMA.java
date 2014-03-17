package uk.ac.imperial.doc.gpa.forecasting.postprocessors.numerical;
import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;


public class PCTMCLinRegARIMA extends AbstractPCTMCAnalysis {

  public PCTMCLinRegARIMA(PCTMC pctmc) {
    super(pctmc);
  }

  @Override
  public String toString() {
    return "Approximating IPCTMC using linear regression with arima error";
  }

  @Override
  public AbstractPCTMCAnalysis regenerate(PCTMC pctmc) {
    return new PCTMCLinRegARIMA(pctmc);
  }

  @Override
  public void prepare(Constants constants) {}

}
