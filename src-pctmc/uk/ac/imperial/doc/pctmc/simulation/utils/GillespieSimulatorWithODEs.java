package uk.ac.imperial.doc.pctmc.simulation.utils;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;

import uk.ac.imperial.doc.pctmc.odeanalysis.utils.SystemOfODEs;
import uk.ac.imperial.doc.pctmc.utils.PCTMCLogging;
public class GillespieSimulatorWithODEs extends GillespieSimulator {
	
	@Override
	public double[][] simulateAccumulated(AggregatedStateNextEventGenerator g,
			double initial[], double stopTime, double stepSize, final AccumulatorUpdater accumulator) {
		final int n = initial.length - accumulator.n;
		
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
			newInitial[i] = initial[i];
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

		for (int i = 0; i < initial.length; i++) {
			counts[i] = initial[i];
			ret[0][i] = initial[i];
		}

		g.initCoefficients();
	
		double[] previousCounts = new double[n];
		
	
		PCTMCLogging.setVisible(false);
		double oldTime = 0.0;
		
		SimulationODEHelper odes = new SimulationODEHelper(g, initial, np, stopTime, stepSize, accumulator);
		while (currentTime < stopTime) {
						
			for (int i = 0; i<n; i++){
				previousCounts[i] = counts[i]; 
			}
			
			double duration = g.nextStep(counts, currentTime);
			
			if (duration == 0.0) {
				duration = stopTime - currentTime;
			}
			
			oldTime = currentTime; 
			currentTime += duration;
			
			odes.integrate(oldTime, currentTime, previousCounts);

			for (int i = 0; i < n - np; i++) {
				counts[np + i] = previousCounts[np + i];
			}
		}		
		PCTMCLogging.setVisible(true);
		return odes.getRet();
	}

	
}
