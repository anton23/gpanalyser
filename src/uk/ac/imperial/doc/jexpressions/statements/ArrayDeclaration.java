package uk.ac.imperial.doc.jexpressions.statements;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;

public class ArrayDeclaration extends AbstractStatement {
	
	private String type; 
	private String array; 
	private AbstractExpression size; 

	
	
	
	public ArrayDeclaration(String type, String array, AbstractExpression size) {
		super();
		this.type = type;
		this.array = array;
		this.size = size;
	}

	public String getType() {
		return type;
	}

	public String getArray() {
		return array;
	}

	public AbstractExpression getSize() {
		return size;
	}

	@Override
	public void accept(IStatementVisitor v) {
		v.visit(this); 
	}

	@Override
	public String toString() {
		return type + "[] " + array + " = new " + type + "[" + size.toString() + "];"; 
	}

}
