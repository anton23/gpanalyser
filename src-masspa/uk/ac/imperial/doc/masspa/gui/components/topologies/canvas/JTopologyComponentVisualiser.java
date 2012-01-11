package uk.ac.imperial.doc.masspa.gui.components.topologies.canvas;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;

public class JTopologyComponentVisualiser<T> extends Path2D.Double
{
	private static final long serialVersionUID = -6174484259803105410L;
	
	private boolean m_selected = false;
	private Color m_fillColor;
	private Color m_borderColor;
	
	public JTopologyComponentVisualiser()
	{
	}
	
	/**
	 * Change border color to {@code _borderColor} and fill color to {@code _fillColor}
	 * @param _borderColor
	 * @param _fillColor
	 */
	public void setColours(Color _borderColor, Color _fillColor)
	{
		m_borderColor  = _borderColor;
		m_fillColor = _fillColor;
	}

	
	/**
	 * @return border color of the shape
	 */
	public Color getBorderColor()
	{
		return m_borderColor;
	}
	
	/**
	 * @return fill color of the shape
	 */
	public Color getFillColor()
	{
		return m_fillColor;
	}

	/**
	 * @return true if component is selected
	 */
	public boolean isSelected() {return m_selected;}
	
	/**
	 * Select selection status to {@code _selected}
	 * @param _selected
	 */
	public void setSelected(boolean _selected) {m_selected=_selected;}

	/**
	 * First undo the last transform then apply new transform
	 */
	public void idempotentTransform(AffineTransform _at)
	{
	}

	/**
	 * Set default colors location
	 */
	public void changeToDefaultColors()
	{
	}
}

