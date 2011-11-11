package uk.ac.imperial.doc.pctmc.postprocessors.languageoutput;


public class MatlabIteratePostprocessor {
	
//	private String globalOptimName = "gobal_optim";
	
//	private String getConstantsName = "getConstants"; 
	/* 
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
                if (minSpecification!=null){
                        EvaluatorMethod minSpecEvaluatorMethod = asODEAnalysis.getEvaluatorMethod(minSpecification.getPlotExpressions(),constants);
                        writeEvaluatorMethod(minSpecEvaluatorMethod, folder, "_minSpecification", constants, momentIndex, generalExpectationIndex);
                        writeIterateMainFile(iterate, folder);
                }

        }
}*/
	
/*	private void writeIterateMainFile(PCTMCIterate iterate,String folder, double stopTime, double stepSize){
		StringBuilder out = new StringBuilder(); 
		out.append("function [x,exitflag,output,solutions]="+globalOptimName+"()\n");
		out.append("   param = "+getConstantsName+"();\n");
		out.append("   stopTime = "+ stopTime +";\n");
		out.append("   stepSize = "+ stepSize+";\n\n");
		for (RangeSpecification r:iterate.getMinRanges()){
			out.append("   start"+r.getConstant()+" = param." + r.getConstant() + ";\n");
		}
		out.append("   startx = [");
		boolean first = true; 
		for (RangeSpecification r:iterate.getMinRanges()){
			if (first) first = false; else out.append(",");
			out.append("start"+r.getConstant());
		}
		out.append("];\n\n");
		out.append("   lb=[");
		first = true; 
		for (RangeSpecification r:iterate.getMinRanges()){
			if (first) first = false; else out.append(",");
			out.append(r.getFrom());
		}		
		out.append("];\n");
		out.append("   ub=[");
		first = true; 
		for (RangeSpecification r:iterate.getMinRanges()){
			if (first) first = false; else out.append(",");
			out.append(r.getTo());
		}
		out.append("];\n\n");
		out.append("   rewardAtTime = " + iterate.getMinSpecification().getTime()+";\n");
		out.append("   rewardTimeIndex = " + ((int)Math.floor(iterate.getMinSpecification().getTime()/stepSize)+1)+";\n\n");
		out.append("   reward_evaluator = @evaluator_minSpecification;\n\n" );
		out.append("   ode_calls = 0;\n");
		
		int i = 0;
		for (PlotConstraint c:iterate.getMinSpecification().getConstraints()){
			out.append("   threshold"+i+" = " + c.getMinValue() + ";\n");
			out.append("   atTime"+i+" = " + c.getAtTime() + ";\n");
			out.append("   timeIndex"+i+" = " + ((int)Math.floor(c.getAtTime()/stepSize)+1)+";\n"); 
			i++;
		}
		out.append("\n   options = optimset('Algorithm','interior-point');\n");
		out.append("   options = optimset(options,'AlwaysHonorConstraints','None');\n\n");
		
		out.append("   problem=createOptimProblem('fmincon',...\n'objective',@cost,'nonlcon',@nlcon,'x0',startx,'options',options);\n");
		out.append("   problem.lb = lb;\n");
		out.append("   problem.ub = ub;\n\n");
		out.append("   gs = GlobalSearch('Display','iter');\n\n");
		out.append("   [x,fval,exitflag,output,solutions] = run(gs,problem);\n\n");
		
		
		out.append("   function c = cost(x)\n");
		out.append("      [t,y] = solve_odes(x);\n");
		out.append("      tmp = reward_evaluator(y(rewardTimeIndex,:),rewardTimeIndex*stepSize,param);\n");
		out.append("      c = tmp(1);\n");
		out.append("   end\n\n");
		
		out.append("   function [c,ceq] = nlcon(x)\n");
		out.append("      ceq = [];\n");
		out.append("      [t,y] = solve_odes(x);\n");
	
		out.append("      c = zeros("+iterate.getMinSpecification().getConstraints().size()+",1);\n");
 
		for (i = 0; i< iterate.getMinSpecification().getConstraints().size(); i++){
			out.append("     tmp = reward_evaluator(y(timeIndex"+i+",:),timeIndex"+i+"*stepSize,param);\n");
			out.append("     c("+(i+1)+") = threshold"+i+" - tmp(" + (i+2) + ");\n");
		}
		out.append("   end\n\n");

		out.append("   function [t,y] = solve_odes(x)\n");
		out.append("      ode_calls = ode_calls+1;\n");
		out.append("      updateParam(x);\n");
		out.append("      init = getInitialValues(param);\n");
		out.append("      [t,y] = main(@odes,init,stopTime,stepSize,param);\n");
		out.append("   end\n\n");
		
		out.append("   function updateParam(x)\n");
		i = 1; 
		for (RangeSpecification r:iterate.getMinRanges()){
			out.append("      param."+r.getConstant() + " = x(" + i + ");\n");
			i++;
		}
		out.append("   end\n");
		out.append("end\n");
		
		FileUtils.writeGeneralFile(out.toString(), folder+"/global_optim.m");
	}*/
}
