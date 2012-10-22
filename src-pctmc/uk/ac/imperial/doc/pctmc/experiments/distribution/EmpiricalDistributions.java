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
		boolean allIntegers = true;
		for (int r = 0; r < tmpData.length; r++) {
			for (int t = 0; t < tmpData[r].length; t++) { // TODO ingoring initial and last time step
				for (int e = 0; e < tmpData[r][t].length; e++) {
					if (tmpData[r][t][e] < min) {
						min = tmpData[r][t][e];						
					}				
					if (tmpData[r][t][e] > max) {
						max = tmpData[r][t][e];
					}
					if (allIntegers && tmpData[r][t][e] - Math.floor(tmpData[r][t][e]) > 0) {
						allIntegers = false;
					}
				}
			}
		}
		dstep = (max - min) / nbuckets;
		if (allIntegers) {			
			dstep = Math.ceil(dstep);
			nbuckets = (int)Math.ceil((max - min) / dstep);
			max = min + nbuckets * dstep;
		}
		data = new double[nexpressions][nbuckets][timeSteps];		
		for (int e = 0; e < nexpressions; e++) {
			for (int t = 0; t < timeSteps; t++) {
				for (int r = 0; r < replications; r++) {
					double value = tmpData[r][t][e];					
					int bucket = dstep > 0.0 ? (int)Math.floor((value - min)/dstep) : 0;
					if (value == max) {
						bucket--;
					}
					data[e][bucket][t]++;
				}
				for (int b = 0; b < nbuckets; b++) {
					data[e][b][t] /= replications * dstep;
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
