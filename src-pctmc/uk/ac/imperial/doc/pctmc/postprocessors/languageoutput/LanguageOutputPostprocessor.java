package uk.ac.imperial.doc.pctmc.postprocessors.languageoutput;

import uk.ac.imperial.doc.pctmc.analysis.PCTMCAnalysisPostprocessor;
import uk.ac.imperial.doc.pctmc.utils.FileUtils;
import uk.ac.imperial.doc.pctmc.utils.PCTMCLogging;

public abstract class LanguageOutputPostprocessor implements PCTMCAnalysisPostprocessor{
	
	protected String baseFolder; 
	
	protected LanguageOutputPostprocessor(String baseFolder){
		this.baseFolder = baseFolder; 
	}
	
	protected int analysisCounter = 0;
	
	protected void writeFile(String filename, String contents, String message){
		PCTMCLogging.debug(message);
		FileUtils.writeGeneralFile(contents, filename);
	}
	
	protected String getAnalysisFolder(){
		return baseFolder + "/" +"analysis"+analysisCounter;
	}
	
	
	
}
