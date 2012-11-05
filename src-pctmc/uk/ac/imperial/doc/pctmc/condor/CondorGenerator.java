package uk.ac.imperial.doc.pctmc.condor;

import java.util.List;
import java.util.Map;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
import uk.ac.imperial.doc.pctmc.experiments.iterate.PCTMCExperiment;
import uk.ac.imperial.doc.pctmc.experiments.iterate.PCTMCIterate;
import uk.ac.imperial.doc.pctmc.experiments.iterate.PlotAtDescription;
import uk.ac.imperial.doc.pctmc.interpreter.PCTMCFileRepresentation;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;
import uk.ac.imperial.doc.pctmc.utils.FileUtils;
import uk.ac.imperial.doc.pctmc.utils.PCTMCLogging;
import uk.ac.imperial.doc.pctmc.utils.PCTMCOptions;

public class CondorGenerator {
	
	protected Constants constants;
	protected PCTMC pctmc;
	protected Map<ExpressionVariable, AbstractExpression> unfoldedVariables;
	protected PCTMCIterate iterate;
	protected String file;

	public CondorGenerator(PCTMCFileRepresentation fileRepresentation, String file) {
		this.constants = fileRepresentation.getConstants();
		this.pctmc = fileRepresentation.getPctmc();
		this.unfoldedVariables = fileRepresentation.getUnfoldedVariables();
		this.file = file;
		for (PCTMCExperiment e : fileRepresentation.getExperiments()) {
			if (e instanceof PCTMCIterate) {
				iterate = (PCTMCIterate) e;
			}
		}
		if (iterate == null) {
			throw new AssertionError("At least one iterate has to be present!");
		}
}
	
	protected String generatePartInput(PCTMCIterate part) {
		StringBuilder out = new StringBuilder();
		out.append(constants.toString());

		for (Map.Entry<ExpressionVariable, AbstractExpression> e:unfoldedVariables.entrySet()) {
			out.append(e.getKey().toString());
			out.append(" = ");
			out.append(e.getValue().toString());
			out.append(";\n");
		}		
		out.append("\n");
		out.append(pctmc.toString());
		out.append("\n");
		out.append(part.toString());		
		return out.toString();
	}
	
	public void generate() {
		PCTMCLogging.info("Writing condor input files");
		List<PCTMCIterate> parts = iterate.split(PCTMCOptions.condor_parts);
		int i = 0;
		
		for (PCTMCIterate part : parts) {
			for (PlotAtDescription p : part.getPlots()) {
				if (p.getFilename() != null && !p.getFilename().isEmpty()) {
					p.setFilename(p.getFilename()+i);
				}
			}
			String input = generatePartInput(part);
			FileUtils.writeGeneralFile(input, file + i);
			i++;
		}
	}
	

}
