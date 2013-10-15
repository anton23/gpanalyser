package uk.ac.imperial.doc.pctmc.experiments.distribution;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.jfree.data.xy.XYSeriesCollection;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.javaoutput.statements.AbstractExpressionEvaluator;
import uk.ac.imperial.doc.pctmc.analysis.AnalysisUtils;
import uk.ac.imperial.doc.pctmc.charts.PCTMCChartUtilities;
import uk.ac.imperial.doc.pctmc.experiments.iterate.PlotAtDescription;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.SimulationAnalysisNumericalPostprocessor;
import uk.ac.imperial.doc.pctmc.utils.FileUtils;

import com.google.common.collect.Lists;

public class DistributionsAtTimes extends GroupOfDistributions {

	protected List<PlotAtDescription> expressionAts;
	protected Constants constants;
	protected double stepSize;
	 
	
	public DistributionsAtTimes(List<PlotAtDescription> expressionAts, int nbins, String filename) {
		super(nbins, filename);
		this.expressionAts = new ArrayList<PlotAtDescription>(expressionAts);
	}

	@Override
	public List<AbstractExpression> getUsedExpressions() {
		List<AbstractExpression> ret = new LinkedList<AbstractExpression>();
		for (PlotAtDescription p : expressionAts) {
			ret.add(p.getExpression());
		}
		return ret;
	}

	@Override
	public void newReplication(double[][] data) {
		double[][] values = new double[1][expressionAts.size()];
		int i = 0;
		for (PlotAtDescription p : expressionAts) {
			values[0][i++] = p.getEvaluator().updateAtTimes(
					constants.getFlatConstants(), data, p.getAtTimes(), stepSize)[0];
			
		}
		distributions.addReplication(values);
	}

	@Override
	public void prepare(Constants constants,
			SimulationAnalysisNumericalPostprocessor postprocessor) {
		
		for (PlotAtDescription p : expressionAts) {
			AbstractExpressionEvaluator updater = postprocessor
					.getExpressionEvaluator(Lists.newArrayList(p.getExpression()), constants);
			p.setEvaluator(updater);
		}
		
		distributions = new EmpiricalDistributions(expressionAts.size(),
				1, postprocessor.getReplications(), nbins);
		this.constants = constants;
		this.stepSize = postprocessor.getStepSize();
	}

	@Override
	public void simulationFinished() {
		String[] names = new String[expressionAts.size()];
		int i = 0;
		for (PlotAtDescription p : expressionAts) {
			names[i++] = p.toString();
		}
		distributions.calculateDistributions();
		double[][][] tmpData = distributions.getData();
		double[][] data = new double[tmpData[0].length][tmpData.length];
		for (int e = 0; e < tmpData.length; e++) {
			for (int v = 0; v < tmpData[e].length; v++) {
				data[v][e] = tmpData[e][v][0];
			}
		}
		XYSeriesCollection fullDataset = AnalysisUtils.getDatasetFromArray(data,
				distributions.getMin(), distributions.getDstep(), names);
		PCTMCChartUtilities.drawChart(fullDataset, "Value", "Probability", "",
				"");
		if (filename != null && !filename.equals("")) {
			List<String> labels = new LinkedList<String>();
			for (PlotAtDescription p : expressionAts) {
				labels.add(p.toString());
			}
			FileUtils.writeGnuplotFile(filename, "", labels, "Value", "Probability");
			FileUtils.writeCSVfile(filename, fullDataset);
		}
	}
}
