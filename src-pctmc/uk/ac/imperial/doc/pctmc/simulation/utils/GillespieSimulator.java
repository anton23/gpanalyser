package uk.ac.imperial.doc.pctmc.simulation.utils;


public class GillespieSimulator {

		

	
	public static double[][] simulateAccumulated(AggregatedStateNextEventGenerator g,
			double initial[], double stopTime, double stepSize, AccumulatorUpdater accumulator) {
		int n = initial.length;
		double[][] ret = new double[(int) Math.ceil(stopTime / stepSize)][n+accumulator.n];

		double currentTime = 0;

		double[] counts = new double[n+accumulator.n];
		for (int i = 0; i < initial.length; i++) {
			counts[i] = initial[i];//(int) Math.floor(initial[i]);
			ret[0][i] = initial[i];
			ret[1][i] = initial[i];
		}

		g.initCoefficients();

		int t = 1;
		double[] toAccumulate = new double[accumulator.n];
		
		double[] previousCounts = new double[n+accumulator.n]; 
			
		while (currentTime < stopTime) {
						
			for (int i = 0; i<n+accumulator.n; i++){
				previousCounts[i] = counts[i]; 
			}
			
			double duration = g.nextStep(counts);
			double[] realAccumulate = accumulator.update(previousCounts, duration); 
			
			for (int i = 0; i<accumulator.n; i++){
				counts[n+i]+=realAccumulate[i]; 
			}
			
			
			if (duration <= 0.0) {
				
				if (t==1){
					toAccumulate = accumulator.update(ret[t-1],stepSize);
					
					for (int i = 0; i<accumulator.n; i++){
						ret[t][n+i]=toAccumulate[i]; 
					}
				}
				t++;
				break;
			}
			
			double oldTime = currentTime; 
			currentTime += duration;

			
			int currentT = (int) Math.floor(currentTime / stepSize);
			if (currentT >= t) {
				//accumulate remainder of the subinterval
				toAccumulate = accumulator.update(ret[t],(stepSize*t-oldTime));
				for (int i = 0; i<accumulator.n; i++){
					ret[t][n+i]+=toAccumulate[i]; 
				}
				
				while (t <= currentT && t < ret.length - 1) {					
					t++;
					for (int i = 0; i < n; i++) {
						ret[t][i] = ret[t - 1][i];
					}
					toAccumulate = accumulator.update(ret[t-1],stepSize);
					//accumulate the whole subinterval
					//if (t<currentT){
						for (int i = 0; i<accumulator.n; i++){
							ret[t][n+i]=ret[t-1][n+i]+toAccumulate[i]; 
						}
					//}
				}
				toAccumulate = accumulator.update(ret[t-1],currentTime-stepSize*(t-1));
				//accumulate start of the new interval
				for (int i = 0; i<accumulator.n; i++){
					ret[t][n+i]=ret[t-1][n+i]+toAccumulate[i]; 
				}
			} else {
				//accumulate the event
				toAccumulate = accumulator.update(ret[t],duration);
				for (int i = 0; i<accumulator.n; i++){
					ret[t][n+i]+=toAccumulate[i]; 
				}
			}
			
			if (t < ret.length) {
				for (int i = 0; i < n; i++) {
					ret[t][i] = counts[i];
				}
			}
		}
		while (t < ret.length) {
			for (int i = 0; i < n; i++) {
				ret[t][i] = ret[t - 1][i];
			}
			toAccumulate = accumulator.update(ret[t-1],stepSize);
			for (int i = 0; i<accumulator.n; i++){
				ret[t][n+i]=ret[t-1][n+i]+toAccumulate[i]; 
			}

			
			t++;
		}
		return ret;
	}

	
}
