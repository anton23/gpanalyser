package uk.ac.imperial.doc.pctmc.interpreter;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.CollectUsedMomentsVisitor;
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.PlotDescription;
import uk.ac.imperial.doc.pctmc.experiments.iterate.PCTMCIterate;
import uk.ac.imperial.doc.pctmc.experiments.iterate.PlotAtDescription;
import uk.ac.imperial.doc.pctmc.experiments.iterate.PlotConstraint;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.ExpressionVariableSetterPCTMC;
import uk.ac.imperial.doc.pctmc.expressions.patterns.PatternMatcher;
import uk.ac.imperial.doc.pctmc.expressions.patterns.PatternSetterVisitor;
import uk.ac.imperial.doc.pctmc.representation.EvolutionEvent;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;

import com.google.common.collect.Multimap;

public class PCTMCFileRepresentation {
	private Constants constants;
	private PCTMC pctmc;
	private Map<ExpressionVariable, AbstractExpression> unfoldedVariables;
	private Multimap<AbstractPCTMCAnalysis, PlotDescription> plots;
	private List<PCTMCIterate> experiments;

	@SuppressWarnings("unchecked")
	public PCTMCFileRepresentation(Object compilerReturn) throws Exception {
		constants = (Constants) compilerReturn.getClass().getField("constants")
				.get(compilerReturn);

		plots = (Multimap<AbstractPCTMCAnalysis, PlotDescription>) compilerReturn
				.getClass().getField("plots").get(compilerReturn);
		experiments = (List<PCTMCIterate>) compilerReturn.getClass().getField(
				"experiments").get(compilerReturn);
		unfoldedVariables = (Map<ExpressionVariable, AbstractExpression>) compilerReturn
				.getClass().getField("unfoldedVariables").get(compilerReturn);
		pctmc = (PCTMC) compilerReturn.getClass().getField("pctmc").get(
				compilerReturn);
	}

	public void unfoldPatterns(PatternMatcher patternMatcher) {
		PatternSetterVisitor.unfoldPatterns(unfoldedVariables, patternMatcher);
		for (EvolutionEvent e : pctmc.getEvolutionEvents()) {
			AbstractExpression rate = e.getRate();
			PatternSetterVisitor.unfoldPatterns(rate, patternMatcher);
		}
		for (PlotDescription pd : plots.values()) {
			for (AbstractExpression e : pd.getExpressions()) {
				PatternSetterVisitor.unfoldPatterns(e, patternMatcher);
			}
		}
		for (PCTMCIterate iterate : experiments) {
			for (PlotAtDescription pd : iterate.getPlots()) {
				PatternSetterVisitor.unfoldPatterns(pd.getExpression(),
						patternMatcher);
				for (PlotConstraint c : pd.getConstraints()) {
					PatternSetterVisitor.unfoldPatterns(c.getExpression(),
							patternMatcher);
				}
			}
		}
	}

	public void unfoldVariablesAndSetUsedProducts(
			AbstractPCTMCAnalysis analysis,
			Collection<PlotDescription> plotDescriptions) {
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

	public void unfoldVariablesAndSetUsedProducts() {
		for (AbstractPCTMCAnalysis analysis : plots.keySet()) {
			unfoldVariablesAndSetUsedProducts(analysis, plots.get(analysis));
		}
	}
	
	public Constants getConstants() {
		return constants;
	}

	public PCTMC getPctmc() {
		return pctmc;
	}

	public Map<ExpressionVariable, AbstractExpression> getUnfoldedVariables() {
		return unfoldedVariables;
	}

	public Multimap<AbstractPCTMCAnalysis, PlotDescription> getPlots() {
		return plots;
	}

	public List<PCTMCIterate> getExperiments() {
		return experiments;
	}

}
