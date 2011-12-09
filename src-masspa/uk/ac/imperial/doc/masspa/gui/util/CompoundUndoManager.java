package uk.ac.imperial.doc.masspa.gui.util;

import java.util.LinkedList;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.JTextComponent;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

/**
 * This class will merge individual edits into a single larger edit.
 * That is, characters entered sequentially will be grouped together and
 * undone as a group. Any attribute changes will be considered as part
 * of the group and will therefore be undone when the group is undone.
 *
 * @author {@link http://www.thatsjava.com/java-core-gui-apis/64422/}
 */

public class CompoundUndoManager extends UndoManager implements UndoableEditListener, DocumentListener
{
	private static final long serialVersionUID = -7793476474974230146L;
	private static final List<CompoundUndoManager> m_undoManagers = new LinkedList<CompoundUndoManager>();
	
	protected CompoundEdit m_compoundEdit;
	protected JTextComponent m_editor;

	// These fields are used to help determine whether the edit is an
	// incremental edit. For each character added the offset and length
	// should increase by 1 or decrease by 1 for each character removed.
	protected int m_lastOffset;
	protected int m_lastLength;
	
	// Max length of a series of Undo operations
	public static final int s_MAX_UNDO_LENGTH = 10;
	protected int m_undoLenCount = 0;
	
	public CompoundUndoManager(JTextComponent _editor)
	{
		m_editor = _editor;
		m_editor.getDocument().addUndoableEditListener(this);
		m_undoManagers.add(this);
	}

	/**
	 * Remove all undo/redo events from all registered managers.
	 */
	public static void resetAllUndoManagers()
	{
		for (CompoundUndoManager mgr : m_undoManagers)
		{
			mgr.reset();
		}
	}
	
	/**
	 * Reset the undo manager
	 */
	public void reset()
	{
		discardAllEdits();
		edits.clear();
		m_lastLength=0;
		m_lastOffset=0;
		m_compoundEdit=null;
	}
	
	/**
	 * @return true iff underlying editor is focused
	 */
	public boolean isComponentFocused()
	{
		return m_editor.isFocusOwner();
	}
	
	/**
	 * Add a DocumentLister before the undo is done so we can position
	 * the Caret correctly as each edit is undone.
	 */
	@Override
	public void undo()
	{
		m_editor.getDocument().addDocumentListener(this);
		super.undo();
		m_editor.getDocument().removeDocumentListener(this);
	}

	/**
	 * Add a DocumentLister before the redo is done so we can position
	 * the Caret correctly as each edit is redone.
	 */
	@Override
	public void redo()
	{
		m_editor.getDocument().addDocumentListener(this);
		super.redo();
		m_editor.getDocument().removeDocumentListener(this);
	}

	/**
	 * Whenever an UndoableEdit happens the edit will either be absorbed
	 * by the current compound edit or a new compound edit will be started
	 */
	@Override
	public void undoableEditHappened(UndoableEditEvent _e)
	{
		// Start a new compound edit
		if (m_compoundEdit == null)
		{
			m_compoundEdit = startCompoundEdit(_e.getEdit());
			m_lastLength = m_editor.getDocument().getLength();
			return;
		}
		else if (m_undoLenCount >= s_MAX_UNDO_LENGTH)
		{
			m_compoundEdit.end();
			m_compoundEdit = startCompoundEdit(_e.getEdit());
			return;
		}

		// Check for an attribute change
		AbstractDocument.DefaultDocumentEvent event = (AbstractDocument.DefaultDocumentEvent)_e.getEdit();
		if (event.getType().equals(DocumentEvent.EventType.CHANGE))
		{
			m_compoundEdit.addEdit(_e.getEdit());
			return;
		}
		
		// Check for an incremental edit or backspace. The change
		// in Caret position and Document length should be either 1 or -1.
		int offsetChange = m_editor.getCaretPosition() - m_lastOffset;
		int lengthChange = m_editor.getDocument().getLength() - m_lastLength;
		m_undoLenCount += lengthChange;
		if (Math.abs(offsetChange) == 1 && Math.abs(lengthChange) == 1)
		{
			m_compoundEdit.addEdit(_e.getEdit());
			m_lastOffset = m_editor.getCaretPosition();
			m_lastLength = m_editor.getDocument().getLength();
			return;
		}

		// Not incremental edit, end previous edit and start a new one
		m_compoundEdit.end();
		m_compoundEdit = startCompoundEdit(_e.getEdit());
	}

	/**
	 * Each CompoundEdit will store a group of related incremental edits
	 * (ie. each character typed or backspaced is an incremental edit)
	 */
	private CompoundEdit startCompoundEdit(UndoableEdit _anEdit)
	{
		// Track Caret and Document information of this compound edit
		m_lastOffset = m_editor.getCaretPosition();
		m_lastLength = m_editor.getDocument().getLength();
		m_undoLenCount = 0;
		
		// The compound edit is used to store incremental edits
		m_compoundEdit = new MyCompoundEdit();
		m_compoundEdit.addEdit(_anEdit);
		
		// The compound edit is added to the UndoManager. All incremental
		// edits stored in the compound edit will be undone/redone at once
		addEdit(m_compoundEdit);

		return m_compoundEdit;
	}

	//***************************************************************
	// Implement DocumentListener. Updates to the Document as a
	// result of Undo/Redo will cause the Caret to be repositioned
	//***************************************************************
	@Override
	public void insertUpdate(final DocumentEvent _e)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				int offset = _e.getOffset() + _e.getLength();
				offset = Math.min(offset, m_editor.getDocument().getLength());
				m_editor.setCaretPosition(offset);
			}
		});
	}
	
	@Override
	public void removeUpdate(DocumentEvent _e)
	{
		m_editor.setCaretPosition(_e.getOffset());
	}
	
	@Override
	public void changedUpdate(DocumentEvent _e)
	{
	}

	class MyCompoundEdit extends CompoundEdit
	{
		private static final long serialVersionUID = -5765686014239393689L;

		@Override
		public boolean isInProgress()
		{
			// in order for the canUndo() and canRedo() methods to work
			// assume that the compound edit is never in progress
			return false;
		}

		@Override
		public void undo() throws CannotUndoException
		{
			// End the edit so future edits don't get absorbed by this edit
			if (m_compoundEdit != null)
			{
				m_compoundEdit.end();
			}
			super.undo();
			
			// Always start a new compound edit after an undo
			m_compoundEdit = null;
		}
	}
}