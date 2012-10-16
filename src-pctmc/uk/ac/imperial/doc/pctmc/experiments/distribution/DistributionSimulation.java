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
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.PlotDescription;
import uk.ac.imperial.doc.pctmc.charts.PCTMCChartUtilities;
import uk.ac.imperial.doc.pctmc.experiments.iterate.PCTMCExperiment;
import uk.ac.imperial.doc.pctmc.experiments.iterate.PlotAtDescription;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.ExpressionVariableSetterPCTMC;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.ISimulationReplicationObserver;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.SimulationAnalysisNumericalPostprocessor;
import uk.ac.imperial.doc.pctmc.simulation.PCTMCSimulation;
import uk.ac.imperial.doc.pctmc.utils.PCTMCLogging;

public class DistributionSimulation extends PCTMCExperiment implements ISimulationReplicationObserver {

	private PCTMCSimulation simulation;
	private List<PlotDescription> simulationPlots;
	private SimulationAnalysisNumericalPostprocessor postprocessor;
	private List<PlotAtDescription> plots;
	private Map<ExpressionVariable, AbstractExpression> unfoldedVariables;
	private List<GroupOfDistributions> distributionGroups;
	int replications;

	public DistributionSimulation(PCTMCSimulation simulation,
			List<PlotDescription> simulationPlots,
			SimulationAnalysisNumericalPostprocessor postprocessor,
			List<GroupOfDistributions> distributionsGroups,
			Map<ExpressionVariable, AbstractExpression> unfoldedVariables) {
		super();
		this.simulation = simulation;
		this.simulationPlots = simulationPlots;
		this.postprocessor = postprocessor;
		this.plots = new LinkedList<PlotAtDescription>(); // REMOVE
		this.unfoldedVariables = unfoldedVariables;
		this.replications = postprocessor.getReplications();
		this.distributionGroups = distributionsGroups;
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
		
		for (PlotDescription p:simulationPlots) {
			usedExpressions.addAll(p.getExpressions());
		}
		
		for (GroupOfDistributions gd : distributionGroups) {
			usedExpressions.addAll(gd.getUsedExpressions());
		}
		
		Set<CombinedPopulationProduct> usedProducts = new HashSet<CombinedPopulationProduct>();
		Set<AbstractExpression> usedGeneralExpectations = new HashSet<AbstractExpression>();
		for (AbstractExpression exp : usedExpressions) {
			ExpressionVariableSetterPCTMC setter = new ExpressionVariableSetterPCTMC(
					unfoldedVariables);
			exp.accept(setter);
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
		
		for (GroupOfDistributions gd : distributionGroups) {
			gd.prepare(constants, postprocessor);
		}
		
		for (PlotAtDescription p : tmpPlots) {
			AbstractExpressionEvaluator updater = postprocessor
					.getExpressionEvaluator(p.getPlotExpressions(), constants);
			p.setEvaluator(updater);
		}
	}
	

	Constants constants;
	double[][] data;
	int r;

	@Override
	public void run(Constants constants) {	
		this.constants = constants;
		this.r = 0;
		data = new double[plots.size()][replications];


		postprocessor.addReplicationObserver(this);
		postprocessor.calculateDataPoints(constants);
		for (PlotDescription pd:simulationPlots) {
			postprocessor.plotData(simulation.toString(), constants, pd.getExpressions(), pd.getFilename());
		}

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
		}
		for (GroupOfDistributions gd : distributionGroups) {
			gd.simulationFinished();
		}
	}
	
	@Override
	public void newReplication(double[][] tmp) {
		int i = 0;
		for (PlotAtDescription plot : plots) {
			double[] values = plot.getEvaluator().updateAtTimes(constants.getFlatConstants(), tmp, plot.getAtTimes(), postprocessor.getStepSize()); 
			data[i][r] = values[0];
			i++;
		}
		
		for (GroupOfDistributions gd : distributionGroups) {
			gd.newReplication(tmp);
		}
		r++;
	}


}
