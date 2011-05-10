package uk.ac.imperial.doc.pctmc.matlaboutput.analysis;

import java.util.Map;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.javaoutput.statements.AbstractExpressionEvaluator;
import uk.ac.imperial.doc.jexpressions.javaoutput.statements.JavaStatementPrinter;
import uk.ac.imperial.doc.jexpressions.statements.AbstractStatement;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.statements.odeanalysis.EvaluatorMethod;

import com.google.common.collect.BiMap;

/**
 * Java printer for expression evaluator methods.
 * @author Anton Stefanek
 *
 */
public class MatlabMethodPrinter {
	
	
	private Constants constants;
	private BiMap<CombinedPopulationProduct, Integer> combinedMomentsIndex;
	private Map<AbstractExpression,Integer> generalExpectationIndex;
	
	

	
	public MatlabMethodPrinter(Constants constants,
			BiMap<CombinedPopulationProduct, Integer> combinedMomentsIndex,
			Map<AbstractExpression, Integer> generalExpectationIndex) {
		super();
		this.constants = constants;
		this.combinedMomentsIndex = combinedMomentsIndex;
		this.generalExpectationIndex = generalExpectationIndex;
	}




	public String printEvaluatorMethod(EvaluatorMethod method,String className){
		StringBuilder ret = new StringBuilder();
		ret.append("import " + AbstractExpressionEvaluator.class.getName()
				+ ";\n");
		ret.append("public class " + className + " extends "
				+ AbstractExpressionEvaluator.class.getName() + "{\n");
		ret.append("    public int getNumberOfExpressions(){\n");
		ret.append("      return " + method.getNumberOfExpressions() + ";\n");
		ret.append("    }\n");
		
		ret.append("    public double[] update(double[] values,double t, double[] oldValues, double stepSize){\n");
		for (AbstractStatement s:method.getBody()){
			JavaStatementPrinter printer = new JavaStatementPrinter(new MatlabCombinedProductBasedExpressionPrinterFactory(constants, combinedMomentsIndex, generalExpectationIndex, "values"));
			s.accept(printer); 
			ret.append("    " + printer+"\n");
		}
		ret.append("    return ret;\n");
		ret.append("}\n}");
		return ret.toString(); 
	}
}
