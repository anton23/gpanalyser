package uk.ac.imperial.doc.masspa.gui.models;

import javax.swing.text.DefaultStyledDocument;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;

import uk.ac.imperial.doc.masspa.language.Messages;
import uk.ac.imperial.doc.masspa.util.MASSPALogging;

/**
 * This class represents a serializable swing document.
 * 
 * @author Chris Guenther
 */
@Root @Namespace(reference="http://uk.ac.imperial.doc.masspa/ObservableDocument/1")
public class ObservableDocument extends DefaultStyledDocument
{
	private static final long serialVersionUID = 8254733913990585041L;

	//**********************************************
	// Getters/Setters and SimpleXML-Serialization
	//**********************************************
	/**
	 * @return document content as String.
	 */
	@Element(name="doc", data=true, required=false) protected String getDoc()
	{
		try
		{
			return getText(0, getLength());
		}
		catch (Exception e)
		{
			MASSPALogging.fatalError(Messages.s_OBSERVABLE_DOCUMENT_SERIALIZATION_FAILED);
		}
		return "";
	}
	
	/**
	 * Copy {@code _s} into the document.
	 * @param _s  
	 */
	@Element(name="doc", data=true, required=false) protected void setDoc(String _s)
	{
		copyStringIntoDocument(_s);
	}

	/**
	 * Copy text from {@code _str} into this document
	 * @param _str
	 */
	private void copyStringIntoDocument(String _str)
	{
		try
		{
			remove(0, getLength());
			insertString(0, _str, null);
		}
		catch (Exception e)
		{
			MASSPALogging.fatalError(Messages.s_OBSERVABLE_DOCUMENT_COPY_ERROR);
		}
	}
	
	/**
	 * Copy text from {@code _doc} into this document
	 * @param _doc
	 */
	public void setDocument(ObservableDocument _doc)
	{
		try
		{
			if (_doc.getLength() == 0) {copyStringIntoDocument("");}
			copyStringIntoDocument(_doc.getText(0, _doc.getLength()));
		}
		catch (Exception e)
		{
			MASSPALogging.fatalError(Messages.s_OBSERVABLE_DOCUMENT_DESERIALIZATION_FAILED);
		}
	}
}
