package uk.ac.imperial.doc.jexpressions.cppoutput;

import uk.ac.imperial.doc.jexpressions.expressions.*;

/**
 * Expression visitor that prints the Java implementation of the given
 * expression.
 * 
 * @author as1005
 * 
 */
public class CPPExpressionPrinter implements IExpressionVisitor {
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
		if (e.getName().equals("ifpos")) {
			output.append("JExpressionsCPPUtils::ifpos(");
		} else if (e.getName().equals("chebyshev")) {
			output
					.append("JExpressionsCPPUtils::chebyshev(");
		} else if (e.getName().equals("div")) {
			output.append("JExpressionsCPPUtils::div(");
		} else {
            String str = e.getName() + "(";
			output.append(str);
		}
		boolean first = true;
		for (AbstractExpression arg : e.getArguments()) {
			if (first) {
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
		output.append("pow(");
		e.getExpression().accept(this);
		output.append(",");
		e.getExponent().accept(this);
		output.append(")");
	}

	protected StringBuilder output;

	public String toString() {
		return output.toString();
	}

	public CPPExpressionPrinter() {
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
		output.append("JExpressionsCPPUtils::div(");
		e.getNumerator().accept(this);
		output.append(",");
		e.getDenominator().accept(this);
		output.append(")");
	}

	@Override
	public void visit(MinExpression e) {
		output.append("std::min(");
		e.getA().accept(this);
		output.append(",");
		e.getB().accept(this);
		output.append(")");
	}
	
	@Override
	public void visit(MaxExpression e) {
		output.append("std::max(");
		e.getA().accept(this);
		output.append(",");
		e.getB().accept(this);
		output.append(")");
	}

	@Override
	public void visit(DivMinExpression e) {
		output.append("JExpressionsCPPUtils::divmin(");
		e.getA().accept(this);
		output.append(",");
		e.getB().accept(this);
		output.append(",");
		e.getC().accept(this);
		output.append(")");
	}

	@Override
	public void visit(DivDivMinExpression e) {
		output.append("JExpressionsCPPUtils::divdivmin(");
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
	public void visit(IndicatorFunction e) {
		output.append("(");
		output.append("(");
		e.getCondition().getLeft().accept(this);
		output.append(e.getCondition().getOperator());
		e.getCondition().getRight().accept(this);
		output.append(") ? 1.0 : 0.0 )");
	}
	
	

}
