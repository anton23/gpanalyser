package uk.ac.imperial.doc.jexpressions.javaoutput;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DivDivMinExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DivExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DivMinExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.jexpressions.expressions.FunctionCallExpression;
import uk.ac.imperial.doc.jexpressions.expressions.IExpressionVisitor;
import uk.ac.imperial.doc.jexpressions.expressions.IndicatorFunction;
import uk.ac.imperial.doc.jexpressions.expressions.IntegerExpression;
import uk.ac.imperial.doc.jexpressions.expressions.MaxExpression;
import uk.ac.imperial.doc.jexpressions.expressions.MinExpression;
import uk.ac.imperial.doc.jexpressions.expressions.MinusExpression;
import uk.ac.imperial.doc.jexpressions.expressions.PEPADivExpression;
import uk.ac.imperial.doc.jexpressions.expressions.PowerExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ProductExpression;
import uk.ac.imperial.doc.jexpressions.expressions.SumExpression;
import uk.ac.imperial.doc.jexpressions.expressions.TimeExpression;
import uk.ac.imperial.doc.jexpressions.expressions.UMinusExpression;
import uk.ac.imperial.doc.jexpressions.javaoutput.utils.JExpressionsJavaUtils;

/**
 * Expression visitor that prints the Java implementation of the given
 * expression.
 * 
 * @author as1005
 * 
 */
public class JavaExpressionPrinter implements IExpressionVisitor {
	public static String utilsClassName;
	protected static Set<String> utilMethods;
	
	static {
		utilsClassName = JExpressionsJavaUtils.class.getName();
		String[] packagePath = utilsClassName.split("\\.");
		utilsClassName = packagePath[packagePath.length - 1];
		utilMethods = new HashSet<String>();
		for (Method m : JExpressionsJavaUtils.class.getMethods()){
			if (Modifier.isStatic(m.getModifiers())) {
				utilMethods.add(m.getName());
			}
		}
	}
	
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
		if (utilMethods.contains(e.getName())) {
			output.append(utilsClassName + "." + e.getName() + "(");
		} else {
			output.append("Math." + e.getName() + "(");
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
		output.append("Math.pow(");
		e.getExpression().accept(this);
		output.append(",");
		e.getExponent().accept(this);
		output.append(")");
	}

	protected StringBuilder output;

	public String toString() {
		return output.toString();
	}

	public JavaExpressionPrinter() {
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
		output.append(utilsClassName + ".div(");
		e.getNumerator().accept(this);
		output.append(",");
		e.getDenominator().accept(this);
		output.append(")");
	}

	@Override
	public void visit(MinExpression e) {
		output.append("Math.min(");
		e.getA().accept(this);
		output.append(",");
		e.getB().accept(this);
		output.append(")");
	}
	
	@Override
	public void visit(MaxExpression e) {
		output.append("Math.max(");
		e.getA().accept(this);
		output.append(",");
		e.getB().accept(this);
		output.append(")");
	}

	@Override
	public void visit(DivMinExpression e) {
		output.append(utilsClassName + ".divmin(");
		e.getA().accept(this);
		output.append(",");
		e.getB().accept(this);
		output.append(",");
		e.getC().accept(this);
		output.append(")");
	}

	@Override
	public void visit(DivDivMinExpression e) {
		output.append(utilsClassName + ".divdivmin(");
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
