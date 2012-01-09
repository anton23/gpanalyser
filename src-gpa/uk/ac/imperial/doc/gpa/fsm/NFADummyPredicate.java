package uk.ac.imperial.doc.gpa.fsm;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;

import java.util.List;
import java.util.Map;

public class NFADummyPredicate extends NFAPredicate
{
    public boolean eval (
            List<AbstractExpression> statesCountExpressions,
            Map<String, AbstractExpression> mapping, double[] data
    )
    {
        return true;
    }
}
