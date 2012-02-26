package uk.ac.imperial.doc.pctmc.cppoutput;

import uk.ac.imperial.doc.pctmc.cppoutput.utils.NativeSystemOfODEs;
import uk.ac.imperial.doc.pctmc.implementation.PCTMCImplementationPreprocessed;

public class CPPODEsPreprocessed extends PCTMCImplementationPreprocessed {
	private NativeSystemOfODEs odes;

	public CPPODEsPreprocessed(NativeSystemOfODEs odes) {
		super();
		this.odes = odes;
	}

	public NativeSystemOfODEs getOdes() {
		return odes;
	}
}
