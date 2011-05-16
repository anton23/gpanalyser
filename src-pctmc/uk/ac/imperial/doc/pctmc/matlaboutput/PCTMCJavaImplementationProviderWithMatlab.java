package uk.ac.imperial.doc.pctmc.matlaboutput;

import java.util.Map;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ZeroExpression;
import uk.ac.imperial.doc.jexpressions.javaoutput.statements.AbstractExpressionEvaluator;
import uk.ac.imperial.doc.jexpressions.matlaboutput.MatlabPrinterWithConstants;
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.PlotExpression;
import uk.ac.imperial.doc.pctmc.experiments.iterate.PCTMCIterate;
import uk.ac.imperial.doc.pctmc.experiments.iterate.PlotAtDescription;
import uk.ac.imperial.doc.pctmc.experiments.iterate.PlotConstraint;
import uk.ac.imperial.doc.pctmc.experiments.iterate.RangeSpecification;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.implementation.PCTMCImplementationPreprocessed;
import uk.ac.imperial.doc.pctmc.implementation.PCTMCImplementationProvider;
import uk.ac.imperial.doc.pctmc.javaoutput.PCTMCJavaImplementationProvider;
import uk.ac.imperial.doc.pctmc.matlaboutput.analysis.MatlabMethodPrinter;
import uk.ac.imperial.doc.pctmc.matlaboutput.odeanalysis.MatlabODEMethodPrinter;
import uk.ac.imperial.doc.pctmc.matlaboutput.utils.MatlabOutputUtils;
import uk.ac.imperial.doc.pctmc.odeanalysis.PCTMCODEAnalysis;
import uk.ac.imperial.doc.pctmc.odeanalysis.utils.SystemOfODEs;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;
import uk.ac.imperial.doc.pctmc.representation.State;
import uk.ac.imperial.doc.pctmc.statements.odeanalysis.EvaluatorMethod;
import uk.ac.imperial.doc.pctmc.statements.odeanalysis.ODEMethod;
import uk.ac.imperial.doc.pctmc.utils.FileUtils;
import uk.ac.imperial.doc.pctmc.utils.PCTMCLogging;
import uk.ac.imperial.doc.pctmc.utils.PCTMCOptions;

import com.google.common.collect.BiMap;
import com.google.common.collect.Lists;

public class PCTMCJavaImplementationProviderWithMatlab implements PCTMCImplementationProvider{
	
	PCTMCJavaImplementationProvider javaImplementation;
	
	Constants constants; 
	
	PCTMC pctmc; 
	
	
	public PCTMCJavaImplementationProviderWithMatlab(Constants constants,PCTMC pctmc){
		this.constants = constants; 
		this.pctmc = pctmc;
		String code = getConstantInitialiser(constants); 
		String fileName = PCTMCOptions.matlabFolder + "/" + getConstantsName + ".m";
		PCTMCLogging.debug("Writing constants initialiser function in file " + fileName);
		FileUtils.writeGeneralFile(code, fileName);
		javaImplementation = new PCTMCJavaImplementationProvider(); 
		
		
	}
	
	private String getInitialValuesName = "getInitialValues"; 

	private String getInitialValues(PCTMC pctmc, ODEMethod odes,BiMap<CombinedPopulationProduct, Integer> combinedMomentsIndex){
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
					if (!(v instanceof ZeroExpression)) zero = false; 
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
	
	private int analysis = 0; 
	private int command = 0; 
	
	@Override
	public AbstractExpressionEvaluator getEvaluatorImplementation(
			EvaluatorMethod method, String className, Constants constants,
			BiMap<CombinedPopulationProduct, Integer> combinedMomentsIndex,
			Map<AbstractExpression, Integer> generalExpectationIndex) {
		String suffix = "_"+command;
		String analysisFolder = "analysis"+analysis; 
		String folder = PCTMCOptions.matlabFolder + "/" +analysisFolder;
		writeEvaluatorMethod(method, folder, suffix, constants, combinedMomentsIndex, generalExpectationIndex);
		command++;
		return javaImplementation.getEvaluatorImplementation(method, className, constants, combinedMomentsIndex, generalExpectationIndex);
	}
	
	private void writeEvaluatorMethod(EvaluatorMethod method, String folder,String suffix, Constants constants,
			BiMap<CombinedPopulationProduct, Integer> combinedMomentsIndex,
			Map<AbstractExpression, Integer> generalExpectationIndex){
		MatlabMethodPrinter printer = new MatlabMethodPrinter(constants, combinedMomentsIndex, generalExpectationIndex);
		String code = printer.printEvaluatorMethod(method, suffix);	
		String fileName = folder +"/"+ MatlabMethodPrinter.evaluatorName + suffix + ".m";
		PCTMCLogging.debug("Writing evaluator MATLAB function in file " + fileName);
		FileUtils.writeGeneralFile(code, fileName);
	}

	@Override
	public PCTMCImplementationPreprocessed getPreprocessedODEImplementation(
			ODEMethod method, Constants constants,
			BiMap<CombinedPopulationProduct, Integer> combinedMomentsIndex,
			Map<AbstractExpression, Integer> generalExpectationIndex) {
		analysis++;
		PCTMCLogging.info("Generating MATLAB source files " + PCTMCOptions.matlabFolder+"/analysis"+analysis+"/*.m");
        String analysisFolder = "analysis"+analysis;
		String fileName = PCTMCOptions.matlabFolder + "/" +analysisFolder +"/"+ MatlabODEMethodPrinter.ODESNAME + ".m";
		writeODEFile(method, constants, combinedMomentsIndex, generalExpectationIndex, fileName); 
		
		fileName = PCTMCOptions.matlabFolder + "/" +analysisFolder +"/"+ getInitialValuesName + ".m";
		writeInitialValuesFile(method, constants, combinedMomentsIndex, generalExpectationIndex, fileName);
		
		MatlabOutputUtils.writeODEMain(PCTMCOptions.matlabFolder+"/"+analysisFolder);
		MatlabOutputUtils.writeEvaluate(PCTMCOptions.matlabFolder+"/"+analysisFolder);
		MatlabOutputUtils.writePEPAFunctions(PCTMCOptions.matlabFolder+"/"+analysisFolder);
		return javaImplementation.getPreprocessedODEImplementation(method, constants, combinedMomentsIndex, generalExpectationIndex);
	}
	
	
	private void writeODEFile(ODEMethod method, Constants constants,BiMap<CombinedPopulationProduct, Integer> combinedMomentsIndex,
			Map<AbstractExpression, Integer> generalExpectationIndex,String fileName){
		MatlabODEMethodPrinter printer = new MatlabODEMethodPrinter(constants, combinedMomentsIndex,generalExpectationIndex);
        method.accept(printer);
        String code = printer.toString(); 
		PCTMCLogging.debug("Writing moment ODE MATLAB function in file " + fileName);
		FileUtils.writeGeneralFile(code, fileName);
	}
	
	private void writeInitialValuesFile(ODEMethod method, Constants constants,BiMap<CombinedPopulationProduct, Integer> combinedMomentsIndex,
			Map<AbstractExpression, Integer> generalExpectationIndex,String fileName){
		String code = getInitialValues(pctmc, method, combinedMomentsIndex);
		PCTMCLogging.debug("Writing ODE initial values MATLAB function in file " + fileName);
		FileUtils.writeGeneralFile(code, fileName);
	}
	
	@Override
	public SystemOfODEs getSystemOfODEsImplementation(ODEMethod method,
			String className, Constants constants,
			BiMap<CombinedPopulationProduct, Integer> combinedMomentsIndex,
			Map<AbstractExpression, Integer> generalExpectationIndex) {
        
		return javaImplementation.getSystemOfODEsImplementation(method, className, constants, combinedMomentsIndex, generalExpectationIndex);
	}

	@Override
	public double[][] runODEAnalysis(
			PCTMCImplementationPreprocessed preprocessed, double[] initial,
			double stopTime, double stepSize, int density, Constants constants) {
		return javaImplementation.runODEAnalysis(preprocessed, initial, stopTime, stepSize, density, constants);
	}

	int iterateIndex = 0; 
	
	public void writePCTMCIterateFile(PCTMCIterate iterate, Constants constants){
		iterateIndex++;
		String folder = PCTMCOptions.matlabFolder+"/iterate_"+iterateIndex;
		AbstractPCTMCAnalysis analysis = iterate.getAnalysis(); 
		if (analysis instanceof PCTMCODEAnalysis){
			PCTMCODEAnalysis asODEAnalysis = (PCTMCODEAnalysis)analysis;
			PCTMC pctmc = analysis.getPCTMC(); 
			BiMap<CombinedPopulationProduct, Integer> momentIndex = analysis.getMomentIndex(); 
			BiMap<AbstractExpression, Integer> generalExpectationIndex = analysis.getGeneralExpectationIndex();
			writeODEFile(asODEAnalysis.getOdeMethod(), constants, momentIndex, generalExpectationIndex, folder+"/"+"odes.m");
			MatlabOutputUtils.writeEvaluate(folder);
			MatlabOutputUtils.writeODEMain(folder);
			MatlabOutputUtils.writePEPAFunctions(folder);
		
			writeInitialValuesFile(asODEAnalysis.getOdeMethod(), constants, momentIndex, generalExpectationIndex, folder+"/"+getInitialValuesName+".m");
			PlotAtDescription minSpecification = iterate.getMinSpecification();
			EvaluatorMethod minSpecEvaluatorMethod = asODEAnalysis.getEvaluatorMethod(minSpecification.getPlotExpressions(),constants);
			writeEvaluatorMethod(minSpecEvaluatorMethod, folder, "_minSpecification", constants, momentIndex, generalExpectationIndex);
			writeIterateMainFile(iterate, folder);
			
		}
	}
	
	private String globalOptimName = "gobal_optim";
	
	private void writeIterateMainFile(PCTMCIterate iterate,String folder){
		StringBuilder out = new StringBuilder(); 
		out.append("function [x,ode_calls]="+globalOptimName+"()\n");
		out.append("param = "+getConstantsName+"();\n");
		out.append("stopTime = "+ iterate.getAnalysis().getStopTime() +";\n");
		out.append("stepSize = "+ iterate.getAnalysis().getStepSize() +";\n");
		for (RangeSpecification r:iterate.getMinRanges()){
			out.append("start"+r.getConstant()+" = param." + r.getConstant() + ";\n");
		}
		out.append("startx = [");
		boolean first = true; 
		for (RangeSpecification r:iterate.getMinRanges()){
			if (first) first = false; else out.append(",");
			out.append("start"+r.getConstant());
		}
		out.append("];\n");
		out.append("lb=[");
		first = true; 
		for (RangeSpecification r:iterate.getMinRanges()){
			if (first) first = false; else out.append(",");
			out.append(r.getFrom());
		}		
		out.append("];\n");
		out.append("ub=[");
		first = true; 
		for (RangeSpecification r:iterate.getMinRanges()){
			if (first) first = false; else out.append(",");
			out.append(r.getTo());
		}
		out.append("];\n");
		out.append("rewardAtTime = " + iterate.getMinSpecification().getTime()+"\n");
		out.append("rewardTimeIndex = " + ((int)Math.floor(iterate.getMinSpecification().getTime()/iterate.getAnalysis().getStepSize())+1)+";\n");
		
		int i = 0;
		for (PlotConstraint c:iterate.getMinSpecification().getConstraints()){
			out.append("threshold"+i+" = " + c.getMinValue() + ";\n");
			out.append("atTime"+i+" = " + c.getAtTime() + ";\n");
			out.append("timeIndex"+i+" = " + ((int)Math.floor(c.getAtTime()/iterate.getAnalysis().getStepSize())+1)+";\n"); 
			i++;
		}
		out.append("options optimset('Algorithm','interior-point');\n");
		out.append("options optimset(options,'AlwaysHonorConstraints','None');\n");
		
		
		
		FileUtils.writeGeneralFile(out.toString(), folder+"/global_optim.m");
	}
}
