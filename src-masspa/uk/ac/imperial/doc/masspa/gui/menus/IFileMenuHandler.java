package uk.ac.imperial.doc.masspa.gui.menus;

import java.io.File;

public interface IFileMenuHandler
{
	public void newModel();
	public boolean load(File _file);
	public boolean save(File _file);
	public void exit();
}
