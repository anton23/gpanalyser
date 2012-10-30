package uk.ac.imperial.doc.gpa.plain.representation.timed;

import uk.ac.imperial.doc.jexpressions.constants.Constants;

public class DiscretePopTimedEventUpdater extends DiscreteTimedEventUpdater
{
	ITimedEventPopUpdateFct mUpdateFct;
	DiscretePopTimedEventUpdater(double[][] values, ITimedEventPopUpdateFct updateFct) {
		super(values);
		mUpdateFct = updateFct;
	}

	@Override
	public void update(Constants constants, double[] popVector, double time) {
		mUpdateFct.update(popVector,this.getValue(time));
	}
}
