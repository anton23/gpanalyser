package uk.ac.imperial.doc.jexpressions.javaoutput.statements;

/**
 * An abstract class for specific implementations of Java based expression
 * evaluators.
 * 
 * @author as1005
 * 
 */
public abstract class AbstractExpressionEvaluator {
	public abstract int getNumberOfExpressions();

	public abstract double[] update(double[] r, double[] values, double t);
	
	public double[][] updateAllTimes(double[] r, double[][] dataPoints, double stepSize) {
		double[][] selectedData = new double[dataPoints.length][getNumberOfExpressions()];

		for (int t = 0; t < selectedData.length; t++) {
			selectedData[t] = update(r,dataPoints[t], t * stepSize);
		}

		return selectedData;
	}
	
	public double[] updateAtTimes(double[] r, double[][] dataPoints, double[] times, double stepSize) {
		double[] selectedData = new double[getNumberOfExpressions()];

		for (int e = 0; e < getNumberOfExpressions(); e++){
            int timeIndex = (int) Math.floor(times[e]/stepSize);
			double[] tmp = update(r, dataPoints[timeIndex], timeIndex * stepSize);
			selectedData[e] = tmp[e];
		}
		
		return selectedData;
	}
}
