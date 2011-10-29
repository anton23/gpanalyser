package uk.ac.imperial.doc.pctmc.charts;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jzy3d.chart.Chart;
import org.jzy3d.colors.Color;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.colors.colormaps.ColorMapRainbow;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.Point;
import org.jzy3d.plot3d.primitives.Polygon;
import org.jzy3d.plot3d.primitives.Shape;
import org.jzy3d.plot3d.rendering.canvas.CanvasSwing;

public class ChartUtils3D {

	public static Rectangle DEFAULT_WINDOW_DIMENSIONS = new Rectangle(0, 0,
			600, 500);

	public static void drawChart(String windowTitle, String command,
			double[][] data, double minx, double dx, double miny, double dy,
			String xlabel, String ylabel, String zlabel) {
		if (!PCTMCChartUtilities.jogl)
			return;
		List<Polygon> polygons = new ArrayList<Polygon>();
		for (int ix = 0; ix < data.length - 1; ix++) {
			double x = minx + ix * dx;
			for (int iy = 0; iy < data[0].length - 1; iy++) {
				double y = miny + iy * dy;
				Polygon polygon = new Polygon();
				double z1 = data[ix][iy];
				polygon.add(new Point(new Coord3d(x, y, z1)));
				double z2 = data[ix][iy + 1];
				polygon.add(new Point(new Coord3d(x, y + dy, z2)));
				double z3 = data[ix + 1][iy + 1];
				polygon.add(new Point(new Coord3d(x + dx, y + dy, z3)));
				double z4 = data[ix + 1][iy];
				polygon.add(new Point(new Coord3d(x + dx, y, z4)));
				if (Double.isInfinite(z1) || Double.isInfinite(z2)
						|| Double.isInfinite(z3) || Double.isInfinite(z4))
					continue;
				polygons.add(polygon);
			}
		}

		Shape surface = new Shape(polygons);
		float zmin = surface.getBounds().getZmin();
		float zmax = surface.getBounds().getZmax();
		surface.setColorMapper(new ColorMapper(new ColorMapRainbow(), zmin,
				zmax, new org.jzy3d.colors.Color(1, 1, 1, 1f)));
		surface.setWireframeDisplayed(true);
		surface.setWireframeColor(org.jzy3d.colors.Color.BLACK);

		Chart chart = new Chart("swing");
		chart.getAxeLayout().setXAxeLabel(xlabel);
		chart.getAxeLayout().setYAxeLabel(ylabel);
		chart.getAxeLayout().setZAxeLabel(zlabel);

		chart.getScene().getGraph().add(surface);

		double shadowZ = zmin - (zmax - zmin);
		for (int ix = 0; ix < data.length - 1; ix++) {
			double x = minx + ix * dx;
			for (int iy = 0; iy < data[0].length - 1; iy++) {
				double y = miny + iy * dy;
				Polygon polygon = new Polygon();
				double z1 = data[ix][iy];
				polygon.add(new Point(new Coord3d(x, y, shadowZ)));
				double z2 = data[ix][iy + 1];
				polygon.add(new Point(new Coord3d(x, y + dy, shadowZ)));
				double z3 = data[ix + 1][iy + 1];
				polygon.add(new Point(new Coord3d(x + dx, y + dy, shadowZ)));
				double z4 = data[ix + 1][iy];
				polygon.add(new Point(new Coord3d(x + dx, y, shadowZ)));
				if (Double.isInfinite(z1) || Double.isInfinite(z2)
						|| Double.isInfinite(z3) || Double.isInfinite(z4))
					continue;
				polygons.add(polygon);
			}
		}
		Shape shadow = new Shape(polygons);
		shadow.setColor(Color.GRAY);
		chart.getScene().getGraph().add(shadow);

		chart.getView().getCamera().setStretchToFill(true);
		chart.getView().setMaximized(true);

		CanvasSwing canvas = (CanvasSwing) chart.getCanvas();

		JPanel panel3d = new JPanel();
		panel3d.setLayout(new BorderLayout());
		panel3d.add(new JLabel(command), BorderLayout.SOUTH);
		panel3d.add(canvas);

		ViewMouseControllerSwing mouse = new ViewMouseControllerSwing();
		mouse.addTarget(chart.getView());
		mouse.addMouseSource(canvas);

		PCTMCChartUtilities.addChart(panel3d, windowTitle);
	}
}
