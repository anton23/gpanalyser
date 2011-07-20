package uk.ac.imperial.doc.pctmc.odeanalysis;


import java.util.Collection;
import java.util.HashSet;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;
import uk.ac.imperial.doc.pctmc.statements.odeanalysis.ODEMethod;

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
		usedCombinedProducts = new HashSet<CombinedPopulationProduct>(combinedProducts);
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
	
	public PCTMCODEAnalysis(PCTMC pctmc, double stopTime, double stepSize, int density, int order) {
		this(pctmc,stopTime,stepSize,density); 
		this.order = order; 
	}

	private ODEGenerator odeGenerator; 
	
 

	
	@Override
	public void prepare(Constants variables) {
		this.odeGenerator = new ODEGenerator(pctmc);
		dataPoints = null;

		odeMethod = odeGenerator.getODEMethodWithCombinedMoments(order, usedCombinedProducts);		
		momentIndex = odeGenerator.getMomentIndex();
	}
	
	private ODEMethod odeMethod;
	

	
	public int getDensity() {
		return density;
	}

	
	private double[] initial; 
	
	

	public ODEMethod getOdeMethod() {
		return odeMethod;
	}
	
	
	
}
