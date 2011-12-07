package uk.ac.imperial.doc.masspa.util;

import java.io.PrintStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.OutputStream;

import uk.ac.imperial.doc.masspa.language.Messages;
import uk.ac.imperial.doc.pctmc.utils.IConsole;
import uk.ac.imperial.doc.pctmc.utils.PCTMCLogging;

/**
 * Logs to standard out/err and
 * consoles via PCTMCLogging
 * 
 * @author Chris Guenther
 */
public class MASSPALogging
{
	public static void addConsole(IConsole _c)
	{
		PCTMCLogging.addConsole(_c);
	}
	
	public static void removeConsole(IConsole _c)
	{
		PCTMCLogging.removeConsole(_c);
	}
	
	public static void info(String _msg)
	{
		PCTMCLogging.info(_msg);
	}
	
	public static void ok(String _msg)
	{
		PCTMCLogging.ok(_msg);
	}
	
	public static void warn(String _msg)
	{
		PCTMCLogging.warn(String.format(Messages.s_WARNING, _msg));
	}
	
	public static void error(String _msg)
	{
		PCTMCLogging.error(String.format(Messages.s_ERROR, _msg));
	}

	public static void fatalError(String _msg)
	{
		PCTMCLogging.fatalError(String.format(Messages.s_FATAL_ERROR, _msg));
	}
	
	public static void clearConsoles()
	{
		PCTMCLogging.clearConsoles();
	}

	public static void printConsoleStats()
	{
		PCTMCLogging.printConsoleStats();
	}
	
	public PrintStream createErrorStream()
	{
		return new PrintStream(new ErrorLogStream(new ByteArrayOutputStream()));
	}
	
	class ErrorLogStream extends FilterOutputStream
	{
		public ErrorLogStream(OutputStream aStream)
		{
			super(aStream);
		}

		public void write(byte b[])
		{
			print(new String(b));
		}

		public void write(byte b[], int off, int len)
		{
			print(new String(b , off , len));
		}
		
		private void print(String _err)
		{
			if (_err.contains("AssertionError"))
			{
				MASSPALogging.fatalError(_err);
			}
			else if(_err.contains("line"))
			{
				MASSPALogging.error(_err);
			}
		}
	}
}
