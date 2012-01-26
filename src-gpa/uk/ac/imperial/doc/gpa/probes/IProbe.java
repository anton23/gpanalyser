package uk.ac.imperial.doc.gpa.probes;

import uk.ac.imperial.doc.gpa.fsm.ITransition;
import uk.ac.imperial.doc.gpa.fsm.NFAState;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IProbe
{
	public String getName ();

	public void setName (String name);

	public void setStartingState (NFAState state);

    public NFAState getStartingState ();

    Set<ITransition> getAvailableTransitions ();

    ITransition advanceWithTransition
        (ITransition transition,
         List<AbstractExpression> statesCountExpressions,
         Map<String, AbstractExpression> mapping, double[] data);
}
