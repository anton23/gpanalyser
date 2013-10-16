package uk.ac.imperial.doc.pctmc.postprocessors.numerical;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.constants.visitors.ExpressionEvaluatorWithConstants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.jexpressions.javaoutput.statements.AbstractExpressionEvaluator;
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.analysis.PCTMCAnalysisPostprocessor;
import uk.ac.imperial.doc.pctmc.cppoutput.PCTMCCPPImplementationProvider;
import uk.ac.imperial.doc.pctmc.cppoutput.simulation.AccumulatorUpdaterPrinter;
import uk.ac.imperial.doc.pctmc.cppoutput.simulation.AggregatedStateNextEventGeneratorPrinter;
import uk.ac.imperial.doc.pctmc.cppoutput.simulation.NativeAggregatedStateNextEventGenerator;
import uk.ac.imperial.doc.pctmc.cppoutput.simulation.SimulationUpdaterPrinter;
import uk.ac.imperial.doc.pctmc.cppoutput.utils.CPPClassCompiler;
import uk.ac.imperial.doc.pctmc.representation.EvolutionEvent;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;
import uk.ac.imperial.doc.pctmc.simulation.PCTMCSimulation;
import uk.ac.imperial.doc.pctmc.simulation.SimulationUpdater;
import uk.ac.imperial.doc.pctmc.simulation.utils.AccumulatorUpdater;
import uk.ac.imperial.doc.pctmc.simulation.utils.GillespieSimulator;
import uk.ac.imperial.doc.pctmc.statements.odeanalysis.EvaluatorMethod;
import uk.ac.imperial.doc.pctmc.utils.PCTMCLogging;

public class CPPSimulationAnalysisNumericalPostprocessor
        extends NumericalPostprocessor {

	
	
	@Override
	public PCTMCAnalysisPostprocessor regenerate() {
		throw new AssertionError("Not implemented!");
	}

	private PCTMCSimulation simulation;

	private SimulationUpdater updater;
	private AccumulatorUpdater accUpdater;
	private NativeAggregatedStateNextEventGenerator eventGenerator;

    private PCTMC pctmc;
    private boolean initCoeff = true;
    private Collection<EvolutionEvent> observableEvents;


    private final int replications;

	@Override
	public String toString() {
		return "(stopTime = " + stopTime + ", stepSize = "
                + stepSize + ", replications = " + replications+")";
	}


	@Override
	public NumericalPostprocessor getNewPreparedPostprocessor
            (Constants constants) {
		// TODO Auto-generated method stub
		return null;
	}



	public CPPSimulationAnalysisNumericalPostprocessor
            (double stopTime, double stepSize, int replications) {
		super(stopTime, stepSize);
		this.replications = replications;
	}


	@Override
	public void prepare(AbstractPCTMCAnalysis analysis, Constants constants) {
		super.prepare(analysis, constants);
		simulation = null;
		if (analysis instanceof PCTMCSimulation) {
            this.simulation = (PCTMCSimulation) analysis;
            pctmc = simulation.getPCTMC();

            SimulationUpdaterPrinter printer = new SimulationUpdaterPrinter
                    (constants, momentIndex, simulation, generalExpectationIndex);
            updater = (SimulationUpdater) CPPClassCompiler
                .getInstance(printer.toClassString(), printer.getNativeClassName(),
                        printer.toString(), printer.getNativeClassName(),
                        SimulationUpdaterPrinter.PACKAGE);

            AccumulatorUpdaterPrinter accPrinter
                    = new AccumulatorUpdaterPrinter(constants, simulation);
            accUpdater = (AccumulatorUpdater) CPPClassCompiler
                .getInstance(accPrinter.toClassString(),
                        accPrinter.getNativeClassName(), accPrinter.toString(),
                        accPrinter.getNativeClassName(),
                        AccumulatorUpdaterPrinter.PACKAGE);

            observableEvents = new LinkedList<EvolutionEvent>();
            Collection<EvolutionEvent> events = pctmc.getEvolutionEvents();
            for (EvolutionEvent event : events) {
                if (!event.getRate().equals(DoubleExpression.ZERO)) {
                    observableEvents.add(event);
                }
            }

            PCTMCLogging.info("Generating one step generator.");

            AggregatedStateNextEventGeneratorPrinter egPrinter
                    = new AggregatedStateNextEventGeneratorPrinter
                    (constants, simulation, pctmc, observableEvents);
            eventGenerator = (NativeAggregatedStateNextEventGenerator)
                CPPClassCompiler.getInstance(egPrinter.toClassString(),
                        egPrinter.getNativeClassName(),
                        egPrinter.toString(), egPrinter.getNativeClassName(),
                        AggregatedStateNextEventGeneratorPrinter.PACKAGE);
		}
	}

	@Override
	public void calculateDataPoints(Constants constants) {
        dataPoints = new double[(int) Math.ceil(stopTime / stepSize)]
                [momentIndex.size() + generalExpectationIndex.size()];

		if (simulation!=null) {
			simulate(constants);
		}		
	}

    private void simulate(Constants constants) {

        if (initCoeff) {
            eventGenerator.setRates(constants.getFlatConstants());
            eventGenerator.initCoefficients(pctmc, observableEvents);
        }

		int n = pctmc.getStateIndex().size();
        double[] initial = new double[n];
		for (int i = 0; i < n; i++) {
			ExpressionEvaluatorWithConstants evaluator
                    = new ExpressionEvaluatorWithConstants(constants);
			pctmc.getInitCounts()[i].accept(evaluator);
			initial[i] = evaluator.getResult();
		}

		//PCTMCLogging.info("Running Gillespie simulator.");
		//PCTMCLogging.increaseIndent();

        if (initCoeff) {
            updater.setRates(constants.getFlatConstants());
            accUpdater.setRates(constants.getFlatConstants());
        }

		int m = momentIndex.size();

		double[][] tmp;
		for (int r = 0; r < replications; r++) {
			if (r > 0 && r % (replications / 5 > 0 ? replications/ 5 : 1) == 0) {
				PCTMCLogging.info(r + " replications finished.");
			}
			tmp = new GillespieSimulator().simulateAccumulated(eventGenerator,
                    initial, stopTime, stepSize, accUpdater);
			for (int t = 0; t < (int) Math.ceil(stopTime / stepSize); t++) {
				updater.update(dataPoints[t], tmp[t]);				
			}
		}

		for (int t = 0; t < dataPoints.length; t++) {
			for (int i = 0; i < m + generalExpectationIndex.size(); i++) {
				dataPoints[t][i] = dataPoints[t][i] / replications;
			}
		}
        initCoeff = false;
		//PCTMCLogging.decreaseIndent();
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
