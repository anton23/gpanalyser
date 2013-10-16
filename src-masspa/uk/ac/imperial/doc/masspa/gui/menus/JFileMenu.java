package uk.ac.imperial.doc.masspa.gui.menus;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.filechooser.FileFilter;

import uk.ac.imperial.doc.masspa.gui.interfaces.ITabController;
import uk.ac.imperial.doc.masspa.gui.util.Shortcuts;
import uk.ac.imperial.doc.masspa.language.Labels;

/**
 * The file menu
 */
public class JFileMenu extends JMainMenu
{
	private static final long serialVersionUID = -8784827434207268559L;
	
	private final JMenuItem	m_mntmNew;
	private final JMenuItem	m_mntmSave;
	private final JMenuItem	m_mntmSaveAs;
	private final JMenuItem	m_mntmOpen;
	private final JMenuItem	m_mntmExit;
	private final JMenuItem	m_mntmPrint;
	
	private final JFileChooser m_fileChooser;
	private File m_curFile;
	
	private final IFileMenuHandler m_fileMenuListener;
	
	public JFileMenu(IFileMenuHandler _fileMenuListener, JTabbedPane _editorTabs, ITabController _initEditorCtrl)
	{
		super(Labels.s_MENU_FILE, _editorTabs, _initEditorCtrl);
		m_fileMenuListener = _fileMenuListener;

		// Menu items
		m_mntmNew = new JMenuItem(Labels.s_MENU_FILE_NEW);
		m_mntmOpen = new JMenuItem(Labels.s_MENU_FILE_OPEN);
		m_mntmSave = new JMenuItem(Labels.s_MENU_FILE_SAVE);
		m_mntmSaveAs = new JMenuItem(Labels.s_MENU_FILE_SAVE_AS);
		m_mntmPrint = new JMenuItem(Labels.s_MENU_FILE_PRINT);
		m_mntmExit = new JMenuItem(Labels.s_MENU_FILE_EXIT);
		
		// Mnemonics and Shortcuts
		this.setMnemonic(Shortcuts.s_MENU_FILE_MNEMONIC);
		m_mntmOpen.setMnemonic(Shortcuts.s_MENU_FILE_OPEN_MNEMONIC);
		m_mntmOpen.setAccelerator(Shortcuts.s_MENU_FILE_OPEN_ACCELERATOR);
		m_mntmSave.setMnemonic(Shortcuts.s_MENU_FILE_SAVE_MNEMONIC);
		m_mntmSave.setAccelerator(Shortcuts.s_MENU_FILE_SAVE_ACCELERATOR);
		m_mntmSaveAs.setMnemonic(Shortcuts.s_MENU_FILE_SAVE_AS_MNEMONIC);
		m_mntmSaveAs.setAccelerator(Shortcuts.s_MENU_FILE_SAVE_AS_ACCELERATOR);
		m_mntmPrint.setMnemonic(Shortcuts.s_MENU_FILE_PRINT_MNEMONIC);
		m_mntmPrint.setAccelerator(Shortcuts.s_MENU_FILE_PRINT_ACCELERATOR);
		m_mntmExit.setMnemonic(Shortcuts.s_MENU_FILE_EXIT_MNEMONIC);
		m_mntmExit.setAccelerator(Shortcuts.s_MENU_FILE_EXIT_ACCELERATOR);
		
		// Layout
		add(m_mntmNew);
		add(new JSeparator());
		add(m_mntmOpen);
		add(m_mntmSave);
		add(m_mntmSaveAs);
		add(new JSeparator());
		add(m_mntmPrint);
		add(new JSeparator());
		add(m_mntmExit);
		
		// Register as Listener
		m_mntmNew.addActionListener(new NewFileActionListener());
		m_mntmOpen.addActionListener(new OpenFileActionListener());
		m_mntmSave.addActionListener(new SaveFileActionListener());
		m_mntmSaveAs.addActionListener(new SaveFileActionListener());
		m_mntmExit.addActionListener(new ExitActionListener());
		
		// File chooser
		m_fileChooser = new JFileChooser();
		m_fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		m_fileChooser.setFileFilter(new FileFilter(){
			@Override
			public boolean accept(File _f)
			{
				return _f.getName().endsWith(".masspa");
			}

			@Override
			public String getDescription()
			{
				return "*.masspa";
			}});
	}

	private class NewFileActionListener implements ActionListener
	{
		//****************************************************************
		// Implement the ActionListener interface
		//****************************************************************
		@Override
		public void actionPerformed(ActionEvent _e)
		{
			m_curFile = null;
			m_fileMenuListener.newModel();
		}
	}
	
	private class OpenFileActionListener implements ActionListener
	{
		//****************************************************************
		// Implement the ActionListener interface
		//****************************************************************
		@Override
		public void actionPerformed(ActionEvent _e)
		{
			int btnClicked = m_fileChooser.showOpenDialog(null);
			switch (btnClicked)
			{
			  case JFileChooser.APPROVE_OPTION: break;
			  case JFileChooser.CANCEL_OPTION: return;
			  case JFileChooser.ERROR_OPTION: return;
			}

			m_curFile = m_fileChooser.getSelectedFile();
			m_curFile = (m_fileMenuListener.load(m_curFile)) ? m_curFile : null;
		}
	}
	
	private class SaveFileActionListener implements ActionListener
	{
		//****************************************************************
		// Implement the ActionListener interface
		//****************************************************************
		@Override
		public void actionPerformed(ActionEvent _e)
		{
			if (!_e.getSource().equals(m_mntmSave) || m_curFile==null)
			{
				int btnClicked = m_fileChooser.showSaveDialog(null);
				switch (btnClicked)
				{
				  case JFileChooser.APPROVE_OPTION: break;
				  case JFileChooser.CANCEL_OPTION: return;
				  case JFileChooser.ERROR_OPTION: return;
				}
				m_curFile = m_fileChooser.getSelectedFile();
			}
			m_curFile = (m_fileMenuListener.save(m_curFile)) ? m_curFile : null;
		}
	}
	
	private class ExitActionListener implements ActionListener
	{
		//****************************************************************
		// Implement the ActionListener interface
		//****************************************************************
		@Override
		public void actionPerformed(ActionEvent _e)
		{
			m_fileMenuListener.exit();
		}
	}
}
