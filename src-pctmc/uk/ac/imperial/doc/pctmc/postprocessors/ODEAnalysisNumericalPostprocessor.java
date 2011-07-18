package uk.ac.imperial.doc.pctmc.postprocessors;

import java.util.Map;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.visitors.ExpressionEvaluatorWithConstants;
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.implementation.PCTMCImplementationPreprocessed;
import uk.ac.imperial.doc.pctmc.javaoutput.PCTMCJavaImplementationProvider;
import uk.ac.imperial.doc.pctmc.odeanalysis.PCTMCODEAnalysis;
import uk.ac.imperial.doc.pctmc.representation.State;

import com.google.common.collect.BiMap;

public class ODEAnalysisNumericalPostprocessor extends NumericalPostprocessor {
	
	private PCTMCODEAnalysis odeAnalysis; 
	
	@Override
	protected void prepare(AbstractPCTMCAnalysis analysis, Constants constants) {
		odeAnalysis = null;
		if (analysis instanceof PCTMCODEAnalysis){
			this.odeAnalysis = (PCTMCODEAnalysis)analysis; 
			PCTMCJavaImplementationProvider javaImplementation = new PCTMCJavaImplementationProvider(); 			
			preprocessedImplementation = 
				javaImplementation.getPreprocessedODEImplementation(odeAnalysis.getOdeMethod(), constants,momentIndex);
		}
	}
	
	private PCTMCImplementationPreprocessed preprocessedImplementation;

	@Override
	protected void calculateDataPoints(Constants constants) {
		if (odeAnalysis!=null){
			initial = getInitialValues(constants); 
			dataPoints = new PCTMCJavaImplementationProvider().runODEAnalysis(
					preprocessedImplementation, initial, stopTime, stepSize, odeAnalysis.getDensity(), constants);
		}		
	}



	protected double[] initial; 
	
	public double[] getInitialValues(Constants constants){
		initial = new double[momentIndex.size()];
		
		BiMap<State, Integer> stateIndex = odeAnalysis.getPCTMC().getStateIndex();
		int size = stateIndex.size();
		double[] initialCounts = new double[size];

		for (int i = 0; i < size; i++) {
			ExpressionEvaluatorWithConstants evaluator = new ExpressionEvaluatorWithConstants(constants);
			odeAnalysis.getPCTMC().getInitCounts()[i].accept(evaluator);
			initialCounts[i] = evaluator.getResult();
		}

		for (Map.Entry<CombinedPopulationProduct, Integer> e:momentIndex.entrySet()) {
			if (!e.getKey().getAccumulatedProducts().isEmpty()){
				initial[e.getValue()]=0;
			} else {
				double tmp = 1.0;
				
				for (Map.Entry<State, Integer> s:e.getKey().getNakedProduct().getProduct().entrySet()){
					for (int p = 0; p < s.getValue(); p++) {
						if (!stateIndex.containsKey(s.getKey())){
							throw new AssertionError("State " + s.getKey() + " unknown!"); 
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
