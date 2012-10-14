package uk.ac.imperial.doc.pctmc.experiments.distribution;

public class EmpiricalDistributions {
	double[][][] tmpData;
	double[][][] data;
	int r;
	int nbuckets;
	
	
	double min, max, dstep;
	int nexpressions, timeSteps, replications;
	
	public EmpiricalDistributions(int nexpressions, int timeSteps, int replications, int nbuckets) {
		tmpData = new double[replications][timeSteps][nexpressions];
		this.replications = replications; 
		this.timeSteps = timeSteps;
		this.nexpressions = nexpressions;
		r = 0;
		this.nbuckets = nbuckets;
	}
	
	public void addReplication(double[][] data) {
		tmpData[r++] = data;
	}
	
	public void calculateDistributions() {
		min = Double.MAX_VALUE;
		max = Double.MIN_VALUE;
		for (int r = 0; r < tmpData.length; r++) {
			for (int t = 0; t < tmpData[r].length; t++) {
				for (int e = 0; e < tmpData[r][t].length; e++) {
					if (tmpData[r][t][e] < min) {
						min = tmpData[r][t][e];						
					}				
					if (tmpData[r][t][e] > max) {
						max = tmpData[r][t][e];
					}
				}
			}
		}
		dstep = (max - min) / nbuckets;
		data = new double[nexpressions][nbuckets][timeSteps];		
		for (int e = 0; e < nexpressions; e++) {
			for (int t = 1; t < timeSteps; t++) {
				for (int r = 0; r < replications; r++) {
					double value = tmpData[r][t][e];					
					int bucket = dstep > 0.0 ? (int)Math.floor((value - min)/dstep) : 0;
					if (value == max) {
						bucket--;
					}
					data[e][bucket][t]++;
				}
				for (int b = 0; b < nbuckets; b++) {
					data[e][b][t] /= replications;
				}
			}
		}		
	}

	public double[][][] getData() {
		return data;
	}

	public double getMin() {
		return min;
	}

	public double getMax() {
		return max;
	}

	public double getDstep() {
		return dstep;
	}
	
	

}
