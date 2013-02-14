package uk.ac.imperial.doc.pctmc.odeanalysis.utils;

import java.util.Arrays;

import org.apache.commons.math3.ode.FirstOrderIntegrator;
import org.apache.commons.math3.ode.nonstiff.ClassicalRungeKuttaIntegrator;
import org.apache.commons.math3.ode.sampling.FixedStepHandler;
import org.apache.commons.math3.ode.sampling.StepNormalizer;


public class ODEIntegrator {
	
	public static double[][] rungeKutta(final SystemOfODEs f, double initial[],
			double stopTime, final double stepSize, int density) {
		final int n = initial.length;

		double[] init = Arrays.copyOf(initial, n);		
		FirstOrderIntegrator integrator = new ClassicalRungeKuttaIntegrator(stepSize / density);
		final double ret[][] = new double[(int) Math.ceil(stopTime / stepSize)][];
		
		FixedStepHandler fixedStepHandler = new FixedStepHandler() {
			int step;
		    public void init(double t0, double[] y0, double t) {step = 0;}
			public void handleStep(double t, double[] y, double[] yDot,
					boolean isLast) {
				ret[step++] = Arrays.copyOf(y, n);
			}
		};
		
		integrator.addStepHandler(new StepNormalizer(stepSize, fixedStepHandler));
		integrator.integrate(f, 0.0, init, stopTime-stepSize, init);
		return ret;
	}
}
