package uk.ac.imperial.doc.masspa.gui.components.topologies.canvas;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import uk.ac.imperial.doc.masspa.gui.models.channels.ChannelComponent;
import uk.ac.imperial.doc.masspa.gui.models.channels.Channels;
import uk.ac.imperial.doc.masspa.gui.models.channels.MASSPAChannelComponent;
import uk.ac.imperial.doc.masspa.gui.models.topologies.LocationComponent;
import uk.ac.imperial.doc.masspa.gui.models.topologies.Topology;
import uk.ac.imperial.doc.masspa.gui.util.Constants;
import uk.ac.imperial.doc.masspa.gui.util.KeyStateManager;
import uk.ac.imperial.doc.masspa.gui.util.Shortcuts;

public class JTopologyViewerCanvas extends JPanel
{
	private static final long serialVersionUID = -6912178975131202394L;

	private final EventListenerList m_eventListeners;
	private double m_scale = Constants.s_LOCATION_VIEWER_SCALE_DEFAULT;
	private final Map<LocationComponent,JLocationComponentVisualiser> m_locationComponentVisualisers;
	private final Map<ChannelComponent,JChannelComponentVisualiser> m_channelComponentVisualisers;
	private JSelectionBox m_selectionBox;
	private final List<JTopologyComponentVisualiser<?>> m_selection;
	private final Map<JTopologyComponentVisualiser<?>,JTopologyComponentVisualiser<?>> m_selectionMap;
	private boolean m_showChannels = false;

	public JTopologyViewerCanvas()
	{
		setLayout(null); // absolute layout
		setFocusable(true);
		m_eventListeners = new EventListenerList();
		
		// Shapes
		m_locationComponentVisualisers = new HashMap<LocationComponent,JLocationComponentVisualiser> ();
		m_channelComponentVisualisers = new HashMap<ChannelComponent,JChannelComponentVisualiser>();
		
		// Selections
		m_selection = new LinkedList<JTopologyComponentVisualiser<?>>();
		m_selectionMap = new HashMap<JTopologyComponentVisualiser<?>,JTopologyComponentVisualiser<?>>();
		
		// Listeners
		addComponentListener(new ComponentChangeListener());
		addMouseListener(new MouseEventListener());
		addMouseMotionListener(new MouseMotionEventListener());
	}
	
	/**
	 * Add ListSelectionListener listener {@code _l} to listeners
	 * @param _l
	 */
	public void addListSelectionListener(ListSelectionListener _l)
	{
		m_eventListeners.add(ListSelectionListener.class, _l);
	}
	
	/**
	 * Fire event {@code _e} to all registered list selection listeners
	 * @param _e
	 */
	private void fireListSelectionEvent(ListSelectionEvent _e)
	{
		for (ListSelectionListener l : m_eventListeners.getListeners(ListSelectionListener.class))
	    {
			l.valueChanged(_e);
	    }
	}
	
	/**
	 * Notify listeners of change in selection
	 */
	private void fireListSelectionEvent()
	{
		List<JTopologyComponentVisualiser<?>> sel = getSelection();
		fireListSelectionEvent(new ListSelectionEvent(sel,0,sel.size(),false));
	}
	
	/**
	 * Set zoom to {@code _scale} and repaint
	 * @param _scale
	 */
	public void setScale(double _scale)
	{
		m_scale = _scale;
		repaint();
	}
	
	/**
	 * Set ability to see and edit channels
	 * @param _b iff true users can see and edit channels
	 */
	public void showChannels(boolean _showChannels)
	{
		m_showChannels = _showChannels;
	}
	
	/**
	 * @return true if channels are being displayed
	 */
	public boolean channelsVisible()
	{
		return m_showChannels;
	}
	
	/**
	 * Find visualiser for location component
	 * @param _lc
	 * @return visualiser for {@code _lc} or null if it does not exist
	 */
	public JLocationComponentVisualiser getLocationVisualiser(LocationComponent _lc)
	{
		return m_locationComponentVisualisers.get(_lc);
	}
	
	/**
	 * @return number of objects that have been selected
	 */
	private int getSelectionSize()
	{
		return m_selection.size();
	}

	/**
	 * @param _lcv
	 * @return true iff component {@_lcv} is already selected
	 */
	private boolean isSelected(JTopologyComponentVisualiser<?> _lcv)
	{
		return m_selectionMap.containsKey(_lcv);
	}
	
	/**
	 * Ensure that selection only ever contains one type of
	 * visual component at a time
	 * @param _c
	 * @return true if selection is empty or contains objects of class {@code _c}
	 */
	@SuppressWarnings("rawtypes")
	private boolean isSelectable(Class<? extends JTopologyComponentVisualiser> _c)
	{
		if (getSelectionSize() == 0) {return true;}
		return _c==m_selection.get(0).getClass();
	}
	
	/**
	 * Select component {@code _tcv}
	 * @param _tcv
	 */
	private void select(JTopologyComponentVisualiser<?> _tcv)
	{
		_tcv.setSelected(true);
		if (m_selectionMap.put(_tcv,_tcv)==null)
		{
			m_selection.add(_tcv);
		}
		fireListSelectionEvent();
		repaint();
	}
	
	/**
	 * Select all components in {@code _comps}
	 * @param _comps
	 */
	private void selectAll(Set<JTopologyComponentVisualiser<?>> _comps)
	{
		// Check for multiselection
		if (KeyStateManager.getKeyStateManager().getCurrentlyPressedKey() != Shortcuts.s_MULTISELECTION)
		{
			clearSelection();
		}
		for (JTopologyComponentVisualiser<?> tcv : _comps)
		{
			tcv.setSelected(true);
			m_selection.add(tcv);
			m_selectionMap.put(tcv, tcv);
		}
		fireListSelectionEvent();
		repaint();
	}
	
	/** 
	 * Deselect component {@code _tcv}
	 * @param _tcv
	 */
	private void deselect(JTopologyComponentVisualiser<?> _tcv)
	{
		_tcv.setSelected(false);
		if (m_selectionMap.remove(_tcv)!=null)
		{
			m_selection.remove(_tcv);
		}
		fireListSelectionEvent();
		repaint();
	}
	
	/** 
	 * @return current selection as read-only list
	 */
	private List<JTopologyComponentVisualiser<?>> getSelection()
	{
		return Collections.unmodifiableList(m_selection);
	}
	
	/** 
	 * Clear current selection
	 */
	public void clearSelection()
	{
		for (JTopologyComponentVisualiser<?> tcv : m_selection)
		{
			tcv.setSelected(false);
		}
		m_selection.clear();
		m_selectionMap.clear();
		fireListSelectionEvent();
		repaint();
	}
	
	/**
	 * If selected component {@code _tcv} is deselected. Otherwise it is selected.
	 * @param _tcv
	 */
	public void toggleSelection(JTopologyComponentVisualiser<?> _tcv)
	{
		if (!isSelectable(_tcv.getClass())) {return;}

		if (!_tcv.isSelected())
		{
			// Check for multiselection
			if (KeyStateManager.getKeyStateManager().getCurrentlyPressedKey() != Shortcuts.s_MULTISELECTION)
			{
				clearSelection();
			}
			select(_tcv);
		}
		else
		{
			deselect(_tcv);
		}
	}

	/**
	 * Retrieve selected locations and
	 * empty selection
	 * @return selected locations
	 */
	@SuppressWarnings("rawtypes")
	public Collection<LocationComponent> getSelectedLocations()
	{
		Set<LocationComponent> locs = new HashSet<LocationComponent>();
		if (!isSelectable(JLocationComponentVisualiser.class)) {return locs;}

		for (JTopologyComponentVisualiser visual : getSelection())
		{
			JLocationComponentVisualiser tcl = (JLocationComponentVisualiser) visual;
			locs.add(tcl.getLocationComponent());
		}
		clearSelection();

		return locs;
	}
	
	/**
	 * Programmatically select locations int {@code locs}
	 * @param _locs locations to be selected
	 */
	public void setSelectedLocations(Set<LocationComponent> _locs)
	{		
		// Select components
		Set<JTopologyComponentVisualiser<?>> locs = new HashSet<JTopologyComponentVisualiser<?>>();
		for (LocationComponent lc : _locs)
		{
			JLocationComponentVisualiser lcv = m_locationComponentVisualisers.get(lc);
			if (lcv != null)
			{
				locs.add(lcv);
			}
		}
		selectAll(locs);
	}

	/**
	 * Highlight all components in {@code _locs}
	 * @param _locs
	 */
	public void highlightLocations(Set<LocationComponent> _locs)
	{
        for (JLocationComponentVisualiser lcv : m_locationComponentVisualisers.values())
        {
        	if (isSelected(lcv)) {continue;}
        	else if (_locs.contains(lcv.getLocationComponent())){lcv.highlight();}
        	else {lcv.changeToDefaultColors();}
        }
    	repaint();
	}

	/**
	 * Retrieve selected channels and
	 * empty selection
	 * @return selected channels
	 */
	@SuppressWarnings("rawtypes")
	public Collection<ChannelComponent> getSelectedChannels()
	{
		Set<ChannelComponent> chans = new HashSet<ChannelComponent>();
		if(!isSelectable(JChannelComponentVisualiser.class)){return chans;}

		for (JTopologyComponentVisualiser visual : getSelection())
		{
			JChannelComponentVisualiser ccl = (JChannelComponentVisualiser) visual;
			chans.add(ccl.getChannelComponent());
		}
		clearSelection();

		return chans;
	}
	
	/**
	 * Programmatically select channels int {@code _chans}
	 * @param _chans channels to be selected
	 */
	public void setSelectedChannels(Set<MASSPAChannelComponent> _chans)
	{
		if (!channelsVisible()) {return;}
		
		// Select components
		Set<JTopologyComponentVisualiser<?>> chans = new HashSet<JTopologyComponentVisualiser<?>>();
		for (MASSPAChannelComponent chan : _chans)
		{
			LocationComponent senderLoc = new LocationComponent(chan.getSender().getLocation().getCoords());
			LocationComponent receiverLoc = new LocationComponent(chan.getReceiver().getLocation().getCoords());
			ChannelComponent c = new ChannelComponent(senderLoc,receiverLoc);
			JChannelComponentVisualiser ccv = m_channelComponentVisualisers.get(c);
			chans.add(ccv);
		}
		selectAll(chans);
	}
	
	/**
	 * Visualise the {@code topology}
	 * @param _topology
	 */
	public void createTopology(Topology _topology)
	{
		// Clear all
		m_channelComponentVisualisers.clear();
		m_locationComponentVisualisers.clear();
		clearSelection();
		
		if (_topology == null) {return;}

		// Create locations
		createLocations(_topology.getLocations());
		
		// Add channels so they are drawn on top
		createChannels(_topology.getChannels());
	}

	/**
	 * Visualise locations {@code _locs}
	 * @param _locs
	 */
	protected void createLocations(Collection<LocationComponent> _locs)
	{
		for (LocationComponent lc : _locs)
		{
			m_locationComponentVisualisers.put(lc, new JLocationComponentVisualiser(lc));
		}
	}
	
	/**
	 * Visualise channels {@code _channels}
	 * @param _channels
	 */
	protected void createChannels(Channels _channels)
	{
		if (!channelsVisible() || _channels == null) {return;}

		for (ChannelComponent c : _channels.getChannels())
		{
			JChannelComponentVisualiser ccv = new JChannelComponentVisualiser(c);
			m_channelComponentVisualisers.put(c, ccv);
		}
	}
	
	/**
	 * Paint all visible components
	 * @param _g
	 */
	@Override
    protected void paintComponent(Graphics _g)
    {
        super.paintComponent(_g);
        
        Graphics2D g2 = (Graphics2D)_g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        // Transform
        double cx = getWidth()/2;
        double cy = getHeight()/2;
        AffineTransform at = AffineTransform.getTranslateInstance(cx, cy);
        at.scale(m_scale, m_scale);
        
        // DrawLocations
        for (JLocationComponentVisualiser lcv : m_locationComponentVisualisers.values())
        {
        	lcv.idempotentTransform(at);
        	
        	g2.setPaint(lcv.getFillColor());
        	g2.fill(lcv);
        	g2.setPaint(lcv.getBorderColor());
        	g2.draw(lcv);
        }
        
        // Draw Channels
        for (JChannelComponentVisualiser ccv : m_channelComponentVisualisers.values())
        {
        	ccv.idempotentTransform(at);
        	
        	g2.setPaint(ccv.getFillColor());
        	g2.fill(ccv);
        	g2.setPaint(ccv.getBorderColor());
        	g2.draw(ccv);
        }
        
        // Draw SelectionBox
        if (m_selectionBox != null)
        {
            g2.setPaint(JSelectionBox.s_color);
        	g2.setStroke(JSelectionBox.s_stroke);
        	g2.draw(m_selectionBox);
            g2.setComposite(JSelectionBox.s_transparency);
            g2.fill(m_selectionBox);
        }
    }
   	    
	private class ComponentChangeListener extends ComponentAdapter
	{
		//****************************************************************
		// Overwrite dummy implementation of ComponentListener interface
		//****************************************************************
		@Override
		public void componentResized(ComponentEvent _e)
	    {
			repaint();
	    }
	}

	private class MouseEventListener extends MouseAdapter
	{
		//****************************************************************
		// Overwrite dummy implementation of MouseListener interface
		//****************************************************************
		@Override
		public void mouseClicked(MouseEvent _e)
		{		
			if (KeyStateManager.getKeyStateManager().getCurrentlyPressedKey() != Shortcuts.s_MULTISELECTION)
			{
				// Deselect visual components
				clearSelection();
			}
	
	        // Select Locations
	        for (JLocationComponentVisualiser lcv : m_locationComponentVisualisers.values())
	        {
	        	if (lcv.contains(_e.getPoint()))
	        	{
	        		toggleSelection(lcv);
	        		break;
	        	}
	        }
	
			// Select Channels
			double dist=100000000;
			JChannelComponentVisualiser selectedChan = null;
			for (JChannelComponentVisualiser ccv : m_channelComponentVisualisers.values())
			{
				// Ensure we only select the channel that is closest to the click
				Rectangle2D rect = ccv.getBounds2D();
				Point2D p = _e.getPoint();
				if (rect.contains(p))
				{
					double dist2 = Math.pow(rect.getCenterX()-p.getX(),2)+Math.pow(rect.getCenterY()-p.getY(),2);
					if (dist2 < dist)
					{
						dist = dist2;
						selectedChan = ccv;
					}
				}
			}
			if (selectedChan!=null){toggleSelection(selectedChan);}
		}
		
		@Override
		public void mousePressed(MouseEvent _e)
		{
			// Create Selection box
			m_selectionBox = new JSelectionBox(_e.getPoint());
		}
		
		@Override
		public void mouseReleased(MouseEvent _e)
		{
			// Select components that lie within the selection box
			Set<JTopologyComponentVisualiser<?>> comps = new HashSet<JTopologyComponentVisualiser<?>>();
			
			// We are interested in editing channels
			if (channelsVisible())
			{
		        // Select channels
		        for (JChannelComponentVisualiser ccv : m_channelComponentVisualisers.values())
		        {
		        	if (m_selectionBox.intersects(ccv.getBounds2D()))
		        	{
		        		comps.add(ccv);
		        	}
		        }
			}
			// We are interested in editing locations
			else
			{
		        // Select Locations
		        for (JLocationComponentVisualiser lcv : m_locationComponentVisualisers.values())
		        {
		        	if (m_selectionBox.intersects(lcv.getBounds2D()))
		        	{
		        		comps.add(lcv);
		        	}
		        }
			}
			selectAll(comps);
			
			// Remove Selection box
			m_selectionBox = null;
			repaint();
		}
	}
	
	private class MouseMotionEventListener extends MouseMotionAdapter
	{
		//*******************************************************************
		// Overwrite dummy implementation of MouseMotionListener interface
		//*******************************************************************
		@Override
		public void mouseMoved(MouseEvent _e)
		{
			// Ensure that we receive key events
			requestFocusInWindow();
			
	        // Change Locations
			Set<LocationComponent> loc = new HashSet<LocationComponent>();
			for (JLocationComponentVisualiser lcv : m_locationComponentVisualisers.values())
	        {
	        	if (lcv.contains(_e.getPoint()))
	        	{
	        		loc.add(lcv.getLocationComponent());
	        	}
	        }
			highlightLocations(loc);
		}
		
		@Override
		public void mouseDragged(MouseEvent _e)
		{
			m_selectionBox.updateEndPoint(_e.getPoint());
			repaint();
		}
	}
}
