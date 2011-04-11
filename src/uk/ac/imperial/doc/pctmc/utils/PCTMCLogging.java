package uk.ac.imperial.doc.pctmc.utils;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class PCTMCLogging {

	public static int indent = 0;
	
	private static boolean visible = true; 

	public static boolean isVisible() {
		return visible;
	}

	public static void setVisible(boolean visible) {
		PCTMCLogging.visible = visible;
	}

	static final Logger LOGGER = Logger.getLogger(PCTMCLogging.class);
	static {
		setLogger(LOGGER);
	}

	public static void info(String message) {
		message=message.replaceAll("\n", "\n"+getSpaces()+getOverhead());
		if (visible) LOGGER.info(message);
	}

	public static void error(String message) {
		message=message.replaceAll("\n", "\n"+getSpaces()+getOverhead());
		LOGGER.error(message);
	}
	
	public static String getOverhead(){
		return "              ";
	}

	public static void debug(String message) {
		message=message.replaceAll("\n", "\n"+getSpaces()+getOverhead());
		if (visible) LOGGER.debug("[D] " + message);
	}

	public static void increaseIndent() {
		indent++;
		setLogger(LOGGER);
	}

	public static void decreaseIndent() {
		indent--;
		setLogger(LOGGER);
	}

	private static String getSpaces(){
		String indentSpace = "";

		for (int i = 0; i < indent; i++) {
			indentSpace += "   ";
		}
		return indentSpace;
	}
	
	private static void setLogger(final Logger logger) {
		final String pattern = "%10r " + getSpaces() + "- %m %n";
		final PatternLayout layout = new PatternLayout(pattern);
		final ConsoleAppender appender = new ConsoleAppender(layout);
		logger.removeAllAppenders();
		logger.addAppender(appender);
	}
}
