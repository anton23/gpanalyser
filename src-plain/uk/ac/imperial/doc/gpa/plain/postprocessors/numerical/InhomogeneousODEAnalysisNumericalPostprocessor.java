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
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.analysis.PCTMCAnalysisPostprocessor;
import uk.ac.imperial.doc.pctmc.javaoutput.JavaODEsPreprocessed;
import uk.ac.imperial.doc.pctmc.javaoutput.PCTMCJavaImplementationProvider;
import uk.ac.imperial.doc.pctmc.odeanalysis.PCTMCODEAnalysis;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.NumericalPostprocessor;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.ODEAnalysisNumericalPostprocessor;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;
import uk.ac.imperial.doc.pctmc.representation.State;

public class InhomogeneousODEAnalysisNumericalPostprocessor extends ODEAnalysisNumericalPostprocessor {

	public InhomogeneousODEAnalysisNumericalPostprocessor(double stopTime, double stepSize, int density) {
		super(stopTime, stepSize, density);
	}

	@Override
	public PCTMCAnalysisPostprocessor regenerate() {
		return new InhomogeneousODEAnalysisNumericalPostprocessor(stopTime, stepSize, density);
	}
	
	protected InhomogeneousODEAnalysisNumericalPostprocessor(double stopTime, double stepSize, int density,
			PCTMCODEAnalysis odeAnalysis, JavaODEsPreprocessed preprocessedImplementation) {
		super(stopTime, stepSize, density, odeAnalysis, preprocessedImplementation);
	}
		
	public InhomogeneousODEAnalysisNumericalPostprocessor(double stopTime, double stepSize,	int density, Map<String, Object> parameters) {
		super(stopTime, stepSize, density, parameters);
	}

	@Override
	public NumericalPostprocessor getNewPreparedPostprocessor(Constants constants) {
		assert(odeAnalysis!=null);
		PCTMCJavaImplementationProvider javaImplementation = new PCTMCJavaImplementationProvider();
		InhomogeneousODEAnalysisNumericalPostprocessor ret = new InhomogeneousODEAnalysisNumericalPostprocessor(stopTime, stepSize, density, odeAnalysis, javaImplementation
				.getPreprocessedODEImplementation(odeAnalysis.getOdeMethod(), constants, momentIndex));
		return ret;
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

		TimedEvents te = getPlainPCMTC(odeAnalysis).getTimedEvents();
		if (mJumpUpdateFcts == null) {mJumpUpdateFcts = te.getJumpUpdateMomentsFcts(analysis.getMomentIndex());}
		if (mResetUpdateFcts == null) {mResetUpdateFcts = te.getResetUpdateMomentsFcts(analysis.getMomentIndex());}
	}

	@Override
	public void calculateDataPoints(Constants constants) {
		Constants constanstTmp = constants.getCopyOf();
		if (odeAnalysis != null) {
			PCTMCJavaImplementationProvider analysisRunner = new PCTMCJavaImplementationProvider();
			
			// Get schedule for deterministic events
			TimedEvents te = getPlainPCMTC(odeAnalysis).getTimedEvents();
			Map<Double,Collection<TimedEventUpdater>> updates = te.genTimedEventUpdates(mJumpUpdateFcts,mResetUpdateFcts);
			updates.put(stopTime, new LinkedList<TimedEventUpdater>());
			
			// Check that the schedule can be applied to our ODE solver
			double h = stepSize / density;
			for (double t : updates.keySet()) {
				// Does the event time coincide with an integration end point?
				if (t/h != Math.floor(t/h) && t != stopTime)
				{
					throw new AssertionError("Change at time "+t+" falls between two integration end points.\n" +
											 "Please change event time, stepSize or density.");
				}
			}
			
			// Do analysis
			initial = getInitialValues(constanstTmp);
			dataPoints = new double[(int) Math.ceil(stopTime / stepSize)][initial.length];
			
			// The analysis now does the integration between any two deterministic events.
			// This basically means that we compute the mean-field solution of a time
			// inhomogeneous PCTMC 
			int index = 0;
			double lastStopTime = 0;
			for (Entry<Double, Collection<TimedEventUpdater>> e : updates.entrySet()){
				// We do not integrate between events that occur prior to time 0
				if (e.getKey() > 0) {
					// Run the ODE analysis
					double duration =  ((double)Math.round((e.getKey()-lastStopTime)*100000))/100000;
					double[][] dataPointsTemp = analysisRunner.runODEAnalysis(
												preprocessedImplementation, initial,
												duration, stepSize,	density, constanstTmp);
					// Append new values to array
					// TODO: At the moment we are copying the entire array. This could be done more efficiently
					// by passing the array to the evaluator. We did not do this so far to avoid having to change
					// interfaces.
					index = (int) (lastStopTime/stepSize);
					for (int i=0; i < dataPointsTemp.length; ++i) {
						for (int j=0; j < initial.length; ++j) {
							dataPoints[index][j] = dataPointsTemp[i][j];
						}
						index++;
					}
				}
				lastStopTime = e.getKey();
				
				// Execute rate and population moment changes for current events
				for (TimedEventUpdater teu : e.getValue()) {
					teu.update(constanstTmp, initial, lastStopTime);
				}
			}
		}
	}
}
