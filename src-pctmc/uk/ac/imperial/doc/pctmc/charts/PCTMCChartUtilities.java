package uk.ac.imperial.doc.pctmc.charts;

import java.awt.Color;
import java.awt.Component;
import java.awt.Paint;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.DrawingSupplier;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.GrayPaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYZDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.RefineryUtilities;

public class PCTMCChartUtilities {
	public static boolean gui = true;

	private static double lastx = 0, lasty = 0;

	public static void nextBatch() {
		lasty = 0;
		lastx += 0.1;
	}
	
	
	
	public static void drawBlockChart(XYZDataset dataset, String xlabel,
			String ylabel, String zlabel, double dx, double dy, double minz, double maxz,String chartTitle, String windowTitle) {
		if (!gui)
			return;
		NumberAxis xAxis = new NumberAxis(xlabel);
		xAxis.setAutoRangeIncludesZero(false);
		NumberAxis yAxis = new NumberAxis(ylabel);

		yAxis.setAutoRangeIncludesZero(false);
		XYBlockRenderer renderer = new XYBlockRenderer();
		if (minz==maxz) return; 
		GrayPaintScale scale = new GrayPaintScale(minz, maxz);
		renderer.setPaintScale(scale);
		renderer.setBlockWidth(dx);
		renderer.setBlockHeight(dy);
		
		XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
		plot.setBackgroundPaint(Color.DARK_GRAY);

		JFreeChart chart = new JFreeChart(chartTitle, plot);
		chart.removeLegend();

		NumberAxis scaleAxis = new NumberAxis("");//zlabel);
		scaleAxis.setRange(minz,maxz);
		
		PaintScaleLegend legend = new PaintScaleLegend(scale, scaleAxis);
		
	      legend.setFrame(new BlockBorder(Color.GRAY));
	        legend.setPadding(new RectangleInsets(5, 5, 5, 5));
	        legend.setMargin(new RectangleInsets(4, 6, 40, 6));
	        legend.setPosition(RectangleEdge.RIGHT);

		chart.addSubtitle(legend);
		
		
		chart.setBackgroundPaint(Color.white);
		
		ChartPanel chartPanel = new ChartPanel(chart);

		chartPanel.setMouseZoomable(true, false);
		chartPanel.setDoubleBuffered(true);
		
		final JFrame frame = new JFrame(windowTitle);
		
		frame.setPreferredSize(new java.awt.Dimension(600, 400));
		frame.setContentPane(chartPanel);
		frame.pack();
		frame.setVisible(true);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		RefineryUtilities.positionFrameOnScreen(frame, lastx, lasty);
		lastx += 0.05;
		lasty += 0.05;
	}
	
	
	 
	  
	
	public static void setWindow(String windowTitle){
		if (!gui) return; 
		String mainClass = "";// System.getenv("JAVA_MAIN_CLASS");
		JFrame frame;
		frame = new JFrame(mainClass+" " + windowTitle);
		JTabbedPane tab = new JTabbedPane();		
		frame.setPreferredSize(new java.awt.Dimension(600, 480));
 
		frame.setContentPane(tab);
		frame.pack();
		frame.setVisible(true);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		windows.put(windowTitle,frame);
		tabs.put(windowTitle,tab); 
	}
	
	private static Map<String,JFrame> windows = new HashMap<String, JFrame>(); 
	private static Map<String,JTabbedPane> tabs = new HashMap<String, JTabbedPane>();
	
	public static JFrame getWindow(String title){
		return windows.get(title); 
	}
	
	public static void addChart(Component component,String windowTitle){
		if (!gui) return;
		JTabbedPane tab;
		if (!windows.containsKey(windowTitle)) setWindow(windowTitle);
		tab = tabs.get(windowTitle);
		tab.addTab("", component);
	}

	public static void drawChart(XYDataset dataset, String xlabel,
			String ylabel, String chartTitle, String windowTitle) {
		if (!gui) return;
		JFrame frame;	
		JTabbedPane tab;
		if (!windows.containsKey(windowTitle)) setWindow(windowTitle);
		tab = tabs.get(windowTitle);
		frame = windows.get(windowTitle);
		
		JFreeChart chart = ChartFactory.createXYLineChart(null, // chart //
																// title
				xlabel, // x axis label
				ylabel, // y axis label
				dataset, // data
				PlotOrientation.VERTICAL, true, // include legend
				true, // tooltips
				false // urls
				);

		
		 
		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setMouseZoomable(true, false);
		chartPanel.setDoubleBuffered(true);		
		tab.addTab("", chartPanel);

		//RefineryUtilities.positionFrameOnScreen(frame, lastx, lasty);
		//lastx += 0.05;
		//lasty += 0.05;
	}
	
	
	public static void drawChartPairs(XYDataset dataset, String xlabel,
			String ylabel, String chartTitle, String windowTitle) {
		if (!gui)
			return;

		JFreeChart chart = ChartFactory.createXYLineChart(null, // chart //
																// title
				xlabel, // x axis label
				ylabel, // y axis label
				dataset, // data
				PlotOrientation.VERTICAL, true, // include legend
				true, // tooltips
				false // urls
				);

		
		
		Paint[] colors = DefaultDrawingSupplier.DEFAULT_PAINT_SEQUENCE;
		Paint[] newColors = new Paint[colors.length*2];
		
		for (int i = 0; i<colors.length; i++){
			newColors[2*i] = colors[i]; 
			newColors[2*i+1] = colors[i]; 
		}
		

		DrawingSupplier newSupplier = new DefaultDrawingSupplier(
		newColors,
		DefaultDrawingSupplier.DEFAULT_OUTLINE_PAINT_SEQUENCE, DefaultDrawingSupplier.DEFAULT_STROKE_SEQUENCE, DefaultDrawingSupplier.DEFAULT_OUTLINE_STROKE_SEQUENCE, DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE );
		
		chart.getPlot().setDrawingSupplier(newSupplier);
		
		final JFrame frame = new JFrame(windowTitle);
		ChartPanel chartPanel = new ChartPanel(chart);

		chartPanel.setMouseZoomable(true, false);
		chartPanel.setDoubleBuffered(true);
		frame.setPreferredSize(new java.awt.Dimension(600, 400));
		frame.setContentPane(chartPanel);
		frame.pack();
		frame.setVisible(true);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		RefineryUtilities.positionFrameOnScreen(frame, lastx, lasty);
		lastx += 0.05;
		lasty += 0.05;
	}

}
