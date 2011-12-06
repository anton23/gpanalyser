package uk.ac.imperial.doc.pctmc.utils;

import java.util.StringTokenizer;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
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
		message=formatMessage(message);
		if (visible) LOGGER.info(message);
	}

	public static void error(String message) {
		message=formatMessage(message);
		LOGGER.error(message);
	}
	
	public static String getOverhead(){
		return "              ";
	}

	public static void debug(String message) {
			message=formatMessage(message);
			if (visible) LOGGER.debug("[D] " + message);
	}
	
	protected static int maxChars =  100;
	
	protected static String formatMessage(String message) {
		StringBuilder ret = new StringBuilder();
		StringTokenizer tok = new StringTokenizer(message, " ",true);
		int lineLength = 0;
		String padding = "\n"+getSpaces()+getOverhead();
		while(tok.hasMoreTokens()) {
			String word = tok.nextToken();
			boolean first = true;
			StringTokenizer tmp = new StringTokenizer(word, "\n", true);
			while (tmp.hasMoreElements()){
				String subWord = tmp.nextToken();
				if (first) {
					first = false;
					if ((lineLength + subWord.length() > maxChars)) {
						ret.append(padding);
						lineLength = subWord.length();
					} else {
						lineLength += subWord.length();
					}
				} else {
					if (subWord.equals("\n")) {
						lineLength = 0;
					} else {
						lineLength = subWord.length();
					}
				}
				ret.append(subWord.replace("\n", padding));
			}
		}
		return ret.toString();
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
		if (PCTMCOptions.debug) {
			logger.setLevel(Level.DEBUG);
		} else {
			logger.setLevel(Level.INFO);
		}
	}
	
	
}
