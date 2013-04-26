package uk.ac.imperial.doc.jexpressions.javaoutput.utils;

import org.apache.commons.math3.analysis.solvers.LaguerreSolver;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.util.ArithmeticUtils;

/* An attempt to port the distribution estimation code in http://webspn.hit.bme.hu/~telek/tools.htm */

public class DistributionEstimation {
	
	private static double INFTY = Double.MAX_VALUE;

	public static double[] estimateDistribution(double moments[], double x,
			double a, double b) {
		int nmoments = moments.length;
		double Ucdf;
		double Lcdf;
		double Uccdf;
		double Lccdf;
		double[][] bounds1 = new double[21][4];
		double[][] bounds2 = new double[21][4];
		double[][] bounds3 = new double[21][4];

		double[] mom = new double[21];
		double[] pos = new double[10];
		double[] weight = new double[10];

		double p;
		double max_pos, min_pos, sum;

		int i, n;

		if (nmoments < 2 || moments[0] != 1) {
			Ucdf = 1.0;
			Uccdf = 1.0;
			Lcdf = 0.0;
			Lccdf = 0.0;
			return new double[] { Ucdf, Lcdf, Uccdf, Lccdf };
		}
		if (nmoments > 21)
			nmoments = 21;

		if (x < a) {
			Ucdf = 0.0;
			Uccdf = 1.0;
			Lcdf = 0.0;
			Lccdf = 1.0;
			return new double[] { Ucdf, Lcdf, Uccdf, Lccdf };
		}
		if (x > b) {
			Ucdf = 1.0;
			Uccdf = 0.0;
			Lcdf = 1.0;
			Lccdf = 0.0;
			return new double[] { Ucdf, Lcdf, Uccdf, Lccdf };
		}

		for (n = 0; n < nmoments; n++) {
			bounds1[n][0] = 1.0;
			bounds2[n][0] = 1.0;
			bounds3[n][0] = 1.0;
			bounds1[n][1] = 0.0;
			bounds2[n][1] = 0.0;
			bounds3[n][1] = 0.0;
			bounds1[n][2] = 1.0;
			bounds2[n][2] = 1.0;
			bounds3[n][2] = 1.0;
			bounds1[n][3] = 0.0;
			bounds2[n][3] = 0.0;
			bounds3[n][3] = 0.0;
		}

		moveMoments(nmoments, moments, mom, 1.0, -x);
		for (n = 3; n <= nmoments; n = n + 2) {
			mom[0] = 1.0;
			if (!checkMoments1(n, mom))
				continue;
			p = mass1(n, mom);
			if (p <= 0 || p >= 1)
				continue;
			mom[0] = 1.0 - p;
			discreteConstruction(n, mom, pos, weight);

			max_pos = -INFTY;
			min_pos = INFTY;
			for (i = 0; i <= (n - 1) / 2 - 1; i++) {
				max_pos = Math.max(max_pos, pos[i]);
				min_pos = Math.min(min_pos, pos[i]);
			}

			if (max_pos < 0) {
				bounds1[n - 1][0] = 1.0;
				bounds1[n - 1][1] = 1.0 - p;
				bounds1[n - 1][2] = p;
				bounds1[n - 1][3] = 0;
			} else if (min_pos > 0) {
				bounds1[n - 1][0] = p;
				bounds1[n - 1][1] = 0;
				bounds1[n - 1][2] = 1;
				bounds1[n - 1][3] = 1.0 - p;
			} else {
				sum = 0;
				for (i = 0; i <= (n - 1) / 2 - 1; i++) {
					sum = sum + weight[i];
					if (weight[i] < 0)
						sum = -INFTY;
				}
				if (Math.abs(sum + p - 1.0) > 1e-10)
					continue;

				bounds1[n - 1][1] = bounds1[n - 1][3] = 0;
				for (i = 0; i <= (n - 1) / 2 - 1; i++) {
					if (pos[i] < 0)
						bounds1[n - 1][1] = bounds1[n - 1][1] + weight[i];
					if (pos[i] > 0)
						bounds1[n - 1][3] = bounds1[n - 1][3] + weight[i];
				}
				bounds1[n - 1][0] = bounds1[n - 1][1] + p;
				bounds1[n - 1][2] = bounds1[n - 1][3] + p;
			}
			;
		}

		Ucdf = 1.0;
		Uccdf = 1.0;
		Lcdf = 0.0;
		Lccdf = 0.0;
		for (n = 1; n <= nmoments; n++) {
			Ucdf = Math.min(Ucdf, bounds1[n - 1][0]);
			Lcdf = Math.max(Lcdf, bounds1[n - 1][1]);
			Uccdf = Math.min(Uccdf, bounds1[n - 1][2]);
			Lccdf = Math.max(Lccdf, bounds1[n - 1][3]);
		}
		return new double[] { Ucdf, Lcdf, Uccdf, Lccdf };
	}


	public static void moveMoments(int nmoments, double moments[],
			double res_moments[], double A, double B) {
		
		int i, j;

		double[] tmp_mom = new double[21];

		tmp_mom[0] = moments[0];
		for (i = 1; i < nmoments; i++)
			tmp_mom[i] = moments[i] * Math.pow(A, i);

		for (i = 0; i < nmoments; i++) {
			res_moments[i] = 0;
			for (j = 0; j <= i; j++)
				res_moments[i] = res_moments[i] + ArithmeticUtils.binomialCoefficientDouble(i, j) * tmp_mom[j]
						* Math.pow(B, i - j);
		}

	} 
	
	public static boolean checkMoments1(int nmoments, double mom[]) {

		int dim, row, col;
		RealMatrix M;

		if (nmoments % 2 == 0)
			return false;

		dim = (nmoments - 1) / 2 + 1;

		while (dim >= 1) {
			M = MatrixUtils.createRealMatrix(dim, dim);
			for (row = 0; row < dim; row++)
				for (col = 0; col < dim; col++)
					M.setEntry(row, col, mom[row + col]);
			if (new LUDecomposition(M).getDeterminant() <= 0) {
				return false;
			}
			dim--;
		}

		return true;
	}

	public static double alpha(int size, double a[]) {
		int N, i, j;

		N = size / 2 + 1;

		RealMatrix A = MatrixUtils.createRealMatrix(N, N);

		for (i = 0; i < N; i++)
			for (j = 0; j < N; j++)
				A.setEntry(i, j, a[i + j]);

		return new LUDecomposition(A).getDeterminant();

	} 

	public static double mass1(int nmoments, double moments[]) {
		int i;
		double p, num, denom;

		double[] numerator = new double[21];
		double[] denominator = new double[21];

		if (nmoments % 2 == 0)
			return 2.0;

		for (i = 0; i < nmoments; i++)
			numerator[i] = moments[i];
		for (i = 0; i < nmoments - 2; i++)
			denominator[i] = moments[i + 2];

		num = alpha(nmoments, numerator);
		denom = alpha(nmoments - 2, denominator);

		if (denom != 0) {
			p = num / denom;
		} else {
			p = 2.0;
		}
		;

		return p;

	} 

	public static void Vandermonde(int dim, double[] x, double[] w,
			double mom[]) {
		int i, j, k;
		double b, s, t, xx;
		double[] c;

		if (dim == 1) {
			w[0] = mom[0];
			return;
		}

		c = new double[dim];

		for (i = 1; i <= dim; i++)
			c[i - 1] = 0.0;
		c[dim - 1] = -x[0];

		for (i = 2; i <= dim; i++) {
			xx = -x[i - 1];
			for (j = (dim + 1 - i); j <= (dim - 1); j++)
				c[j - 1] += xx * c[j];
			c[dim - 1] += xx;
		}

		for (i = 1; i <= dim; i++) {
			xx = x[i - 1];
			t = b = 1.0;
			s = mom[dim - 1];
			for (k = dim; k >= 2; k--) {
				b = c[k - 1] + xx * b;
				s += mom[k - 2] * b;
				t = xx * t + b;
			}
			if (t != 0) {
				w[i - 1] = s / t;
			} else {
				w[i - 1] = -1;
			}
		}

	} 
	
	public static void discreteConstruction(int nmoments, double moments[],
			double x[], double w[]) {

		int N, dim, i, j, col, c;
		double[] coef;

		N = nmoments;

		if (N == 1) {
			x[0] = 0;
			w[0] = moments[0];
			return;
		}

		if (N % 2 == 1)
			N = N - 1; 
		dim = N / 2;

		RealMatrix M;
		coef = new double[dim + 1];

		M = MatrixUtils.createRealMatrix(dim, dim);

		for (c = dim; c >= 0; c--) {
			for (i = 0; i < dim; i++) 
			{
				col = 0;
				for (j = 0; j <= dim; j++)
					if (j != c) {
						M.setEntry(i, col, moments[i + j]);
						col++;
					}
			}
			coef[dim - c] = Math.pow(-1, c)
					* new LUDecomposition(M).getDeterminant();
		}
		Complex[] zero = new LaguerreSolver().solveAllComplex(coef,
				Math.random());

		for (i = 0; i < dim; i++)
			x[i] = zero[i].getReal();

		Vandermonde(dim, x, w, moments);
	} 
}
