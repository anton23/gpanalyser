package uk.ac.imperial.doc.masspa.representation.model;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.masspa.language.Messages;
import uk.ac.imperial.doc.masspa.representation.components.MASSPAMessage;

/***
 * This class defines a channel between two MASSPAAgent populations for a specific message.
 * There is a unique channel for every message for each pair of agent populations.
 * The intensity of the channel can be use to model message loss (0<m_intensity<1) or
 * message amplification (1<=m_intensity).
 * 
 * @author Chris Guenther
 */
public class MASSPAChannel implements Comparable<MASSPAChannel>
{
	public enum RateType{MULTISERVER,MASSACTION};
	public final static RateType s_defaultRate = RateType.MULTISERVER;
	
	private MASSPAAgentPop m_sender;
	private MASSPAAgentPop m_receiver;
	private MASSPAMessage m_msg;
	private AbstractExpression m_intensity;
	private RateType m_rateType;
	
	/**
	 *
	 */
	protected MASSPAChannel()
	{	
	}
	
	/***
	 * Create a new channel between any two agent populations
	 * @param _sender sending agent population
	 * @param _receiver receiving agent population
	 * @param _msg message that is sent
	 * @param _intensity regulate channel quality (0<_intensity<1 => message loss, 1<_intensity message multiplication)
	 * @param _rateType defines the rate kinetics we assume for this channel
	 */
	public MASSPAChannel(final MASSPAAgentPop _sender, final MASSPAAgentPop _receiver, final MASSPAMessage _msg, final AbstractExpression _intensity, final RateType _rateType)
	{
		setSender(_sender);
		if (getSender() == null) {throw new AssertionError(Messages.s_COMPILER_CHANNEL_NULL_SENDER);}
		setReceiver(_receiver);
		if (getReceiver() == null) {throw new AssertionError(Messages.s_COMPILER_CHANNEL_NULL_RECEIVER);}
		setMsg(_msg);
		if (getMsg() == null) {throw new AssertionError(Messages.s_COMPILER_CHANNEL_NULL_MESSAGE);}
		setIntensity(_intensity);
		if (getIntensity() == null) {throw new AssertionError(Messages.s_COMPILER_CHANNEL_NULL_INTENSITY);}
		setRateType(_rateType);	
	}

	// Getters/Setters
	protected void setSender(MASSPAAgentPop _s) {m_sender=_s;}
	public MASSPAAgentPop getSender() {return m_sender;}
	protected void setReceiver(MASSPAAgentPop _r) {m_receiver=_r;}
	public MASSPAAgentPop getReceiver() {return m_receiver;}
	protected void setMsg(MASSPAMessage _m) {m_msg=_m;}
	public MASSPAMessage getMsg() {return m_msg;}
	public void setIntensity(AbstractExpression _intensity) {m_intensity =_intensity;}
	public AbstractExpression getIntensity() {return m_intensity;}
	public void setRateType(RateType _rateType) {m_rateType = _rateType;}
	public RateType getRateType() {return m_rateType;}
	
	//*******************************************
	// Implement Comparable interface
	//*******************************************
	@Override
	public int compareTo(MASSPAChannel _chan)
	{
		int diff=getSender().compareTo(_chan.getSender());
		if (diff != 0) {return diff;}
		diff=getReceiver().compareTo(_chan.getReceiver());
		return (diff != 0) ? diff : getMsg().compareTo(_chan.getMsg());
	}
		
	//*******************************************
	// Object overrides
	//*******************************************
	@Override
	public String toString()
	{
		return "Channel(" + m_sender.getName() + "," + m_receiver.getName() + "," + m_msg + ") = " + m_intensity;
	}
	
	@Override
	public int hashCode()
	{
		return m_sender.hashCode()+m_receiver.hashCode()+m_msg.hashCode();
	}	
	
	@Override
	public boolean equals(final Object _o)
	{
		if (this == _o) {return true;}
		if (!(_o instanceof MASSPAChannel)) {return false;}
		MASSPAChannel chan = (MASSPAChannel) _o;
		return m_sender.equals(chan.m_sender) && m_receiver.equals(chan.m_receiver) && m_msg.equals(chan.m_msg);
	}
}
