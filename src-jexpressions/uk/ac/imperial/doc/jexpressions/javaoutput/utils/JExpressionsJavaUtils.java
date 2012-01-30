package uk.ac.imperial.doc.jexpressions.javaoutput.utils;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.NormalDistributionImpl;


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
	
	private static NormalDistributionImpl normalDist = new NormalDistributionImpl(0,1,1.0E-12);
	
	public static double phi(double x) {
		double ret = normalDist.density(x);
		if (Double.isNaN(ret) || Double.isInfinite(ret)) {
			throw new AssertionError("AAA");
		}
		return ret;		
	}
	
	public static double safe_phi(double top, double bottom) {
		if (bottom == 0.0) {
			return 0.0;
		}
		double ret = normalDist.density(top/bottom);
		return ret;		
	}
	
	public static double safe_Phi(double top, double bottom) {
		try {
			if (bottom == 0.0) {
				if (top > 0.0) return 1.0;
				else return 0.0;
			} else 
			return normalDist.cumulativeProbability(top / bottom);
		} catch (MathException e) {
			throw new AssertionError("Math exception!");
		}		
	}
	
	public static double Phi(double x) {
		try {
			return normalDist.cumulativeProbability(x);
		} catch (MathException e) {
			throw new AssertionError("Math exception!");
		}		
	}

}
