package uk.ac.imperial.doc.pctmc;

import java.util.List;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.PlotDescription;

public interface PCTMCAnalysisPostprocessor {
	public void postprocessAnalysis(Constants constants, AbstractPCTMCAnalysis analysis,List<PlotDescription> plotDescriptions);
}
