package uk.ac.imperial.doc.pctmc.utils;

public abstract class SystemOfODEs {
	protected double[] r;

	public void setRates(double[] r) {
		this.r = r;
	}

	public abstract double[] derivn(double x, double[] y);

}
