package uk.ac.imperial.doc.gpa.plain.representation.timed;

import uk.ac.imperial.doc.jexpressions.constants.Constants;

/**
 * Update a constant according to a timed event
 * 
 * @author Chris Guenther
 */
public class DiscreteRateTimedEventUpdater extends DiscreteTimedEventUpdater {

	protected String mName;

	/**
	 * @param values for event i we get {@code values[i][0]} = time of event
	 * 		  and {@code values[i][1]} = value of event
	 * @param name of constant that is to be updated
	 */
	DiscreteRateTimedEventUpdater(double[][] values, String name) {
		super(values);
		mName = name;
	}
	
	@Override
	public void update(Constants constants, double[] popVector, double time) {
		constants.setConstantValue(mName, getValue(time));
	}
}
