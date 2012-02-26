package uk.ac.imperial.doc.masspa.tests.unit.representation.components;

import java.util.ArrayList;
import java.util.Arrays;

import uk.ac.imperial.doc.masspa.representation.components.ChoiceComponent;
import uk.ac.imperial.doc.masspa.representation.components.MASSPAComponent;
import uk.ac.imperial.doc.masspa.representation.components.Prefix;
import uk.ac.imperial.doc.masspa.representation.components.ReceivePrefix;
import uk.ac.imperial.doc.masspa.representation.components.SendPrefix;

public class MASSPAComponentTestUtil extends PrefixTestUtil
{
	public class MASSPAComponentNonAbstract extends MASSPAComponent
	{
		public MASSPAComponentNonAbstract(String _name)
		{
			super(_name);
		}
	}
	
	protected final String s_name1 = "CompName1";
	protected final String s_name2 = "CompName2";
	protected final String s_name3 = "";
	
	protected Prefix[] s_prefixes1 = {new Prefix(s_action1,s_expr1,s_comp1), new ReceivePrefix(s_action1,s_msg1,s_accProb1,s_comp3), new SendPrefix(s_action1,s_expr1,s_msg2,s_sendRate1,s_comp1), new SendPrefix(s_action2,s_expr2,s_msg2,s_sendRate2,s_comp2)};
	protected final ChoiceComponent s_choice1 = new ChoiceComponent(new ArrayList<Prefix>(Arrays.asList(s_prefixes1)));

	protected Prefix[] s_prefixes2 = {new ReceivePrefix(s_action1,s_msg1,s_accProb1,s_comp1), new Prefix(s_action1,s_expr1,s_comp1), new Prefix(s_action2,s_expr1,s_comp3), new SendPrefix(s_action1,s_expr1,s_msg2,s_sendRate1,s_comp1), new SendPrefix(s_action2,s_expr2,s_msg2,s_sendRate2,s_comp2)};
	protected final ChoiceComponent s_choice2 = new ChoiceComponent(new ArrayList<Prefix>(Arrays.asList(s_prefixes2)));
	
	protected Prefix[] s_prefixes3 = {new ReceivePrefix(s_action1,s_msg1,s_accProb1,s_comp4), new SendPrefix(s_action2,s_expr2,s_msg2,s_sendRate2,s_comp3)};
	protected final ChoiceComponent s_choice3 = new ChoiceComponent(new ArrayList<Prefix>(Arrays.asList(s_prefixes3)));
}
