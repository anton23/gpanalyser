package uk.ac.imperial.doc.masspa.gui.components.channels.gen;

import java.util.Collection;

import uk.ac.imperial.doc.masspa.gui.models.topologies.LocationComponent;

/**
 * Implementor needs to be
 * informed about which
 * nodes constitute the
 * sink nodes;
 * 
 * @author Chris Guenther
 */
public interface ISinkSetter
{
	void setSink(Collection<LocationComponent> selectedLocations);
}
