package uk.ac.imperial.doc.pctmc.expressions.patterns;

import java.util.Map;

import uk.ac.imperial.doc.jexpressions.constants.visitors.ExpressionWalkerWithConstants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
import uk.ac.imperial.doc.jexpressions.variables.IExpressionVariableVisitor;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;
import uk.ac.imperial.doc.pctmc.expressions.GeneralExpectationExpression;
import uk.ac.imperial.doc.pctmc.expressions.ICombinedProductExpressionVisitor;
import uk.ac.imperial.doc.pctmc.expressions.IGeneralExpectationExpressionVisitor;
import uk.ac.imperial.doc.pctmc.expressions.IPopulationVisitor;
import uk.ac.imperial.doc.pctmc.expressions.PopulationExpression;

public class PatternSetterVisitor extends ExpressionWalkerWithConstants 
 implements IPatternVisitor,IExpressionVariableVisitor,
 IPopulationVisitor,ICombinedProductExpressionVisitor,IGeneralExpectationExpressionVisitor {
	
	protected PatternMatcher patternMatcher; 
	
	
	
	
	@Override
	public void visit(GeneralExpectationExpression e) {
		e.getExpression().accept(this);
	}

	public PatternSetterVisitor(PatternMatcher patternMatcher) {
		super();
		this.patternMatcher = patternMatcher;
	}

	public static void unfoldPatterns(AbstractExpression e,PatternMatcher patternMatcher){
		PatternSetterVisitor visitor = new PatternSetterVisitor(patternMatcher);
		e.accept(visitor);		
	}
	
	
	public static void unfoldPatterns(Map<ExpressionVariable,AbstractExpression> map,PatternMatcher patternMatcher){
		PatternSetterVisitor visitor = new PatternSetterVisitor(patternMatcher);
		for (AbstractExpression e:map.values()){
			if (e!=null) e.accept(visitor);
		}
	}

	@Override
	public void visit(PatternPopulationExpression e) {
		e.setUnfolded(patternMatcher.getMatchingExpression(e.getState())); 
	}

	@Override
	public void visit(CombinedProductExpression e) {	}

	@Override
	public void visit(ExpressionVariable e) { 
		AbstractExpression unfolded = e.getUnfolded();
		if (unfolded!=null) unfolded.accept(this); 
	}

	@Override
	public void visit(PopulationExpression e) {}
}
