package uk.ac.imperial.doc.pctmc.analysis;

import java.util.ArrayList;
import java.util.List;

import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.YIntervalSeries;
import org.jfree.data.xy.YIntervalSeriesCollection;

public class AnalysisUtils {

	
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
	
	/** 
	 * Creates a Dataset from an array and step information.
	 * @param dataPoints
	 * @param dataPointsCIWidth
	 * @param stepSize
	 * @param names
	 * @return
	 */
	public static XYDataset getDatasetFromArray(double[][] dataPoints, double[][] dataPointsCIWidth, double stepSize, String[] names)
	{
		return getDatasetFromArray(dataPoints, dataPointsCIWidth, 0.0, stepSize, names);		
	}

	public static XYDataset getDatasetFromArray(double[][] dataPoints, double[][] dataPointsCIWidth, double min, double stepSize, String[] names)
	{
		XYDataset dataset = null;
		if (dataPointsCIWidth==null)
		{
			dataset = new XYSeriesCollection();
			for (int i = 0; i < names.length; i++) {
				((XYSeriesCollection)dataset).addSeries(new XYSeries(names[i]));
			}
		}
		else
		{
			dataset = new YIntervalSeriesCollection();
			for (int i = 0; i < names.length; i++) {
				((YIntervalSeriesCollection)dataset).addSeries(new YIntervalSeries(names[i]));
			}
		}

		double currentTime = min;
		for (int p = 0; p < dataPoints.length; p++)
		{
			for (int i = 0; i < dataPoints[p].length; i++)
			{
				if (!Double.isInfinite(dataPoints[p][i]))
				{
					if (dataPointsCIWidth==null)
					{
						((XYSeriesCollection)dataset).getSeries(i).add(currentTime, dataPoints[p][i]);
					}
					else
					{
						((YIntervalSeriesCollection)dataset).getSeries(i).add(currentTime, dataPoints[p][i], Math.max(0,dataPoints[p][i]-dataPointsCIWidth[p][i]), dataPoints[p][i]+dataPointsCIWidth[p][i]);
					}
				}
			}
			currentTime += stepSize;
		}
		return dataset;
	}
}