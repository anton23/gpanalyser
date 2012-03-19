package uk.ac.imperial.doc.gpa.plain.expressions;

import uk.ac.imperial.doc.gpa.plain.representation.Transaction;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.IExpressionVisitor;


public class TransactionExpression extends AbstractExpression{
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 0;
		result = prime * result
				+ ((transaction == null) ? 0 : transaction.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		TransactionExpression other = (TransactionExpression) obj;
		if (transaction == null) {
			if (other.transaction != null)
				return false;
		} else if (!transaction.equals(other.transaction))
			return false;
		return true;
	}

	Transaction transaction;

	@Override
	public void accept(IExpressionVisitor v) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public String toString() {
		return transaction.toString(); 
	}

	public TransactionExpression(Transaction transaction) {
		super();
		this.transaction = transaction;
	}
	
	

}
