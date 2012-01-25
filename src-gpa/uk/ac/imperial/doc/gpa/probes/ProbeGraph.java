package uk.ac.imperial.doc.gpa.probes;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.jfree.data.time.SimpleTimePeriod;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;

public class ProbeGraph extends JFrame
{
    private final static String title = "Global probes measurements";
    private TaskSeries measurements = new TaskSeries ("Measurements");

    public ProbeGraph ()
    {
        super (title);
        final ChartPanel chartPanel = createDemoPanel ();
        this.add (chartPanel, BorderLayout.CENTER);
        setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
        setSize (new Dimension(640, 480));
        setLocationRelativeTo (null);
        setVisible (true);
    }

    public void renderData
            (Collection<ProbeTime> times, String name, double end)
    {
        Task newTask = new Task
            (name, new SimpleTimePeriod(0, (long) end));

        for (ProbeTime time : times)
        {
            newTask.addSubtask(new Task
                    ("", new SimpleTimePeriod
                            ((long) time.getStartTime (),
                             (long) time.getStopTime ())));
        }

        measurements.add (newTask);
    }

    private ChartPanel createDemoPanel ()
    {
        TaskSeriesCollection times = new TaskSeriesCollection ();
        times.add (measurements);
        JFreeChart jfreechart = ChartFactory.createGanttChart
            (null, null, "Time", times, false, false, false);
        CategoryPlot categoryPlot = (CategoryPlot) jfreechart.getPlot ();
        categoryPlot.setDomainCrosshairVisible (true);
        categoryPlot.setRangeCrosshairVisible(true);
        return new ChartPanel (jfreechart);
    }
}
