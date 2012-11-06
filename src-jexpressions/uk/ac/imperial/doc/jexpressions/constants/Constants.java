package uk.ac.imperial.doc.jexpressions.constants;

import java.util.HashMap;
import java.util.Map;

import uk.ac.imperial.doc.jexpressions.utils.ToStringUtils;

/**
 * This class provides mapping from constants to their (double) values. It also
 * maintains an integer valued index of the constants names.
 * 
 * @author Anton Stefanek
 * 
 */
public class Constants {

	protected Map<String, Integer> constantIndex;

	private Map<String, Double> constants;

	/**
	 * Creates a Constants objects with the given underlying value map. Also
	 * creates an index from constant names to integers.
	 * 
	 * @param constants
	 */
	public Constants(Map<String, Double> constants) {
		super();
		this.constants = constants;
		calculateConstantIndex();
	}

	private Constants(Map<String, Double> constants,
			Map<String, Integer> constantIndex) {
		this.constantIndex = constantIndex;
		this.constants = constants;
	}

	private void calculateConstantIndex() {
		constantIndex = new HashMap<String, Integer>();

		int i = 0;
		for (String par : constants.keySet()) {

			constantIndex.put(par, i++);
		}
	}

	public void recalculateConstantIndex()
	{
		calculateConstantIndex();
	}
	
	/**
	 * Returns the index of the constant with the given name.
	 * 
	 * @param name
	 *            of the constant
	 * @return index of the constant
	 */
	public int getConstantsIndex(String name) {
		if (!constantIndex.containsKey(name)) {
			throw new AssertionError("Constant " + name + " unknown!");
		}
		return constantIndex.get(name);
	}

	/**
	 * Returns the underlying map of name -> value.
	 * 
	 * @return Map from constant names to their values.
	 */
	public Map<String, Double> getConstantsMap() {
		return constants;
	}

	/**
	 * Returns the current value of the given constant.
	 * 
	 * @param name
	 * @return value of the constant with the given name
	 */
	public Double getConstantValue(String name) {
		return constants.get(name);
	}

	/**
	 * Creates a copy of the constants, using the same index as the current
	 * instance.
	 * 
	 * @return A copy of the current Constants instance with the same index.
	 */
	public Constants getCopyOf() {
		HashMap<String, Double> newMap = new HashMap<String, Double>();
		newMap.putAll(constants);
		Constants ret = new Constants(newMap,
				constantIndex);
		return ret;
	}

	/**
	 * Returns the value of the constants flattened in an array according to the
	 * index.
	 * 
	 * @return An array with the values of the constants according to the index.
	 */
	public double[] getFlatConstants() {
		double[] ret = new double[constantIndex.size()];
		for (Map.Entry<String, Integer> e : constantIndex.entrySet()) {
			ret[e.getValue()] = constants.get(e.getKey());
		}
		return ret;
	}

	/**
	 * Sets the current value of the given parameter.
	 * 
	 * @param name
	 * @param value
	 */
	public void setConstantValue(String name, Double value) {
		constants.put(name, value);
	}

	/**
	 * Returns textual representation of the system.
	 */
	public String toString() {
		StringBuilder out = new StringBuilder();
		for (Map.Entry<String, Double> e : constants.entrySet()) {
			out.append(e.getKey());
			out.append(" = ");
			String value = String.format("%.10f", e.getValue());		
			value = value.replaceAll("0+$", "").replaceAll("\\.$", ".0");		
			out.append(value);
			out.append(";\n");
		}
		return out.toString();
	}
}
