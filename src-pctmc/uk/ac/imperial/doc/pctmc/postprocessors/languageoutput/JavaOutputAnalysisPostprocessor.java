package uk.ac.imperial.doc.pctmc.postprocessors.languageoutput;

import java.util.List;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.PlotDescription;
import uk.ac.imperial.doc.pctmc.javaoutput.odeanalysis.JavaODEMethodPrinter;
import uk.ac.imperial.doc.pctmc.odeanalysis.PCTMCODEAnalysis;
import uk.ac.imperial.doc.pctmc.utils.PCTMCOptions;

public class JavaOutputAnalysisPostprocessor extends LanguageOutputPostprocessor{

	public JavaOutputAnalysisPostprocessor(){
		super(PCTMCOptions.javaFolder);
	}
	
	@Override
	public void postprocessAnalysis(Constants constants,
			AbstractPCTMCAnalysis analysis,
			List<PlotDescription> plotDescriptions) {
		if (analysis instanceof PCTMCODEAnalysis){
			PCTMCODEAnalysis asODEs = (PCTMCODEAnalysis) analysis; 
			postprocessODEAnalysis(constants, asODEs);
		}
	}
	
	private void postprocessODEAnalysis(Constants constants, PCTMCODEAnalysis analysis){
		JavaODEMethodPrinter printer = new JavaODEMethodPrinter(constants, analysis.getMomentIndex(), analysis.getGeneralExpectationIndex());
		analysis.getOdeMethod().accept(printer);
		String code = printer.toString(); 
		String filename = getAnalysisFolder() + "/" + JavaODEMethodPrinter.GENERATEDCLASSNAME + ".java";
		writeFile(filename, code, "Writing Java ODE method in file " + filename);
	}

}
