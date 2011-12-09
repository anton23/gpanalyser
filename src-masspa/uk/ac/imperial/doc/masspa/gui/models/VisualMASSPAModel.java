package uk.ac.imperial.doc.masspa.gui.models;

import javax.swing.text.BadLocationException;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;

import uk.ac.imperial.doc.masspa.gui.util.MASSPAAgentsValidator;
import uk.ac.imperial.doc.masspa.language.Messages;
import uk.ac.imperial.doc.masspa.representation.components.MASSPAAgents;
import uk.ac.imperial.doc.masspa.util.MASSPALogging;

/**
 * This class is needed to serialize
 * and deserialise a model defined in
 * MASSPA-Modeller.
 * 
 * @author Chris Guenther
 */
@Root @Namespace(reference="http://uk.ac.imperial.doc.masspa/VisualModel/1")
public class VisualMASSPAModel
{	
	//**********************************************
	// Getters/Setters and SimpleXML-Serialization
	//**********************************************
	@Element(name="agents") private ObservableDocument m_agentDefDoc;
	@Element(name="topology") private ObservableTopology m_topology;
	@Element(name="model") private ObservableDocument m_generatedMASSPADoc;
	@Element(name="eval") private ObservableDocument m_evaluationDefDoc;

	public VisualMASSPAModel(@Element(name="agents") ObservableDocument _agentDefDoc,
							 @Element(name="topology") ObservableTopology _topology,
							 @Element(name="model") ObservableDocument _generatedMASSPADoc,
							 @Element(name="eval") ObservableDocument _evaluationDefDoc)
	{
		m_agentDefDoc = _agentDefDoc;
		m_topology = _topology;
		m_generatedMASSPADoc = _generatedMASSPADoc;
		m_evaluationDefDoc = _evaluationDefDoc;
	}

	//**********************************************
	// Auxillary
	//**********************************************
	/**
	 * Populate {@code _agentDefDoc, _agents, _topology} and {@code _evaluationDefDoc}
	 * @param _agentDefDoc
	 * @param _agents
	 * @param _topology
	 * @param _evaluationDefDoc
	 */
	public void copyIntoObservableModels(ObservableDocument _agentDefDoc,
										 ObservableAgents _agents,
										 ObservableTopology _topology,
										 ObservableDocument _generatedMASSPADoc,
										 ObservableDocument _evaluationDefDoc)
	{
		MASSPALogging.clearConsoles();
		
		// Validate and compile Agent definitions
		_agentDefDoc.setDocument(m_agentDefDoc);
		_agents.setMASSPAAgents(null);
		if (m_agentDefDoc.getLength() > 0)
		{
			try
			{
				MASSPAAgents agents = MASSPAAgentsValidator.validate(_agentDefDoc.getText(0, _agentDefDoc.getLength()));
				_agents.setMASSPAAgents(agents);
			}
			catch (BadLocationException e)
			{
				MASSPALogging.error(Messages.s_AGENT_EDITOR_IO_ERROR);
				return;
			}
			catch (AssertionError e)
			{
				MASSPALogging.fatalError(e.getMessage());
			}
		}
		
		// Prepare Topology
		_topology.setTopology(m_topology.getTopology());
		
		// Prepare Generated Model
		_generatedMASSPADoc.setDocument(m_generatedMASSPADoc);
		
		// Prepare Evaluation definitions
		_evaluationDefDoc.setDocument(m_evaluationDefDoc);
	}
}
