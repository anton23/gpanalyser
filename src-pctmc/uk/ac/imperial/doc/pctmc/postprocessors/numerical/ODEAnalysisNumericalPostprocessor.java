package uk.ac.imperial.doc.pctmc.postprocessors.numerical;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.constants.visitors.ExpressionEvaluatorWithConstants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.utils.ToStringUtils;
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.analysis.PCTMCAnalysisPostprocessor;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.javaoutput.JavaODEsPreprocessed;
import uk.ac.imperial.doc.pctmc.javaoutput.PCTMCJavaImplementationProvider;
import uk.ac.imperial.doc.pctmc.odeanalysis.PCTMCODEAnalysis;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;
import uk.ac.imperial.doc.pctmc.representation.PCTMCWithAccumulations;
import uk.ac.imperial.doc.pctmc.representation.State;
import uk.ac.imperial.doc.pctmc.representation.accumulations.AccumulationVariable;
import uk.ac.imperial.doc.pctmc.representation.accumulations.NamedAccumulation;
import uk.ac.imperial.doc.pctmc.utils.FileUtils;

import com.google.common.collect.Multiset;

public class ODEAnalysisNumericalPostprocessor extends NumericalPostprocessor {

	private PCTMCODEAnalysis odeAnalysis;

	private Map<String, Object> parameters;

	private String overrideCode;
	private String overrideCodeClassName;
	
	
	@Override
	public PCTMCAnalysisPostprocessor regenerate() {
		return new ODEAnalysisNumericalPostprocessor(stopTime, stepSize, parameters);
	}

	private ODEAnalysisNumericalPostprocessor(double stopTime, double stepSize, Map<String, Object> parameters,
			PCTMCODEAnalysis odeAnalysis, JavaODEsPreprocessed preprocessedImplementation) {
		this(stopTime, stepSize, parameters);
		this.odeAnalysis = odeAnalysis;
		this.preprocessedImplementation = preprocessedImplementation;
		this.momentIndex = odeAnalysis.getMomentIndex();
		this.generalExpectationIndex = odeAnalysis.getGeneralExpectationIndex();
		this.dataPoints = null;
	}
	
	
	public ODEAnalysisNumericalPostprocessor(double stopTime, double stepSize,
			 Map<String, Object> parameters) {
		super(stopTime, stepSize);
		if (parameters.containsKey("overrideCode")) {
			Object value = parameters.get("overrideCode");
			if (value instanceof String) {
				String asString = ((String) value);
				try {
					overrideCode =  FileUtils.readFile(asString);
					String[] split = asString.split("/");
					overrideCodeClassName = split[split.length-1].replace(".java", "");
				}
				catch (IOException e) {
					throw new AssertionError("File + " + asString + " cannot be open!");
				}
				
			} else {
				throw new AssertionError("Given value of 'overrideCode' has to be a filename!");
			}
		}
		this.parameters = parameters;
	}

	@Override
	public NumericalPostprocessor getNewPreparedPostprocessor(Constants constants) {
		assert(odeAnalysis!=null);
		PCTMCJavaImplementationProvider javaImplementation = new PCTMCJavaImplementationProvider();
		ODEAnalysisNumericalPostprocessor ret = new ODEAnalysisNumericalPostprocessor(stopTime, stepSize, parameters, odeAnalysis, javaImplementation
				.getPreprocessedODEImplementation(
						odeAnalysis.getOdeMethod(), constants, momentIndex));
		return ret;
	}

	@Override
	public String toString() {
		return "(stopTime = " + stopTime + ", stepSize = " + stepSize + ", " + ToStringUtils.mapToDefinitionList(parameters, "=", ", ") + ")"; 
	}

	@Override
	public void prepare(AbstractPCTMCAnalysis analysis, Constants constants) {
		super.prepare(analysis, constants);
		odeAnalysis = null;
		if (analysis instanceof PCTMCODEAnalysis) {
			this.odeAnalysis = (PCTMCODEAnalysis) analysis;
			PCTMCJavaImplementationProvider javaImplementation = new PCTMCJavaImplementationProvider();
			if (overrideCode == null) {
				preprocessedImplementation = javaImplementation
						.getPreprocessedODEImplementation(
								odeAnalysis.getOdeMethod(), constants, momentIndex);
			} else {
				preprocessedImplementation = javaImplementation.getPreprocessedODEImplementationFromCode(overrideCode, overrideCodeClassName);
			}
		} else {
			throw new AssertionError("ODE postprocessor attached to an incompatible analysis " + analysis.toString());
		}
		
	}

	private JavaODEsPreprocessed preprocessedImplementation;

	@Override
	public void calculateDataPoints(Constants constants) {
		if (odeAnalysis != null) {
			initial = getInitialValues(constants);			
			dataPoints = new PCTMCJavaImplementationProvider().runODEAnalysis(
					preprocessedImplementation, initial, stopTime, stepSize,
					parameters, constants);
		}
	}

	public JavaODEsPreprocessed getPreprocessedImplementation() {
		return preprocessedImplementation;
	}

	protected double[] initial;

	public double[] getInitialValues(Constants constants) {
		initial = new double[momentIndex.size()];

		PCTMC pctmc = odeAnalysis.getPCTMC();
		Map<State, Integer> stateIndex = pctmc
				.getStateIndex();
		int size = stateIndex.size();
		double[] initialCounts = new double[size];

		for (int i = 0; i < size; i++) {
			ExpressionEvaluatorWithConstants evaluator = new ExpressionEvaluatorWithConstants(
					constants);
			pctmc.getInitCounts()[i].accept(evaluator);
			initialCounts[i] = evaluator.getResult();
		}
		
		Map<NamedAccumulation, Double> initialAcc = new HashMap<NamedAccumulation, Double>();
		if (pctmc instanceof PCTMCWithAccumulations) {
			Map<NamedAccumulation, AbstractExpression> accInit = ((PCTMCWithAccumulations)pctmc).getAccInit();
			for (Map.Entry<NamedAccumulation, AbstractExpression> e : accInit.entrySet()) {
				ExpressionEvaluatorWithConstants evaluator = new ExpressionEvaluatorWithConstants(
						constants);
				e.getValue().accept(evaluator);
				initialAcc.put(e.getKey(), evaluator.getResult());
			}
		}
		
		for (Map.Entry<CombinedPopulationProduct, Integer> e : momentIndex
				.entrySet()) {
			
				double tmp = 1.0;

				for (Multiset.Entry<State> s : e.getKey().getPopulationProduct()
						.getRepresentation().entrySet()) {
					for (int p = 0; p < s.getCount(); p++) {
						if (!stateIndex.containsKey(s.getElement())) {
							throw new AssertionError("State " + s.getElement()
									+ " unknown!");
						}
						tmp *= initialCounts[stateIndex.get(s.getElement())];
					}
				}
				for (Multiset.Entry<AccumulationVariable> a : e.getKey().getAccumulatedProducts().entrySet()) {
					if (a.getElement() instanceof NamedAccumulation) {
						Double init = initialAcc.get((NamedAccumulation)a.getElement());
						for (int p = 0; p < a.getCount(); p++) {
							tmp *= init;
						}						
					} else {
						tmp = 0;
					}
				}
				initial[e.getValue()] = tmp;
		}		
		return initial;
	}
}
