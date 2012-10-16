package uk.ac.imperial.doc.pctmc.experiments.distribution;

import java.util.List;
import java.util.Map;

import org.jfree.data.xy.XYSeriesCollection;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.javaoutput.statements.AbstractExpressionEvaluator;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
import uk.ac.imperial.doc.pctmc.analysis.AnalysisUtils;
import uk.ac.imperial.doc.pctmc.charts.ChartUtils3D;
import uk.ac.imperial.doc.pctmc.charts.PCTMCChartUtilities;
import uk.ac.imperial.doc.pctmc.expressions.ExpressionVariableSetterPCTMC;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.SimulationAnalysisNumericalPostprocessor;
import uk.ac.imperial.doc.pctmc.utils.FileUtils;

import com.google.common.collect.Lists;

public class DistributionsAtAllTimes extends GroupOfDistributions {
	
	protected AbstractExpression expression;
	protected AbstractExpressionEvaluator updater;
	protected int replication;
	protected Constants constants;
	protected double stepSize;
	protected int numberOfSteps;
	
	public DistributionsAtAllTimes(AbstractExpression expression, int nbins, String filename) {
		super(nbins, filename);
		this.expression = expression;
	}


	@Override
	public void prepare(Constants constants,
			SimulationAnalysisNumericalPostprocessor postprocessor) {
			updater = postprocessor
				.getExpressionEvaluator(Lists.newArrayList(expression), constants);
			replication = 0;
			this.constants = constants;
			this.stepSize = postprocessor.getStepSize();
			this.numberOfSteps = postprocessor.getNumberOfSteps();
			distributions = new EmpiricalDistributions(1, postprocessor.getNumberOfSteps(), postprocessor.getReplications(), nbins);
	}
	
	
	@Override
	public List<AbstractExpression> getUsedExpressions(
			Map<ExpressionVariable, AbstractExpression> unfoldedVariables) {
		ExpressionVariableSetterPCTMC setter = new ExpressionVariableSetterPCTMC(
				unfoldedVariables);
		expression.accept(setter);
		return Lists.newArrayList(expression);		
	}


	@Override
	public void newReplication(double[][] tmp) {
		double[][] evaluateExpression = updater.updateAllTimes(constants.getFlatConstants(), tmp, stepSize); 
		distributions.addReplication(evaluateExpression);
	}

	@Override
	public void simulationFinished() {
		String[] timeNames = new String[numberOfSteps];
		for (int t = 0; t < numberOfSteps; t++) {
			timeNames[t] = (t * stepSize) + "";
		}
		distributions.calculateDistributions();
		XYSeriesCollection fullDataset = AnalysisUtils.getDatasetFromArray(distributions.getData()[0], distributions.getMin(), distributions.getDstep(), timeNames);
		PCTMCChartUtilities.drawChart(fullDataset, "Value", "Probability", "",
		expression.toString());
		
		ChartUtils3D.drawChart(expression.toString(), "Distribution", distributions.getData()[0],
				distributions.getMin(), distributions.getDstep(),
				0.0, stepSize,				
				"Value", "Time", "Probability");
		
		if (filename!=null && !filename.isEmpty()) {
			FileUtils.write3Dfile(filename, distributions.getData()[0],
					distributions.getMin(), distributions.getDstep(),
					0.0, stepSize
					);
			FileUtils.write3DGnuplotFile(filename, "Value", "Time", "Probability");
		}
	}
}
