package uk.ac.imperial.doc.masspa.gui.menus;

import javax.swing.JMenu;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import uk.ac.imperial.doc.masspa.gui.interfaces.ITab;
import uk.ac.imperial.doc.masspa.gui.interfaces.ITabController;

public abstract class JMainMenu extends JMenu
{
	private static final long serialVersionUID = -7517189170235957147L;

	protected ITabController m_currentEditorCtrl;
	
	public JMainMenu(String name,  JTabbedPane _editorTabs, ITabController _initEditorCtrl)
	{
		super(name);
		m_currentEditorCtrl = _initEditorCtrl;
		
		// Register as Listener
		addMenuListener(new MenuChangeListener());
		_editorTabs.addChangeListener(new TabChangeListener());
	}
	
	/**
	 * Enable/Disable all menu items
	 * @param _b iff true all menu items will be enabled
	 */
	protected void enableAll(boolean _b)
	{	
	}
	
	/**
	 * Enable/Disable all menu items for current tab
	 */
	protected void enableForTab()
	{
	}
	
	private class MenuChangeListener implements MenuListener
	{
		//****************************************************************
		// Implement the MenuListener interface
		//****************************************************************
		@Override
		public void menuCanceled(MenuEvent _e)
		{
			// Enable so that we do not miss shortcut events.
			// This is necessary since we are not actively listening
			// to changes that enable/disable the menuItems
			enableAll(true);
		}
	
		@Override
		public void menuDeselected(MenuEvent _e)
		{
			// Enable so that we do not miss shortcut events.
			// This is necessary since we are not actively listening
			// to changes that enable/disable the menuItems
			enableAll(true);
		}
	
		public void menuSelected(MenuEvent _e)
		{
			enableForTab();
		}
	}

	private class TabChangeListener implements ChangeListener
	{
		//****************************************************************
		// Implement the ChangeListener interface
		//****************************************************************
		@Override
		public void stateChanged(ChangeEvent _e)
		{
			// Set current controller
			JTabbedPane t = (JTabbedPane) _e.getSource();
			m_currentEditorCtrl = ((ITab)t.getSelectedComponent()).getTabController();
		}
	}
}
