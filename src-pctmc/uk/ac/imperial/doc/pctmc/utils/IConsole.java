package uk.ac.imperial.doc.pctmc.utils;

/**
 * An interface for logging consoles.
 * 
 * @author Chris Guenther
 */
public interface IConsole
{
	/**
	 * Display info message {@code _msg}
	 * @param _msg
	 */
	public void info(String _msg);
	
	/**
	 * Display message to show that everything is fine {@code _msg}
	 * @param _msg
	 */
	public void ok(String _msg);
	
	/**
	 * Display warning message {@code _msg}
	 * @param _msg
	 */
	public void warn(String _msg);
	
	/**
	 * Display error message {@code _msg}
	 * @param _msg
	 */
	public void error(String _msg);
	
	/**
	 * Display fatal error message {@code _msg}
	 * @param _msg
	 */
	public void fatalError(String _msg);
	
	/**
	 * Display debug message {@code _msg}
	 * @param _msg
	 */
	public void debug(String _msg);
	
	/**
	 * Reset console (incl. stats)
	 */
	public void clear();
	
	/**
	 * Display statistics such as number of warnings, errors etc.
	 */
	public void printStats();
}
