package uk.ac.imperial.doc.jexpressions.variables;

import java.util.LinkedHashMap;
import java.util.Map;

import uk.ac.imperial.doc.jexpressions.constants.visitors.ExpressionTransformerWithConstants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;


public class ExpressionVariableUnfolder extends ExpressionTransformerWithConstants 
implements IExpressionVariableVisitor{
	protected Map<ExpressionVariable, AbstractExpression> variables;

	public ExpressionVariableUnfolder(
			Map<ExpressionVariable, AbstractExpression> variables) {
		super();
		this.variables = variables;
	}

	@Override
	public void visit(ExpressionVariable e) {
		variables.get(e).accept(this);
	}	

	public Map<ExpressionVariable,AbstractExpression> unfoldVariables(){
		Map<ExpressionVariable,AbstractExpression> ret = new LinkedHashMap<ExpressionVariable, AbstractExpression>();
		for (Map.Entry<ExpressionVariable,AbstractExpression> e:variables.entrySet()){
			this.result = null; 
			e.getValue().accept(this);
			ret.put(e.getKey(), this.getResult());
		}
		return ret; 
	}
}
