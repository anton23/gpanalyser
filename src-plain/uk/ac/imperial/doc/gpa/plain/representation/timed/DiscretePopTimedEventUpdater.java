package uk.ac.imperial.doc.gpa.plain.representation.timed;

import uk.ac.imperial.doc.jexpressions.constants.Constants;

/**
 * Update populations according to a timed event
 * 
 * @author Chris Guenther
 */
public class DiscretePopTimedEventUpdater extends DiscreteTimedEventUpdater
{
	protected ITimedEventPopUpdateFct mUpdateFct;
	
	/**
	 * @param values for event i we get {@code values[i][0]} = time of event
	 * 		  and {@code values[i][1]} = value of event
	 * @param updateFct object modifies {@code popVector} when event occurs
	 */
	DiscretePopTimedEventUpdater(double[][] values, ITimedEventPopUpdateFct updateFct) {
		super(values);
		mUpdateFct = updateFct;
	}

	@Override
	public void update(Constants constants, double[] popVector, double time) {
		mUpdateFct.update(popVector,this.getValue(time));
	}
}
