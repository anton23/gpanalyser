package uk.ac.imperial.doc.pctmc.odeanalysis.utils;

import java.util.Arrays;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math3.ode.FirstOrderIntegrator;
import org.apache.commons.math3.ode.nonstiff.ClassicalRungeKuttaIntegrator;
import org.apache.commons.math3.ode.sampling.FixedStepHandler;
import org.apache.commons.math3.ode.sampling.StepNormalizer;


public class RungeKutta {
	
	
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
		FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
			
			@Override
			public int getDimension() {
				return n;
			}
			
			@Override
			public void computeDerivatives(double t, double[] y, double[] yDot)
					throws MaxCountExceededException, DimensionMismatchException {
				double[] yDotF = f.derivn(t, y);
				for (int i = 0; i < n; i ++) {
					yDot[i] = yDotF[i];
				}				
			}
		};
		
		integrator.integrate(ode, 0.0, init, stopTime, init);
		return ret;
		
	}
	
/*	// see wikipedia
	public static double[][] rungeKutta(SystemOfODEs f, double initial[],
			double stopTime, double stepSize, int density) {		
		double h = stepSize / density;
		double x = 0;
		int n = initial.length;
		double y[] = initial;
		double k1[] = new double[n];
		double k2[] = new double[n];
		double k3[] = new double[n];
		double k4[] = new double[n];

		double ret[][] = new double[(int) Math.ceil(stopTime / stepSize)][n];

		double tmp[] = new double[n];
		int p = 0;
		while (x < stopTime) {
			if (p % density == 0 && p / density < ret.length) {
				for (int i = 0; i < n; i++) {
					ret[p / density][i] = y[i];
				}
			}
			p++;

			// k1 = f(t_n,y_n)
			k1 = f.derivn(x, y);

			// k2 = f(t_n+1/2h,y_n+1/2h_k1)
			for (int i = 0; i < n; i++) {
				tmp[i] = y[i] + h * k1[i] / 2;
			}
			k2 = f.derivn(x + h / 2, tmp);
			// k3 = f(t_n+1/2h,y_n+1/2hk+2)
			for (int i = 0; i < n; i++) {
				tmp[i] = y[i] + h * k2[i] / 2;
			}
			k3 = f.derivn(x + h / 2, tmp);
			// k4 = f(t_n+h,y_n+hk_3)
			for (int i = 0; i < n; i++) {
				tmp[i] = y[i] + h * k3[i];
			}
			k4 = f.derivn(x + h, tmp);

			for (int i = 0; i < n; i++) {
				y[i] += h / 6 * (k1[i] + 2 * k2[i] + 2 * k3[i] + k4[i]);
			}
			x = x + h;
		}
		return ret;
	}*/
}
