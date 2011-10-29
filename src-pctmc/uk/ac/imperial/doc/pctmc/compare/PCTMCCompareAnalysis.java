package uk.ac.imperial.doc.pctmc.compare;

import java.util.Collection;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;

public class PCTMCCompareAnalysis extends AbstractPCTMCAnalysis {

	private AbstractPCTMCAnalysis analysis1, analysis2;

	public PCTMCCompareAnalysis(AbstractPCTMCAnalysis analysis1,
			AbstractPCTMCAnalysis analysis2) {
		super(analysis1.getPCTMC());
		this.analysis1 = analysis1;
		this.analysis2 = analysis2;
	}

	@Override
	public void prepare(Constants constants) {
		analysis1.prepare(constants);
		analysis2.prepare(constants);
	}

	@Override
	public String toString() {
		return "Compare(" + analysis1.toString() + "," + analysis2.toString()
				+ ")";
	}

	public AbstractPCTMCAnalysis getAnalysis1() {
		return analysis1;
	}

	public AbstractPCTMCAnalysis getAnalysis2() {
		return analysis2;
	}

	@Override
	public void setUsedMoments(
			Collection<CombinedPopulationProduct> combinedProducts) {
		analysis1.setUsedMoments(combinedProducts);
		analysis2.setUsedMoments(combinedProducts);
	}
}
