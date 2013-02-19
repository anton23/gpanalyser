package uk.ac.imperial.doc.masspa.tests.unit.representation.components;

import uk.ac.imperial.doc.jexpressions.constants.ConstantExpression;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.jexpressions.expressions.IntegerExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ProductExpression;
import uk.ac.imperial.doc.masspa.representation.components.ConstComponent;
import uk.ac.imperial.doc.masspa.representation.components.MASSPAComponent;
import uk.ac.imperial.doc.masspa.representation.components.MASSPAMessage;
import uk.ac.imperial.doc.masspa.representation.components.MessagePrefix;
import uk.ac.imperial.doc.masspa.representation.components.StopComponent;

public class PrefixTestUtil
{
	// Need to make class instantiable
	public class MessagePrefixNonAbstract extends MessagePrefix
	{
		public MessagePrefixNonAbstract(String _action,	AbstractExpression _rate, MASSPAMessage msg, MASSPAComponent _continuation)
		{
			super(_action, _rate, msg, _continuation);
		}
	}
	
	protected static final String s_action1 = "action1";
	protected static final String s_action2 = "action2";
	protected static final MASSPAMessage s_msg1 = new MASSPAMessage("msg1");
	protected static final MASSPAMessage s_msg2 = new MASSPAMessage("msg2");
	protected static final ConstComponent s_comp1 = new ConstComponent("State1");
	protected static final ConstComponent s_comp2 = new ConstComponent("State2");
	protected static final StopComponent s_comp3 = new StopComponent();
	protected static final ConstComponent s_comp4 = new ConstComponent("OtherState2");
	protected static final AbstractExpression s_expr1 = ProductExpression.create(new IntegerExpression(1), new ConstantExpression("mu"));
	protected static final AbstractExpression s_expr2 = ProductExpression.create(new IntegerExpression(3), new ConstantExpression("lambda"));
	protected static final AbstractExpression s_expr3 = new ConstantExpression("lambda");
	protected static final AbstractExpression s_sendRate1 = new DoubleExpression(0.5);
	protected static final AbstractExpression s_sendRate2 = new IntegerExpression(1);
	protected static final AbstractExpression s_accProb1 = new DoubleExpression(0.5);
	protected static final AbstractExpression s_accProb2 = new IntegerExpression(1);
}
