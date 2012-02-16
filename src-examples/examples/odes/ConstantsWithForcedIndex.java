package examples.odes;

import java.util.Map;

import uk.ac.imperial.doc.jexpressions.constants.Constants;

public class ConstantsWithForcedIndex extends Constants{

	public ConstantsWithForcedIndex(Map<String, Double> constants, Map<String, Integer> index) {
		super(constants);
		this.constantIndex = index;
	}

}
