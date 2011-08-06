package uk.ac.imperial.doc.pctmc.postprocessors.numerical;

import java.util.Map;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.constants.visitors.ExpressionEvaluatorWithConstants;
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.javaoutput.JavaODEsPreprocessed;
import uk.ac.imperial.doc.pctmc.javaoutput.PCTMCJavaImplementationProvider;
import uk.ac.imperial.doc.pctmc.odeanalysis.PCTMCODEAnalysis;
import uk.ac.imperial.doc.pctmc.representation.State;

import com.google.common.collect.BiMap;

public class ODEAnalysisNumericalPostprocessor extends NumericalPostprocessor {

	private PCTMCODEAnalysis odeAnalysis;

	private int density;

	
	
	public int getDensity() {
		return density;
	}

	public void setDensity(int density) {
		this.density = density;
	}

	public ODEAnalysisNumericalPostprocessor(double stopTime, double stepSize,
			int density) {
		super(stopTime, stepSize);
		this.density = density;
	}

	@Override
	public String toString() {
		return super.toString() + ", density = " + density; 
	}

	@Override
	public void prepare(AbstractPCTMCAnalysis analysis, Constants constants) {
		super.prepare(analysis, constants);
		odeAnalysis = null;
		if (analysis instanceof PCTMCODEAnalysis) {
			this.odeAnalysis = (PCTMCODEAnalysis) analysis;
			PCTMCJavaImplementationProvider javaImplementation = new PCTMCJavaImplementationProvider();
			preprocessedImplementation = javaImplementation
					.getPreprocessedODEImplementation(
							odeAnalysis.getOdeMethod(), constants, momentIndex);
		}
	}

	private JavaODEsPreprocessed preprocessedImplementation;

	@Override
	public void calculateDataPoints(Constants constants) {
		if (odeAnalysis != null) {
			initial = getInitialValues(constants);
			dataPoints = new PCTMCJavaImplementationProvider().runODEAnalysis(
					preprocessedImplementation, initial, stopTime, stepSize,
					density, constants);
		}
	}

	public JavaODEsPreprocessed getPreprocessedImplementation() {
		return preprocessedImplementation;
	}

	protected double[] initial;

	public double[] getInitialValues(Constants constants) {
		initial = new double[momentIndex.size()];

		BiMap<State, Integer> stateIndex = odeAnalysis.getPCTMC()
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
						.getProduct().entrySet()) {
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
