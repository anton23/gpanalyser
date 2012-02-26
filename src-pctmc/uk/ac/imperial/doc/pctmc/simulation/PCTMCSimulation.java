package uk.ac.imperial.doc.pctmc.simulation;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.PopulationProduct;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class PCTMCSimulation extends AbstractPCTMCAnalysis {



	@Override
	public String toString() {
		return "Simulation"; 
	}
	
	public PCTMCSimulation(PCTMC pctmc) {
		super(pctmc);
		n = pctmc.getStateIndex().size();
	}

	private BiMap<PopulationProduct, Integer> accumulatedMomentIndex;
	
	

	public BiMap<PopulationProduct, Integer> getAccumulatedMomentIndex() {
		return accumulatedMomentIndex;
	}

	private void prepareAccumulatedIndex() {
		int j = 0;
		accumulatedMomentIndex = HashBiMap.<PopulationProduct, Integer> create();
		for (CombinedPopulationProduct combinedProduct : usedCombinedProducts) {
			for (PopulationProduct accumulatedProduct : combinedProduct
					.getAccumulatedProducts()) {
				if (!accumulatedMomentIndex.containsKey(accumulatedProduct)) 
					accumulatedMomentIndex.put(accumulatedProduct, j++);
			}
		}
	}
	
	private void prepareGeneralExpectationIndex(){
		int j = 0; 
		generalExpectationIndex = HashBiMap.<AbstractExpression,Integer>create(); 
		for (AbstractExpression eg: usedGeneralExpectations){
			generalExpectationIndex.put(eg,j++);
		}
	}

	int n;

	public void prepare(Constants variables) {		
		prepareAccumulatedIndex();
		prepareGeneralExpectationIndex();

			}	
}
