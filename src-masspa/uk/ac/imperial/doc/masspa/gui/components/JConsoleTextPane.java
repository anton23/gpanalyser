package uk.ac.imperial.doc.masspa.gui.components;

import java.awt.Color;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import uk.ac.imperial.doc.masspa.language.Messages;
import uk.ac.imperial.doc.masspa.util.MASSPALogging;
import uk.ac.imperial.doc.pctmc.utils.IConsole;

/**
 * A standard console for logging purposes.
 * 
 * @author Chris Guenther
 */
public class JConsoleTextPane extends JTextPane implements IConsole
{
	private static final long serialVersionUID = 8953340306181649463L;

	private final SimpleAttributeSet 	m_infoStyle;
	private final SimpleAttributeSet 	m_okStyle;
	private final SimpleAttributeSet 	m_warnStyle;
	private final SimpleAttributeSet 	m_errorStyle;
	private final SimpleAttributeSet 	m_fatalErrorStyle;
	private final SimpleAttributeSet 	m_debugStyle;
	private final Object				m_wLock;
	
	private enum Stats {WARNINGS, ERRORS, FATAL_ERRORS, DEBUG};
	private final Map<Stats, Integer> m_stats;
	
	public JConsoleTextPane()
	{
		MASSPALogging.addConsole(this);
		setEditable(false);
		
		// Create styles
		StyledDocument doc = (StyledDocument)this.getDocument();
		m_infoStyle = new SimpleAttributeSet(doc.getStyle("default"));
		StyleConstants.setForeground(m_infoStyle, Color.BLACK);
		m_okStyle = new SimpleAttributeSet(doc.getStyle("default"));
		StyleConstants.setForeground(m_okStyle, Color.getHSBColor(0.3f, 1.0f, 0.5f));
		m_warnStyle = new SimpleAttributeSet(doc.getStyle("default"));
		StyleConstants.setForeground(m_warnStyle, Color.MAGENTA);
		m_errorStyle = new SimpleAttributeSet(doc.getStyle("default"));
		StyleConstants.setForeground(m_errorStyle, Color.RED);
		m_fatalErrorStyle = new SimpleAttributeSet(doc.getStyle("default"));
		StyleConstants.setForeground(m_fatalErrorStyle, Color.RED);
		StyleConstants.setBold(m_fatalErrorStyle, true);
		m_debugStyle = new SimpleAttributeSet(doc.getStyle("default"));
		StyleConstants.setForeground(m_debugStyle, Color.BLUE);
		StyleConstants.setItalic(m_debugStyle, true);
		
		// Create Stats
		m_stats = new HashMap<Stats, Integer>();
		
		// Create Lock
		m_wLock = new Object();
	}

	@Override
	public void info(final String _msg)
	{
		writln(_msg, m_infoStyle);
	}

	@Override
	public void ok(final String _msg)
	{
		writln(_msg, m_okStyle);
	}
	
	@Override
	public void warn(final String _msg)
	{
		writln(_msg, m_warnStyle);
		incStats(Stats.WARNINGS);
	}
	
	@Override
	public void error(final String _msg)
	{
		writln(_msg, m_errorStyle);
		incStats(Stats.ERRORS);
	}
	
	@Override
	public void fatalError(final String _msg)
	{
		writln(_msg, m_fatalErrorStyle);
		incStats(Stats.FATAL_ERRORS);
	}
	
	@Override
	public void debug(final String _msg)
	{
		writln(_msg, m_debugStyle);
		incStats(Stats.DEBUG);
	}
	
	protected void writln(final String _msg, final SimpleAttributeSet _style)
	{
		write(_msg,_style,true,true);
	}
	
	protected void writln(final String _msg, final SimpleAttributeSet _style, final boolean _showTime)
	{
		write(_msg,_style,_showTime,true);
	}
	
	protected void write(final String _msg, final SimpleAttributeSet _style)
	{
		write(_msg,_style,true,false);
	}
	
	protected void write(final String _msg, final SimpleAttributeSet _style, final boolean _showTime)
	{
		write(_msg,_style,_showTime,false);
	}
	
	protected void write(final String _msg, final SimpleAttributeSet _style, final boolean _showTime, final boolean _linebreak)
	{
		synchronized(m_wLock)
		{
			Document doc = getDocument();
			try
			{
				if (_showTime)
				{
					DateFormat formatter = new SimpleDateFormat("hh:mm:ss");
					Date date = new Date();
					doc.insertString(doc.getLength(), "["+formatter.format(date)+"] ", m_infoStyle);
				}
				doc.insertString(doc.getLength(), _msg, _style);
				if(_linebreak)
				{
					doc.insertString(doc.getLength(), System.getProperty("line.separator"), _style);
				}
			} 
			catch (BadLocationException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void clear()
	{
		synchronized(m_wLock)
		{
			setDocument(new DefaultStyledDocument());
			clearStats();
		}
	}
	
	@Override
	public void printStats()
	{
		writln("",m_infoStyle,false);
		writln(Messages.s_CONSOLE_STATS, m_infoStyle);
		write(String.format(Messages.s_NUM_WARNINGS, getStats(Stats.WARNINGS)), m_warnStyle);
		write(", ", m_infoStyle,false);
		write(String.format(Messages.s_NUM_ERRORS, getStats(Stats.ERRORS)), m_errorStyle,false);
		write(", ", m_infoStyle,false);
		writln(String.format(Messages.s_NUM_FATAL_ERRORS, getStats(Stats.FATAL_ERRORS)),m_fatalErrorStyle,false);
		writln("",m_infoStyle,false);
	}
	
	protected void clearStats()
	{
		m_stats.clear();
	}

	protected int getStats(final Stats _stat)
	{
		synchronized(m_wLock)
		{
			Integer i = m_stats.get(_stat);
			return (i==null) ? 0 : i;
		}
	}
	
	protected void incStats(Stats _stat)
	{
		synchronized(m_wLock)
		{
			m_stats.put(_stat, getStats(_stat)+1);
		}
	}
}
