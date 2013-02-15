package uk.ac.imperial.doc.pctmc.postprocessors.numerical;

import java.io.IOException;
import java.util.Map;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.constants.visitors.ExpressionEvaluatorWithConstants;
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.analysis.PCTMCAnalysisPostprocessor;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.javaoutput.JavaODEsPreprocessed;
import uk.ac.imperial.doc.pctmc.javaoutput.PCTMCJavaImplementationProvider;
import uk.ac.imperial.doc.pctmc.odeanalysis.PCTMCODEAnalysis;
import uk.ac.imperial.doc.pctmc.representation.State;
import uk.ac.imperial.doc.pctmc.utils.FileUtils;

public class ODEAnalysisNumericalPostprocessor extends NumericalPostprocessor {

	private PCTMCODEAnalysis odeAnalysis;

	private int density;
	
	private Map<String, Object> parameters;

	private String overrideCode;
	private String overrideCodeClassName;
	
	public int getDensity() {
		return density;
	}

	public void setDensity(int density) {
		this.density = density;
	}

	public ODEAnalysisNumericalPostprocessor(double stopTime, double stepSize,
			int density) {
		super(stopTime, stepSize);
		this.density = density;
	}
	
	
	
	@Override
	public PCTMCAnalysisPostprocessor regenerate() {
		return new ODEAnalysisNumericalPostprocessor(stopTime, stepSize, density);
	}

	private ODEAnalysisNumericalPostprocessor(double stopTime, double stepSize, int density,
			PCTMCODEAnalysis odeAnalysis, JavaODEsPreprocessed preprocessedImplementation) {
		this(stopTime, stepSize, density);
		this.odeAnalysis = odeAnalysis;
		this.preprocessedImplementation = preprocessedImplementation;
		this.momentIndex = odeAnalysis.getMomentIndex();
		this.generalExpectationIndex = odeAnalysis.getGeneralExpectationIndex();
		this.dataPoints = null;
	}
	
	
	public ODEAnalysisNumericalPostprocessor(double stopTime, double stepSize,
			int density, Map<String, Object> parameters) {
		this(stopTime, stepSize, density);
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
		ODEAnalysisNumericalPostprocessor ret = new ODEAnalysisNumericalPostprocessor(stopTime, stepSize, density, odeAnalysis, javaImplementation
				.getPreprocessedODEImplementation(
						odeAnalysis.getOdeMethod(), constants, momentIndex));
		return ret;
	}

	@Override
	public String toString() {
		return "(stopTime = " + stopTime + ", stepSize = " + stepSize + ", density = " + density+")"; 
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
					density, constants);
		}
	}

	public JavaODEsPreprocessed getPreprocessedImplementation() {
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
			ExpressionEvaluatorWithConstants evaluator = new ExpressionEvaluatorWithConstants(
					constants);
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
}
