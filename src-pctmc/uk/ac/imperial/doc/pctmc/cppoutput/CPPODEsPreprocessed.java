package uk.ac.imperial.doc.pctmc.cppoutput;

import uk.ac.imperial.doc.pctmc.cppoutput.utils.NativeSystemOfODEs;
import uk.ac.imperial.doc.pctmc.implementation.PCTMCImplementationPreprocessed;
import uk.ac.imperial.doc.pctmc.odeanalysis.utils.ISystemOfODEs;

public class CPPODEsPreprocessed extends PCTMCImplementationPreprocessed {
	private final NativeSystemOfODEs odes;

	public CPPODEsPreprocessed(NativeSystemOfODEs odes) {
		super();
		this.odes = odes;
	}

	public ISystemOfODEs getOdes() {
		return odes;
	}
}
