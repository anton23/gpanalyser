package uk.ac.imperial.doc.jexpressions.javaoutput.utils;

import org.apache.commons.math3.distribution.NormalDistribution;


public class JExpressionsJavaUtils {
	
	public static double estimateLCDF(double a, double b, double x, double... moments) {
		return DistributionEstimation.estimateDistribution(moments, x, a, b)[3];
	}
	
	public static double estimateUCDF(double a, double b, double x, double... moments) {
		return DistributionEstimation.estimateDistribution(moments, x, a, b)[2];
	}


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
	
	private static NormalDistribution normalDist = new NormalDistribution(0,1,1.0E-12);
	
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
		
			if (bottom == 0.0) {
				if (top > 0.0) return 1.0;
				else return 0.0;
			} else 
			return normalDist.cumulativeProbability(top / bottom);
				
	}
	
	public static double normalMin(double mA, double mB, double thetaSq) {
		double theta = Math.sqrt(Math.max(thetaSq, 0.0));
		double safePhi = safe_Phi(mB - mA, theta);
		return 
		mA * safePhi + mB * (1.0 - safePhi) - theta * safe_phi(mB - mA, theta);
	}
	
	public static double normalMinProduct(double mA, double mB, double thetaSq, double mA2, double mB2, double add) {
		double theta = Math.sqrt(Math.max(thetaSq, 0.0));
		double safePhi = safe_Phi(mB2 - mA2, theta*add);
		return 
		mA2 * safePhi + mB2 * (1.0 - safePhi) - theta * add * safe_phi(mB2 - mA2, theta*add);					
	}
	
	public static double normalMinProduct2(double mA, double mB, double thetaSq, double mA2, double mB2, double add) {
		double theta = Math.sqrt(Math.max(thetaSq, 0.0));
		double safePhi = safe_Phi(mB - mA, theta);
		return 
		mA2 * safePhi + mB2 * (1.0 - safePhi) - theta * add * safe_phi(mB - mA, theta);
		
	}
	
	public static double normalInequality(double muL, double varL, double muR, double varR, double cov) {
		double stDev = Math.sqrt(varL + varR + 2*cov);
		if (Double.isNaN(stDev)) {
			stDev = 0.0;
		}
		double mu = -muL + muR;
		double ret = safe_Phi(0.0-mu, stDev);
		return ret;
	}
	
	public static double Phi(double x) {		
		return normalDist.cumulativeProbability(x);			
	}

}
