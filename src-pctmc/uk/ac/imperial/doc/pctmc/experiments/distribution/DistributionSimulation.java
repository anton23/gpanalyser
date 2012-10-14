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
import uk.ac.imperial.doc.pctmc.charts.ChartUtils3D;
import uk.ac.imperial.doc.pctmc.charts.PCTMCChartUtilities;
import uk.ac.imperial.doc.pctmc.experiments.iterate.PCTMCExperiment;
import uk.ac.imperial.doc.pctmc.experiments.iterate.PlotAtDescription;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.ISimulationReplicationObserver;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.SimulationAnalysisNumericalPostprocessor;
import uk.ac.imperial.doc.pctmc.simulation.PCTMCSimulation;
import uk.ac.imperial.doc.pctmc.utils.FileUtils;
import uk.ac.imperial.doc.pctmc.utils.PCTMCLogging;

public class DistributionSimulation extends PCTMCExperiment implements ISimulationReplicationObserver {

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
		postprocessor.prepare(simulation, constants);	
		
		for (PlotAtDescription p : tmpPlots) {
			AbstractExpressionEvaluator updater = postprocessor
					.getExpressionEvaluator(p.getPlotExpressions(), constants);
			p.setEvaluator(updater);
		}
	}
	
	EmpiricalDistributions[] distributions;
	Constants constants;
	double[][] data;
	int r;

	@Override
	public void run(Constants constants) {	
		this.constants = constants;
		this.r = 0;
		data = new double[plots.size()][replications];

		distributions = new EmpiricalDistributions[plots.size()];
		for (int i = 0; i < plots.size(); i++) {
			distributions[i] = new EmpiricalDistributions(1, postprocessor.getNumberOfSteps(), replications, 100);
		}
		postprocessor.addReplicationObserver(this);
		postprocessor.calculateDataPoints(constants);

		PCTMCLogging.info("Simulation finished.");

		
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
			PlotAtDescription plot = plots.get(p);
			XYSeriesCollection dataset = AnalysisUtils.getDatasetFromArray(ps, min, stepSize, new String[]{plot.toString()});	
			PCTMCChartUtilities.drawChart(dataset, "Value", "Probability", "",
					"Distribution");
			
			String[] timeNames = new String[postprocessor.getNumberOfSteps()];
			for (int t = 0; t < postprocessor.getNumberOfSteps(); t++) {
				timeNames[t] = (t * postprocessor.getStepSize()) + "";
			}
			distributions[p].calculateDistributions();
			XYSeriesCollection fullDataset = AnalysisUtils.getDatasetFromArray(distributions[p].getData()[0], distributions[p].getMin(), distributions[p].getDstep(), timeNames);
			PCTMCChartUtilities.drawChart(fullDataset, "Value", "Probability", "",
			plot.toString());
			
			ChartUtils3D.drawChart(plot.toString(), "Distribution", distributions[p].getData()[0],
					0.0, postprocessor.getStepSize(),
					distributions[p].getMin(), distributions[p].getDstep(),
					"Time", "Value", "Probability");
			
			if (!plot.getFilename().isEmpty()) {
				FileUtils.write3Dfile(plot.getFilename(), distributions[p].getData()[0],
						0.0, postprocessor.getStepSize(),
						distributions[p].getMin(), distributions[p].getDstep());
				FileUtils.write3DGnuplotFile(plot.getFilename(), "Time", "Value", "Probability");
			}
		}
	}
	
	@Override
	public void newReplication(double[][] tmp) {
		int i = 0;
		for (PlotAtDescription plot : plots) {
			double[] values = plot.getEvaluator().updateAtTimes(constants.getFlatConstants(), tmp, plot.getAtTimes(), postprocessor.getStepSize()); 
				
			double[][] evaluateExpressions = plot.getEvaluator().updateAllTimes(constants.getFlatConstants(), tmp, postprocessor.getStepSize()); 
			distributions[i].addReplication(evaluateExpressions);
			data[i][r] = values[0];
			i++;
		}
		r++;
	}


}
