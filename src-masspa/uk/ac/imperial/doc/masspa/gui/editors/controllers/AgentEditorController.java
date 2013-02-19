package uk.ac.imperial.doc.masspa.gui.editors.controllers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import uk.ac.imperial.doc.masspa.gui.interfaces.ITabController;
import uk.ac.imperial.doc.masspa.gui.menus.ICopyPasteDeleteHandler;
import uk.ac.imperial.doc.masspa.gui.menus.IFindReplaceHandler;
import uk.ac.imperial.doc.masspa.gui.menus.IUndoRedoHandler;
import uk.ac.imperial.doc.masspa.gui.models.ObservableAgents;
import uk.ac.imperial.doc.masspa.gui.util.CompoundUndoManager;
import uk.ac.imperial.doc.masspa.gui.util.MASSPAAgentsValidator;
import uk.ac.imperial.doc.masspa.language.Messages;
import uk.ac.imperial.doc.masspa.representation.components.MASSPAAgents;
import uk.ac.imperial.doc.masspa.util.MASSPALogging;

/**
 * This class listens to the agent definitions
 * in the editor and forwards them to the compiler
 * to be checked. The results are then output in the
 * console and the agent tree is updated.
 * 
 * @author Chris Guenther
 */
public class AgentEditorController implements ITabController, IUndoRedoHandler, IFindReplaceHandler, ICopyPasteDeleteHandler
{
	protected CompileActionListener 				 	m_actionListener;
	protected Document								 	m_agentDefText;
	protected ObservableAgents							m_agents;
	protected CompoundUndoManager 						m_undoRedoMgr;
	
	public AgentEditorController(JButton _compileBtn, Document _agentDefText, CompoundUndoManager _undoRedoMgr, ObservableAgents _agents)
	{
		m_agentDefText = _agentDefText;
		setUndoRedoMgr(_undoRedoMgr);
		m_agents = _agents;

		// Register as Listener
		_compileBtn.addActionListener(new CompileActionListener());
		
		// Compile
		compile();	
	}

	/**
	 * Parse MASSPA agent definitions. And update output window
	 * and agentTree.
	 */
	private void compile()
	{
		// No work to do
		if (m_agentDefText.getLength() == 0) {return;}
		m_agents.setMASSPAAgents(null);
		MASSPALogging.clearConsoles();
		
		// Get agent definitions
		String agentDef="";
		try
		{
			agentDef = m_agentDefText.getText(0, m_agentDefText.getLength());
		}
		catch (BadLocationException e)
		{
			MASSPALogging.error(Messages.s_AGENT_EDITOR_IO_ERROR);
			return;
		}
		MASSPAAgents agents = MASSPAAgentsValidator.validate(agentDef);
		
		// Update agent tree
		if (agents != null)
		{
			m_agents.setMASSPAAgents(agents);
		}
	}

	//**************************************************
	// Implement IUndoRedoHandler interface
	//**************************************************
	@Override
	public void undo() throws CannotUndoException
	{
		m_undoRedoMgr.undo();
	}

	@Override
	public boolean canUndo()
	{
		return m_undoRedoMgr.canUndo();
	}

	@Override
	public void redo() throws CannotRedoException
	{
		m_undoRedoMgr.redo();
	}

	@Override
	public boolean canRedo()
	{
		return m_undoRedoMgr.canRedo();
	}

	@Override
	public void setUndoRedoMgr(CompoundUndoManager _mgr)
	{
		m_undoRedoMgr = _mgr;
	}
	
	class CompileActionListener implements ActionListener
	{
		//**************************************************
		// Implement ActionListener interface
		//**************************************************
		public void actionPerformed(ActionEvent _e)
		{
			try
			{
				compile();
			} 
			catch (AssertionError e)
			{
				MASSPALogging.fatalError(e.getMessage());
			}
			MASSPALogging.printConsoleStats();
		}	
	}
}
