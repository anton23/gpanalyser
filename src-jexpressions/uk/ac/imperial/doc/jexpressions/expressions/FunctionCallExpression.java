package uk.ac.imperial.doc.jexpressions.expressions;

import java.util.List;

import uk.ac.imperial.doc.jexpressions.utils.ToStringUtils;


/**
 * An expression for calls to functions, such as 'foo(x,y,z)'.
 * @author Anton Stefanek
 *
 */
public class FunctionCallExpression extends AbstractExpression {
	
	private List<AbstractExpression> arguments; 
	private String name;
	

	private FunctionCallExpression(String name, List<AbstractExpression> arguments) {
		super();
		this.name = name;
		this.arguments = arguments;
	}
	
	public static AbstractExpression create(String name,List<AbstractExpression> arguments){
		if (name.equals("div")&&arguments.size()==2){
			return PEPADivExpression.create(arguments.get(0), arguments.get(1));
		}
		return new FunctionCallExpression(name, arguments);
	}

	@Override
	public void accept(IExpressionVisitor v) {
		v.visit(this); 

	}

	@Override
	public boolean equals(Object o) {
		if (this==o) return true; 
		if (!(o instanceof FunctionCallExpression)){
			return false; 
		}
		FunctionCallExpression asFun = (FunctionCallExpression)o; 
		return name.equals(asFun.getName())&&arguments.equals(asFun.getArguments());
	}

	public List<AbstractExpression> getArguments() {
		return arguments;
	}

	public String getName() {
		return name;
	}

	@Override
	public int hashCode() {
		return name.hashCode()*23 + arguments.hashCode(); 
	}

	@Override
	public String toString() {
		return name + "(" + ToStringUtils.iterableToSSV(arguments, ",") + ")";
	}

}
