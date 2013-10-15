package uk.ac.imperial.doc.pctmc.simulation;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.CollectUsedMomentsVisitor;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.representation.EvolutionEvent;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;
import uk.ac.imperial.doc.pctmc.representation.accumulations.AccumulationVariable;

public class PCTMCSimulation extends AbstractPCTMCAnalysis {

	@Override
	public AbstractPCTMCAnalysis regenerate(PCTMC pctmc) {
		return new PCTMCSimulation(pctmc);
	}

	@Override
	public String toString() {
		return "Simulation"; 
	}
	
	public PCTMCSimulation(PCTMC pctmc) {
		super(pctmc);
		if (pctmc != null) {
			n = pctmc.getStateIndex().size();
		}
	}

	private Map<AccumulationVariable, Integer> accumulatedMomentIndex;
	
	

	public Map<AccumulationVariable, Integer> getAccumulatedMomentIndex() {
		return accumulatedMomentIndex;
	}

	private void prepareAccumulatedIndex() {
		int j = 0;
		accumulatedMomentIndex = new HashMap<AccumulationVariable, Integer>();
		Collection<CombinedPopulationProduct> requiredProducts = new HashSet<CombinedPopulationProduct>(usedCombinedProducts);
		for (EvolutionEvent event:pctmc.getEvolutionEvents()) {
			CollectUsedMomentsVisitor visitor = new CollectUsedMomentsVisitor();
			event.getRate().accept(visitor);
			requiredProducts.addAll(visitor.getUsedCombinedMoments());
		}
		for (CombinedPopulationProduct combinedProduct : requiredProducts) {
			for (AccumulationVariable accumulatedProduct : combinedProduct
					.getAccumulatedProducts()) {
				if (!accumulatedMomentIndex.containsKey(accumulatedProduct)) 
					accumulatedMomentIndex.put(accumulatedProduct, j++);
			}
		}
	}
	
	private void prepareGeneralExpectationIndex(){
		int j = 0; 
		generalExpectationIndex = new HashMap<AbstractExpression,Integer>(); 
		for (AbstractExpression eg: usedGeneralExpectations){
			generalExpectationIndex.put(eg,j++);
		}
	}

	int n;

    @Override
	public void prepare(Constants variables) {		
		prepareAccumulatedIndex();
		prepareGeneralExpectationIndex();
	}
}
