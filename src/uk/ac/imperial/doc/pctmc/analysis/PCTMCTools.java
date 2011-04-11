package uk.ac.imperial.doc.pctmc.analysis;

import uk.ac.imperial.doc.pctmc.implementation.PCTMCImplementationProvider;
import uk.ac.imperial.doc.pctmc.javaoutput.PCTMCJavaImplementationProvider;


/**
 * Main class keeping implementation providers and other global objects. 
 * @author Anton Stefanek
 *
 */
public class PCTMCTools {
	
	private static PCTMCImplementationProvider provider = new PCTMCJavaImplementationProvider(); 
	
	public static PCTMCImplementationProvider getImplementationProvider(){
		return provider; 
	}
}
