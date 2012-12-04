package uk.ac.imperial.doc.pctmc.condor;

import java.io.File;
import java.util.List;
import java.util.Map;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
import uk.ac.imperial.doc.pctmc.experiments.iterate.PCTMCIterate;
import uk.ac.imperial.doc.pctmc.experiments.iterate.PlotAtDescription;
import uk.ac.imperial.doc.pctmc.interpreter.PCTMCFileRepresentation;
import uk.ac.imperial.doc.pctmc.utils.FileUtils;
import uk.ac.imperial.doc.pctmc.utils.PCTMCLogging;
import uk.ac.imperial.doc.pctmc.utils.PCTMCOptions;

public class CondorGenerator extends CondorBase {
	
	public CondorGenerator(PCTMCFileRepresentation fileRepresentation,
			String file, String options) {
		super(fileRepresentation, file, options);
	}

	protected String generatePartInput(PCTMCIterate part) {
		StringBuilder out = new StringBuilder();
		out.append(constants.toString());
		out.append("\n");
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
		
		String cmd = getCondorCommand();
		FileUtils.writeGeneralFile(cmd, file + ".cmd");
	}
	
	protected String getCondorCommand() {
		StringBuilder out = new StringBuilder();
		String jar = new File(CondorGenerator.class.getProtectionDomain().getCodeSource().getLocation().getPath()).toString();
		out.append("universe = Java\n" + 
				   "executable = " + jar + "\n" +
				   "jar_files = " + jar + "\n" +
				   "should_transfer_files = YES\n" +
				   "when_to_transfer_output = ON_EXIT\n" +
				   "output = " + file + ".output$(Process)\n" +
				   "error = " + file + ".error$(Process)\n" +
				   "input = " + file + "$(Process)\n" +
				   "transfer_output_files = .\n" +
				   "log = test.log\n" +
				   "arguments = uk.ac.imperial.doc.gpa.GPAPMain -noGUI " + options + " " + file + "$(Process)\n" +
				   "queue " + PCTMCOptions.condor_parts +"\n");
		return out.toString();
				   
	}
	
	
}
