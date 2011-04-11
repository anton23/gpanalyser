package uk.ac.imperial.doc.pctmc.javaoutput;

import uk.ac.imperial.doc.pctmc.implementation.PCTMCImplementationPreprocessed;
import uk.ac.imperial.doc.pctmc.utils.SystemOfODEs;

public class JavaODEsPreprocessed extends PCTMCImplementationPreprocessed{
	private SystemOfODEs odes;

	public JavaODEsPreprocessed(SystemOfODEs odes) {
		super();
		this.odes = odes;
	}

	public SystemOfODEs getOdes() {
		return odes;
	} 
}
