package uk.ac.imperial.doc.masspa.gui.editors;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;

import uk.ac.imperial.doc.masspa.gui.components.JLineNumberCol;
import uk.ac.imperial.doc.masspa.gui.editors.controllers.MASSPAEvalEditorController;
import uk.ac.imperial.doc.masspa.gui.interfaces.ITab;
import uk.ac.imperial.doc.masspa.gui.interfaces.ITabController;
import uk.ac.imperial.doc.masspa.gui.models.ObservableAgents;
import uk.ac.imperial.doc.masspa.gui.models.ObservableDocument;
import uk.ac.imperial.doc.masspa.gui.models.ObservableTopology;
import uk.ac.imperial.doc.masspa.gui.util.CompoundUndoManager;
import uk.ac.imperial.doc.masspa.gui.util.Constants;
import uk.ac.imperial.doc.masspa.language.Labels;

/**
 * This class defines the layout of the
 * MASSPA Evaluator which allows the user
 * to view the final generated MASSPA
 * process algebra definition. And add
 * analysis targets
 * 
 * @author Chris Guenther
 */
public class MASSPAEvalEditor extends JPanel implements ITab
{
	private static final long serialVersionUID = -384715157193648430L;

	// Layout components
	private final JPanel			m_masspaDefPanel;
	private final JPanel			m_evalDefPanel;
	protected final JSplitPane 		m_vertSplit;
	
	// Editor Components
	private final JLabel 			m_masspaDefLabel;
	private final JLineNumberCol	m_masspaDefLineNos;
	private final JTextPane 		m_masspaDefText;
	private final JScrollPane		m_masspaDefScroll;
	private final JButton			m_masspaDefGen;
		
	private final JLabel 			m_evalDefLabel;
	private final JLineNumberCol	m_evalDefLineNos;
	private final JTextPane 		m_evalDefText;
	private final JScrollPane		m_evalDefScroll;
	private final JButton			m_eval;
	
	// The models
	private final ObservableDocument m_constAndAgentDoc;
	private final ObservableDocument m_masspaDefDoc;
	private final ObservableDocument m_evalDoc;

	// The controller
	private final MASSPAEvalEditorController m_ctrl;
	
	/**
	 * Create the tab
	 */
	public MASSPAEvalEditor(ObservableDocument _constAndAgentDoc, ObservableAgents _agents, ObservableTopology _topology, ObservableDocument _generatedMASSPADoc, ObservableDocument _evaluationDefDoc)
	{
		// Overall layout
		super(new BorderLayout());
		
		// Setup models
		m_constAndAgentDoc = _constAndAgentDoc;
		m_masspaDefDoc = _generatedMASSPADoc;
		m_evalDoc = _evaluationDefDoc;
		
		// Initialise editor components
		m_masspaDefLabel = new JLabel(Labels.s_MASSPA_EVAL_MASSPA_DEF_LABEL);
		m_masspaDefText = new JTextPane();
		m_masspaDefText.setDocument(m_masspaDefDoc);
		m_masspaDefLineNos = new JLineNumberCol(m_masspaDefDoc);
		m_masspaDefText.getDocument().addDocumentListener(m_masspaDefLineNos.getDocListener());
		m_masspaDefScroll = new JScrollPane(m_masspaDefText, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		m_masspaDefScroll.setRowHeaderView(m_masspaDefLineNos);
		m_masspaDefGen = new JButton(Labels.s_MASSPA_EVAL_GENERATE);
		
		m_evalDefLabel = new JLabel(Labels.s_MASSPA_EVAL_EVAL_DEF_LABEL);
		m_evalDefText = new JTextPane();
		m_evalDefText.setDocument(m_evalDoc);
		m_evalDefLineNos = new JLineNumberCol(m_evalDoc);
		m_evalDefText.getDocument().addDocumentListener(m_evalDefLineNos.getDocListener());
		m_evalDefScroll = new JScrollPane(m_evalDefText, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		m_evalDefScroll.setRowHeaderView(m_evalDefLineNos);
		m_eval = new JButton(Labels.s_MASSPA_EVAL_EVAL);		

		// Initialise layout components
		m_masspaDefPanel = new JPanel(new BorderLayout());
		m_evalDefPanel = new JPanel(new BorderLayout());

		// Create composition 
		m_masspaDefPanel.add(m_masspaDefLabel, BorderLayout.NORTH);
		m_masspaDefPanel.add(m_masspaDefScroll);
		m_masspaDefPanel.add(m_masspaDefGen, BorderLayout.SOUTH);
		
		m_evalDefPanel.add(m_evalDefLabel, BorderLayout.NORTH);
		m_evalDefPanel.add(m_evalDefScroll);
		m_evalDefPanel.add(m_eval, BorderLayout.SOUTH);
		
		m_vertSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, m_masspaDefPanel, m_evalDefPanel);
		m_vertSplit.setResizeWeight(Constants.s_MASSPA_EVAL_EDITOR_DEF_SIZE_VERT);
		add(m_vertSplit);
		
		// Create and setup model/controller
		m_ctrl = new MASSPAEvalEditorController(m_constAndAgentDoc,_agents, _topology, m_masspaDefDoc, m_evalDoc, m_masspaDefGen, 
												m_eval, new CompoundUndoManager(m_masspaDefText), new CompoundUndoManager(m_evalDefText));
	}
	
	//**************************************************
	// Implement ITab interface
	//**************************************************
	@Override
	public ITabController getTabController()
	{
		return m_ctrl;
	}
}
