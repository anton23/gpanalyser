package uk.ac.imperial.doc.pctmc.odeanalysis.utils;

public abstract class SystemOfODEs implements ISystemOfODEs {
	protected double[] r;

	public void setRates(double[] r) {
		this.r = r;
	}

	public abstract double[] derivn(double x, double[] y);

}
