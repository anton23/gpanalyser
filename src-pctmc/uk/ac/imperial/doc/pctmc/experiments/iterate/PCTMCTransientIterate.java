package uk.ac.imperial.doc.pctmc.experiments.iterate;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jfree.data.xy.XYSeriesCollection;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.javaoutput.statements.AbstractExpressionEvaluator;
import uk.ac.imperial.doc.jexpressions.utils.ToStringUtils;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.analysis.AnalysisUtils;
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.PlotDescription;
import uk.ac.imperial.doc.pctmc.charts.PCTMCChartUtilities;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.NumericalPostprocessor;
import uk.ac.imperial.doc.pctmc.utils.PCTMCLogging;

import com.google.common.collect.Lists;

public class PCTMCTransientIterate extends PCTMCExperiment {
	private List<RangeSpecification> ranges;
	private AbstractPCTMCAnalysis analysis;
	private Map<String, AbstractExpression> reEvaluations;
	private Map<ExpressionVariable, AbstractExpression> unfoldedVariables;
	private List<PlotDescription> plotDescriptions;
	private NumericalPostprocessor postprocessor;

	private AbstractExpressionEvaluator[] evaluators;
	
	public PCTMCTransientIterate(List<RangeSpecification> ranges,
			Map<String, AbstractExpression> reEvaluations,
			AbstractPCTMCAnalysis analysis,
			NumericalPostprocessor postprocessor,
			List<PlotDescription> plotDescriptions,
			Map<ExpressionVariable, AbstractExpression> unfoldedVariables) {
		this.ranges = ranges;
		this.reEvaluations = reEvaluations;
		this.analysis = analysis;
		this.postprocessor = postprocessor;
		this.unfoldedVariables = unfoldedVariables;
		this.plotDescriptions = plotDescriptions;
		evaluators = new AbstractExpressionEvaluator[plotDescriptions.size()];
	}
	
	
	@Override
	public List<PlotAtDescription> getPlots() {
		return new LinkedList<PlotAtDescription>();
	}

	@Override
	public void prepare(Constants constants) {
		AbstractPCTMCAnalysis.unfoldVariablesAndSetUsedProducts(analysis, plotDescriptions, unfoldedVariables);
		PCTMCLogging.info("Preparing analysis:");
		PCTMCLogging.increaseIndent();
		analysis.prepare(constants);
		PCTMCLogging.decreaseIndent();
		postprocessor.prepare(analysis, constants);
		int i = 0;
		for (PlotDescription pd: plotDescriptions) {
			evaluators[i++] = postprocessor.getExpressionEvaluator(pd.getExpressions(), constants);
		}
		
	}

	@Override
	public void run(Constants constants) {
		RangeSpecification[] rangesArray = Lists.newArrayList(ranges).toArray(new RangeSpecification[0]);
		PlotDescription[] plotDescriptionArray = Lists.newArrayList(plotDescriptions).toArray(new PlotDescription[0]);
		
		int step[] = new int[ranges.size()];
		int steps[] = new int[ranges.size()];
		int show = 1;
		
		for (int i = 0; i<ranges.size(); i++) {
			steps[i] = rangesArray[i].getSteps();
			show *= steps[i];
		}
		show = Math.max(show / 5, 1);
		double[][][] min = new double[plotDescriptions.size()][][];
		double[][][] max = new double[plotDescriptions.size()][][];
		for (int i = 0; i<plotDescriptions.size(); i++) {
			min[i] = new double[postprocessor.getNumberOfSteps()][plotDescriptionArray[i].getExpressions().size()];
			max[i] = new double[postprocessor.getNumberOfSteps()][plotDescriptionArray[i].getExpressions().size()];
		}
		int iterations = 0;
		boolean first = true;
		PCTMCLogging.setVisible(false);
		do {
			for (int s = 0; s < step.length; s++) {
				constants.setConstantValue(rangesArray[s].getConstant(),
						rangesArray[s].getStep(step[s]));
			}
			PCTMCIterate.reEvaluate(constants, reEvaluations);
			postprocessor.calculateDataPoints(constants);
			for (int i = 0; i<evaluators.length; i++) {
				double[][] tmp = postprocessor.evaluateExpressions(evaluators[i], constants);
				for (int t = 0; t<tmp.length; t++) {
					for (int j = 0; j<tmp[t].length; j++) {
						if (first|| tmp[t][j] < min[i][t][j]) {
							min[i][t][j] = tmp[t][j];
						}
						if (first|| tmp[t][j] > max[i][t][j]) {
							max[i][t][j] = tmp[t][j];
						}
					}
				}
			}
			iterations++;
			if ((iterations) % show == 0) {
				PCTMCLogging.setVisible(true);
				PCTMCLogging.info(iterations + "iterations finished.");
				PCTMCLogging.setVisible(false);
			}
			first = false;
		} while (PCTMCIterate.next(step, steps));
		for (int i = 0; i < plotDescriptionArray.length; i++) {
			double[][] tmp = new double[postprocessor.getNumberOfSteps()]
			                           [plotDescriptionArray[i].getExpressions().size()*2];
			for (int t = 0; t < postprocessor.getNumberOfSteps(); t++) {
				for (int j = 0; j < plotDescriptionArray[i].getExpressions().size(); j++) {
					tmp[t][2*j]   = min[i][t][j];
					tmp[t][2*j+1] = max[i][t][j];
				}
			}
			String[] labels = new String[plotDescriptionArray[i].getExpressions().size()*2];
			for (int j = 0; j < plotDescriptionArray[i].getExpressions().size(); j++) {
				labels[2*j]   = "min " + plotDescriptionArray[i].getExpressions().get(j);
				labels[2*j+1] = "max " + plotDescriptionArray[i].getExpressions().get(j);
			}
			XYSeriesCollection dataset = AnalysisUtils.getDatasetFromArray(tmp, postprocessor.getStepSize(), labels);
			PCTMCChartUtilities.drawChartPairs(dataset, "time", "count", "", this.toString());
		}
	}
	
	@Override
	public String toString() {
		return "TransientIterate " + 
			ToStringUtils.iterableToSSV(ranges, ", ");
	}

}
