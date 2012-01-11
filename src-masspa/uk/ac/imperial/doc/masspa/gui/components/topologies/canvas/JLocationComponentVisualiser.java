package uk.ac.imperial.doc.masspa.gui.components.topologies.canvas;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;

import uk.ac.imperial.doc.masspa.gui.models.topologies.LocationComponent;
import uk.ac.imperial.doc.masspa.gui.util.Constants;

public class JLocationComponentVisualiser extends JTopologyComponentVisualiser<LocationComponent>
{
	private static final long serialVersionUID = 1411438650808955948L;

	private LocationComponent m_loc = null;

	public JLocationComponentVisualiser(LocationComponent _loc)
	{
		super();
		m_loc = _loc;

		// Init path
		idempotentTransform(null);

		// Other
		setSelected(false);
	}
	
	/**
	 * @return underlying location component
	 */
	public LocationComponent getLocationComponent()
	{
		return m_loc;
	}

	@Override
	public void idempotentTransform(AffineTransform _at)
	{
		reset();
		double radius = m_loc.getRadius();
		// Flip y axis
		append(new Ellipse2D.Double(m_loc.getCoords().get(0), -m_loc.getCoords().get(1), radius*2, radius*2),false);
		if (_at != null) {transform(_at);}
	}
	
	public void setColours(Color _borderColor, Color _fillColor, boolean _activeOverwrite)
	{	
		Color borderColor = (_activeOverwrite || m_loc.getActive()) ? _borderColor : Constants.s_LOCATION_VIEWER_LOC_COMP_BORDER_INACTIVE;
		Color fillColor = (_activeOverwrite || m_loc.getActive()) ? _fillColor : Constants.s_LOCATION_VIEWER_LOC_COMP_FILL_INACTIVE;
		setColours(borderColor, fillColor);
	}

	@Override
	public void setSelected(boolean _selected)
	{
		super.setSelected(_selected);
		if (isSelected()  && m_loc.getActive())
		{
			setColours(Constants.s_LOCATION_VIEWER_LOC_COMP_BORDER_SELECTED, Constants.s_LOCATION_VIEWER_LOC_COMP_FILL_SELECTED, false);
		}
		else if (isSelected()  && !m_loc.getActive())
		{
			highlight();
		}
		else
		{
			changeToDefaultColors();
		}
	}
	
	/**
	 * Highlight location
	 */
	public void highlight()
	{
		if (!m_loc.getActive())
		{
			setColours(Constants.s_LOCATION_VIEWER_LOC_COMP_BORDER_HIGHLIGHT, Constants.s_LOCATION_VIEWER_LOC_COMP_FILL_HIGHLIGHT, true);
		}
		if (m_loc.getActive())
		{
			setColours(Constants.s_LOCATION_VIEWER_LOC_COMP_BORDER_MOUSE_OVER, Constants.s_LOCATION_VIEWER_LOC_COMP_FILL_MOUSE_OVER, false);
		}
	}

	@Override
	public void changeToDefaultColors()
	{
		setColours(Constants.s_LOCATION_VIEWER_LOC_COMP_BORDER_DEFAULT, Constants.s_LOCATION_VIEWER_LOC_COMP_FILL_DEFAULT, false);
	}
	
	@Override
	public String toString()
	{
		return JLocationComponentVisualiser.class.getName() + " " + m_loc;
	}
}
