package uk.ac.imperial.doc.pctmc.utils;

public class DiscreteSampler {

	public static double totalRate;

	public static int getSample(double[] weights, double totalWeight) {
		Double rateSample = Math.random() * totalWeight;
		int result = -1;
		double currentTotal = 0;
		for (int i = 0; i < weights.length; i++) {
			Double p = weights[i];
			currentTotal += p;
			if (currentTotal >= rateSample) {
				result = i;
				break;
			}
		}

		return result;
	}

}
