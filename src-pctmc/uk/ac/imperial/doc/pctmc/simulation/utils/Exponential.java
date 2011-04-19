package uk.ac.imperial.doc.pctmc.simulation.utils;

public class Exponential {
	public static double getSample(double rate) {
		double u = Math.random();
		return (-Math.log(u)) / rate;
	}

}
