package uk.ac.imperial.doc.jexpressions.javaoutput.utils;

public class JExpressionsJavaUtils {

	public static double div(double a, double b) {
		if (b == 0.0) {
			return 0.0;
		} else {
			return a / b;
		}
	}

	public static double divdivmin(double a, double b, double c, double d) {
		return div(a * b, c * d) * Math.min(c, d);
	}

	public static double divmin(double a, double b, double c) {
		return div(a, b) * Math.min(c, b);
	}

	public static double ifpos(double c, double n, double p) {
		if (c < 0)
			return n;
		else
			return p;
	}

	public static double chebyshev(double e, double m) {
		return m / (m + e);
	}

}
