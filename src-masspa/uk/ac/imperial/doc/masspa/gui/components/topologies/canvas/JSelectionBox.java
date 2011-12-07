package uk.ac.imperial.doc.masspa.gui.components.topologies.canvas;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Rectangle2D;


public class JSelectionBox extends Rectangle2D.Double
{
	private static final long serialVersionUID = 1123916408799750210L;
	
	// Layout
	public final static Color s_color = Color.BLACK;
	public final static float s_dashSize1[] = {10.0f};
	public final static BasicStroke s_stroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, s_dashSize1, 0.0f);
	public final static AlphaComposite s_transparency = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.05f);
	private final Point m_root;
	
	/**
	 * Create a new rectangular selection box with
	 * root at point {@code _p}
	 * @param _p
	 */
	public JSelectionBox(Point _p)
	{
		super(_p.x,_p.y,0,0);
		m_root = _p;
	}

	/**
	 * Change shape of selection box according to
	 * new endpoint {@code _p}
	 * @param _p
	 */
	public void updateEndPoint(Point _p)
	{
		double width = Math.abs(m_root.x-_p.x);
		double height = Math.abs(m_root.y-_p.y);
		double x = Math.min(m_root.x,_p.x);
		double y = Math.min(m_root.y,_p.y);
		setRect(x, y, width, height);
	}

}
