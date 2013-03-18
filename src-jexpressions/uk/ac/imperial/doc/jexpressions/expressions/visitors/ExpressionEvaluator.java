package uk.ac.imperial.doc.jexpressions.expressions.visitors;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.Math;

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
 * Numerical evaluator of basic expressions.
 * 
 * @author as1005
 * 
 */
public class ExpressionEvaluator implements IExpressionVisitor {

	protected double result;

	private double div(double a, double b) {
		if (b == 0.0)
			return 0.0;
		else
			return a / b;
	}

	public double getResult() {
		return result;
	}

	@Override
	public void visit(AbstractExpression e) {
		throw new AssertionError("Unsuported visit by "
				+ this.getClass().getName());
	}

	@Override
	public void visit(DivDivMinExpression e) {
		e.getA().accept(this);
		double a = result;
		e.getB().accept(this);
		double b = result;
		e.getC().accept(this);
		double c = result;
		e.getD().accept(this);
		double d = result;
		result = div(a * b, c * d) * Math.min(c, d);
	}

	@Override
	public void visit(DivExpression e) {
		e.getNumerator().accept(this);
		double a = result;
		e.getDenominator().accept(this);
		double b = result;
		result = a / b;
	}

	@Override
	public void visit(DivMinExpression e) {
		e.getA().accept(this);
		double a = result;
		e.getB().accept(this);
		double b = result;
		e.getC().accept(this);
		double c = result;
		result = div(a, b) * Math.min(b, c);

	}

	@Override
	public void visit(DoubleExpression e) {
		result = e.getValue();

	}

	@Override
	public void visit(FunctionCallExpression e) {
		Class<?>[] argTypes = new Class[e.getArguments().size()];
		Object[] args = new Double[e.getArguments().size()];
		int i = 0;
		for (AbstractExpression arg : e.getArguments()) {
			argTypes[i] = double.class;
			arg.accept(this);
			args[i] = result;
			i++;
		}
		
		try {
			if (JExpressionsJavaUtils.fileValues.containsKey(e.getName())) {
				result = JExpressionsJavaUtils.evaluate(e.getName(), (Double)args[0]);
			} else {
				
				result = (Double) Math.class.getMethod(e.getName(), argTypes)
					.invoke(null, args); // TODO: doesn't seem to find static methods						
			}
		} catch (SecurityException e1) {
			e1.printStackTrace();
		} catch (NoSuchMethodException e1) {
			throw new AssertionError("Function with the name " + e.getName()
					+ " unknown!");
		} catch (IllegalArgumentException e1) {
			throw new AssertionError("Function " + e.getName()
					+ " cannot be called as " + e.toString() + "!");
		} catch (IllegalAccessException e1) {
			e1.printStackTrace();
		} catch (InvocationTargetException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public void visit(IntegerExpression e) {
		result = e.getValue();
	}

	@Override
	public void visit(MinExpression e) {
		e.getA().accept(this);
		double a = result;
		e.getB().accept(this);
		double b = result;
		result = Math.min(a, b);
	}

	@Override
	public void visit(MaxExpression e) {
		e.getA().accept(this);
		double a = result;
		e.getB().accept(this);
		double b = result;
		result = Math.max(a, b);
	}

	@Override
	public void visit(MinusExpression e) {
		e.getA().accept(this);
		double a = result;
		e.getB().accept(this);
		double b = result;
		result = a - b;
	}

	@Override
	public void visit(PEPADivExpression e) {
		e.getNumerator().accept(this);
		double a = result;
		e.getDenominator().accept(this);
		double b = result;
		result = div(a, b);
	}

	@Override
	public void visit(PowerExpression e) {
		e.getExpression().accept(this);
		double expr = result;
		e.getExponent().accept(this);
		double expo = result;
		result = Math.pow(expr, expo);
	}

	@Override
	public void visit(ProductExpression e) {
		double res = 1.0;
		for (AbstractExpression t : e.getTerms()) {
			t.accept(this);
			res *= result;
		}
		result = res;
	}

	@Override
	public void visit(SumExpression e) {
		double res = 0.0;
		for (AbstractExpression t : e.getSummands()) {
			t.accept(this);
			res += result;
		}
		result = res;
	}

	@Override
	public void visit(TimeExpression e) {
		throw new AssertionError("Time cannot be evaluated!");
	}

	@Override
	public void visit(UMinusExpression e) {
		e.getE().accept(this);
		result = -result;
	}

	@Override
	public void visit(IndicatorFunction e) {
		e.getCondition().getLeft().accept(this);
		double leftValue = result;
		e.getCondition().getRight().accept(this);
		double rightValue = result;
		result = e.getCondition().getOperator().compare(leftValue, rightValue)
		          ? 1.0 : 0.0;
	}
	

}
