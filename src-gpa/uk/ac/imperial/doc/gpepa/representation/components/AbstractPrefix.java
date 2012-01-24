package uk.ac.imperial.doc.gpepa.representation.components;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.jexpressions.expressions.SumExpression;

import java.util.LinkedList;
import java.util.List;

public abstract class AbstractPrefix
{
    protected String action;
    protected PEPAComponent continuation;
    protected AbstractExpression immediateSum = DoubleExpression.ZERO;
    protected List<ImmediatePrefix> immediates
        = new LinkedList<ImmediatePrefix>();

    public String getAction() {
        return action;
    }

    public PEPAComponent getContinuation() {
        return continuation;
    }

    public void setContinuation(PEPAComponent continuation) {
        this.continuation = continuation;
    }

    public List<ImmediatePrefix> getImmediatesRaw() {
        return immediates;
    }

    public List<ImmediatePrefix> getImmediatesRawCopy() {
        List<ImmediatePrefix> actions = new LinkedList<ImmediatePrefix>();
        for (ImmediatePrefix immediate : immediates) {
            actions.add (immediate);
        }
        return actions;
    }

    public List<String> getImmediates() {
        List<String> actions = new LinkedList<String>();
        for (ImmediatePrefix immediate : immediates) {
            actions.add (immediate.getAction());
        }
        return actions;
    }

    public void addImmediate(ImmediatePrefix imm) {
        immediateSum = SumExpression.create (immediateSum, imm.getWeight());
        immediates.add(imm);
    }
    
    public void addImmediates(List<ImmediatePrefix> immediates) {
        for (ImmediatePrefix immediate : immediates) {
            addImmediate(immediate);
        }
    }

    public abstract AbstractExpression getRate();

    public AbstractPrefix getCooperation(String cooperationAction,
                                         AbstractPrefix otherAbstractPrefix,
                                         AbstractExpression otherApparentRate,
                                         AbstractExpression thisApparentRate,
                                         PEPAComponent newContinuation) {
        int thisContains = 0;
        int otherContains = 0;
        if (getImmediates().contains(cooperationAction)) {
            thisContains = 2;
        }
        if (cooperationAction.equals(getAction())) {
            thisContains = 1;
        }
        if (otherAbstractPrefix.getImmediates().contains(cooperationAction)) {
            otherContains = 2;
        }
        if (cooperationAction.equals(otherAbstractPrefix.getAction())) {
            otherContains = 1;
        }

        // we disallow the cooperation on action, if it isn't called by one
        // component, or if it is ensuing immediate action on both only
        if ((thisContains == 0 || otherContains == 0)
                || (thisContains == 2 && otherContains == 2)) {
            return null;
        }

        List<ImmediatePrefix> newImmediates = getImmediatesRawCopy();
        newImmediates.addAll(otherAbstractPrefix.getImmediatesRawCopy());
        String action = cooperationAction;

        // new prefix will have the main action as the new action
        if (thisContains == 2) {
            action = getAction();
        }
        if (otherContains == 2) {
            action = otherAbstractPrefix.getAction();
        }

        return getCooperationImpl(action, otherAbstractPrefix, otherApparentRate,
            thisApparentRate, newContinuation, newImmediates);
    }

    protected abstract AbstractPrefix getCooperationImpl(String newAction,
                                                         AbstractPrefix otherAbstractPrefix,
                                                         AbstractExpression otherApparentRate,
                                                         AbstractExpression thisApparentRate,
                                                         PEPAComponent newContinuation,
                                                         List<ImmediatePrefix> newImmediates);
}
