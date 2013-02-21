package uk.ac.imperial.doc.pctmc.interpreter;

import java.util.List;
import java.util.Map;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.PlotDescription;
import uk.ac.imperial.doc.pctmc.experiments.iterate.PCTMCExperiment;
import uk.ac.imperial.doc.pctmc.experiments.iterate.PlotAtDescription;
import uk.ac.imperial.doc.pctmc.experiments.iterate.PlotConstraint;
import uk.ac.imperial.doc.pctmc.expressions.patterns.PatternMatcher;
import uk.ac.imperial.doc.pctmc.expressions.patterns.PatternSetterVisitor;
import uk.ac.imperial.doc.pctmc.representation.EvolutionEvent;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;
import uk.ac.imperial.doc.pctmc.representation.PCTMCWithAccumulations;
import uk.ac.imperial.doc.pctmc.representation.accumulations.NamedAccumulation;
import uk.ac.imperial.doc.pctmc.representation.accumulations.NamedAccumulationUnfolder;

import com.google.common.collect.Multimap;

public class PCTMCFileRepresentation {
	private Constants constants;
	private PCTMC pctmc;
	private Map<ExpressionVariable, AbstractExpression> unfoldedVariables;
	private Multimap<AbstractPCTMCAnalysis, PlotDescription> plots;
	private List<PCTMCExperiment> experiments;
	private List<IExtension> extensions;

	public List<IExtension> getExtensions() {
		return extensions;
	}

	@SuppressWarnings("unchecked")
	public PCTMCFileRepresentation(Object compilerReturn) throws Exception {
		constants = (Constants) compilerReturn.getClass().getField("constants")
				.get(compilerReturn);

		plots = (Multimap<AbstractPCTMCAnalysis, PlotDescription>) compilerReturn
				.getClass().getField("plots").get(compilerReturn);
		experiments = (List<PCTMCExperiment>) compilerReturn.getClass().getField(
				"experiments").get(compilerReturn);

		extensions = (List<IExtension>) compilerReturn.getClass().getField(
		"extensions").get(compilerReturn);
		
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
		for (PCTMCExperiment iterate : experiments) {
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


	public void unfoldVariablesAndSetUsedProducts() {
		for (AbstractPCTMCAnalysis analysis : plots.keySet()) {
			AbstractPCTMCAnalysis.unfoldVariablesAndSetUsedProducts(analysis, plots.get(analysis), unfoldedVariables);
		}
		unfoldAccumulations();
	}
	
	public void unfoldAccumulations() {
		if (pctmc instanceof PCTMCWithAccumulations) {
			Map<NamedAccumulation, AbstractExpression> accODEs = ((PCTMCWithAccumulations) pctmc).getAccODEs();
			NamedAccumulationUnfolder unfolder = new NamedAccumulationUnfolder(accODEs);
			for (AbstractPCTMCAnalysis analysis : plots.keySet()) {
				for (PlotDescription p : plots.get(analysis)) {
					for (AbstractExpression e : p.getExpressions()) {
						e.accept(unfolder);
					}
				}
			}
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

	public List<PCTMCExperiment> getExperiments() {
		return experiments;
	}

}
