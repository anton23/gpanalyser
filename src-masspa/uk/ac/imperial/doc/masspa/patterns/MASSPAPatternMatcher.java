package uk.ac.imperial.doc.masspa.patterns;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.IntegerExpression;
import uk.ac.imperial.doc.masspa.language.Messages;
import uk.ac.imperial.doc.masspa.pctmc.MASSPAPCTMC;
import uk.ac.imperial.doc.masspa.representation.model.MASSPAModel;
import uk.ac.imperial.doc.pctmc.expressions.patterns.PatternMatcher;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;
import uk.ac.imperial.doc.pctmc.representation.State;

public class MASSPAPatternMatcher extends PatternMatcher
{
	MASSPAModel m_model = null;	
	
	public MASSPAPatternMatcher(PCTMC _pctmc)
	{		
		super(_pctmc);
		if (_pctmc instanceof MASSPAPCTMC)
		{
			MASSPAPCTMC masspapctmc = (MASSPAPCTMC)_pctmc; 
			m_model=masspapctmc.getModel();
		} 
		else
		{
			throw new AssertionError(Messages.s_COMPILER_MASSPA_MATCHER_INCOMPATIBLE_WITH_PCTMC);
		}
	} 	
	
	@Override
	public AbstractExpression getMatchingExpression(State s)
	{
		// TODO: Do same as in GPEPAPatternMatcher
		return new IntegerExpression(0);
	}
}
