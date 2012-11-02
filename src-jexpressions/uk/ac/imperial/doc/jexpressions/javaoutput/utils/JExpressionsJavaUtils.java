package uk.ac.imperial.doc.jexpressions.javaoutput.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.NormalDistributionImpl;


public class JExpressionsJavaUtils {
	
	private static Comparator<double[]> comparator = new Comparator<double[]> () {
		@Override
		public int compare(final double[] a, final double[] b) {
			return Double.compare(a[0], b[0]);
		}
	};
	
	public static<T> double[][] loadTimeSeriesFromFile(String fileName) {	
		try {
			FileReader f = new FileReader(fileName);
			BufferedReader in = new BufferedReader(f);
			String s = "";
			List<double[]> valuesList = new LinkedList<double[]>();
			while(true) {
				s = in.readLine();
				if (s == null) break;
				String[] tmp = s.split(" ");
				double x = Double.parseDouble(tmp[0]);
				double y = Double.parseDouble(tmp[1]);
				valuesList.add(new double[]{x, y});				
			}
			in.close();
			double[][] values = new double[valuesList.size()][];
			int i = 0;
			for (double[] v : valuesList) {
				values[i++] = v;
			}
			Arrays.sort(values, comparator);
			return values;
		} catch (FileNotFoundException ex) {
			throw new AssertionError("Time series file " + fileName + " not found!");
		} catch (IOException ex) {
			throw new AssertionError("Problems reading the file " + fileName + "!");
		}
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
	
	public static double normalMin(double mA, double mB, double thetaSq) {
		double theta = Math.sqrt(Math.max(thetaSq, 0.0));
		double safePhi = safe_Phi(mB - mA, theta);
		return 
		mA * safePhi + mB * (1.0 - safePhi) - theta * safe_phi(mB - mA, theta);
	}
	
	public static double normalMinProduct(double mA, double mB, double thetaSq, double mA2, double mB2, double add) {
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
		try {
			return normalDist.cumulativeProbability(x);
		} catch (MathException e) {
			throw new AssertionError("Math exception!");
		}		
	}

}
