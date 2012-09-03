package uk.ac.imperial.doc.gpepa.representation.components;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;

public abstract class AbstractPrefix
{
    protected String action;
    protected PEPAComponent continuation;

    public String getAction() {
        return action;
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
