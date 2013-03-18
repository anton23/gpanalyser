package uk.ac.imperial.doc.jexpressions.javaoutput.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.distribution.NormalDistribution;

import uk.ac.imperial.doc.jexpressions.constants.FileColumn;
import uk.ac.imperial.doc.pctmc.utils.PCTMCOptions;


public class JExpressionsJavaUtils {
	
	static Map<String, FileColumn> files;
	public static Map<String, double[][]> fileValues;

	
	private static int binarySearch(double[][] values, double v) {
		int l = 0; int r = values.length - 1;
		while (l <= r) {
			int mid = l + (r - l) / 2;
			if (values[mid][0] < v) {
				l = mid + 1;
			} else if (values[mid][0] > v) {
				r = mid - 1;
			} else {
				return mid;
			}
				
		}
		return r;
	}
	
	public static double evaluate(String fun, double t) {
		double[][] values = fileValues.get(fun);		
		int i = binarySearch(values, t);
		if (i < 0) {
			throw new AssertionError("Function " + fun + " is not defined for values " + t);
		}
		// Interpolates two successive values
		/*if (i < values.length - 1) {
			return values[i][1] + (values[i+1][1] - values[i][1]) * (t - values[i][0]) / (values[i+1][0] - values[i][0]); 
		}*/
		return values[i][1];
	}
	
	private static Comparator<double[]> comparator = new Comparator<double[]> () {
		
		@Override
		public int compare(final double[] a, final double[] b) {
			return Double.compare(a[0], b[0]);
		}
	};
	
	private static void loadFile(String fun, FileColumn fileColumn) {
		try {
			String path = PCTMCOptions.filePath;
			// TODO investigate why this doesn't work properly in jar files
			FileReader f = new FileReader(path + "/" + fileColumn.getFile());
			BufferedReader in = new BufferedReader(f);
			String s = "";
			List<double[]> valuesList = new LinkedList<double[]>();
			while(true) {
				s = in.readLine();
				if (s == null) break;
				String[] tmp = s.split(" ");
				double x = Double.parseDouble(tmp[0]);
				double y = Double.parseDouble(tmp[fileColumn.getColumn()]);
				valuesList.add(new double[]{x, y});				
			}
			double[][] values = new double[valuesList.size()][];
			int i = 0;
			for (double[] v : valuesList) {
				values[i++] = v;
			}
			Arrays.sort(values, comparator);
			fileValues.put(fun, values);
			in.close();
		} catch (FileNotFoundException e) {
			throw new AssertionError("File " + fileColumn + " not found!");
		} catch (IOException e) {
			throw new AssertionError("Problems reading the file " + fileColumn + "!");
		} 
	}
	
	public static void loadFiles(Map<String, FileColumn> _files) {
		if (files == null) {
			files = _files;
			fileValues = new HashMap<String, double[][]>();
		}		
		for (Map.Entry<String, FileColumn> e : _files.entrySet()) {
			loadFile(e.getKey(), e.getValue());
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
