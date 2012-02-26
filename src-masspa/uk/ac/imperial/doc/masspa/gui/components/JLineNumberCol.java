package uk.ac.imperial.doc.masspa.gui.components;

import java.awt.Color;

import javax.swing.JTextPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import uk.ac.imperial.doc.masspa.gui.models.ObservableDocument;

/**
 * A simple class that generates line numbers in text editors. 
 * This class should be used as a RowHeaderView of a
 * {@link javax.swing#JScrollPane}.
 * 
 * @author Chris Guenther
 */
public class JLineNumberCol extends JTextPane
{
	private static final long serialVersionUID = -8942592605981010529L;

	protected final SimpleAttributeSet m_numStyle;
	protected ObservableDocument m_editorDoc;
	protected EditorLineChangeListener m_editorListener;
	protected int m_nofLines;
	
	public JLineNumberCol(ObservableDocument document)
	{
		super();
		setEditable(false);
		m_editorDoc = document;
			
	    // Create number style
		m_numStyle = new SimpleAttributeSet(document.getStyle("default"));
		StyleConstants.setForeground(m_numStyle, Color.LIGHT_GRAY);
		StyleConstants.setAlignment(m_numStyle, StyleConstants.ALIGN_RIGHT);
		setParagraphAttributes(m_numStyle, true);
				
		// Create listener and generate number for the first line
		setText(((EditorLineChangeListener) getDocListener()).getText());
	}

	protected void setNofLines(int _lines)
	{
		m_nofLines = _lines;
	}
		
	protected int getNofLines()
	{
		return m_nofLines;
	}
	
	protected int getTextAreaNofLines()
	{
		int len = m_editorDoc.getLength();
		Element root = m_editorDoc.getDefaultRootElement();
		return root.getElementIndex(len) + 2;
	}
	
	protected boolean hasNofLinesChanged()
	{
		return getTextAreaNofLines() - getNofLines() != 0;
	}
	
	/**
	 * Get the EditorChangeListener Singleton instance
	 */
	public DocumentListener getDocListener()
	{
		m_editorListener = (m_editorListener == null) ? new EditorLineChangeListener() : m_editorListener;
		return m_editorListener;
	}
	
	/**
	 * A simple class that acts as a {@link javax.swing.event#DocumentListener}
	 * for changes made in the text editor that JLineNumberCol produces the numbers
	 * for.
	 */
	class EditorLineChangeListener implements DocumentListener
	{	
		protected String getText()
		{
			String text = "    1" + System.getProperty("line.separator");
			int newNofLines = getTextAreaNofLines();
			for(int i = 2; i <= newNofLines-1; i++)
			{
				text += i + System.getProperty("line.separator");
			}
			setNofLines(newNofLines);
			return text;
		}
		
		protected void updateLines()
		{
			if (hasNofLinesChanged())
			{
				setText(getText());
				setCaretPosition(getDocument().getLength());
			}
		}
		
		@Override
		public void changedUpdate(DocumentEvent _de)
		{
			updateLines();
		}
	
		@Override
		public void insertUpdate(DocumentEvent _de)
		{
			updateLines();
		}
	
		@Override
		public void removeUpdate(DocumentEvent _de)
		{
			updateLines();
		}
	};
}
