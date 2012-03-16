package uk.ac.imperial.doc.masspa.gui.models;

import javax.swing.event.ChangeEvent;

import uk.ac.imperial.doc.masspa.representation.components.MASSPAAgents;

/**
 * This is a container class for a MASSPAAgents
 * which fires events whenever the MASSPAAgents
 * are replaced.
 * @author Chris Guenther
 */
public class ObservableAgents extends ObservableContainer
{
	private MASSPAAgents m_agents = null;
	
	public ObservableAgents()
	{
		this(null);
	}
	
	public ObservableAgents(MASSPAAgents _agents)
	{
		m_agents = _agents;
	}

	public MASSPAAgents getMASSPAAgents()
	{
		return m_agents;
	}
	
	public void setMASSPAAgents(MASSPAAgents _agents)
	{
		m_agents = _agents;
		fireChangeEvent(new ChangeEvent(this));
	}
}
