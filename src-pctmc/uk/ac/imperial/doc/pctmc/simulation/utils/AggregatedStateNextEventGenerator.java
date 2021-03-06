package uk.ac.imperial.doc.pctmc.simulation.utils;

import java.util.List;

import uk.ac.imperial.doc.pctmc.utils.DiscreteSampler;

public abstract class AggregatedStateNextEventGenerator {
	protected double[] r;

	public void setRates(double[] r) {
		this.r = r;
	}

	protected double[] weights;
	protected double totalRate;
	protected List<Integer>[] increasing;
	protected List<Integer>[] decreasing;

	public abstract void initCoefficients(); // needs only at the beginning

	public abstract void recalculateWeights(double[] counts); // responsible for
	// calculating the total weight as well

	public double nextStep(double[] counts) {
		recalculateWeights(counts);
		if (totalRate == 0.0)
			return -1;
		int nextEvent = DiscreteSampler.getSample(weights, totalRate);
		if (nextEvent == -1)
			return -1;
		for (Integer i : increasing[nextEvent]) {
			counts[i]++;
		}
		for (Integer d : decreasing[nextEvent]) {
			counts[d]--;
			if (counts[d]<0.0){
				throw new AssertionError("Counts going negative");
			}
		}		
		return Exponential.getSample(totalRate);
	}
	

}
