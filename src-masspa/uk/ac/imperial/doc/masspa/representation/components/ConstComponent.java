package uk.ac.imperial.doc.masspa.representation.components;

import java.util.Set;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.IntegerExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ProductExpression;
import uk.ac.imperial.doc.jexpressions.expressions.SumExpression;
import uk.ac.imperial.doc.masspa.language.Messages;
import uk.ac.imperial.doc.masspa.util.MASSPALogging;
/***
 * This class represents an agent definition
 * 
 * @author Chris Guenther
 */
public class ConstComponent extends MASSPAComponent
{
	private MASSPAComponent m_definition = null;
	
	public ConstComponent(final String _name)
	{
		super(_name);
	}
	
	@Override
	public MASSPAComponent getDefinition()
	{
		return m_definition;
	}

	/***
	 * Define the constant i.e. _name =  | ChoiceComponent | StopComponent | ... 
	 * If already defined or _def == null definition is ignored. 
	 * @param _def e.g. ChoiceComponent | StopComponent | ...
	 * @param line is the line number of definition
	 * @return true iff constant was previously undefined and is now defined
	 */
	public boolean define(final MASSPAComponent _def, final int _line)
	{
		if (hasDefinition())
		{
			MASSPALogging.warn(String.format(Messages.s_AGENT_STATE_REDEFINITION, getName(), _def, _line));
			return false;
		} 
		m_definition = _def;
		if (m_definition==null)
		{
			String err = String.format(Messages.s_AGENT_STATE_INVALID_DEFINITION2, getName(), _def, _line);
			MASSPALogging.fatalError(err);
			throw new AssertionError(err);
		}
		return true;
	}
	
	/**
	 * @param _msg
	 * @return true iff agent can send messages of type {@code _msg}
	 */
	public boolean canSend(final MASSPAMessage _msg)
	{
		if (m_definition instanceof ChoiceComponent)
		{
			for (Prefix p : ((ChoiceComponent)m_definition).getChoices())
			{
				if (p instanceof SendPrefix)
				{
					if (((SendPrefix)p).getMsg().equals(_msg))
					{
						return true;
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * @param _msg
	 * @return rate of outgoing messages {@code _msg}. If no message
	 *         is sent new IntegerExpression(0) will be returned
	 */
	public AbstractExpression getSendingRate(final MASSPAMessage _msg)
	{
		AbstractExpression ae = null;
		if (m_definition instanceof ChoiceComponent)
		{
			for (Prefix p : ((ChoiceComponent)m_definition).getChoices())
			{
				if (p instanceof SendPrefix)
				{
					SendPrefix sp = (SendPrefix)p;
					if (sp.getMsg().equals(_msg))
					{
						ae = (ae == null) ? ProductExpression.create(sp.getRate(),sp.getNofMsgsSent())
										  : SumExpression.create(ae,ProductExpression.create(sp.getRate(),sp.getNofMsgsSent()));
					}
				}
			}
		}
		return (ae != null) ? ae : new IntegerExpression(0);
	}
	
	/**
	 * @param _msg
	 * @return true iff agent can receive messages of type {@code _msg}
	 */
	public boolean canReceive(final MASSPAMessage _msg)
	{
		if (m_definition instanceof ChoiceComponent)
		{
			for (Prefix p : ((ChoiceComponent)m_definition).getChoices())
			{
				if (p instanceof ReceivePrefix)
				{
					if (((ReceivePrefix)p).getMsg().equals(_msg))
					{
						return true;
					}
				}
			}
		}
		return false;
	}
	
	@Override
	public boolean matchPattern(final MASSPAComponent _pattern)
	{
		if(_pattern instanceof AnyComponent) {return true;}
		return equals(_pattern);
	}
	
	@Override
	protected void getDerivativeStates(final Set<MASSPAComponent> _known)
	{
		if (m_definition != null) {m_definition.getDerivativeStates(_known);}
	}

	//***************************************
	// Object overwrites
	//***************************************
	@Override
	public boolean equals(final Object _o)
	{
		if (this == _o) {return true;}
		if (!(_o instanceof ConstComponent)) {return false;}
		return ((ConstComponent)_o).getName().equals(getName()); 
	}
}
