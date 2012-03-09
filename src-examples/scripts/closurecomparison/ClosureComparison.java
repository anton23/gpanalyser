package scripts.closurecomparison;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.imperial.doc.gpa.GPAPMain;
import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.experiments.iterate.RangeSpecification;
import uk.ac.imperial.doc.pctmc.interpreter.PCTMCFileRepresentation;
import uk.ac.imperial.doc.pctmc.interpreter.PCTMCInterpreter;
import uk.ac.imperial.doc.pctmc.interpreter.ParseException;
import uk.ac.imperial.doc.pctmc.odeanalysis.PCTMCODEAnalysis;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;
import uk.ac.imperial.doc.pctmc.representation.State;

public class ClosureComparison {
	
	final String modelFile = "src-examples/scripts/closurecomparison/models/clientServer.gpepa";
	
	PCTMCInterpreter interpreter = GPAPMain.createGPEPAInterpreter();
	
	// The evaluated model and used constants
	PCTMC pctmc;
	Constants constants;
	
	// Analyses to use for evaluation and expressions
	// for comparison
	List<PCTMCODEAnalysis> analyses;
	List<AbstractExpression> expressions;
	
	// Specification of the parameter space to explore
	List<RangeSpecification> ranges;
	
	double stopTime;
	double stepSize;
	
	private void loadAnalyses() {
		Map<String, Object> options = new HashMap<String, Object>();
		options.put("momentClosure", "AccumulatedMinClosure");
		options.put("maxOrder", 2);
		PCTMCODEAnalysis a = new PCTMCODEAnalysis(pctmc, options);
		AbstractPCTMCAnalysis.unfoldVariablesAndSetUsedProducts(a, new PlotDescription(expressions), )
	}
	
	private void loadModel() {
		try {
			PCTMCFileRepresentation fileRepresentation = interpreter.parsePCTMCFile(modelFile);
			pctmc = fileRepresentation.getPctmc();
			constants = fileRepresentation.getConstants();
			stopTime = constants.getConstantValue("stopTime");
			stepSize = constants.getConstantValue("stepSize");
			
			fileRepresentation.getUnfoldedVariables();
			
			for (State s : pctmc.getStateIndex().keySet()) {
				
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	private void runAnalyses() {
		for (PCTMCODEAnalysis analysis : analyses) {
			
		}
	}
	
	public static void main(String[] args) {
		
	}

}
