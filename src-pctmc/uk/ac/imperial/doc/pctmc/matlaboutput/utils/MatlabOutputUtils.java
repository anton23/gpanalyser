package uk.ac.imperial.doc.pctmc.matlaboutput.utils;

import uk.ac.imperial.doc.pctmc.utils.FileUtils;
import uk.ac.imperial.doc.pctmc.utils.PCTMCLogging;

public class MatlabOutputUtils {
	public static int getMatlabIndex(int i){
		return i+1;
	}
	
	
	public static void writeODEMain(String folder){
		String code = "function [t,y] = main(odesF,y,stopTime,stepSize,param)\n" +
			   "    tspan = [0.0:stepSize:stopTime];\n"+
			   "    options = odeset('RelTol', 1e-3, 'AbsTol', 1e-3);\n"+
		       "    [t,y] = ode45(@my_odes,tspan,y,odeset);\n" + 
		       "    function dydt = my_odes(t,y)\n"+
		       "       dydt = odesF(t,y,param);\n"+  
		       "    end\n"+
		       "end\n";
		String fileName = folder+"/main.m"; 
		PCTMCLogging.debug("Writing main ODE MATLAB function in file " + fileName);
		FileUtils.writeGeneralFile(code, fileName);
	}
	
	public static void writeEvaluate(String folder){
		String code = "function e = evaluate(t,y,evaluator,param)\n"+
//					  "  numExp = size(evaluator(y(1,:),0.0,param));\n"+ 
//					  "   e = zeros(size(t),numExp);\n"+ 
					  "   for i = 1:size(t)\n"+
					  "     e(i,:) = evaluator(y(i,:),t(i),param);\n"+			  
					  "   end\n"+
					  "end\n";
		String fileName = folder+"/evaluate.m";
		PCTMCLogging.debug("Writing evaluate MATLAB function in file " + fileName);
		FileUtils.writeGeneralFile(code, fileName);		
	}
}
