package uk.ac.imperial.doc.pctmc.postprocessors.languageoutput;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.jexpressions.matlaboutput.MatlabPrinterWithConstants;
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.PlotDescription;
import uk.ac.imperial.doc.pctmc.compare.PCTMCCompareAnalysis;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.matlaboutput.analysis.MatlabMethodPrinter;
import uk.ac.imperial.doc.pctmc.matlaboutput.odeanalysis.MatlabODEMethodPrinter;
import uk.ac.imperial.doc.pctmc.matlaboutput.utils.MatlabOutputUtils;
import uk.ac.imperial.doc.pctmc.odeanalysis.PCTMCODEAnalysis;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.NumericalPostprocessor;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;
import uk.ac.imperial.doc.pctmc.representation.State;
import uk.ac.imperial.doc.pctmc.statements.odeanalysis.ODEMethod;
import uk.ac.imperial.doc.pctmc.utils.PCTMCOptions;

import com.google.common.collect.BiMap;

public class MatlabAnalysisPostprocessor extends LanguageOutputPostprocessor{

	public MatlabAnalysisPostprocessor(){
		super(PCTMCOptions.matlabFolder);
	}
	
	
	@Override
	public void postprocessAnalysis(Constants constants, AbstractPCTMCAnalysis analysis,
			List<PlotDescription> plotDescriptions) {
		processConstants(constants);
		if (analysis instanceof PCTMCODEAnalysis){
			processODEAnalysis((PCTMCODEAnalysis) analysis, constants);
		}
		if (!(analysis instanceof PCTMCCompareAnalysis))processPlotDescriptions(constants, analysis, plotDescriptions);
		analysisCounter++;
	}
	
	private void processPlotDescriptions(Constants constants, AbstractPCTMCAnalysis analysis, List<PlotDescription> plotDescriptions){
		String analysisFolder = getAnalysisFolder();
		int i = 0; 
		for (PlotDescription plotDescription:plotDescriptions){
			MatlabMethodPrinter printer = new MatlabMethodPrinter(constants, analysis.getMomentIndex(), analysis.getGeneralExpectationIndex());
			List<AbstractExpression> plotExpressionList = new LinkedList<AbstractExpression>(); 
			for (AbstractExpression e:plotDescription.getExpressions()){
				plotExpressionList.add(e);
			}
			String suffix = "_"+i;
			String fileName = analysisFolder +"/"+ MatlabMethodPrinter.evaluatorName + suffix + ".m";
 
			writeFile(fileName, 
					  printer.printEvaluatorMethod(NumericalPostprocessor.getEvaluatorMethod(plotExpressionList, constants), suffix),				
					  "Writing evaluator MATLAB function in file " + fileName);
			i++;
		}
		MatlabOutputUtils.writeEvaluate(analysisFolder);
		MatlabOutputUtils.writePEPAFunctions(analysisFolder);
	}

	
	private void processConstants(Constants constants){
		String filenameConstants = PCTMCOptions.matlabFolder + "/" + getConstantsName + ".m";
		writeFile(filenameConstants,
				  getConstantInitialiser(constants), 
				  "Writing constants initialiser function in file " + filenameConstants);
	}
	
	private void processODEAnalysis(PCTMCODEAnalysis analysis, Constants constants){
		String analysisFolder = getAnalysisFolder();
		
		String filenameInitial = analysisFolder +"/"+ getInitialValuesName + ".m";
		writeFile(filenameInitial,
				  getInitialValues(analysis.getPCTMC(), constants, analysis.getOdeMethod(), analysis.getMomentIndex()),
		          "Writing ODE initial values in file " + filenameInitial);

		String filenameODEs = analysisFolder +"/odes.m";
		writeFile(filenameODEs,
				  getODEMethodcode(analysis, constants), 
				  "Writing moment ODE MATLAB function in file " + filenameODEs);
	
		MatlabOutputUtils.writeODEMain(analysisFolder);		
	}
	
	private String getODEMethodcode(PCTMCODEAnalysis analysis, Constants constants){
		MatlabODEMethodPrinter printer = new MatlabODEMethodPrinter(
				constants, analysis.getMomentIndex(),analysis.getGeneralExpectationIndex());
        analysis.getOdeMethod().accept(printer);
		return printer.toString();
	}
	
	

	
	private String getInitialValuesName = "getInitialValues";
	
	private String getInitialValues(PCTMC pctmc, Constants constants,ODEMethod odes,BiMap<CombinedPopulationProduct, Integer> combinedMomentsIndex){
		StringBuilder out = new StringBuilder(); 
		out.append("function y = " + getInitialValuesName+"(" + MatlabPrinterWithConstants.param+")\n");
		out.append("   y = zeros("+combinedMomentsIndex.size()+",1);\n");
		for(Map.Entry<CombinedPopulationProduct, Integer> e:combinedMomentsIndex.entrySet()){
			out.append("   y("+MatlabOutputUtils.getMatlabIndex(e.getValue())+")=");
			if (!e.getKey().getAccumulatedProducts().isEmpty()){
				out.append("0;");
			} else {
				boolean first = true; 
				boolean zero = true; 
				StringBuilder rhs = new StringBuilder(); 
				for (Map.Entry<State, Integer> p:e.getKey().getNakedProduct().getProduct().entrySet()){
					AbstractExpression v = pctmc.getInitMap().get(p.getKey());
					if (!(v.equals(DoubleExpression.ZERO))) zero = false; 
					MatlabPrinterWithConstants printer = new MatlabPrinterWithConstants(constants); 
					v.accept(printer); 
					if (first) first = false; else rhs.append("*");
					rhs.append("("+printer.toString()+")^"+p.getValue());
				}
				if (!zero) out.append(rhs.toString()+";");
				else out.append("0;");
			}
			out.append("\t\t%"+e.getKey().toString()+"\n");
		}
		out.append("end\n");		
		
		
		return out.toString(); 
	}

		
	private String getConstantsName = "getConstants";
	
	
	private String getConstantInitialiser(Constants constants){
		StringBuilder out = new StringBuilder(); 
		out.append("function param="+getConstantsName+"()\n");
		out.append("   param = struct(...\n");
		int i = 0; 
		int size = constants.getConstantsMap().entrySet().size();
		for (Map.Entry<String, Double> e:constants.getConstantsMap().entrySet()){
			out.append("   \'"+e.getKey()+"\'," + e.getValue() + (i<size-1?",":"")+"...\n");
			i++; 
		}			
		out.append("   );\n");
		out.append("end\n");
		return out.toString(); 
	}


}
