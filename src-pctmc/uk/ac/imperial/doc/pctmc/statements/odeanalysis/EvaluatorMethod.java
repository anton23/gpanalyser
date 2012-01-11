package uk.ac.imperial.doc.pctmc.statements.odeanalysis;

import java.util.List;

import uk.ac.imperial.doc.jexpressions.statements.AbstractStatement;


public class EvaluatorMethod {
	
	
	private List<AbstractStatement> body;

	public List<AbstractStatement> getBody() {
		return body;
	} 
	
	private String returnArray;
	
	
	
	public String getReturnArray() {
		return returnArray;
	}

	public EvaluatorMethod(List<AbstractStatement> body, int numberOfExpressions, String returnArray) {
		super();
		this.body = body;
		this.returnArray = returnArray;
		this.numberOfExpressions = numberOfExpressions;
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
