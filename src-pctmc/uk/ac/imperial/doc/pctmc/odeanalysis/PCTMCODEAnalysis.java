package uk.ac.imperial.doc.pctmc.odeanalysis;


import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.visitors.ExpressionEvaluatorWithConstants;
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.analysis.PCTMCTools;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.implementation.PCTMCImplementationPreprocessed;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;
import uk.ac.imperial.doc.pctmc.representation.State;
import uk.ac.imperial.doc.pctmc.statements.odeanalysis.ODEMethod;
import uk.ac.imperial.doc.pctmc.utils.PCTMCLogging;

public class PCTMCODEAnalysis extends AbstractPCTMCAnalysis{
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + density;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		PCTMCODEAnalysis other = (PCTMCODEAnalysis) obj;
		if (density != other.density)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ODEs(stopTime="+stopTime+", stepSize="+stepSize+", density="+density+")"; 
	}

	protected int density; 
	private int order; 
	
	@Override
	public void setUsedMoments(Collection<CombinedPopulationProduct> combinedProducts){
		usedCombinedMoments = new HashSet<CombinedPopulationProduct>(combinedProducts);
		for (CombinedPopulationProduct product:combinedProducts){
			int o = product.getOrder();
			if (o>order) order = o;
		}		
	}
	
	public PCTMCODEAnalysis(PCTMC pctmc, double stopTime, double stepSize, int density) {
		super(pctmc,stopTime,stepSize);
		this.density = density;
		order = 1; 
	}

	ODEGenerator odeGenerator; 
	
 
	Set<CombinedPopulationProduct> usedCombinedMoments;
	
	PCTMCImplementationPreprocessed preprocessedImplementation;

	
	
	@Override
	public void prepare(Constants variables) {
		this.odeGenerator = new ODEGenerator(pctmc);
		dataPoints = null;

		odeMethod = odeGenerator.getODEMethodWithCombinedMoments(order, usedCombinedMoments);		
		momentIndex = odeGenerator.getMomentIndex();
		preprocessedImplementation = PCTMCTools.getImplementationProvider().
		getPreprocessedODEImplementation(odeMethod,variables, momentIndex, generalExpectationIndex);
	}
	
	ODEMethod odeMethod;
	
	
	@Override
	public void analyse(Constants variables) {
		long time = System.currentTimeMillis(); 
		solveMomentODEs(variables);
		PCTMCLogging.info("The analysis took " + (-time + System.currentTimeMillis()) + " mseconds.");
	}
	
	double[] initial; 
	
	private void solveMomentODEs(Constants variables) {
		if (odeMethod==null){
			prepare(variables); 
		} 
		initial = new double[momentIndex.size()];
		
		double[] initialCounts = new double[stateIndex.size()];


		
		for (int i = 0; i < stateIndex.size(); i++) {
			ExpressionEvaluatorWithConstants evaluator = new ExpressionEvaluatorWithConstants(variables);
			pctmc.getInitCounts()[i].accept(evaluator);
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
		solveMomentODEs(initial,variables);
	}
	
	private void solveMomentODEs(double[] initial,Constants variables) {
		if (odeMethod==null) {
			prepare(variables);
		}
		PCTMCLogging.info("Running Runge-Kutta solver.");
		dataPoints = PCTMCTools.getImplementationProvider().runODEAnalysis(preprocessedImplementation, initial, stopTime, stepSize, density, variables);
	}
	
}
