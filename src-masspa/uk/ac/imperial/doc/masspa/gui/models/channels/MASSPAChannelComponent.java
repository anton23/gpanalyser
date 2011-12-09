package uk.ac.imperial.doc.masspa.gui.models.channels;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.masspa.gui.models.topologies.MASSPAAgentPopComponent;
import uk.ac.imperial.doc.masspa.gui.util.ExpressionValidator;
import uk.ac.imperial.doc.masspa.representation.components.AllMessage;
import uk.ac.imperial.doc.masspa.representation.components.MASSPAMessage;
import uk.ac.imperial.doc.masspa.representation.model.MASSPAChannel;

/**
 * This class is a serializable version of MASSPAChannel
 * 
 * @author Chris Guenther
 */
@Root
public class MASSPAChannelComponent extends MASSPAChannel
{
	/** Needed by SimpleXML-Serializer*/
	protected MASSPAChannelComponent()
	{
		super();
	}
	
	/**
	 * Create a new masspa channel
	 * @param _sender
	 * @param _receiver
	 * @param _msg
	 * @param _intensity
	 */
	public MASSPAChannelComponent(MASSPAAgentPopComponent _sender,
								  MASSPAAgentPopComponent _receiver,
								  MASSPAMessage _msg,
								  AbstractExpression _intensity)
	{
		super(_sender, _receiver, _msg, _intensity);
	}
	
	//**********************************************
	// Getters/Setters and SimpleXML-Serialization
	//**********************************************
	/**
	 * Set sender population to {@code _s}
	 * @param _s
	 */
	@Element(name="sender") protected void setSender(MASSPAAgentPopComponent _s) {super.setSender(_s);}
	
	/**
	 * @return sender population
	 */
	@Element(name="sender") @Override public MASSPAAgentPopComponent getSender() {return (MASSPAAgentPopComponent)super.getSender();}
	
	/**
	 * Set receiver population to {@code _r}
	 * @param _r
	 */
	@Element(name="receiver") protected void setReceiver(MASSPAAgentPopComponent _r) {super.setReceiver(_r);}
	
	/**
	 * @return receiver population
	 */
	@Element(name="receiver") @Override public MASSPAAgentPopComponent getReceiver() {return (MASSPAAgentPopComponent)super.getReceiver();}
	
	/**
	 * Set channel message to {@code _m}
	 * @param _m
	 */
	@Attribute(name="msg") protected void setMsgStr(String _m)
	{
		AllMessage m = new AllMessage();
		super.setMsg(!(_m.equals(m.getMsg())) ? new MASSPAMessage(_m) : m);
	}
	
	/**
	 * @return channel message as String
	 */
	@Attribute(name="msg") public String getMsgStr() {return super.getMsg().toString();}
	
	/**
	 * Set channel intensity to {@code _i}
	 * @param _i
	 */
	@Attribute(name="intensity") protected void setIntensityStr(String _i){super.setIntensity(ExpressionValidator.validate(_i));}
	
	/**
	 * @return channel intensity
	 */
	@Attribute(name="intensity") public String getIntensityStr() {return super.getIntensity().toString();}
}
