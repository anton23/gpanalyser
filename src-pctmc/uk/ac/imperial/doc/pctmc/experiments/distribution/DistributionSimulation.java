package uk.ac.imperial.doc.pctmc.experiments.distribution;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.CollectUsedMomentsVisitor;
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.PlotDescription;
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

		this.unfoldedVariables = unfoldedVariables;
		this.replications = postprocessor.getReplications();
		this.distributionGroups = distributionsGroups;
	}

	@Override
	public List<PlotAtDescription> getPlots() {
		return new LinkedList<PlotAtDescription>();
	}

	@Override
	public void prepare(Constants constants) {
		List<AbstractExpression> usedExpressions = new LinkedList<AbstractExpression>();
	
		
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
		
	}


	@Override
	public void run(Constants constants) {	
		postprocessor.addReplicationObserver(this);
		postprocessor.calculateDataPoints(constants);
		
		for (PlotDescription pd:simulationPlots) {
			postprocessor.plotData(simulation.toString(), constants, pd.getExpressions(), pd.getFilename());
		}

		PCTMCLogging.info("Simulation finished.");

		for (GroupOfDistributions gd : distributionGroups) {
			gd.simulationFinished();
		}
	}
	
	@Override
	public void newReplication(double[][] tmp) {
		for (GroupOfDistributions gd : distributionGroups) {
			gd.newReplication(tmp);
		}
	}


}
