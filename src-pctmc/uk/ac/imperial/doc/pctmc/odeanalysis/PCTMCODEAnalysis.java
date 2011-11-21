package uk.ac.imperial.doc.pctmc.odeanalysis;

import java.util.Collection;
import java.util.HashSet;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;
import uk.ac.imperial.doc.pctmc.statements.odeanalysis.ODEMethod;

public class PCTMCODEAnalysis extends AbstractPCTMCAnalysis {

	@Override
	public String toString() {
		return "ODEs";
	}

	private int order;

	@Override
	public void setUsedMoments(
			Collection<CombinedPopulationProduct> combinedProducts) {
		usedCombinedProducts = new HashSet<CombinedPopulationProduct>(
				combinedProducts);
		for (CombinedPopulationProduct product : combinedProducts) {
			int o = product.getOrder();
			if (o > order)
				order = o;
		}
	}

	public PCTMCODEAnalysis(PCTMC pctmc) {
		super(pctmc);
		order = 1;
	}

	public PCTMCODEAnalysis(PCTMC pctmc, int order) {
		this(pctmc);
		this.order = order;
	}

	//private ODEGenerator odeGenerator;
	private NewODEGenerator odeGenerator;

	@Override
	public void prepare(Constants variables) {
		//this.odeGenerator = new ODEGenerator(pctmc);
		//odeMethod = odeGenerator.getODEMethodWithCombinedMoments(order, usedCombinedProducts);
		this.odeGenerator = new NewODEGenerator(pctmc, new NormalMomentClosure(order));
		odeMethod = odeGenerator.getODEMethodWithCombinedMoments(usedCombinedProducts);
		momentIndex = odeGenerator.getMomentIndex();
	}
	
	private ODEMethod odeMethod;

	public ODEMethod getOdeMethod() {
		return odeMethod;
	}
}
