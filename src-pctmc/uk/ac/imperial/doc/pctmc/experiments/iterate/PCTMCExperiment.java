package uk.ac.imperial.doc.pctmc.experiments.iterate;

import java.util.List;

import uk.ac.imperial.doc.jexpressions.constants.Constants;

public abstract class PCTMCExperiment {
	public abstract List<PlotAtDescription> getPlots();
	public abstract void prepare(Constants constants);
	public abstract void run(Constants constants);
}
