package uk.ac.imperial.doc.jexpressions.cppoutput;

import uk.ac.imperial.doc.jexpressions.expressions.*;

/**
 * Expression visitor that prints the C++ implementation of the given
 * expression.
 *
 * @author as1005
 *
 */
public class CPPExpressionPrinter implements IExpressionVisitor {

    public void visit(IntegerExpression e) {
        output.append(e.getValue());
    }

    public void visit(UMinusExpression e) {
        output.append("-(");
        e.getE().accept(this);
        output.append(")");

    }

    public void visit(FunctionCallExpression e) {
        if (e.getName().equals("ifpos")) {
            output.append("J::ifpos(");
        } else if (e.getName().equals("chebyshev")) {
            output
                    .append("J::chebyshev(");
        } else if (e.getName().equals("div")) {
            output.append("J::div(");
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

    public void visit(TimeExpression e) {
        output.append("t");

    }

    public void visit(DivExpression e) {
        output.append("J::div(");
        e.getNumerator().accept(this);
        output.append(", ");
        e.getDenominator().accept(this);
        output.append(")");
    }

    public void visit(MinusExpression e) {
        e.getA().accept(this);
        output.append("-(");
        e.getB().accept(this);
        output.append(")");
    }

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

    public void visit(AbstractExpression e) {
        throw new AssertionError("Unsupported printing");
    }

    public void visit(DoubleExpression e) {
        output.append(e.getValue());
    }

    public void visit(PEPADivExpression e) {
        output.append("J::div(");
        e.getNumerator().accept(this);
        output.append(",");
        e.getDenominator().accept(this);
        output.append(")");
    }

    public void visit(MinExpression e) {
        output.append("std::min(");
        e.getA().accept(this);
        output.append(",");
        e.getB().accept(this);
        output.append(")");
    }

    public void visit(MaxExpression e) {
        output.append("std::max(");
        e.getA().accept(this);
        output.append(",");
        e.getB().accept(this);
        output.append(")");
    }

    public void visit(DivMinExpression e) {
        output.append("J::divmin(");
        e.getA().accept(this);
        output.append(",");
        e.getB().accept(this);
        output.append(",");
        e.getC().accept(this);
        output.append(")");
    }

    public void visit(DivDivMinExpression e) {
        output.append("J::divdivmin(");
        e.getA().accept(this);
        output.append(",");
        e.getB().accept(this);
        output.append(",");
        e.getC().accept(this);
        output.append(",");
        e.getD().accept(this);
        output.append(")");
    }

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

    public void visit(IndicatorFunction e) {
        output.append("(");
        output.append("(");
        e.getCondition().getLeft().accept(this);
        output.append(e.getCondition().getOperator());
        e.getCondition().getRight().accept(this);
        output.append(") ? 1.0 : 0.0 )");
    }



}
