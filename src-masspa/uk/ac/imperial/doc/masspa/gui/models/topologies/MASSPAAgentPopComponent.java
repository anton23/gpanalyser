package uk.ac.imperial.doc.masspa.gui.models.topologies;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import uk.ac.imperial.doc.masspa.gui.util.ExpressionValidator;
import uk.ac.imperial.doc.masspa.representation.components.AllComponent;
import uk.ac.imperial.doc.masspa.representation.components.ConstComponent;
import uk.ac.imperial.doc.masspa.representation.components.MASSPAComponent;
import uk.ac.imperial.doc.masspa.representation.model.MASSPAAgentPop;

/**
 * This class has a slightly different to String
 * method than MASSPAAgentPop and is also serializable.
 * 
 * @author Chris Guenther
 */
@Root
public class MASSPAAgentPopComponent extends MASSPAAgentPop
{
	/** Needed by SimpleXML-Serializer*/
	protected MASSPAAgentPopComponent()
	{
		super();
	}
	
	/**
	 * Create a new population for agent state {@code _component} at location {@code _loc}
	 * @param _component
	 * @param _loc
	 */
	public MASSPAAgentPopComponent(MASSPAComponent _component, LocationComponent _loc)
	{
		super(_component, _loc);
	}
	
	//**********************************************
	// Getters/Setters and SimpleXML-Serialization
	//**********************************************
	/**
	 * Set component to new state with name {@code _s}
	 * @param _s
	 */
	@Attribute(name="state") protected void setState(String _s)
	{
		AllComponent a = new AllComponent();
		super.setComponent(!(_s.equals(a.getName())) ? new ConstComponent(_s) : a);
	}
	
	/**
	 * @return state name
	 */
	@Attribute(name="state") protected String getState() {return super.getComponent().toString();}
	
	/**
	 * Set location to {@code _l}
	 * @param _l
	 */
	@Element(name="location") protected void setLocation(LocationComponent _l) {super.setLocation(_l);}
	
	/**
	 * Get location
	 */
	@Element(name="location") @Override public LocationComponent getLocation() {return (LocationComponent)super.getLocation();}
	
	/**
	 * Parse {@code _i} and set it as initial population expression
	 * @param _i string representation of initial population expression
	 */
	@Attribute(name="initPop") protected void setInitPopStr(String _i){if(!_i.isEmpty()){super.setInitialPopulation(ExpressionValidator.validate(_i));}}
	
	/**
	 * @return initial population expression as string
	 */
	@Attribute(name="initPop") protected String getInitPopStr() {return (super.getInitialPopulation() != null) ? super.getInitialPopulation().toString() : "";}
	
	//************************************
	// Object overwrites
	//************************************
	@Override
	public String toString()
	{
		return getNameAndInitPop();
	}
}