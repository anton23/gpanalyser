package uk.ac.imperial.doc.jexpressions.expressions;

import java.io.Serializable;

/**
 * An abstract class for expressions. 
 * @author Anton Stefanek
 *
 */
public abstract class AbstractExpression implements Serializable {
	@Override
	public abstract int hashCode();

	@Override
	public abstract boolean equals(Object o);

	@Override
	public abstract String toString();
	
	public abstract void accept(IExpressionVisitor v);
}
