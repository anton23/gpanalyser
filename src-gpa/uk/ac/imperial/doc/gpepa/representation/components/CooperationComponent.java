package uk.ac.imperial.doc.gpepa.representation.components;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DivDivMinExpression;
import uk.ac.imperial.doc.jexpressions.utils.ToStringUtils;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

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
		return null;
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
		for (Prefix p : getPrefixes(definitions)) {
			ret.addAll(p.getContinuation().getDerivativeStates(known,
					definitions));
		}
		return ret;
	}

	@Override
	public List<Prefix> getPrefixes(PEPAComponentDefinitions definitions) {
		List<Prefix> leftPrefixes = left.getPrefixes(definitions);
		List<Prefix> rightPrefixes = right.getPrefixes(definitions);
		List<Prefix> ret = new LinkedList<Prefix>();

		Multimap<String, Prefix> leftActionmap = LinkedHashMultimap
				.<String, Prefix> create();

		Multimap<String, Prefix> rightActionmap = LinkedHashMultimap
				.<String, Prefix> create();

		// only left evolves
		for (Prefix leftPrefix : leftPrefixes) {
			String action = leftPrefix.getAction();
			if (!cooperationSet.contains(action)) {
				PEPAComponent newContinuation = definitions
						.getShorthand(new CooperationComponent(leftPrefix
								.getContinuation(), right, cooperationSet));
				ret.add(new Prefix(action, leftPrefix.getRate(),
						newContinuation));
			} else {
				leftActionmap.put(action, leftPrefix);
			}
		}
		// only right evolves
		for (Prefix rightPrefix : rightPrefixes) {
			String action = rightPrefix.getAction();
			if (!cooperationSet.contains(action)) {
				PEPAComponent newContinuation = definitions
						.getShorthand(new CooperationComponent(left,
								rightPrefix.getContinuation(), cooperationSet));
				ret.add(new Prefix(action, rightPrefix.getRate(),
						newContinuation));
			} else {
				rightActionmap.put(action, rightPrefix);
			}
		}
		// both evolve
		for (String action : leftActionmap.keySet()) {
			for (Prefix leftPrefix : leftActionmap.get(action)) {
				for (Prefix rightPrefix : rightActionmap.get(action)) {
					PEPAComponent newContinuation = definitions
							.getShorthand(new CooperationComponent(leftPrefix
									.getContinuation(), rightPrefix
									.getContinuation(), cooperationSet));
					AbstractExpression leftRate = leftPrefix.getRate();
					AbstractExpression rightRate = rightPrefix.getRate();
					AbstractExpression leftApparentRate = definitions
							.getApparentRateExpression(action, left);
					AbstractExpression rightApparentRate = definitions
							.getApparentRateExpression(action, right);
					AbstractExpression rate = DivDivMinExpression.create(
							leftRate, rightRate, leftApparentRate,
							rightApparentRate);
					ret.add(new Prefix(action, rate, newContinuation));
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
