package uk.ac.imperial.doc.pctmc.simulation.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.ode.nonstiff.DormandPrince853Integrator;

import uk.ac.imperial.doc.pctmc.odeanalysis.utils.ODEIntegrators;
import uk.ac.imperial.doc.pctmc.odeanalysis.utils.SystemOfODEs;
import uk.ac.imperial.doc.pctmc.utils.PCTMCLogging;



public class GillespieSimulator {

	
	/*public static double[][] simulateAccumulated(AggregatedStateNextEventGenerator g,
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
			
			double oldTime = currentTime; 
			currentTime += duration;
			
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

			
			
			double[] realAccumulate = accumulator.update(previousCounts, duration); 
			
			for (int i = 0; i<accumulator.n; i++){
				counts[n+i]+=realAccumulate[i]; 
			}
			
			


			
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
	}*/
	
	
	public static double[][] simulateAccumulated(AggregatedStateNextEventGenerator g,
			double initial[], double stopTime, double stepSize, final AccumulatorUpdater accumulator) {
		final int n = initial.length;
		
		SystemOfODEs f = new SystemOfODEs() {
			
			@Override
			public int getDimension() {
				return n + accumulator.n;
			}
			
			@Override
			public void computeDerivatives(double t, double[] y, double[] yDot)
					throws MaxCountExceededException, DimensionMismatchException {
				double[] tmp = accumulator.update(y, 1.0);
				for (int i = 0; i < n; i++) {
					yDot[i] = 0;
				}
				for (int i = 0; i < accumulator.n; i++) {
					yDot[n + i] = tmp[i];
				}
			}
		};

		double[] newInitial = new double[n+accumulator.n];
		for (int i = 0; i < initial.length; i++) {
			newInitial[i] = initial[i];//(int) Math.floor(initial[i]);
		}
		
		return simulateAccumulatedWithODEs(g, newInitial, n, stopTime, stepSize, f);
	}
	
	// initial - vector of length #populations + #accumulations
	// 
	public static double[][] simulateAccumulatedWithODEs(AggregatedStateNextEventGenerator g,
			double initial[], int np,  double stopTime, double stepSize, SystemOfODEs accumulator) {
		int n = accumulator.getDimension();
		double[][] ret = new double[(int) Math.ceil(stopTime / stepSize)][n];

		double currentTime = 0;

		double[] counts = new double[n];

		for (int i = 0; i < np; i++) {
			counts[i] = initial[i];
			ret[0][i] = initial[i];
		}

		g.initCoefficients();

		int t = 1;
		
		double[] previousCounts = new double[n];
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("integrator", DormandPrince853Integrator.class.getSimpleName().replace("Integrator", ""));
		//parameters.put("density", 10);
		parameters.put("bounds", "LAST");
		PCTMCLogging.setVisible(false);
		double oldTime = 0.0;
		double[][] newValues = null;
		while (currentTime < stopTime) {
						
			for (int i = 0; i<n; i++){
				previousCounts[i] = counts[i]; 
			}
			
			double duration = g.nextStep(counts);
			
			if (duration == 0.0) {
				duration = stopTime - currentTime;
			}
			
			oldTime = currentTime; 
			currentTime += duration;
			
            newValues = ODEIntegrators.solveODEs(accumulator, previousCounts, oldTime, currentTime, stepSize, parameters);
			
			for (int i = 0; i < newValues.length - 1; i++) {
				if (t < ret.length) { // In case the last step jumps over further
					ret[t++] = newValues[i];
				}
			}

			for (int i = 0; i < n - np; i++) {
				counts[np + i] = newValues[newValues.length - 1][np + i];
			}
			
			// In case current time is a multiple of stepSize, take it
			if (Math.abs(Math.IEEEremainder(currentTime, stepSize)) < 1e-10) {
				ret[t++] = Arrays.copyOf(counts, n);
			}
		}		
		PCTMCLogging.setVisible(true);
		return ret;
	}

	
}
