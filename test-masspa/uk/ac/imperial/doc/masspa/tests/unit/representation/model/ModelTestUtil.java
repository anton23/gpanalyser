package uk.ac.imperial.doc.masspa.tests.unit.representation.model;

import java.util.ArrayList;
import java.util.Arrays;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.jexpressions.expressions.IntegerExpression;
import uk.ac.imperial.doc.masspa.representation.components.ConstComponent;
import uk.ac.imperial.doc.masspa.representation.components.MASSPAMessage;
import uk.ac.imperial.doc.masspa.representation.model.Location;
import uk.ac.imperial.doc.masspa.representation.model.MASSPAAgentPop;

public class ModelTestUtil
{
	protected static final ConstComponent s_comp1 = new ConstComponent("Comp1");
	protected static final ConstComponent s_comp2 = new ConstComponent("Comp2");
	protected static final ConstComponent s_comp3 = new ConstComponent("Comp3");
	
	protected static final Location s_loc1 = new Location(new ArrayList<Integer>(Arrays.asList(0)));
	protected static final Location s_loc2 = new Location(new ArrayList<Integer>(Arrays.asList(1,2)));
	protected static final Location s_loc3 = new Location(new ArrayList<Integer>(Arrays.asList(2,1)));
	
	protected static final MASSPAAgentPop s_pop1 = new MASSPAAgentPop(s_comp1,s_loc1);
	protected static final MASSPAAgentPop s_pop2 = new MASSPAAgentPop(s_comp2,s_loc2);
	protected static final MASSPAAgentPop s_pop3 = new MASSPAAgentPop(s_comp3,s_loc3);
	
	protected static final MASSPAMessage s_msg1 = new MASSPAMessage("Msg1");
	protected static final MASSPAMessage s_msg2 = new MASSPAMessage("Msg2");
	
	protected static final AbstractExpression s_expr1 = new IntegerExpression(2);
	protected static final AbstractExpression s_expr2 = new DoubleExpression(3.0);
}
