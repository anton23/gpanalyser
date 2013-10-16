package uk.ac.imperial.doc.masspa.gui.util;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

public class Shortcuts
{
	// File menu
	public static int s_MENU_FILE_MNEMONIC = KeyEvent.VK_F;
	public static final int s_MENU_FILE_OPEN_MNEMONIC = KeyEvent.VK_O;
	public static final KeyStroke s_MENU_FILE_OPEN_ACCELERATOR = KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK);
	public static final int s_MENU_FILE_SAVE_MNEMONIC = KeyEvent.VK_S;
	public static final KeyStroke s_MENU_FILE_SAVE_ACCELERATOR = KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK);
	public static final int s_MENU_FILE_SAVE_AS_MNEMONIC = KeyEvent.VK_A;
	public static final KeyStroke s_MENU_FILE_SAVE_AS_ACCELERATOR = KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK);
	public static final int s_MENU_FILE_PRINT_MNEMONIC = KeyEvent.VK_P;
	public static final KeyStroke s_MENU_FILE_PRINT_ACCELERATOR = KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK);
	public static final int s_MENU_FILE_EXIT_MNEMONIC = KeyEvent.VK_E;
	public static final KeyStroke s_MENU_FILE_EXIT_ACCELERATOR = KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK);
	
	// Edit menu
	public static int s_MENU_EDIT_MNEMONIC = KeyEvent.VK_E;
	public static final int s_MENU_EDIT_COPY_MNEMONIC = KeyEvent.VK_C;
	public static final KeyStroke s_MENU_EDIT_COPY_ACCELERATOR = KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK);
	public static final int s_MENU_EDIT_CUT_MNEMONIC = KeyEvent.VK_T;
	public static final KeyStroke s_MENU_EDIT_CUT_ACCELERATOR = KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK);
	public static final int s_MENU_EDIT_PASTE_MNEMONIC = KeyEvent.VK_P;
	public static final KeyStroke s_MENU_EDIT_PASTE_ACCELERATOR = KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK);
	public static final int s_MENU_EDIT_DELETE_MNEMONIC = KeyEvent.VK_D;
	public static final KeyStroke s_MENU_EDIT_DELETE_ACCELERATOR = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, ActionEvent.CTRL_MASK);
	public static final int s_MENU_EDIT_UNDO_MNEMONIC = KeyEvent.VK_U;
	public static final KeyStroke s_MENU_EDIT_UNDO_ACCELERATOR = KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK);
	public static final int s_MENU_EDIT_REDO_MNEMONIC = KeyEvent.VK_R;
	public static final KeyStroke s_MENU_EDIT_REDO_ACCELERATOR = KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK);
	public static final int s_MENU_EDIT_FIND_MNEMONIC = KeyEvent.VK_F;
	public static final KeyStroke s_MENU_EDIT_FIND_ACCELERATOR = KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK);
	public static final int s_MENU_EDIT_REPLACE_MNEMONIC = KeyEvent.VK_R;
	public static final KeyStroke s_MENU_EDIT_REPLACE_ACCELERATOR = KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.CTRL_MASK);
	
	// Multiselection
	public static final int s_MULTISELECTION = KeyEvent.VK_CONTROL;
	public static final int s_DELETE = KeyEvent.VK_DELETE;
	public static final int s_LOCATION_DISABLE = KeyEvent.VK_D;
	public static final int s_LOCATION_ENABLE = KeyEvent.VK_E;
}
