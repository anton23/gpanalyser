package uk.ac.imperial.doc.gpa.forecasting.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class MathExtra {
	
	/**
	 * @param num
	 * @return Write out {@code num} in ####.######## format
	 */
	public static String getDecimalFmt(final double num) {
		NumberFormat f = new DecimalFormat("####.########");
		f.setGroupingUsed(false);
		return f.format(num);
	}
	
	/**
	 * @param num
	 * @param numDec
	 * @return round {@code num} to {@code numDec} decimal places
	 */
	public static double round(final double num, final double numDec) {
		double mult = Math.pow(10, numDec); 
		return ((double)Math.round(num*mult))/mult;
	}
	
	/**
	 * @param num
	 * @return round {@code num} to two decimal places
	 */
	public static double twoDecim(final double num) {
		return round(num,2);
	}
	
	/**
	 * @param a
	 * @param b
	 * @return rounded {@code a} / {@code b} * 100
	 */
	public static double dividePerc(final int a, final int b) {
		return dividePerc((double)a,(double)b);
	}
	
	/**
	 * @param a
	 * @param b
	 * @return rounded {@code a} / {@code b} * 100
	 */
	public static double dividePerc(final double a, final double b) {
		return twoDecim(a/b*100);
	}

	/**
	 * @param samples {x1,x2,...}
	 * @return mean of elements in {@code l}
	 */
	public static double calcMean(final List<Integer> samples) {
		if (samples==null || samples.size()==0) {throw new AssertionError("Samples empty or NULL");}
		double sum = 0;
		for (double i : samples) {sum += i;}
		return sum / (double)samples.size();
	}

	/**
	 * @param samples {x1,x2,...}
	 * @return E[X^2]
	 */
	public static double calc2ndRawMoment(final List<Integer> samples) {
		if (samples==null || samples.size()==0) {throw new AssertionError("Samples empty or NULL");}
		double sum = 0;
		for (double i : samples) {sum += i*i;}
		return sum / (double)samples.size();
	}
	
	/**
	 * @param samples {x1,x2,...}
	 * @return E[X^3]
	 */
	public static double calc3rdRawMoment(final List<Integer> samples) {
		if (samples==null || samples.size()==0) {throw new AssertionError("Samples empty or NULL");}
		double sum = 0;
		for (double i : samples) {sum += i*i*i;}
		return sum / (double)samples.size();
	}
	
	/**
	 * @param samples {x1,x2,...}
	 * @return std dev of elements in {@code l}
	 */
	public static double calcStdDev(final List<Integer> samples) {
		if (samples==null || samples.size()==0) {throw new AssertionError("Samples empty or NULL");}
		double mean = calcMean(samples);
		double sqSum = 0;
		for (double i : samples) {sqSum += i*i;}
		return Math.sqrt(sqSum / (double)(samples.size()-1) - mean * mean);
	}
	
	public static Map<Integer, Double> getPMF(Map<Integer, Integer> map) {
		Map<Integer,Double> pmf = new TreeMap<Integer,Double>();
		double sum = 0;
		for (Integer i : map.values()) {sum += i;}
		for (Entry<Integer, Integer> e : map.entrySet()) {
			pmf.put(e.getKey(), ((double)e.getValue())/sum);
		}
		return pmf;
	}
	
	public static Map<Integer,Integer> genCDF(List<Integer> samples, int stepSize)
	{
		Map<Integer,Integer> cdf = new TreeMap<Integer,Integer>();
		Collections.sort(samples);
		for (int i=0;i<=100;i+=stepSize) {
			int index = (int)Math.min(Math.ceil(((double)i)/100.0*((double)samples.size())),samples.size());
			index = (index > 0) ? index-1: index;
			cdf.put(i, samples.get(index));
		}
		return cdf;
	}

	public static int[] genCDFQuantiles(List<Integer> samples, int[] quantiles)
	{
		if (samples == null || samples.size() == 0) {return null;}
		int[] quants = new int[quantiles.length];
		Collections.sort(samples);
		for (int i=0; i<quantiles.length; i++){
			int index = (int)Math.min(Math.ceil(((double)quantiles[i])/100.0*((double)samples.size())),samples.size());
			index = (index > 0) ? index-1: index;
			quants[i] = samples.get(index);
		}
		return quants;
	}
}
