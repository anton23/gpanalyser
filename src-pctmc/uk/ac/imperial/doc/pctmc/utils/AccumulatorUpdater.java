package uk.ac.imperial.doc.pctmc.utils;

public 	abstract class AccumulatorUpdater{
	protected int n; 
	
	protected double[] r;

	public void setRates(double[] r) {
		this.r = r;
	}
	
	public abstract double[] update(double[] counts,double delta); 
}
