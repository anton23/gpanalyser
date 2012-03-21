package uk.ac.imperial.doc.masspa.gui.util;

import java.awt.KeyEventDispatcher;
import java.awt.event.KeyEvent;

/**
 * A singleton class that keeps
 * track which key(s) have been
 * pressed 
 * 
 * @author Chris Guenther
 */
public class KeyStateManager implements KeyEventDispatcher
{
	private static KeyStateManager s_mgr = null;
	private int m_pressedKeyCode = 0;
	
	private KeyStateManager()
	{
		
	}
	
	public static KeyStateManager getKeyStateManager()
	{
		if (s_mgr == null)
		{
			s_mgr = new KeyStateManager();
		}
		return s_mgr;
	}
	
	/**
	 * @return key code of the key that is currently being pressed
	 */
	public int getCurrentlyPressedKey()
	{
		return m_pressedKeyCode;
	}
	
	//**************************************************
	// Implement KeyListener KeyEventDispatcher
	//**************************************************
	@Override
	public boolean dispatchKeyEvent(KeyEvent _k)
	{
		if (_k.getID() == KeyEvent.KEY_PRESSED)
		{
			m_pressedKeyCode = _k.getKeyCode();
		}
		else if (_k.getID() == KeyEvent.KEY_RELEASED)
		{
			m_pressedKeyCode = 0;
		}
		
		// Make sure the event is process further
		return false;
	}
}
