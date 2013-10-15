package uk.ac.imperial.doc.gpa.probes;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class ProbeGraph extends JFrame
{
	private static final long serialVersionUID = -2098466371691836445L;
	private final static String title = "CDF";
    private final XYSeriesCollection times = new XYSeriesCollection ();

    public ProbeGraph ()
    {
        super (title);
        final ChartPanel chartPanel = createDemoPanel ();
        this.add (chartPanel, BorderLayout.CENTER);
        setDefaultCloseOperation (WindowConstants.EXIT_ON_CLOSE);
        setSize (new Dimension(640, 480));
        setLocationRelativeTo (null);
        setVisible (true);
    }

    public void renderData (CDF cdf, String name, double stepSize)
    {
        XYSeries measurements = new XYSeries (name);

        double[] data = cdf.getValues ();
        for (int i = 0; i < data.length; ++i)
        {
            measurements.add (i * stepSize, data[i]);
        }

        times.addSeries (measurements);
    }

    private ChartPanel createDemoPanel ()
    {
        JFreeChart jfreechart = ChartFactory.createXYLineChart
                ("CDF", "Time", "Probability", times, PlotOrientation.VERTICAL,
                        true, false, false);
        XYPlot xyPlot = (XYPlot) jfreechart.getPlot ();
        xyPlot.setDomainCrosshairVisible (true);
        xyPlot.setRangeCrosshairVisible(true);
        return new ChartPanel (jfreechart);
    }
}
