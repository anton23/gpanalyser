package uk.ac.imperial.doc.gpepa.representation.components;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.utils.ToStringUtils;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class CooperationComponent extends PEPAComponent {

	@Override
	public boolean matchPattern(PEPAComponent pattern) {
		if (pattern instanceof AnyComponent)
			return true;
		if (!(pattern instanceof CooperationComponent))
			return false;
		CooperationComponent asCoop = (CooperationComponent) pattern;
		if (!cooperationSet.equals(asCoop.getCooperationSet()))
			return false;

		return left.matchPattern(asCoop.getLeft())
				&& right.matchPattern(asCoop.getRight());
	}

    @Override
    public boolean containsComponent(ComponentId component) {
        return (left.containsComponent(component)
                    || right.containsComponent(component));
    }

    protected PEPAComponent left;
	protected PEPAComponent right;
	protected Set<String> cooperationSet;

	public CooperationComponent(PEPAComponent left, PEPAComponent right,
			Set<String> cooperationSet) {
		super();
		this.left = left;
		this.right = right;
		this.cooperationSet = cooperationSet;
	}

	public Set<String> getCooperationSet() {
		return cooperationSet;
	}

	public PEPAComponent getLeft() {
		return left;
	}

	public PEPAComponent getRight() {
		return right;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof CooperationComponent))
			return false;
		CooperationComponent asCooperation = (CooperationComponent) o;
		return cooperationSet.equals(asCooperation.getCooperationSet())
				&& left.equals(asCooperation.getLeft())
				&& right.equals(asCooperation.getRight());
	}

	@Override
	public Set<String> getActions() {
		Set<String> ret = new HashSet<String>();
		ret.addAll(left.getActions());
		ret.addAll(right.getActions());
		return ret;
	}

	@Override
	public Set<String> getActions(PEPAComponentDefinitions definitions) {
		Set<String> ret = new HashSet<String>();
		ret.addAll(left.getActions(definitions));
		ret.addAll(right.getActions(definitions));
		return ret;
	}

	@Override
	public Set<PEPAComponent> getDerivativeStates(Set<PEPAComponent> known,
			PEPAComponentDefinitions definitions) {
		Set<PEPAComponent> ret = new HashSet<PEPAComponent>();
		PEPAComponent shorthand = definitions.getShorthand(this);
		if (known.contains(this))
			return ret;
		ret.add(shorthand);
		known.add(shorthand);
		known.add(this);
		for (AbstractPrefix p : getPrefixes(definitions)) {
			ret.addAll(p.getContinuation().getDerivativeStates(known,
					definitions));
		}
		return ret;
	}

    @Override
	public List<AbstractPrefix> getPrefixes(PEPAComponentDefinitions definitions) {
		List<AbstractPrefix> leftPrefixes = left.getPrefixes(definitions);
		List<AbstractPrefix> rightPrefixes = right.getPrefixes(definitions);
		List<AbstractPrefix> ret = new LinkedList<AbstractPrefix>();

		Multimap<String, AbstractPrefix> leftActionmap = LinkedHashMultimap
				.<String, AbstractPrefix> create();

		Multimap<String, AbstractPrefix> rightActionmap = LinkedHashMultimap
				.<String, AbstractPrefix> create();

		// only left evolves
		for (AbstractPrefix leftPrefix : leftPrefixes) {
            boolean immediatesCleared = true;
            for (String action : leftPrefix.getImmediates()) {
                if (cooperationSet.contains(action)) {
                    leftActionmap.put(action, leftPrefix);
                    immediatesCleared = false;
                }
            }
            if (immediatesCleared &&
                    !cooperationSet.contains(leftPrefix.getAction())) {
                PEPAComponent newContinuation = definitions
                        .getShorthand(new CooperationComponent(leftPrefix
                                .getContinuation(), right, cooperationSet));
                try {
                    ret.add(leftPrefix.getClass().getDeclaredConstructor
                            (String.class, AbstractExpression.class,
                                    PEPAComponent.class, List.class)
                                .newInstance(leftPrefix.getAction(),
                                        leftPrefix.getRate(),
                                        newContinuation,
                                        leftPrefix.getImmediatesRaw()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                leftActionmap.put(leftPrefix.getAction(), leftPrefix);
            }
		}
		// only right evolves
		for (AbstractPrefix rightPrefix : rightPrefixes) {
            boolean immediatesCleared = true;
            for (String action : rightPrefix.getImmediates()) {
                if (cooperationSet.contains(action)) {
                    rightActionmap.put(action, rightPrefix);
                    immediatesCleared = false;
                }
            }
            if (immediatesCleared
                    && !cooperationSet.contains(rightPrefix.getAction())) {
                PEPAComponent newContinuation = definitions
                        .getShorthand(new CooperationComponent(left,
                                rightPrefix.getContinuation(), cooperationSet));
                try {
                    ret.add(rightPrefix.getClass().getDeclaredConstructor
                            (String.class, AbstractExpression.class,
                                    PEPAComponent.class, List.class)
                                .newInstance(rightPrefix.getAction(),
                                        rightPrefix.getRate(),
                                        newContinuation,
                                        rightPrefix.getImmediatesRaw()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                rightActionmap.put(rightPrefix.getAction(), rightPrefix);
            }
		}

        Set<String> cooperationActions = new HashSet<String>();
        cooperationActions.addAll(leftActionmap.keySet());
        cooperationActions.addAll(rightActionmap.keySet());
        
		// both evolve
		for (String action : cooperationActions) {
			for (AbstractPrefix leftPrefix : leftActionmap.get(action)) {
				for (AbstractPrefix rightPrefix : rightActionmap.get(action)) {

					PEPAComponent newContinuation = definitions
							.getShorthand(new CooperationComponent(leftPrefix
									.getContinuation(), rightPrefix
									.getContinuation(), cooperationSet));
                    AbstractExpression leftApparentRate = definitions
                            .getApparentRateExpression
                                    (leftPrefix.getAction(), left);
                    AbstractExpression rightApparentRate = definitions
                            .getApparentRateExpression
                                    (rightPrefix.getAction(), right);
                    AbstractPrefix newPrefix = leftPrefix.getCooperation
                            (action, rightPrefix, rightApparentRate,
                                leftApparentRate, newContinuation);
                    if (newPrefix != null)
                    {
                        ret.add(newPrefix);
                    }
				}
			}
		}

		return ret;
	}

	@Override
	public int hashCode() {
		return left.hashCode() + right.hashCode() * 23;
	}

	@Override
	public String toString() {
		String leftString = left.toString();
		if (left instanceof CooperationComponent) {
			leftString = "(" + leftString + ")";
		}
		String rightString = right.toString();
		if (right instanceof CooperationComponent) {
			rightString = "(" + rightString + ")";
		}
		return leftString + "<"
				+ ToStringUtils.iterableToSSV(cooperationSet, ",") + ">"
				+ rightString;
	}

}
