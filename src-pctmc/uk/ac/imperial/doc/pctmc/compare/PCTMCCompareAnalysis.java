package uk.ac.imperial.doc.pctmc.compare;

import java.util.Collection;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;

//TODO need a postprocessor
public class PCTMCCompareAnalysis extends AbstractPCTMCAnalysis{


	@Override
	public void prepare(Constants constants) {
		analysis1.prepare(constants);
		analysis2.prepare(constants);	
	}

	@Override
	public String toString() {
		return "Compare("+analysis1.toString() + "," + analysis2.toString() + ")"; 
	}

	private AbstractPCTMCAnalysis analysis1, analysis2; 

	public PCTMCCompareAnalysis(AbstractPCTMCAnalysis analysis1, AbstractPCTMCAnalysis analysis2) {
		super(analysis1.getPCTMC(), analysis1.getStopTime(), analysis1.getStepSize());
		if (analysis1.getStepSize()!=analysis2.getStepSize() || analysis1.getStopTime()!=analysis2.getStopTime()){
			throw new AssertionError("Incompatible analyses in compare!"); 
		}
		this.analysis1 = analysis1; 
		this.analysis2 = analysis2;
	} 
	
	@Override
	public void setUsedMoments(
			Collection<CombinedPopulationProduct> combinedProducts) {
			analysis1.setUsedMoments(combinedProducts);
			analysis2.setUsedMoments(combinedProducts);
	}
	


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((analysis1 == null) ? 0 : analysis1.hashCode());
		result = prime * result
				+ ((analysis2 == null) ? 0 : analysis2.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		PCTMCCompareAnalysis other = (PCTMCCompareAnalysis) obj;
		if (analysis1 == null) {
			if (other.analysis1 != null)
				return false;
		} else if (!analysis1.equals(other.analysis1))
			return false;
		if (analysis2 == null) {
			if (other.analysis2 != null)
				return false;
		} else if (!analysis2.equals(other.analysis2))
			return false;
		return true;
	}
}
