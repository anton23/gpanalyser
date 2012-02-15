// f is the function pointer
void rungeKutta (
		void (*f) (double, double*, double*, double*),
		double *r, double *initial, int initial_length,
		double stopTime, double stepSize, int density,
		double **ret, int ret_length
	)
{
	double h = ((double) stepSize) / ((double) density);
	double x = 0;
	int n = initial_length;
	double *y = initial;
	double k1[n];
	double k2[n];
	double k3[n];
	double k4[n];

	double tmp[n];
	int p = 0;
	while (x < stopTime) {
		if (!(p % density) && p / density < ret_length) {
			for (int i = 0; i < n; ++i) {
				ret[p / density][i] = y[i];
			}
		}
		++p;

		// k1 = f(t_n,y_n)
		f (x, y, r, k1);

		// k2 = f(t_n+1/2h,y_n+1/2h_k1)
		for (int i = 0; i < n; ++i)
		{
			tmp[i] = y[i] + h * k1[i] / 2;
		}
		f (x + h / 2, tmp, r, k2);

		// k3 = f(t_n+1/2h,y_n+1/2hk+2)
		for (int i = 0; i < n; ++i)
		{
			tmp[i] = y[i] + h * k2[i] / 2;
		}
		f (x + h / 2, tmp, r, k3);

		// k4 = f(t_n+h,y_n+hk_3)
		for (int i = 0; i < n; ++i)
		{
			tmp[i] = y[i] + h * k3[i];
		}
		f (x + h, tmp, r, k4);

		for (int i = 0; i < n; ++i)
		{
			y[i] += h / 6 * (k1[i] + 2 * k2[i] + 2 * k3[i] + k4[i]);
		}
		x += h;
	}
}
