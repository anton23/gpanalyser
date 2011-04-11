package uk.ac.imperial.doc.pctmc.expressions.patterns;


import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;
import uk.ac.imperial.doc.pctmc.representation.State;

public abstract class PatternMatcher {
	protected PCTMC pctmc;
	public PatternMatcher(PCTMC pctmc){
		this.pctmc = pctmc;		
	}
	public abstract AbstractExpression getMatchingExpression(State s);

}
