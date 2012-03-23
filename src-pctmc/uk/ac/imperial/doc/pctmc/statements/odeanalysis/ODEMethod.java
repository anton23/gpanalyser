package uk.ac.imperial.doc.pctmc.statements.odeanalysis;

import java.util.Map;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.statements.AbstractStatement;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;

public class ODEMethod {
	
	protected Map<ExpressionVariable, AbstractExpression> variables;

	protected AbstractStatement[] body;

	public AbstractStatement[] getBody() {
		return body;
	}


	public ODEMethod(AbstractStatement[] body) {
		this.body = body;

	}
	
	public ODEMethod(AbstractStatement[] body, Map<ExpressionVariable, AbstractExpression> variables) {
		this(body);
		this.variables = variables;
	}

	public void accept(IODEMethodVisitor v) {
		if (v instanceof IODEMethodVisitor)
			((IODEMethodVisitor) v).visit(this);
	}

	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder();
		for (int i = 0; i < body.length; i++) {
			ret.append(body[i]);
			ret.append("\n");
		}
		return ret.toString();
	}

}
