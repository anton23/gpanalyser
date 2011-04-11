package uk.ac.imperial.doc.pctmc.analysis;

import java.util.ArrayList;
import java.util.List;

import org.jfree.data.DomainOrder;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.XYZDataset;

public class AnalysisUtils {

	public static XYSeriesCollection getDataset(double[][] dataPoints,
			double stepSize, String[] names) {
		return getDataset(dataPoints, 0.0, stepSize, names);		
	}

	public static XYSeriesCollection getDataset(double[][] dataPoints,
			double min, double stepSize, String[] names) {
		List<XYSeries> series = new ArrayList<XYSeries>();

		for (int i = 0; i < names.length; i++) {
			series.add(i, new XYSeries(names[i]));
		}
		XYSeriesCollection dataset = new XYSeriesCollection();
		for (XYSeries s : series) {
			dataset.addSeries(s);
		}

		double currentTime = min;

		for (int p = 0; p < dataPoints.length; p++) {
			for (int i = 0; i < dataPoints[p].length; i++) {
				series.get(i).add(currentTime, dataPoints[p][i]);
			}
			currentTime += stepSize;
		}
		return dataset;
	}
	
	public static XYZDataset getXYZDataset(final double[][] dataPoints,final double min1, double max1, final double min2, double max2) {
		final int points1 = dataPoints.length; 
		final int points2 = dataPoints[0].length; 
		final double d1 = (max1-min1)/points1;
		final double d2 = (max2-min2)/points2;
		return new XYZDataset() {
			
			@Override
			public Number getZ(int series, int item) {
				return getZValue(series, item); 
			}

			@Override
			public double getZValue(int series, int item) {
				int[] ij = getIJ(item); 
				if (ij[0]>=dataPoints.length){
					System.out.println("bla");
				}
				return dataPoints[ij[0]][ij[1]];
			}

			@Override
			public void setGroup(DatasetGroup arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void removeChangeListener(DatasetChangeListener arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public DatasetGroup getGroup() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public void addChangeListener(DatasetChangeListener arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public int indexOf(Comparable arg0) {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public Comparable getSeriesKey(int arg0) {
				return "blah";
			}
			
			@Override
			public int getSeriesCount() {
				// TODO Auto-generated method stub
				return 1;
			}
			
			private int[] getIJ(int item){
				int[] ret = new int[2]; 
				ret[0] = (int)Math.floor((double)item/points2); 
				ret[1] = item%points2; 
				return ret; 
			}
			
			@Override
			public double getYValue(int series, int item) {
				int[] ij = getIJ(item);
				return min2+ij[1]*d2+d2/2; //+d2/2 is a hack to align the block chart
			}
			
			@Override
			public Number getY(int series, int item) {
				return getYValue(series, item);
			}
			
			@Override
			public double getXValue(int series, int item) {
				int[] ij = getIJ(item);
				return min1+ij[0]*d1+d1/2;
			}
			
			@Override
			public Number getX(int series, int item) {
				return getXValue(series,item); 
			}
			
			@Override
			public int getItemCount(int series) {
				return points1*points2;
			}
			
			@Override
			public DomainOrder getDomainOrder() {
				return DomainOrder.ASCENDING;
			}
		};
	}

}
