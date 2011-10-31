package uk.ac.imperial.doc.jexpressions.expanded;

import java.util.HashMap;
import java.util.Map;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.IExpressionVisitor;

import com.google.common.collect.Multiset;

public class ExpandedExpression extends AbstractExpression{
	
	private Polynomial numerator;
	private Polynomial denominator;
	
	public ExpandedExpression(Polynomial numerator, Polynomial denominator) {
		super();
		this.numerator = numerator;
		this.denominator = denominator;
	}
	
	public boolean isNumber(){
		return false;
	}
	
	public Double numericalValue(){
		return null;
	}

	@Override
	public void accept(IExpressionVisitor v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean equals(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}
}