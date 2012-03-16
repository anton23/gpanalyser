package uk.ac.imperial.doc.masspa.gui.menus;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import uk.ac.imperial.doc.masspa.gui.util.CompoundUndoManager;

public interface IUndoRedoHandler
{
	public void undo() throws CannotUndoException;
	public boolean canUndo();
	public void redo() throws CannotRedoException;
	public boolean canRedo();
	public void setUndoRedoMgr(CompoundUndoManager _mgr);
}
