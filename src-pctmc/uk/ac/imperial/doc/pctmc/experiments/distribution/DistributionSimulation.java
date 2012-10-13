package uk.ac.imperial.doc.pctmc.experiments.distribution;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math.random.EmpiricalDistribution;
import org.apache.commons.math.random.EmpiricalDistributionImpl;
import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.jfree.data.xy.XYSeriesCollection;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.javaoutput.statements.AbstractExpressionEvaluator;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
import uk.ac.imperial.doc.pctmc.analysis.AnalysisUtils;
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.CollectUsedMomentsVisitor;
import uk.ac.imperial.doc.pctmc.charts.PCTMCChartUtilities;
import uk.ac.imperial.doc.pctmc.experiments.iterate.PCTMCExperiment;
import uk.ac.imperial.doc.pctmc.experiments.iterate.PlotAtDescription;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.SimulationAnalysisNumericalPostprocessor;
import uk.ac.imperial.doc.pctmc.simulation.PCTMCSimulation;
import uk.ac.imperial.doc.pctmc.utils.PCTMCLogging;

public class DistributionSimulation extends PCTMCExperiment {

	private PCTMCSimulation simulation;
	private SimulationAnalysisNumericalPostprocessor postprocessor;
	private List<PlotAtDescription> plots;
	private Map<ExpressionVariable, AbstractExpression> unfoldedVariables;
	int replications;

	public DistributionSimulation(PCTMCSimulation simulation,
			SimulationAnalysisNumericalPostprocessor postprocessor,
			List<PlotAtDescription> plots,
			Map<ExpressionVariable, AbstractExpression> unfoldedVariables) {
		super();
		this.simulation = simulation;
		this.postprocessor = postprocessor;
		this.plots = plots;
		this.unfoldedVariables = unfoldedVariables;
		this.replications = postprocessor.getReplications();
	}

	@Override
	public List<PlotAtDescription> getPlots() {
		return plots;
	}

	@Override
	public void prepare(Constants constants) {
		List<AbstractExpression> usedExpressions = new LinkedList<AbstractExpression>();
		List<PlotAtDescription> tmpPlots = new LinkedList<PlotAtDescription>(
				plots);

		for (PlotAtDescription plot : tmpPlots) {
			plot.unfoldExpressions(unfoldedVariables);
			usedExpressions.addAll(plot.getPlotExpressions());
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

		PCTMCLogging.info("Preparing analysis:");
		PCTMCLogging.increaseIndent();
		simulation.setUsedMoments(usedProducts);

		simulation.prepare(constants);
		PCTMCLogging.decreaseIndent();
		postprocessor.setReplications(1);
		postprocessor.prepare(simulation, constants);	
		
		for (PlotAtDescription p : tmpPlots) {
			AbstractExpressionEvaluator updater = postprocessor
					.getExpressionEvaluator(p.getPlotExpressions(), constants);
			p.setEvaluator(updater);
		}
	}

	@Override
	public void run(Constants constants) {		
		double[][] data = new double[plots.size()][replications];
		PCTMCLogging.setVisible(false);
		EmpiricalDistributions[] distributions = new EmpiricalDistributions[plots.size()];
		for (int i = 0; i < plots.size(); i++) {
			distributions[i] = new EmpiricalDistributions(1, postprocessor.getNumberOfSteps(), replications, 500);
		}
		for (int r = 0; r < replications; r++) {			
			postprocessor.calculateDataPoints(constants);
			int i = 0;
			for (PlotAtDescription plot : plots) {
				double[] values = postprocessor.evaluateExpressionsAtTimes(plot
						.getEvaluator(), plot.getAtTimes(), constants);
				
				distributions[i].addReplication(postprocessor.evaluateExpressions(plot.getEvaluator(), constants));
				data[i][r] = values[0];
				i++;
			}
		}
		PCTMCLogging.setVisible(true);

		
		for (int p = 0; p < plots.size(); p++) {			
			EmpiricalDistribution dist = new EmpiricalDistributionImpl(100);
			double ps[][] = new double[dist.getBinCount()][1];
			dist.load(data[p]);
			double min = dist.getSampleStats().getMin();
			double stepSize = dist.getUpperBounds()[0] - min;
			
			int i = 0;
			for (SummaryStatistics s : dist.getBinStats()) {
				ps[i++][0] = (double) s.getN() / (double) replications;				
			}
			XYSeriesCollection dataset = AnalysisUtils.getDatasetFromArray(ps, min, stepSize, new String[]{plots.get(p).toString()});	
			PCTMCChartUtilities.drawChart(dataset, "Value", "Probability", "",
					"Distribution");
			
			String[] timeNames = new String[postprocessor.getNumberOfSteps()];
			for (int t = 0; t < postprocessor.getNumberOfSteps(); t++) {
				timeNames[t] = (t * postprocessor.getStepSize()) + "";
			}
			distributions[p].calculateDistributions();
			XYSeriesCollection fullDataset = AnalysisUtils.getDatasetFromArray(distributions[p].getData()[0], distributions[p].getMin(), distributions[p].getDstep(), timeNames);
			PCTMCChartUtilities.drawChart(fullDataset, "Value", "Probability", "",
			plots.get(p).toString());
		}

	}

}
