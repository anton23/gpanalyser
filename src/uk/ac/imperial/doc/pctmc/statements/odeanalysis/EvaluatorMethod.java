package uk.ac.imperial.doc.pctmc.statements.odeanalysis;

import java.util.List;

import uk.ac.imperial.doc.jexpressions.statements.AbstractStatement;


public class EvaluatorMethod {
	
	
	private List<AbstractStatement> body;

	public List<AbstractStatement> getBody() {
		return body;
	} 
	
	public EvaluatorMethod(List<AbstractStatement> body, int numberOfExpressions) {
		super();
		this.body = body;
	}

	int numberOfExpressions;
	
	

	public int getNumberOfExpressions() {
		return numberOfExpressions;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString();
	}
	
	
}
