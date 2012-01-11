package uk.ac.imperial.doc.masspa.gui.menus;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;

import uk.ac.imperial.doc.masspa.gui.MASSPAEditorMain;
import uk.ac.imperial.doc.masspa.gui.interfaces.ITabController;
import uk.ac.imperial.doc.masspa.gui.util.Shortcuts;
import uk.ac.imperial.doc.masspa.language.Labels;

/**
 * The edit menu
 */
public class JEditMenu extends JMainMenu
{
	private static final long serialVersionUID = 9198681361850812125L;

	public final JMenuItem	m_mntmCopy;
	public final JMenuItem	m_mntmCut;
	public final JMenuItem	m_mntmPaste;
	public final JMenuItem	m_mntmDelete;
	public final JMenuItem	m_mntmUndo;
	public final JMenuItem	m_mntmRedo;
	public final JMenuItem	m_mntmFind;
	public final JMenuItem	m_mntmReplace;
	
	public JEditMenu(MASSPAEditorMain _mainCtrl, JTabbedPane _editorTabs, ITabController _initEditorTab)
	{
		super(Labels.s_MENU_EDIT, _editorTabs, _initEditorTab);
		
		// Menu items
		m_mntmCopy = new JMenuItem(Labels.s_MENU_EDIT_COPY);
		m_mntmCut = new JMenuItem(Labels.s_MENU_EDIT_CUT);
		m_mntmPaste = new JMenuItem(Labels.s_MENU_EDIT_PASTE);
		m_mntmDelete = new JMenuItem(Labels.s_MENU_EDIT_DELETE);
		m_mntmUndo = new JMenuItem(Labels.s_MENU_EDIT_UNDO);
		m_mntmRedo = new JMenuItem(Labels.s_MENU_EDIT_REDO);
		m_mntmFind = new JMenuItem(Labels.s_MENU_EDIT_FIND);
		m_mntmReplace = new JMenuItem(Labels.s_MENU_EDIT_REPLACE);

		// Mnemonics and Shortcuts
		this.setMnemonic(Shortcuts.s_MENU_EDIT_MNEMONIC);
		m_mntmCopy.setMnemonic(Shortcuts.s_MENU_EDIT_COPY_MNEMONIC);
		m_mntmCopy.setAccelerator(Shortcuts.s_MENU_EDIT_COPY_ACCELERATOR);
		m_mntmCut.setMnemonic(Shortcuts.s_MENU_EDIT_CUT_MNEMONIC);
		m_mntmCut.setAccelerator(Shortcuts.s_MENU_EDIT_CUT_ACCELERATOR);
		m_mntmPaste.setMnemonic(Shortcuts.s_MENU_EDIT_PASTE_MNEMONIC);
		m_mntmPaste.setAccelerator(Shortcuts.s_MENU_EDIT_PASTE_ACCELERATOR);
		m_mntmDelete.setMnemonic(Shortcuts.s_MENU_EDIT_DELETE_MNEMONIC);
		m_mntmDelete.setAccelerator(Shortcuts.s_MENU_EDIT_DELETE_ACCELERATOR);
		m_mntmUndo.setMnemonic(Shortcuts.s_MENU_EDIT_UNDO_MNEMONIC);
		m_mntmUndo.setAccelerator(Shortcuts.s_MENU_EDIT_UNDO_ACCELERATOR);
		m_mntmRedo.setMnemonic(Shortcuts.s_MENU_EDIT_REDO_MNEMONIC);
		m_mntmRedo.setAccelerator(Shortcuts.s_MENU_EDIT_REDO_ACCELERATOR);
		m_mntmFind.setMnemonic(Shortcuts.s_MENU_EDIT_FIND_MNEMONIC);
		m_mntmFind.setAccelerator(Shortcuts.s_MENU_EDIT_FIND_ACCELERATOR);
		m_mntmReplace.setMnemonic(Shortcuts.s_MENU_EDIT_REPLACE_MNEMONIC);
		m_mntmReplace.setAccelerator(Shortcuts.s_MENU_EDIT_REPLACE_ACCELERATOR);
		
		// Layout
		add(m_mntmCopy);
		add(m_mntmCut);
		add(m_mntmPaste);
		add(m_mntmDelete);
		add(new JSeparator());
		add(m_mntmUndo);
		add(m_mntmRedo);
		add(new JSeparator());
		add(m_mntmFind);
		add(m_mntmReplace);
		
		// Register as Listener
		m_mntmCopy.addActionListener(new CopyActionListener());
		m_mntmCut.addActionListener(new CutActionListener());
		m_mntmPaste.addActionListener(new PasteActionListener());
		m_mntmDelete.addActionListener(new DeleteActionListener());
		m_mntmUndo.addActionListener(new UndoActionListener());
		m_mntmRedo.addActionListener(new RedoActionListener());
		m_mntmFind.addActionListener(new FindActionListener());
		m_mntmReplace.addActionListener(new ReplaceActionListener());
		
		enableAll(true);
	}
	
	//****************************************************************
	// Enable/Disable MenuItems
	//****************************************************************	
	protected boolean copyEnabled(ITabController tabCtrl) {m_mntmCopy.setEnabled(tabCtrl instanceof ICopyPasteDeleteHandler); return m_mntmCopy.isEnabled();}
	protected boolean cutEnabled(ITabController tabCtrl) {m_mntmCut.setEnabled(tabCtrl instanceof ICopyPasteDeleteHandler); return m_mntmCut.isEnabled();}
	protected boolean pasteEnabled(ITabController tabCtrl) {m_mntmPaste.setEnabled(tabCtrl instanceof ICopyPasteDeleteHandler); return m_mntmPaste.isEnabled();}
	protected boolean deleteEnabled(ITabController tabCtrl) {m_mntmDelete.setEnabled(tabCtrl instanceof ICopyPasteDeleteHandler); return m_mntmDelete.isEnabled();}
	protected boolean undoEnabled(ITabController tabCtrl) {m_mntmUndo.setEnabled(tabCtrl instanceof IUndoRedoHandler && ((IUndoRedoHandler) tabCtrl).canUndo()); return m_mntmUndo.isEnabled();}
	protected boolean redoEnabled(ITabController tabCtrl) {m_mntmRedo.setEnabled(tabCtrl instanceof IUndoRedoHandler && ((IUndoRedoHandler) tabCtrl).canRedo()); return m_mntmRedo.isEnabled();}
	protected boolean findEnabled(ITabController tabCtrl) {m_mntmFind.setEnabled(tabCtrl instanceof IFindReplaceHandler); return m_mntmFind.isEnabled();}
	protected boolean replaceEnabled(ITabController tabCtrl) {m_mntmReplace.setEnabled(tabCtrl instanceof IFindReplaceHandler); return m_mntmReplace.isEnabled();}	
	
	@Override
	protected void enableAll(boolean _b)
	{
		m_mntmCopy.setEnabled(_b);
		m_mntmCut.setEnabled(_b);
		m_mntmPaste.setEnabled(_b);
		m_mntmDelete.setEnabled(_b);
		m_mntmUndo.setEnabled(_b);
		m_mntmRedo.setEnabled(_b);
		m_mntmFind.setEnabled(_b);
		m_mntmReplace.setEnabled(_b);
	}
	
	@Override
	protected void enableForTab()
	{
		if (m_currentEditorCtrl == null) {return;}
		// Enable/Disable events based on currently active tab
		copyEnabled(m_currentEditorCtrl);
		cutEnabled(m_currentEditorCtrl);
		pasteEnabled(m_currentEditorCtrl);
		deleteEnabled(m_currentEditorCtrl);
		undoEnabled(m_currentEditorCtrl);
		redoEnabled(m_currentEditorCtrl);
		findEnabled(m_currentEditorCtrl);
		replaceEnabled(m_currentEditorCtrl);
	}

	private class CopyActionListener implements ActionListener
	{
		//****************************************************************
		// Implement the ActionListener interface
		//****************************************************************
		@Override
		public void actionPerformed(ActionEvent _e)
		{
			// Enable so that we do not miss shortcut events.
			// This is necessary since we are not actively listening
			// to changes that enable/disable the menuItems
			((JMenuItem) _e.getSource()).setEnabled(true);
		}
	}
	
	private class CutActionListener implements ActionListener
	{
		//****************************************************************
		// Implement the ActionListener interface
		//****************************************************************
		@Override
		public void actionPerformed(ActionEvent _e)
		{
			((JMenuItem) _e.getSource()).setEnabled(true);
		}
	}
	
	private class PasteActionListener implements ActionListener
	{
		//****************************************************************
		// Implement the ActionListener interface
		//****************************************************************
		@Override
		public void actionPerformed(ActionEvent _e)
		{
			((JMenuItem) _e.getSource()).setEnabled(true);
		}
	}

	private class DeleteActionListener implements ActionListener
	{
		//****************************************************************
		// Implement the ActionListener interface
		//****************************************************************
		@Override
		public void actionPerformed(ActionEvent _e)
		{
			((JMenuItem) _e.getSource()).setEnabled(true);
		}
	}

	private class UndoActionListener implements ActionListener
	{
		//****************************************************************
		// Implement the ActionListener interface
		//****************************************************************
		@Override
		public void actionPerformed(ActionEvent _e)
		{
			if (undoEnabled(m_currentEditorCtrl))
			{
				((IUndoRedoHandler) m_currentEditorCtrl).undo();
			}
			((JMenuItem) _e.getSource()).setEnabled(true);
		}
	}

	private class RedoActionListener implements ActionListener
	{
		//****************************************************************
		// Implement the ActionListener interface
		//****************************************************************
		@Override
		public void actionPerformed(ActionEvent _e)
		{
			if (redoEnabled(m_currentEditorCtrl))
			{
				((IUndoRedoHandler) m_currentEditorCtrl).redo();
			}
			((JMenuItem) _e.getSource()).setEnabled(true);
		}
	}
	
	private class FindActionListener implements ActionListener
	{
		//****************************************************************
		// Implement the ActionListener interface
		//****************************************************************
		@Override
		public void actionPerformed(ActionEvent _e)
		{
			((JMenuItem) _e.getSource()).setEnabled(true);
		}

	}
	
	private class ReplaceActionListener implements ActionListener
	{
		//****************************************************************
		// Implement the ActionListener interface
		//****************************************************************
		@Override
		public void actionPerformed(ActionEvent _e)
		{
			((JMenuItem) _e.getSource()).setEnabled(true);
		}
	}
}
