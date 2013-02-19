package uk.ac.imperial.doc.pctmc.odeanalysis.utils;

import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;

public abstract class SystemOfODEs implements FirstOrderDifferentialEquations {
	protected double[] r;

	public void setRates(double[] r) {
		this.r = r;
	}
}
