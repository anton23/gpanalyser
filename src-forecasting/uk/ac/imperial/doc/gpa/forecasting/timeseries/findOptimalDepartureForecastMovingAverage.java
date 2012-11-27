package uk.ac.imperial.doc.gpa.forecasting.timeseries;

import uk.ac.imperial.doc.gpa.forecasting.util.FileExtra;

public class findOptimalDepartureForecastMovingAverage {
	public static void main(String[] args) {
		String[] departTSFiles = FileExtra.getFileInDirectory("../spatialysis/data/training/ts/", "Departures");
		String[] departTSData = new String[departTSFiles.length];
		int dateId = 0;
		for (String file : departTSFiles) {
			departTSData[dateId++] = FileExtra.readFromTextFile(file);
		}
		
		// All days should have the same number of observations for all clusters
		int numDates = departTSData.length;
		int numClusters = departTSData[0].split("\n").length;
		int numTsObs = departTSData[0].split("\n")[0].split(" ").length;
		double[][][] departTS = new double[numDates][numClusters][numTsObs];
		
		// Load time series data
		dateId = 0;
		for (String dateClusterTs : departTSData) {
			int clusterId = 0;
			for (String tsCluster : dateClusterTs.split("\n"))
			{
				int obsId = 0;
				for (String tsObs : tsCluster.split(" "))
				{
					departTS[dateId][clusterId][obsId] = Double.parseDouble(tsObs);
					obsId++;
				}
				clusterId++;
			}
			dateId++;
		}
		
		// We want to know how big the mean-squared error
		// becomes for different window sizes
		int mForecast = 15;
		for (int windowSize=1; windowSize <= 15; ++windowSize) {
			double meanSqError=0;
			for (dateId=0; dateId<numDates; ++dateId) {
				for (int clusterId=0; clusterId<numClusters; ++clusterId) {
					for (int startInd=windowSize; startInd < numTsObs-mForecast; ++startInd) {
						double[] fcastRates = new double[windowSize+mForecast];
						// Copy the last windowSize measurements into fcastRates
						for (int t=0; t < windowSize; ++t)
						{
							fcastRates[t] = departTS[dateId][clusterId][startInd-windowSize+t];
						}
						
						// Iteratively compute the arrival rates for the forecast window
						for (int t=0; t < mForecast; ++t)
						{
							double depRateCurForecast = 0;
							for (int i=0; i < windowSize; ++i) {
								depRateCurForecast += (1/((double)windowSize))*fcastRates[t+i];
							}
							fcastRates[t+windowSize] = depRateCurForecast;
							// Compute the error
							meanSqError += Math.pow(fcastRates[t+windowSize]-departTS[dateId][clusterId][startInd+t], 2);
						}
					}
				}
			}
			System.out.println("Ttl Mean squared error for window size:"+windowSize+"\t " + meanSqError);
		}
	}
	
	
}
