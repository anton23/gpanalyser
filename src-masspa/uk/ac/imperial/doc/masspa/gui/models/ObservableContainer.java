package uk.ac.imperial.doc.masspa.gui.models;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

public class ObservableContainer
{
	private final EventListenerList m_listeners;

	public ObservableContainer()
	{
		m_listeners = new EventListenerList();
	}
	
	protected void fireChangeEvent(ChangeEvent _e) 
	{
	     for (ChangeListener l : m_listeners.getListeners(ChangeListener.class))
	     {
	    	l.stateChanged(_e);
	     }
	}
	
	public void addChangeListener(ChangeListener _l)
	{
		m_listeners.add(ChangeListener.class, _l);
	}
	
	public void removeChangeListener(ChangeListener _l)
	{
		m_listeners.remove(ChangeListener.class, _l);
	}
}
