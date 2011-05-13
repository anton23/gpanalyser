package uk.ac.imperial.doc.jexpressions.matlaboutput;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DivDivMinExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DivExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DivMinExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.jexpressions.expressions.FunctionCallExpression;
import uk.ac.imperial.doc.jexpressions.expressions.IExpressionVisitor;
import uk.ac.imperial.doc.jexpressions.expressions.IntegerExpression;
import uk.ac.imperial.doc.jexpressions.expressions.MinExpression;
import uk.ac.imperial.doc.jexpressions.expressions.MinusExpression;
import uk.ac.imperial.doc.jexpressions.expressions.PEPADivExpression;
import uk.ac.imperial.doc.jexpressions.expressions.PowerExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ProductExpression;
import uk.ac.imperial.doc.jexpressions.expressions.SumExpression;
import uk.ac.imperial.doc.jexpressions.expressions.TimeExpression;
import uk.ac.imperial.doc.jexpressions.expressions.UMinusExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ZeroExpression;
import uk.ac.imperial.doc.jexpressions.matlaboutput.utils.JExpressionMatlabUtils;

/**
 * Expression visitor that prints the Java implementation of the given expression.
 * @author as1005
 *
 */
public class MatlabExpressionPrinter implements IExpressionVisitor {
	@Override
	public void visit(IntegerExpression e) {
		output.append(e.getValue());
	}

	@Override
	public void visit(UMinusExpression e) {
		output.append("-(");
		e.getE().accept(this); 
		output.append(")");
		
	}

	@Override
	public void visit(FunctionCallExpression e) {	
		if (e.getName().equals("ifpos")){
			output.append(JExpressionMatlabUtils.ifposName+"(");
		} 	
		else {
			output.append(e.getName()+"(");
		}
		boolean first = true; 
		for (AbstractExpression arg:e.getArguments()){
			if (first){
				first = false; 
			} else {
				output.append(","); 				
			}
			arg.accept(this); 
		}
		output.append(")");
		
	}

	@Override
	public void visit(TimeExpression e) {
		output.append("t"); 
		
	}

	@Override
	public void visit(DivExpression e) {
		output.append("(");
		e.getNumerator().accept(this);
		output.append(")/(");
		e.getDenominator().accept(this);
		output.append(")");
	}

	@Override
	public void visit(MinusExpression e) {
		e.getA().accept(this);
		output.append("-(");
		e.getB().accept(this);
		output.append(")");
	}

	@Override
	public void visit(PowerExpression e) {
		output.append("(");
		e.getExpression().accept(this);
		output.append(")^(");
		e.getExponent().accept(this);
		output.append(")");
	}

	protected StringBuilder output;

	public String toString() {
		return output.toString();
	}

	public MatlabExpressionPrinter() {
		output = new StringBuilder();
	}

	@Override
	public void visit(AbstractExpression e) {
		throw new AssertionError("Unsupported printing");
	}

	@Override
	public void visit(DoubleExpression e) {
		output.append(e.getValue());
	}

	@Override
	public void visit(PEPADivExpression e) {
		output.append(JExpressionMatlabUtils.divName+"(");
		e.getNumerator().accept(this);
		output.append(",");
		e.getDenominator().accept(this);
		output.append(")");
	}

	@Override
	public void visit(MinExpression e) {
		output.append("min(");
		e.getA().accept(this);
		output.append(",");
		e.getB().accept(this);
		output.append(")");
	}

	@Override
	public void visit(DivMinExpression e) {
		output.append(JExpressionMatlabUtils.divMinName+"(");
		e.getA().accept(this);
		output.append(",");
		e.getB().accept(this);
		output.append(",");
		e.getC().accept(this);
		output.append(")");
	}

	@Override
	public void visit(DivDivMinExpression e) {
		output.append(JExpressionMatlabUtils.divDivMinName+"(");
		e.getA().accept(this);
		output.append(",");
		e.getB().accept(this);
		output.append(",");
		e.getC().accept(this);
		output.append(",");
		e.getD().accept(this);
		output.append(")");
	}

	@Override
	public void visit(ProductExpression e) {
		boolean first = true;
		for (AbstractExpression t : e.getTerms()) {
			if (first) {
				first = false;
			} else {
				output.append("*");
			}
			output.append("(");
			t.accept(this);
			output.append(")");
		}
	}

	@Override
	public void visit(SumExpression e) {
		if (e.getSummands().size() > 1) {
			output.append("(");
		}
		boolean first = true;
		for (AbstractExpression s : e.getSummands()) {
			if (first) {
				first = false;
			} else {
				output.append("+");
			}
			s.accept(this);
		}
		if (e.getSummands().size() > 1) {
			output.append(")");
		}
	}

	@Override
	public void visit(ZeroExpression e) {
		output.append("0.0");
	}

}
