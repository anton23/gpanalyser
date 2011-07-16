package uk.ac.imperial.doc.pctmc;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.pctmc.experiments.iterate.PCTMCIterate;

public interface PCTMCIteratePostprocessor {
	public void postprocessIterate(PCTMCIterate iterate, Constants constants);
}
