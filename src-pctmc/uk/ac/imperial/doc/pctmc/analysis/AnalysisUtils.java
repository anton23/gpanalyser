package uk.ac.imperial.doc.pctmc.analysis;

import java.util.ArrayList;
import java.util.List;

import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class AnalysisUtils {

	/** 
	 * Creates a Dataset from an array and step information.
	 * @param dataPoints
	 * @param stepSize
	 * @param names
	 * @return
	 */
	public static XYSeriesCollection getDatasetFromArray(double[][] dataPoints,
			double stepSize, String[] names) {
		return getDatasetFromArray(dataPoints, 0.0, stepSize, names);		
	}

	public static XYSeriesCollection getDatasetFromArray(double[][] dataPoints,
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
				if (!Double.isInfinite(dataPoints[p][i]))series.get(i).add(currentTime, dataPoints[p][i]);
			}
			currentTime += stepSize;
		}
		return dataset;
	}
}
