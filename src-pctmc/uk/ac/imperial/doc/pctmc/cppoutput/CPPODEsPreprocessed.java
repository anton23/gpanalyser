package uk.ac.imperial.doc.pctmc.cppoutput;

import uk.ac.imperial.doc.pctmc.implementation.PCTMCImplementationPreprocessed;
import uk.ac.imperial.doc.pctmc.odeanalysis.utils.SystemOfODEs;

public class CPPODEsPreprocessed extends PCTMCImplementationPreprocessed {
	private SystemOfODEs odes;

	public CPPODEsPreprocessed(SystemOfODEs odes) {
		super();
		this.odes = odes;
	}

	public SystemOfODEs getOdes() {
		return odes;
	}
}
