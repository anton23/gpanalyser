package uk.ac.imperial.doc.gpa.plain.postprocessors.numerical;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import uk.ac.imperial.doc.gpa.plain.representation.PlainPCTMC;
import uk.ac.imperial.doc.gpa.plain.representation.timed.ITimedEventPopUpdateFct;
import uk.ac.imperial.doc.gpa.plain.representation.timed.TimedEventUpdater;
import uk.ac.imperial.doc.gpa.plain.representation.timed.TimedEvents;
import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.constants.visitors.ExpressionEvaluatorWithConstants;
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.analysis.PCTMCAnalysisPostprocessor;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.ISimulationReplicationObserver;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.NumericalPostprocessor;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.SimulationAnalysisNumericalPostprocessor;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;
import uk.ac.imperial.doc.pctmc.representation.State;
import uk.ac.imperial.doc.pctmc.simulation.utils.GillespieSimulator;
import uk.ac.imperial.doc.pctmc.utils.PCTMCLogging;

public class InhomogeneousSimulationAnalysisNumericalPostprocessor extends SimulationAnalysisNumericalPostprocessor {

	public InhomogeneousSimulationAnalysisNumericalPostprocessor(double stopTime,
			double stepSize, int replications, Map<String, Object> parameters) {
		super(stopTime, stepSize, replications, parameters);
	}

	public InhomogeneousSimulationAnalysisNumericalPostprocessor(double stopTime,
			double stepSize, int replications) {
		super(stopTime, stepSize, replications);
	}
	
	@Override
	public NumericalPostprocessor getNewPreparedPostprocessor(Constants constants) {
		assert(prepared);
		InhomogeneousSimulationAnalysisNumericalPostprocessor ret = new InhomogeneousSimulationAnalysisNumericalPostprocessor(
				stopTime, stepSize, replications, parameters);
		ret.fastPrepare(momentIndex, generalExpectationIndex,
				productUpdaterCode, accumulatorUpdaterCode, eventGeneratorCode,
				initialExpressions, eventGeneratorClassName);
		return ret;
	}
	
	@Override
	public PCTMCAnalysisPostprocessor regenerate() {
		return new InhomogeneousSimulationAnalysisNumericalPostprocessor(stopTime, stepSize, replications, parameters);
	}

	Map<State,ITimedEventPopUpdateFct> mJumpUpdateFcts;
	Map<State,ITimedEventPopUpdateFct> mResetUpdateFcts;

	protected PlainPCTMC getPlainPCMTC(AbstractPCTMCAnalysis analysis) {
		PCTMC pctmc = analysis.getPCTMC();
		if (!(analysis.getPCTMC() instanceof PlainPCTMC)) {
			throw new AssertionError("Expected a PlainPCTMC object but did not receive it");
		}
		return (PlainPCTMC) pctmc;
	}
    
	@Override
	public void prepare(AbstractPCTMCAnalysis analysis, Constants constants) {
		super.prepare(analysis, constants);
		// Time inhomogeneous
		PlainPCTMC pctmc = getPlainPCMTC(analysis);
		TimedEvents te = pctmc.getTimedEvents();
		if (mJumpUpdateFcts == null) {mJumpUpdateFcts = te.getJumpUpdateCountsFcts(pctmc.getStateIndex());}
		if (mResetUpdateFcts == null) {mResetUpdateFcts = te.getResetUpdateCountsFcts(pctmc.getStateIndex());}
	}
	
	@Override
	protected void simulate(Constants constants) {
        setInitialExpressions();
		double[] initial = new double[initialExpressions.length];

		// Get schedule for deterministic events
		TimedEvents te = getPlainPCMTC(simulation).getTimedEvents();
		Map<Double,Collection<TimedEventUpdater>> updates = te.genTimedEventUpdates(mJumpUpdateFcts,mResetUpdateFcts);
		updates.put(stopTime, new LinkedList<TimedEventUpdater>());
		
		PCTMCLogging.info("Running Gillespie simulator.");
		PCTMCLogging.increaseIndent();

		int m = momentIndex.size();
		dataPoints = new double[(int) Math.ceil(stopTime / stepSize)][momentIndex
		                                          					.size()
		                                          					+ generalExpectationIndex.size()];
		for (int r = 0; r < replications; r++) {
			if (r > 0 && r % (replications / 5 > 0 ? replications/ 5 : 1) == 0) {
				PCTMCLogging.info(r + " replications finished.");
			}

			// Prepare run
			Constants constanstTmp = constants.getCopyOf();
			for (int i = 0; i < initialExpressions.length; i++) {
				ExpressionEvaluatorWithConstants evaluator = new ExpressionEvaluatorWithConstants(constanstTmp);
				initialExpressions[i].accept(evaluator);
				initial[i] = evaluator.getResult();
			}
			eventGenerator.setRates(constanstTmp.getFlatConstants());
			updater.setRates(constanstTmp.getFlatConstants());
			accUpdater.setRates(constanstTmp.getFlatConstants());
			
			double[][] dataPointsRep = new double[(int) Math.ceil(stopTime / stepSize)][momentIndex
				                                          					.size()
				                                          					+ generalExpectationIndex.size()];
			
			// The analysis now does the simluation between any two deterministic events.
			// This basically means that we compute the simulation solution of a time
			// inhomogeneous PCTMC 
			int index = 0;
			double lastStopTime = 0;
			for (Entry<Double, Collection<TimedEventUpdater>> e : updates.entrySet()){
				// We do not integrate between events that occur prior to time 0
				double[][] tmp = null;
				if (e.getKey() > 0) {
					// Run the Simulation (each time we the simulation
					// a little bit longer to ensure that the estimate
					// at the time of the event is accurate
					double duration = ((double)Math.round((e.getKey()-lastStopTime)*100000))/100000;
					tmp = GillespieSimulator.simulateAccumulated(eventGenerator,
								initial, duration+2*stepSize, stepSize, accUpdater);
					int tmpLastInd = tmp.length-2;
					for (int i=0; i < initial.length; i++) {
						initial[i] = tmp[tmpLastInd][i];
					}
					index = (int) (lastStopTime/stepSize);
				}
				lastStopTime = e.getKey();
								
				if(tmp == null) {continue;}
				for (int t = index; t < index+tmp.length-2; t++) {
					updater.update(dataPoints[t], tmp[t-index]);
					updater.update(dataPointsRep[t], tmp[t-index]);	
				}
				
				// Execute rate and population changes for current events
				for (TimedEventUpdater teu : e.getValue()) {
					teu.update(constanstTmp, initial, lastStopTime);
				}
				eventGenerator.setRates(constanstTmp.getFlatConstants());
				updater.setRates(constanstTmp.getFlatConstants());
				accUpdater.setRates(constanstTmp.getFlatConstants());
			}

			notifyReplicationObservers(dataPointsRep);
		}

		for (int t = 0; t < dataPoints.length; t++) {
			for (int i = 0; i < m + generalExpectationIndex.size(); i++) {
				dataPoints[t][i] = dataPoints[t][i] / replications;
			}
		}
		PCTMCLogging.decreaseIndent();
	}
	
	@Override
	protected void notifyReplicationObservers(double[][] tmp) {
		if (replicationObservers == null) return;
		for (ISimulationReplicationObserver o: replicationObservers) {
			o.newReplication(tmp);
		}
	}
}
