package uk.ac.imperial.doc.pctmc.odeanalysis.utils;


public class RungeKutta {
	// see wikipedia
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
	}
}
