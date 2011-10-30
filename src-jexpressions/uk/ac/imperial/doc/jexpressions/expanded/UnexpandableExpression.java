package uk.ac.imperial.doc.jexpressions.expanded;

import java.util.HashSet;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;

import com.google.common.collect.Multiset;

public class UnexpandableExpression extends ExpandedExpression {
	
	private AbstractExpression expression;
	
	public UnexpandableExpression(AbstractExpression expression) {
		super(new HashSet<Multiset<ExpandedExpression>>());
		this.expression = expression;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj){
			return true;
		}
		if (!(obj instanceof UnexpandableExpression)){
			return false;
		}
		UnexpandableExpression asUE = (UnexpandableExpression) obj;
		return expression.equals(asUE.getExpression());
	}

	public AbstractExpression getExpression() {
		return expression;
	}
}
