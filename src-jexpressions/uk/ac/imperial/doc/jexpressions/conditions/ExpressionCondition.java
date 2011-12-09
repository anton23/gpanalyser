package uk.ac.imperial.doc.jexpressions.conditions;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.visitors.ExpressionEvaluator;

public class ExpressionCondition {
	
	protected AbstractExpression left; 
	protected AbstractExpression right;
	
	protected ComparisonOperator operator;
	
	public ExpressionCondition(AbstractExpression left, ComparisonOperator operator, AbstractExpression right) {
		this.left = left;
		this.right = right;
	} 
	

	public boolean evaluate(ExpressionEvaluator evaluator) {
		left.accept(evaluator);
		double leftValue = evaluator.getResult();
		right.accept(evaluator);
		double rightValue = evaluator.getResult();
		return operator.compare(leftValue, rightValue);
	}
	
	@Override
	public String toString() {
		return left.toString() + operator.toString() + right.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 0;
		result = prime * result + ((left == null) ? 0 : left.hashCode());
		result = prime * result + ((right == null) ? 0 : right.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (getClass() != obj.getClass())
			return false;
		ExpressionCondition other = (ExpressionCondition) obj;
		if (left == null) {
			if (other.left != null)
				return false;
		} else if (!left.equals(other.left))
			return false;
		if (right == null) {
			if (other.right != null)
				return false;
		} else if (!right.equals(other.right))
			return false;
		return true;
	}


	public AbstractExpression getLeft() {
		return left;
	}


	public AbstractExpression getRight() {
		return right;
	}


	public ComparisonOperator getOperator() {
		return operator;
	}	
	
	
}
