package uk.ac.imperial.doc.pctmc.analysis;

import java.util.Collection;
import java.util.HashSet;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;
import uk.ac.imperial.doc.pctmc.representation.State;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/*
 * Abstract class for transient analysis of PCTMCs. 
 */
public abstract class AbstractPCTMCAnalysis {

	public abstract String toString();

	protected Collection<CombinedPopulationProduct> usedCombinedProducts;
	protected Collection<AbstractExpression> usedGeneralExpectations;

	protected PCTMC pctmc;

	protected BiMap<State, Integer> stateIndex;
	protected BiMap<CombinedPopulationProduct, Integer> momentIndex;

	protected BiMap<AbstractExpression, Integer> generalExpectationIndex;

	protected double stopTime;
	protected double stepSize;

	protected double[][] dataPoints;
	
	
	
	public PCTMC getPCTMC() {
		return pctmc;
	}

	
	
	public BiMap<CombinedPopulationProduct, Integer> getMomentIndex() {
		return momentIndex;
	}



	public BiMap<AbstractExpression, Integer> getGeneralExpectationIndex() {
		return generalExpectationIndex;
	}



	/**
	 * Prepares the analysis with given constants. 
	 * @param constants
	 */
	public abstract void prepare(Constants constants);
	
	/**
	 * Returns the explicit step size for data keeping.
	 * @return
	 */
	public double getStepSize() {
		return stepSize;
	}

	public double getStopTime() {
		return stopTime;
	}

	public AbstractPCTMCAnalysis(PCTMC pctmc, double stopTime, double stepSize) {
		this.pctmc = pctmc;
		this.stopTime = stopTime;
		this.stepSize = stepSize;

		stateIndex = pctmc.getStateIndex();

		momentIndex = HashBiMap.<CombinedPopulationProduct, Integer> create();
		generalExpectationIndex = HashBiMap
				.<AbstractExpression, Integer> create();
		usedCombinedProducts = new HashSet<CombinedPopulationProduct>();
		usedGeneralExpectations = new HashSet<AbstractExpression>();
	}
	
	/**
	 * Sets the moments the analysis has to compute.
	 * @param combinedProducts
	 */
	public void setUsedMoments(
			Collection<CombinedPopulationProduct> combinedProducts) {
		momentIndex = HashBiMap.<CombinedPopulationProduct, Integer> create();
		usedCombinedProducts = new HashSet<CombinedPopulationProduct>();
		int i = 0;
		for (CombinedPopulationProduct p : combinedProducts) {
			usedCombinedProducts.add(p);
			momentIndex.put(p, i++);
		}
	}
	
	public void setUsedGeneralExpectations(Collection<AbstractExpression> usedGeneralExpectations){
		this.usedGeneralExpectations = usedGeneralExpectations;
	}
	


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((pctmc == null) ? 0 : pctmc.hashCode());
		long temp;
		temp = Double.doubleToLongBits(stepSize);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(stopTime);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractPCTMCAnalysis other = (AbstractPCTMCAnalysis) obj;
		if (pctmc == null) {
			if (other.pctmc != null)
				return false;
		} else if (!pctmc.equals(other.pctmc))
			return false;
		if (Double.doubleToLongBits(stepSize) != Double
				.doubleToLongBits(other.stepSize))
			return false;
		if (Double.doubleToLongBits(stopTime) != Double
				.doubleToLongBits(other.stopTime))
			return false;
		return true;
	}

}
