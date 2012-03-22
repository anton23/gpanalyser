package uk.ac.imperial.doc.jexpressions.javaoutput;

import java.util.HashSet;
import java.util.Set;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
import uk.ac.imperial.doc.jexpressions.variables.IExpressionVariableVisitor;

public class JavaExpressionPrinterWithVariables extends JavaPrinterWithConstants implements IExpressionVariableVisitor {

	protected Set<ExpressionVariable> variables;
	protected boolean expandVariables;
	
	public static String escapeName(String name) {
		return "_" + name;
	}
	
	public JavaExpressionPrinterWithVariables(Constants constants, boolean expandVariables) {
		super(constants);
		variables = new HashSet<ExpressionVariable>();
		this.expandVariables = expandVariables;
	}

	@Override
	public void visit(ExpressionVariable e) {
		if (expandVariables) {
			e.getUnfolded().accept(this);
		} else {
			output.append(escapeName(e.getName()));
			variables.add(e);
		}
	}

	public Set<ExpressionVariable> getVariables() {
		return variables;
	}
	
}
