package uk.ac.imperial.doc.pctmc.analysis;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.CollectUsedMomentsVisitor;
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.PlotDescription;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.ExpressionVariableSetterPCTMC;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;
import uk.ac.imperial.doc.pctmc.representation.State;

import java.util.*;

/*
 * Abstract class for transient analysis of PCTMCs. 
 */
public abstract class AbstractPCTMCAnalysis {

	public abstract String toString();
	
	public abstract AbstractPCTMCAnalysis regenerate(PCTMC pctmc);

	protected Collection<CombinedPopulationProduct> usedCombinedProducts;
	protected Collection<AbstractExpression> usedGeneralExpectations;

	protected PCTMC pctmc;

	protected Map<State, Integer> stateIndex;
	protected Map<CombinedPopulationProduct, Integer> momentIndex;

	protected Map<AbstractExpression, Integer> generalExpectationIndex;

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

	
	public String getPostprocessorString() {
		StringBuilder ret = new StringBuilder();
		for (PCTMCAnalysisPostprocessor p : postprocessors) {
			ret.append(p.toString());
		}
		return ret.toString();
	}
	
	public PCTMC getPCTMC() {
		return pctmc;
	}

	public Map<CombinedPopulationProduct, Integer> getMomentIndex() {
		return momentIndex;
	}

	public Map<AbstractExpression, Integer> getGeneralExpectationIndex() {
		return generalExpectationIndex;
	}

	/**
	 * Prepares the analysis with given constants.
	 * 
	 * @param constants
	 */
	public abstract void prepare(Constants constants);

    public void prepare(Map<CombinedPopulationProduct, Integer> momentIndex) {
        this.momentIndex = momentIndex;
    }

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
		if (pctmc != null) { 
			stateIndex = pctmc.getStateIndex();
		}
		momentIndex = new HashMap<CombinedPopulationProduct, Integer>();
		generalExpectationIndex = new HashMap
				<AbstractExpression, Integer>();
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
		
		momentIndex = new HashMap<CombinedPopulationProduct, Integer>();
		
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
	
	public static void unfoldVariablesAndSetUsedProducts(
			AbstractPCTMCAnalysis analysis,
			Collection<PlotDescription> plotDescriptions,
			Map<ExpressionVariable, AbstractExpression> unfoldedVariables) {
		List<AbstractExpression> usedExpressions = new LinkedList<AbstractExpression>();
		for (PlotDescription pexp : plotDescriptions) {
			for (AbstractExpression e : pexp.getExpressions()) {
				ExpressionVariableSetterPCTMC setter = new ExpressionVariableSetterPCTMC(
						unfoldedVariables);
				e.accept(setter);
				usedExpressions.add(e);
			}
		}

		Set<CombinedPopulationProduct> usedProducts = new HashSet<CombinedPopulationProduct>();
		Set<AbstractExpression> usedGeneralExpectations = new HashSet<AbstractExpression>();
		for (AbstractExpression exp : usedExpressions) {
			CollectUsedMomentsVisitor visitor = new CollectUsedMomentsVisitor();
			exp.accept(visitor);
			usedProducts.addAll(visitor.getUsedCombinedMoments());
			usedGeneralExpectations
					.addAll(visitor.getUsedGeneralExpectations());
		}
		analysis.setUsedMoments(usedProducts);
		analysis.setUsedGeneralExpectations(usedGeneralExpectations);
	}

}
