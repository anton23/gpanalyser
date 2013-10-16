package uk.ac.imperial.doc.masspa.gui.components.topologies.canvas;

import java.awt.geom.AffineTransform;

import uk.ac.imperial.doc.masspa.gui.models.channels.ChannelComponent;
import uk.ac.imperial.doc.masspa.gui.models.topologies.LocationComponent;
import uk.ac.imperial.doc.masspa.gui.util.Constants;

/**
 * Visualises channels between two
 * different locations a and b
 * 
 * @author Chris Guenther
 */
public class JChannelComponentVisualiser extends JTopologyComponentVisualiser<ChannelComponent>
{
	private static final long serialVersionUID = -8829378815197989792L;

	private final ChannelComponent  m_channel;
	private final LocationComponent m_locA;
	private final LocationComponent m_locB;
	private boolean m_fromAToB = false;
	private boolean m_fromBToA = false;
	
	public JChannelComponentVisualiser(ChannelComponent _channel)
	{
		super();
		m_channel = _channel;
		
		// Order locations in channel (each pair of locations is ordered totally)
		LocationComponent sender = _channel.getSenderLoc();
		LocationComponent receiver = _channel.getReceiverLoc();
		m_locA = (sender.compareTo(receiver) >= 0) ? sender : receiver;
		m_locB = (sender.compareTo(receiver) >= 0) ? receiver : sender;
		setChannelDirection();
		
		// Init path
		idempotentTransform(null);

		// Other
		setSelected(false);
	}

	public ChannelComponent getChannelComponent()
	{
		return m_channel;	
	}
	
	/**
	 * Ensure that the arrow will be displayed correctly
	 */
	private void setChannelDirection()
	{
		if (m_locA.equals(m_channel.getSenderLoc()))
		{
			m_fromAToB = true;
		}
		else if (m_locB.equals(m_channel.getSenderLoc()))
		{
			m_fromBToA = true;
		}
	}
	
	@Override
	public void idempotentTransform(AffineTransform _at)
	{
		reset();

		// Only draw arrow if both locations are active
		if (!m_channel.isActive()) {return;}
		
		// Flip y axis
		double x1 = (double)m_locA.getCoords().get(0) + m_locA.getRadius();
		double y1 = -(double)m_locA.getCoords().get(1) + m_locA.getRadius();
		double x2 = (double)m_locB.getCoords().get(0) + m_locB.getRadius();
		double y2 = -(double)m_locB.getCoords().get(1) + m_locB.getRadius();
		        
		// Draw line
		double dist = m_locA.getDistanceTo(m_locB);
        double lenHalf = dist/2-m_locB.getRadius();
        double bend = m_locB.getRadius()*dist/1.5;
        moveTo(-lenHalf,0);

        if (m_fromAToB)
        {
        	curveTo(-lenHalf/2, bend, lenHalf/2, bend, lenHalf, 0);
        	curveTo(lenHalf/2, bend, -lenHalf/2, bend, -lenHalf, 0);
        	closePath();
        }
        if(m_fromBToA)
        {
        	curveTo(-lenHalf/2, -bend, lenHalf/2, -bend, lenHalf, 0);
        	curveTo(lenHalf/2, -bend, -lenHalf/2, -bend, -lenHalf, 0);
        	closePath();
        }
                
		// Draw arrow
        double arrowOrientation = Math.atan(bend/(lenHalf/1.5));
        double arrowLen = m_locA.getRadius()/2;
        double arrowAngle = Math.toRadians(22.5);
        if (m_fromAToB)
        {
        	double ax1 = lenHalf - arrowLen*Math.cos(arrowAngle+arrowOrientation);
        	double ay1 = arrowLen*Math.sin(arrowAngle+arrowOrientation);
        	double ax2 = lenHalf - arrowLen*Math.cos(-arrowAngle+arrowOrientation);
        	double ay2 = arrowLen*Math.sin(-arrowAngle+arrowOrientation);
            moveTo(ax1,ay1);
            lineTo(lenHalf, 0);
            lineTo(ax2,ay2);
            closePath();
        }
        else if (m_fromBToA)
        {
        	double ax1 = -lenHalf + arrowLen*Math.cos(arrowAngle-arrowOrientation);
        	double ay1 = arrowLen*Math.sin(arrowAngle-arrowOrientation);
        	double ax2 = -lenHalf + arrowLen*Math.cos(-arrowAngle-arrowOrientation);
        	double ay2 = arrowLen*Math.sin(-arrowAngle-arrowOrientation);
            moveTo(ax1,ay1);
            lineTo(-lenHalf, 0);
            lineTo(ax2,ay2);
            closePath();
        }
                
        // Translate and rotate
        double lineAngle = Math.asin((y1-y2)/((lenHalf+m_locB.getRadius())*2)) + Math.toRadians(180);
        AffineTransform at = AffineTransform.getTranslateInstance(x1-(x1-x2)/2, y1-(y1-y2)/2);
        at.rotate(lineAngle);
        transform(at);

        // Transform if necessary
		if (_at != null) {transform(_at);}
	}

	@Override
	public void setSelected(boolean _selected)
	{
		super.setSelected(_selected);
		if (isSelected())
		{
			setColours(Constants.s_LOCATION_VIEWER_CHANNEL_COMP_BORDER_SELECTED, Constants.s_LOCATION_VIEWER_CHANNEL_COMP_FILL_SELECTED);
		}
		else
		{
			changeToDefaultColors();
		}
	}
	
	@Override
	public void changeToDefaultColors()
	{
		setColours(Constants.s_LOCATION_VIEWER_CHANNEL_COMP_BORDER_DEFAULT, Constants.s_LOCATION_VIEWER_CHANNEL_COMP_FILL_DEFAULT);
	}
	
	//**************************************************
	// Object overrides
	//**************************************************
	@Override
	public String toString()
	{
		String direction = (m_fromBToA) ? " <-" : " ";
		direction += (m_fromAToB) ? "-> " : " ";
		return JChannelComponentVisualiser.class.getName() + m_locA.toString() + direction + m_locB.toString();
	}
	
	@Override
	public int hashCode()
	{
		return m_locA.hashCode() + m_locB.hashCode();
	}
	
	@Override
	public boolean equals(Object _o)
	{
		if (this == _o) {return true;}
		if (!(_o instanceof JChannelComponentVisualiser)) {return false;}
		return m_channel.equals(((JChannelComponentVisualiser)_o).getChannelComponent());
	}
}