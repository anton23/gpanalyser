package uk.ac.imperial.doc.gpepa.representation.components;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractPrefix
{
    protected String action;
    protected PEPAComponent continuation;

    public String getAction() {
        return action;
    }

    public List<String> getAllActions() {
        List<String> actions = new ArrayList<String>();
        actions.add(action);
        return actions;
    }

    public PEPAComponent getContinuation() {
        return continuation;
    }

    public void setContinuation(PEPAComponent continuation) {
        this.continuation = continuation;
    }

    public abstract AbstractExpression getRate();
    public abstract AbstractExpression getWeight();

    public AbstractPrefix getCooperation(String cooperationAction,
                                         AbstractPrefix otherAbstractPrefix,
                                         AbstractExpression otherApparentRate,
                                         AbstractExpression otherApparentWeight,
                                         AbstractExpression thisApparentRate,
                                         AbstractExpression thisApparentWeight,
                                         PEPAComponent newContinuation) {
        return getCooperationImpl(cooperationAction, otherAbstractPrefix,
                otherApparentRate, otherApparentWeight,
                thisApparentRate, thisApparentWeight,
                newContinuation);
    }

    protected abstract AbstractPrefix getCooperationImpl(String newAction,
                                                         AbstractPrefix otherAbstractPrefix,
                                                         AbstractExpression otherApparentRate,
                                                         AbstractExpression otherApparentWeight,
                                                         AbstractExpression thisApparentRate,
                                                         AbstractExpression thisApparentWeight,
                                                         PEPAComponent newContinuation);
}
