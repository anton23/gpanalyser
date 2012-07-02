package uk.ac.imperial.doc.gpepa.representation.model;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import uk.ac.imperial.doc.gpepa.representation.components.PEPAComponent;
import uk.ac.imperial.doc.gpepa.representation.components.PEPAComponentDefinitions;
import uk.ac.imperial.doc.gpepa.representation.group.GroupComponentPair;
import uk.ac.imperial.doc.jexpressions.expressions.*;
import uk.ac.imperial.doc.jexpressions.utils.ToStringUtils;

import java.util.*;

/**
 * Representation of the group cooperation structure of a model.
 *
 * @author Anton Stefanek
 *
 */
public class GroupCooperation extends GroupedModel {

    @Override
    public Set<String> getActions(PEPAComponentDefinitions definitions) {
        Set<String> ret = new HashSet<String>();
        ret.addAll(left.getActions(definitions));
        ret.addAll(right.getActions(definitions));
        return ret;
    }


    @Override
    public List<PEPAEvolutionEvent> getEvolutionEvents(

            final PEPAComponentDefinitions definitions,
            Set<String> restrictedActions) {
        List<PEPAEvolutionEvent> events = new LinkedList<PEPAEvolutionEvent>();
        Set<String> newRestrictedActions = new HashSet<String>();
        newRestrictedActions.addAll(restrictedActions);
        newRestrictedActions.addAll(actions);
        // events without cooperation between left and right
        events.addAll(left.getEvolutionEvents(definitions,
                newRestrictedActions));
        events.addAll(right.getEvolutionEvents(definitions,
                newRestrictedActions));
        // events from cooperation between left and right
        List<PEPAEvolutionEvent> leftEvents = left.getEvolutionEvents(
                definitions, restrictedActions);
        List<PEPAEvolutionEvent> rightEvents = right.getEvolutionEvents(
                definitions, restrictedActions);
        Multimap<String, PEPAEvolutionEvent> leftActionmap = LinkedHashMultimap
                .<String, PEPAEvolutionEvent> create();
        Multimap<String, PEPAEvolutionEvent> rightActionmap = LinkedHashMultimap
                .<String, PEPAEvolutionEvent> create();

        for (PEPAEvolutionEvent le : leftEvents) {
            if (actions.contains(le.getAction())) {
                leftActionmap.put(le.getAction(), le);
            }
            for (String immediateAction : le.getImmediateActions()) {
                if (actions.contains(immediateAction)) {
                    leftActionmap.put(immediateAction, le);
                }
            }
        }

        for (PEPAEvolutionEvent re : rightEvents) {
            if (actions.contains(re.getAction())) {
                rightActionmap.put(re.getAction(), re);
            }
            for (String immediateAction : re.getImmediateActions()) {
                if (actions.contains(immediateAction)) {
                    rightActionmap.put(immediateAction, re);
                }
            }
        }

        Collection<String> allActions
                = new HashSet<String>(leftActionmap.keySet());
        allActions.addAll(rightActionmap.keySet());
        for (final String action : allActions) {
            for (final PEPAEvolutionEvent le : leftActionmap.get(action)) {
                for (final PEPAEvolutionEvent re : rightActionmap.get(action)) {
                    List<GroupComponentPair> increases = new LinkedList<GroupComponentPair>();
                    List<GroupComponentPair> decreases = new LinkedList<GroupComponentPair>();
                    increases.addAll(le.getIncreases());
                    increases.addAll(re.getIncreases());
                    decreases.addAll(le.getDecreases());
                    decreases.addAll(re.getDecreases());

                    List<String> immediates
                            = new LinkedList<String>() ;
                    immediates.addAll(le.getImmediateActions());
                    if (!le.getAction().equals(action)) {
                        immediates.remove(action);
                    }
                    immediates.addAll(re.getImmediateActions());
                    if (!re.getAction().equals(action)) {
                        immediates.remove(action);
                    }

                    AbstractExpression leftApparentRate = left
                            .getMomentOrientedRateExpression(action,
                                    definitions);

                    AbstractExpression rightApparentRate = right
                            .getMomentOrientedRateExpression(action,
                                    definitions);
                    AbstractExpression leftRate = le.getRate();
                    AbstractExpression rightRate = re.getRate();

                    AbstractExpression rate = DivDivMinExpression.create(
                            leftRate, rightRate, leftApparentRate,
                            rightApparentRate);
                    events.add(new PEPAEvolutionEvent(action, immediates,
                            rate, increases, decreases));
                }
            }
        }

        return events;
    }

    @Override
    public AbstractExpression getMomentOrientedRateExpression(String action,
                                                              PEPAComponentDefinitions definitions) {
        AbstractExpression leftRate = left.getMomentOrientedRateExpression(
                action, definitions);
        AbstractExpression rightRate = right.getMomentOrientedRateExpression(
                action, definitions);

        if (actions.contains(action)) {
            if (leftRate.equals(DoubleExpression.ZERO)
                    || rightRate.equals(DoubleExpression.ZERO)) {
                return DoubleExpression.ZERO;
            }
            return MinExpression.create(leftRate, rightRate);
        } else {
            if (leftRate.equals(DoubleExpression.ZERO)
                    && rightRate.equals(DoubleExpression.ZERO)) {
                return DoubleExpression.ZERO;
            } else if (leftRate.equals(DoubleExpression.ZERO)) {
                return rightRate;
            } else if (rightRate.equals(DoubleExpression.ZERO)) {
                return leftRate;
            } else
                return SumExpression.create(leftRate, rightRate);
        }
    }

    @Override
    public AbstractExpression getComponentRateExpression
            (String action, PEPAComponentDefinitions definitions,
             GroupComponentPair groupComponentPair) {
        boolean countInLeftGroup = true;
        Map<String, LabelledComponentGroup> groups
                = left.getComponentGroups();
        LabelledComponentGroup lgroup
                = groups.get(groupComponentPair.getGroup());
        if (lgroup == null) {
            groups = right.getComponentGroups();
            lgroup = groups.get(groupComponentPair.getGroup());
            if (lgroup == null) {
                return DoubleExpression.ZERO;
            }
            countInLeftGroup = false;
        }

        if (actions.contains(action)) {
            AbstractExpression leftRate = left.getMomentOrientedRateExpression
                    (action, definitions);
            AbstractExpression rightRate = right.getMomentOrientedRateExpression
                    (action, definitions);
            AbstractExpression denominator;
            if (countInLeftGroup) {
                denominator = leftRate;
            }
            else {
                denominator = rightRate;
            }

            if (denominator.equals(DoubleExpression.ZERO)) {
                return DoubleExpression.ZERO;
            }

            AbstractExpression min = MinExpression.create(leftRate, rightRate);
            if (denominator.equals(min)) {
                return lgroup.getComponentRateExpression
                        (action,  definitions, groupComponentPair);
            }
            else {
                return ProductExpression.create(DivExpression.create
                        (lgroup.getComponentRateExpression
                                (action,  definitions, groupComponentPair),
                                denominator), min);
            }
        }
        else {
            return lgroup.getComponentRateExpression
                    (action,  definitions, groupComponentPair);
        }
    }

    public GroupCooperation(GroupedModel left, GroupedModel right,
                            Set<String> actions) {
        super();
        this.left = left;
        this.right = right;
        this.actions = actions;
        refreshComponentGroups();
    }

    protected GroupedModel left;
    protected GroupedModel right;
    protected Set<String> actions;

    public GroupedModel getLeft() {
        return left;
    }

    public GroupedModel getRight() {
        return right;
    }

    public void refreshComponentGroups() {
        componentGroups = new HashMap<String, LabelledComponentGroup>();
        componentGroups.putAll(left.getComponentGroups());
        componentGroups.putAll(right.getComponentGroups());
    }

    public void setLeft(GroupedModel left) {
        this.left = left;
        refreshComponentGroups();
    }

    public void setRight(GroupedModel right) {
        this.right = right;
        refreshComponentGroups();
    }

    public List<GroupCooperation> getParentsList(Map<GroupedModel, GroupedModel> owners) {
        GroupedModel parent = owners.get(this);
        List<GroupCooperation> ancestors = null;

        // if root group cooperation
        if (parent == null)
        {
            ancestors = new ArrayList<GroupCooperation>();
        }
        else
        {
            ancestors = ((GroupCooperation) parent).getParentsList(owners);
            ancestors.add(0, (GroupCooperation) parent);
        }
        return ancestors;
    }

    public String toString() {
        String leftString = left.toString();
        if (left instanceof GroupCooperation) {
            leftString = "(" + leftString + ")";
        }
        String rightString = right.toString();
        if (right instanceof GroupCooperation) {
            rightString = "(" + rightString + ")";
        }
        return leftString + "<" + ToStringUtils.iterableToSSV(actions, ",")
                + ">" + rightString;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((actions == null) ? 0 : actions.hashCode());
        result = prime * result + ((left == null) ? 0 : left.hashCode());
        result = prime * result + ((right == null) ? 0 : right.hashCode());
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (getClass() != obj.getClass())
            return false;
        GroupCooperation other = (GroupCooperation) obj;
        if (actions == null) {
            if (other.actions != null)
                return false;
        } else if (!actions.equals(other.actions))
            return false;
        if (left == null) {
            if (other.left != null)
                return false;
        } else if (!left.equals(other.left))
            return false;
        if (right == null) {
            if (other.right != null)
                return false;
        } else if (!right.equals(other.right))
            return false;
        return true;
    }

    @Override
    public void enumerateGroupedModelParents(Map<GroupedModel, GroupedModel> groupedModels, GroupedModel owner)
    {
        left.enumerateGroupedModelParents(groupedModels, this);
        right.enumerateGroupedModelParents(groupedModels, this);
        groupedModels.put(left, this);
        groupedModels.put(right, this);
    }

    @Override
    public Set<PEPAComponent> getInitialComponents() {
        Set<PEPAComponent> result = left.getInitialComponents();
        result.addAll(right.getInitialComponents());
        return result;
    }
}
