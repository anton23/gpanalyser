package uk.ac.imperial.doc.gpa.plain.postprocessors.numerical;

import java.io.IOException;
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
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.javaoutput.JavaODEsPreprocessed;
import uk.ac.imperial.doc.pctmc.javaoutput.PCTMCJavaImplementationProvider;
import uk.ac.imperial.doc.pctmc.odeanalysis.PCTMCODEAnalysis;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.NumericalPostprocessor;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;
import uk.ac.imperial.doc.pctmc.representation.State;
import uk.ac.imperial.doc.pctmc.utils.FileUtils;

public class InhomogeneousODEAnalysisNumericalPostprocessor extends NumericalPostprocessor {

	private PCTMCODEAnalysis odeAnalysis;

	private int density;

	private String overrideCode;
	private String overrideCodeClassName;
	
	public int getDensity() {
		return density;
	}

	public void setDensity(int density) {
		this.density = density;
	}

	public InhomogeneousODEAnalysisNumericalPostprocessor(double stopTime, double stepSize,
			int density) {
		super(stopTime, stepSize);
		this.density = density;
	}

	@Override
	public PCTMCAnalysisPostprocessor regenerate() {
		return new InhomogeneousODEAnalysisNumericalPostprocessor(stopTime, stepSize, density);
	}
	
	private InhomogeneousODEAnalysisNumericalPostprocessor(double stopTime, double stepSize, int density,
			PCTMCODEAnalysis odeAnalysis, JavaODEsPreprocessed preprocessedImplementation) {
		this(stopTime, stepSize, density);
		this.odeAnalysis = odeAnalysis;
		this.preprocessedImplementation = preprocessedImplementation;
		this.momentIndex = odeAnalysis.getMomentIndex();
		this.generalExpectationIndex = odeAnalysis.getGeneralExpectationIndex();
		this.dataPoints = null;
	}
		
	public InhomogeneousODEAnalysisNumericalPostprocessor(double stopTime, double stepSize,
			int density, Map<String, Object> parameters) {
		this(stopTime, stepSize, density);
		if (parameters.containsKey("overrideCode")) {
			Object value = parameters.get("overrideCode");
			if (value instanceof String) {
				String asString = ((String) value);
				try {
					overrideCode =  FileUtils.readFile(asString);
					String[] split = asString.split("/");
					overrideCodeClassName = split[split.length-1].replace(".java", "");
				}
				catch (IOException e) {
					throw new AssertionError("File + " + asString + " cannot be open!");
				}
				
			} else {
				throw new AssertionError("Given value of 'overrideCode' has to be a filename!");
			}
		}
	}

	@Override
	public NumericalPostprocessor getNewPreparedPostprocessor(Constants constants) {
		assert(odeAnalysis!=null);
		PCTMCJavaImplementationProvider javaImplementation = new PCTMCJavaImplementationProvider();
		InhomogeneousODEAnalysisNumericalPostprocessor ret = new InhomogeneousODEAnalysisNumericalPostprocessor(stopTime, stepSize, density, odeAnalysis, javaImplementation
				.getPreprocessedODEImplementation(
						odeAnalysis.getOdeMethod(), constants, momentIndex));
		return ret;
	}

	@Override
	public String toString() {
		return "(stopTime = " + stopTime + ", stepSize = " + stepSize + ", density = " + density+")"; 
	}
	
	Map<State,ITimedEventPopUpdateFct> mJumpUpdateFcts;
	Map<State,ITimedEventPopUpdateFct> mResetUpdateFcts;

	private PlainPCTMC getPlainPCMTC(PCTMCODEAnalysis odeAnalysis) {
		PCTMC pctmc = odeAnalysis.getPCTMC();
		if (!(odeAnalysis.getPCTMC() instanceof PlainPCTMC)) {
			throw new AssertionError("Expected a PlainPCTMC object but did not receive it");
		}
		return (PlainPCTMC) pctmc;
	}

	@Override
	public void prepare(AbstractPCTMCAnalysis analysis, Constants constants) {
		super.prepare(analysis, constants);
		odeAnalysis = null;
		if (analysis instanceof PCTMCODEAnalysis) {
			this.odeAnalysis = (PCTMCODEAnalysis) analysis;
			PCTMCJavaImplementationProvider javaImplementation = new PCTMCJavaImplementationProvider();
			if (overrideCode == null) {
				preprocessedImplementation = javaImplementation
						.getPreprocessedODEImplementation(
								odeAnalysis.getOdeMethod(), constants, momentIndex);
			} else {
				preprocessedImplementation = javaImplementation.getPreprocessedODEImplementationFromCode(overrideCode, overrideCodeClassName);
			}
		} else {
			throw new AssertionError("ODE postprocessor attached to an incompatible analysis " + analysis.toString());
		}
		
		// We need to prepare objects that handle inhomogeneous changes.
		// To save time we will create, compile and initialise bespoke
		// classes that execute modifications to the populationVector as
		// deterministic events occur
		TimedEvents te = getPlainPCMTC(odeAnalysis).getTimedEvents();
		if (mJumpUpdateFcts == null) {mJumpUpdateFcts = te.getJumpUpdateFcts(analysis.getMomentIndex());}
		if (mResetUpdateFcts == null) {mResetUpdateFcts = te.getResetUpdateFcts(analysis.getMomentIndex());}
	}

	private JavaODEsPreprocessed preprocessedImplementation;

	@Override
	public void calculateDataPoints(Constants constants) {
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
				if (t/h != Math.floor(t/h))
				{
					throw new AssertionError("Change at time "+t+" falls between two integration end points.\n" +
											 "Please change event time, stepSize or density.");
				}
			}
			
			// Do analysis
			initial = getInitialValues(constants);
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
					double[][] dataPointsTemp = analysisRunner.runODEAnalysis(
												preprocessedImplementation, initial,
												e.getKey()-lastStopTime, stepSize,	density, constants);
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
				
				// Execute rate and population change updates for current events
				for (TimedEventUpdater teu : e.getValue()) {
					teu.update(constants, initial, lastStopTime);
				}
			}
		}
	}

	public JavaODEsPreprocessed getPreprocessedImplementation() {
		return preprocessedImplementation;
	}

	protected double[] initial;

	public double[] getInitialValues(Constants constants) {
		initial = new double[momentIndex.size()];

		Map<State, Integer> stateIndex = odeAnalysis.getPCTMC()
				.getStateIndex();
		int size = stateIndex.size();
		double[] initialCounts = new double[size];

		for (int i = 0; i < size; i++) {
			ExpressionEvaluatorWithConstants evaluator = new ExpressionEvaluatorWithConstants(
					constants);
			odeAnalysis.getPCTMC().getInitCounts()[i].accept(evaluator);
			initialCounts[i] = evaluator.getResult();
		}

		for (Map.Entry<CombinedPopulationProduct, Integer> e : momentIndex
				.entrySet()) {
			if (!e.getKey().getAccumulatedProducts().isEmpty()) {
				initial[e.getValue()] = 0;
			} else {
				double tmp = 1.0;

				for (Map.Entry<State, Integer> s : e.getKey().getNakedProduct()
						.getRepresentation().entrySet()) {
					for (int p = 0; p < s.getValue(); p++) {
						if (!stateIndex.containsKey(s.getKey())) {
							throw new AssertionError("State " + s.getKey()
									+ " unknown!");
						}
						tmp *= initialCounts[stateIndex.get(s.getKey())];
					}
				}
				initial[e.getValue()] = tmp;
			}
		}
		return initial;
	}
}
