package uk.ac.imperial.doc.pctmc.analysis;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.PlotDescription;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;
import uk.ac.imperial.doc.pctmc.representation.State;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/*
 * Abstract class for transient analysis of PCTMCs. 
 */
public abstract class AbstractPCTMCAnalysis {

	public abstract String toString();

	protected Collection<CombinedPopulationProduct> usedCombinedProducts;
	protected Collection<AbstractExpression> usedGeneralExpectations;

	protected PCTMC pctmc;

	protected BiMap<State, Integer> stateIndex;
	protected BiMap<CombinedPopulationProduct, Integer> momentIndex;

	protected BiMap<AbstractExpression, Integer> generalExpectationIndex;

	protected List<PCTMCAnalysisPostprocessor> postprocessors;

	/**
	 * Adds a postprocessor that will be notified when the analysis finishes.
	 * @param postprocessor
	 */
	public void addPostprocessor(PCTMCAnalysisPostprocessor postprocessor) {
		postprocessors.add(postprocessor);
	}

	public List<PCTMCAnalysisPostprocessor> getPostprocessors() {
		return postprocessors;
	}

	public PCTMC getPCTMC() {
		return pctmc;
	}

	public BiMap<CombinedPopulationProduct, Integer> getMomentIndex() {
		return momentIndex;
	}

	public BiMap<AbstractExpression, Integer> getGeneralExpectationIndex() {
		return generalExpectationIndex;
	}

	/**
	 * Prepares the analysis with given constants.
	 * 
	 * @param constants
	 */
	public abstract void prepare(Constants constants);

	/**
	 * Notifies postprocessors about finished analysis generation.
	 * @param constants
	 * @param plotDescriptions
	 */
	public void notifyPostprocessors(Constants constants,
			List<PlotDescription> plotDescriptions) {
		for (PCTMCAnalysisPostprocessor postprocessor : postprocessors) {
			postprocessor
					.postprocessAnalysis(constants, this, plotDescriptions);
		}
	}

	public AbstractPCTMCAnalysis(PCTMC pctmc) {
		this.pctmc = pctmc;
		stateIndex = pctmc.getStateIndex();
		momentIndex = HashBiMap.<CombinedPopulationProduct, Integer> create();
		generalExpectationIndex = HashBiMap
				.<AbstractExpression, Integer> create();
		usedCombinedProducts = new HashSet<CombinedPopulationProduct>();
		usedGeneralExpectations = new HashSet<AbstractExpression>();
		postprocessors = new LinkedList<PCTMCAnalysisPostprocessor>();
	}

	/**
	 * Sets the moments the analysis has to compute.
	 * 
	 * @param combinedProducts
	 */
	public void setUsedMoments(
			Collection<CombinedPopulationProduct> combinedProducts) {
		if (usedCombinedProducts == null) {
			usedCombinedProducts = new HashSet<CombinedPopulationProduct>();
		} else {
			usedCombinedProducts.addAll(combinedProducts);		
		}
		
		momentIndex = HashBiMap.<CombinedPopulationProduct, Integer> create();
		
		int i = 0;
		for (CombinedPopulationProduct p : combinedProducts) {
			usedCombinedProducts.add(p);
			momentIndex.put(p, i++);
		}
	}

	/**
	 * Sets the used general expectations the analysis has to compute.
	 * 
	 * @param usedGeneralExpectations
	 */
	public void setUsedGeneralExpectations(
			Collection<AbstractExpression> usedGeneralExpectations) {
		this.usedGeneralExpectations = usedGeneralExpectations;
	}

}
