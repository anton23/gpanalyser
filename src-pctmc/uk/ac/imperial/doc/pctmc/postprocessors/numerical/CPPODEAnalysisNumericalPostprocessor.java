package uk.ac.imperial.doc.pctmc.postprocessors.numerical;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.constants.visitors.ExpressionEvaluatorWithConstants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.javaoutput.statements.AbstractExpressionEvaluator;
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.analysis.PCTMCAnalysisPostprocessor;
import uk.ac.imperial.doc.pctmc.cppoutput.PCTMCCPPImplementationProvider;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.implementation.PCTMCImplementationPreprocessed;
import uk.ac.imperial.doc.pctmc.odeanalysis.PCTMCODEAnalysis;
import uk.ac.imperial.doc.pctmc.representation.State;
import uk.ac.imperial.doc.pctmc.statements.odeanalysis.EvaluatorMethod;

import java.util.List;
import java.util.Map;

public class CPPODEAnalysisNumericalPostprocessor extends NumericalPostprocessor {

	
	
	@Override
	public PCTMCAnalysisPostprocessor regenerate() {
		throw new AssertionError("Not implemented!");
	}

	private PCTMCODEAnalysis odeAnalysis;
    private final PCTMCCPPImplementationProvider pcctmc
            = new PCTMCCPPImplementationProvider();

    Map<String, Object> parameters;


	public CPPODEAnalysisNumericalPostprocessor(double stopTime, double stepSize,
			Map<String, Object> parameters) {
		super(stopTime, stepSize);
		this.parameters = parameters;
	}

	private CPPODEAnalysisNumericalPostprocessor(double stopTime, double stepSize, Map<String, Object> parameters,
            PCTMCODEAnalysis odeAnalysis, PCTMCImplementationPreprocessed preprocessedImplementation) {
		this(stopTime, stepSize, parameters);
		this.odeAnalysis = odeAnalysis;
		this.preprocessedImplementation = preprocessedImplementation;
		this.momentIndex = odeAnalysis.getMomentIndex();
		this.generalExpectationIndex = odeAnalysis.getGeneralExpectationIndex();
		this.dataPoints = null;
	}
	

	@Override
	public NumericalPostprocessor getNewPreparedPostprocessor(Constants constants) {
		assert(odeAnalysis!=null);
		PCTMCCPPImplementationProvider cppImplementation = new PCTMCCPPImplementationProvider();
		return new CPPODEAnalysisNumericalPostprocessor
                (stopTime, stepSize, parameters, odeAnalysis,
                        cppImplementation.getPreprocessedODEImplementation(
						odeAnalysis.getOdeMethod(), constants, momentIndex));
	}

	@Override
	public String toString() {
		return "(stopTime = " + stopTime + ", stepSize = " + stepSize + ")"; 
	}

	@Override
    public void prepare(AbstractPCTMCAnalysis analysis, Constants constants) {
        super.prepare(analysis, constants);
        odeAnalysis = null;
        if (analysis instanceof PCTMCODEAnalysis) {
            this.odeAnalysis = (PCTMCODEAnalysis) analysis;
            preprocessedImplementation = pcctmc.getPreprocessedODEImplementation
                    (odeAnalysis.getOdeMethod(), constants, momentIndex);
        } else {
            throw new AssertionError("ODE postprocessor attached to an incompatible analysis " + analysis.toString());
        }
    }

	private PCTMCImplementationPreprocessed preprocessedImplementation;

	@Override
	public void calculateDataPoints(Constants constants) {
		if (odeAnalysis != null) {
			initial = getInitialValues(constants);			
			dataPoints = pcctmc.runODEAnalysis(
					preprocessedImplementation, initial, stopTime, stepSize,
					parameters, constants);
		}
	}

	public PCTMCImplementationPreprocessed getPreprocessedImplementation() {
		return preprocessedImplementation;
	}

	protected double[] initial;

	public double[] getInitialValues(Constants constants) {
		initial = new double[momentIndex.size()];

		Map<State, Integer> stateIndex = odeAnalysis.getPCTMC()
				.getStateIndex();
		int size = stateIndex.size();
		double[] initialCounts = new double[size];

		for (int i = 0; i < size; i++) {
			ExpressionEvaluatorWithConstants evaluator
                    = new ExpressionEvaluatorWithConstants(constants);
			odeAnalysis.getPCTMC().getInitCounts()[i].accept(evaluator);
			initialCounts[i] = evaluator.getResult();
		}

		for (Map.Entry<CombinedPopulationProduct, Integer> e : momentIndex
				.entrySet()) {
			if (!e.getKey().getAccumulatedProducts().isEmpty()) {
				initial[e.getValue()] = 0;
			} else {
				double tmp = 1.0;

				for (Map.Entry<State, Integer> s : e.getKey().getNakedProduct()
						.getRepresentation().entrySet()) {
					for (int p = 0; p < s.getValue(); p++) {
						if (!stateIndex.containsKey(s.getKey())) {
							throw new AssertionError("State " + s.getKey()
									+ " unknown!");
						}
						tmp *= initialCounts[stateIndex.get(s.getKey())];
					}
				}
				initial[e.getValue()] = tmp;
			}
		}
		return initial;
	}

    /**
     * Returns an object providing updates to expressions from moment data.
     * @param plotExpressions
     * @param constants
     * @return
     */
    @Override
    public AbstractExpressionEvaluator getExpressionEvaluator(
            final List<AbstractExpression> plotExpressions, Constants constants) {
        EvaluatorMethod updaterMethod = getEvaluatorMethod(plotExpressions, constants);
        return new PCTMCCPPImplementationProvider()
                .getEvaluatorImplementation(updaterMethod, evaluatorClassName,
                        constants, momentIndex, generalExpectationIndex);
    }
}
