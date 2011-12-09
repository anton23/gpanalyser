package uk.ac.imperial.doc.masspa.gui.components.topologies;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyListener;
import java.util.Collection;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionListener;

import uk.ac.imperial.doc.masspa.gui.components.topologies.canvas.JTopologyViewerCanvas;
import uk.ac.imperial.doc.masspa.gui.models.ObservableTopology;
import uk.ac.imperial.doc.masspa.gui.models.channels.ChannelComponent;
import uk.ac.imperial.doc.masspa.gui.models.channels.MASSPAChannelComponent;
import uk.ac.imperial.doc.masspa.gui.models.topologies.LocationComponent;
import uk.ac.imperial.doc.masspa.gui.util.Constants;
import uk.ac.imperial.doc.masspa.language.Labels;

public class JTopologyViewer extends JPanel
{
	private static final long serialVersionUID = 8251099681504152343L;
	
	protected final JTopologyViewerCanvas	m_locationsViewerCanvas;
	protected final JScrollPane				m_locViewerCanvasScroll;
	protected final JSplitPane				m_horiSplit;
	protected final JSlider					m_zoomSlider;

	public JTopologyViewer(ObservableTopology _topology) 
	{
		// Overall layout
		super(new BorderLayout());
		
		// Control components
		m_locationsViewerCanvas = new JTopologyViewerCanvas();
		m_locationsViewerCanvas.setPreferredSize(new Dimension(Constants.s_LOCATION_VIEWER_CANVAS_WIDTH,Constants.s_LOCATION_VIEWER_CANVAS_HEIGHT));
		m_locViewerCanvasScroll = new JScrollPane(m_locationsViewerCanvas, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		m_zoomSlider = new JSlider();
		m_zoomSlider.setValue(Constants.s_LOCATION_VIEWER_ZOOM_DEFAULT);
		m_zoomSlider.setMaximum(Constants.s_LOCATION_VIEWER_ZOOM_MAX);
		m_zoomSlider.setOrientation(SwingConstants.VERTICAL);
		m_zoomSlider.setToolTipText(Labels.s_LOCATION_EDITOR_ZOOM);

		// Composition
		m_horiSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, m_locViewerCanvasScroll,m_zoomSlider);
		m_horiSplit.setResizeWeight(Constants.s_LOCATION_VIEWER_SIZE_VIEWER);
		m_horiSplit.setEnabled(false);
		add(m_horiSplit);
		
		// Register as listener
		addComponentListener(new ComponentChangeListener());
		m_zoomSlider.addChangeListener(new ZoomChangeListener());
		_topology.addChangeListener(new TopologyChangeListener());
		
		// Position canvas
		scrollCanvasToCenter();
	}
	
	/**
	 * Add {@code _l} as an selection listener
	 * @param _l
	 */
	public void addListSelectionListener(ListSelectionListener _l)
	{
		m_locationsViewerCanvas.addListSelectionListener(_l);
	}

	/**
	 * Add {@code _l} as a key listener
	 * @param _l
	 */
	public void addKeyListener(KeyListener _l)
	{
		m_locationsViewerCanvas.addKeyListener(_l);
	}
	
	/**
	 * If {@code _b} is true any channels will be displayed
	 * @param _b
	 */
	public void showChannels(boolean _b)
	{
		m_locationsViewerCanvas.showChannels(_b);
	}
	
	/**
	 * Programmatically highlight locations {@code _locs}
	 * @param _locs
	 */
	public void highlightLocations(Set<LocationComponent> _locs)
	{
		m_locationsViewerCanvas.highlightLocations(_locs);
	}
	
	/**
	 * Programmatically select locations {@code _locs}
	 * @param _locs
	 */
	public void setSelectedLocations(Set<LocationComponent> _locs)
	{
		m_locationsViewerCanvas.setSelectedLocations(_locs);
	}
	
	/**
	 * @return selected locations
	 */
	public Collection<LocationComponent> getSelectedLocations()
	{
		return m_locationsViewerCanvas.getSelectedLocations();
	}
	
	/**
	 * Programmatically select channels {@code _chans}
	 * @param _chans
	 */
	public void setSelectedChannels(Set<MASSPAChannelComponent> _chans)
	{
		m_locationsViewerCanvas.setSelectedChannels(_chans);
	}
	
	/**
	 * @return selected channels
	 */
	public Collection<ChannelComponent> getSelectedChannels()
	{
		return m_locationsViewerCanvas.getSelectedChannels();
	}
	
	/**
	 * Scroll to the center of the canvas
	 */
	private void scrollCanvasToCenter()
	{
		SwingUtilities.invokeLater( new Runnable() {
			public void run()
			{
				JScrollBar horizontal = m_locViewerCanvasScroll.getHorizontalScrollBar();
				horizontal.setValue(horizontal.getMaximum()/2-horizontal.getWidth()/2);
				JScrollBar vertical = m_locViewerCanvasScroll.getVerticalScrollBar();
				vertical.setValue(vertical.getMaximum()/2-vertical.getHeight()/2);
			}
		});
	}

	private class ComponentChangeListener extends ComponentAdapter
	{
		//****************************************************************
		// Overwrite dummy implementation of ComponentListener interface
		//****************************************************************
		@Override
		public void componentResized(ComponentEvent _e)
	    {
			scrollCanvasToCenter();
	    }
	}
	
	private class ZoomChangeListener implements ChangeListener
	{	
		//**************************************************
		// Implement ChangeListener interface
		//**************************************************
		@Override
		public void stateChanged(ChangeEvent _e)
		{
			m_locationsViewerCanvas.setScale(((JSlider) _e.getSource()).getValue());
		}
	}
	
	private class TopologyChangeListener implements ChangeListener
	{
		//**************************************************
		// Implement ChangeListener interface
		//**************************************************
		@Override
		public void stateChanged(ChangeEvent _e)
		{
			m_locationsViewerCanvas.createTopology(((ObservableTopology)_e.getSource()).getTopology());
		}
	}
}
