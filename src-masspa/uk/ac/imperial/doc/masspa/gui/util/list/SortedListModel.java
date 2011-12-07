package uk.ac.imperial.doc.masspa.gui.util.list;

import java.util.Set;
import java.util.TreeSet;

import javax.swing.DefaultListModel;

public class SortedListModel extends DefaultListModel
{
	private static final long serialVersionUID = 1642777029954321601L;

	private Set<Object> m_list = new TreeSet<Object>();
	
	@Override
	public void clear()
	{
		super.clear();
		m_list.clear();
	}
	
	@Override
	public void addElement(Object _obj)
	{
		m_list.add(_obj);
		super.clear();
		for (Object o : m_list) {super.addElement(o);}
	}
}
