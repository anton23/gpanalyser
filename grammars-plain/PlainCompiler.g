tree grammar PlainCompiler; 
 
options{
  language = Java; 
  tokenVocab = PlainParser; 
  ASTLabelType = CommonTree; 
 
}
import PCTMCCompilerPrototype;

@header{
  
  package uk.ac.imperial.doc.gpa.plain.syntax;
 
  
  import java.util.LinkedList;
  import java.util.Map;
  import java.util.HashMap;
  import java.util.Set;
  import java.util.HashSet;
  import java.util.LinkedHashMap;
  import java.util.Collection;
  
  import uk.ac.imperial.doc.jexpressions.expressions.*;
  import uk.ac.imperial.doc.jexpressions.conditions.*;
  import uk.ac.imperial.doc.jexpressions.constants.*;
  import uk.ac.imperial.doc.jexpressions.variables.*;
  
  import uk.ac.imperial.doc.pctmc.analysis.*;
  
  import uk.ac.imperial.doc.pctmc.odeanalysis.*; 
  import uk.ac.imperial.doc.pctmc.simulation.*;
  import uk.ac.imperial.doc.pctmc.compare.*;
  
  import uk.ac.imperial.doc.pctmc.expressions.*;
  import uk.ac.imperial.doc.pctmc.expressions.patterns.*;
  import uk.ac.imperial.doc.pctmc.plain.*;
  import uk.ac.imperial.doc.pctmc.representation.State;
  import uk.ac.imperial.doc.pctmc.representation.*; 
  import uk.ac.imperial.doc.pctmc.experiments.iterate.*; 
  import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.*; 
  import uk.ac.imperial.doc.pctmc.postprocessors.numerical.*;
  
  import uk.ac.imperial.doc.gpa.plain.expressions.*; 
  import uk.ac.imperial.doc.gpa.plain.representation.*;
  import uk.ac.imperial.doc.gpa.plain.representation.timed.*;
  import uk.ac.imperial.doc.gpa.plain.postprocessors.numerical.*;
   
  import uk.ac.imperial.doc.gpa.forecasting.postprocessors.numerical.*;
   
  import com.google.common.collect.Multimap;
  import com.google.common.collect.LinkedHashMultimap;
  
  import com.google.common.collect.HashMultiset;
  import com.google.common.collect.Multiset;
  
  
  import uk.ac.imperial.doc.gpa.syntax.CompilerError;
  import uk.ac.imperial.doc.pctmc.syntax.CustomRecognitionException;
  import uk.ac.imperial.doc.pctmc.syntax.ErrorReporter;
  
  import uk.ac.imperial.doc.jexpressions.constants.visitors.ExpressionEvaluatorWithConstants;
 
  import uk.ac.imperial.doc.pctmc.interpreter.IExtension;
  import uk.ac.imperial.doc.pctmc.experiments.distribution.DistributionSimulation;
  import uk.ac.imperial.doc.pctmc.experiments.distribution.GroupOfDistributions;
  import uk.ac.imperial.doc.pctmc.experiments.distribution.DistributionsAtAllTimes;
  import uk.ac.imperial.doc.pctmc.experiments.distribution.DistributionsAtTimes;
}



@members {
	// Reporting utility
    protected Stack<String> hint = new Stack<String>(); 
    protected ErrorReporter errorReporter;
     
    public void setErrorReporter(ErrorReporter errorReporter) {
    	this.errorReporter = errorReporter;
    }
     
    public String getErrorHeader(RecognitionException e) {
    	return "line "+e.line+":"+e.charPositionInLine;
  	}
     
	public void displayRecognitionError(String[] tokenNames,
                                        RecognitionException e) {
        String hdr = getErrorHeader(e);
        String msg = getErrorMessage(e, tokenNames);
       
        if (errorReporter != null) {
           errorReporter.addError("["+hdr + "] " + msg);
        }
    }
    
        
	public String getErrorMessage(RecognitionException e,
                              String[] tokenNames) {
        if (!hint.isEmpty()) {
          return hint.peek();
        }
        return  super.getErrorMessage(e, tokenNames);
	}
    
    // Events for time inhomogenous PCTMCs 
	protected TimedEvents mTimedEvents = new TimedEvents(); 

  	public PCTMC genNewPCTMC(Map<State,AbstractExpression> initMap, List<EvolutionEvent> events) {
		return new PlainPCTMC(initMap, events, mTimedEvents);
  	}
}

@rulecatch {
	catch (RecognitionException re) {
		reportError(re);  
		recover(input, re);
	}
	catch (AssertionError e) {
		reportError(new CustomRecognitionException(input, e.getMessage()));
		recover(input, new CustomRecognitionException(input, e.getMessage()));
	}
}

start:;

state returns [State t]
@init{
  List<String> components = new LinkedList<String>(); 
}
:
  ^(TRANSACTION (id=UPPERCASENAME{components.add($id.text);})+) {
  $t = new Transaction(components);
  }
| ^(COUNT c=UPPERCASENAME) {$t = new CountingState($c.text);}
;

rateFileDefinitions[]:
   (LOADRATES f=FILENAME INTO fun=LOWERCASENAME {mTimedEvents.addRateEventsFromFile($fun.text,$f.text.replace("\"","")); } SEMI)
;

jumpFileDefinitions[]:
   (LOADJUMPS f=FILENAME INTO t=state {mTimedEvents.addJumpEventsFromFile($t.t,$f.text.replace("\"","")); } SEMI)
;

resetFileDefinitions[]:
   (LOADRESETS f=FILENAME INTO t=state {mTimedEvents.addResetEventsFromFile($t.t,$f.text.replace("\"","")); } SEMI)
;

analysis[PCTMC pctmc, Constants constants, Multimap<AbstractPCTMCAnalysis,PlotDescription> plots]
returns [AbstractPCTMCAnalysis analysis, NumericalPostprocessor postprocessor]
:
(o=odeAnalysis[pctmc,constants] {$analysis=$o.analysis; $postprocessor=$o.postprocessor;}
 | io=inhomogeneousODEAnalysis[pctmc,constants] {$analysis=$io.analysis; $postprocessor=$io.postprocessor;}
 | f=forecastingAnalysis[pctmc,constants] {$analysis=$f.analysis; $postprocessor=$f.postprocessor;}
 | s=simulation[pctmc,constants] {$analysis=$s.analysis; $postprocessor=$s.postprocessor;}
 | is=inhomogeneousSimulation[pctmc,constants] {$analysis=$is.analysis; $postprocessor=$is.postprocessor;}
 | accs=accurateSimulation[pctmc,constants] {$analysis=$s.analysis; $postprocessor=$s.postprocessor;}
 | c=compare[pctmc, constants, plots] {$analysis=$c.analysis; $postprocessor=$c.postprocessor;}
)
 (LBRACE       
         ps=plotDescriptions
 RBRACE
 {
    if ($plots!=null) $plots.putAll($analysis,$ps.p);
 }
)? 
;

inhomogeneousODEAnalysis[PCTMC pctmc, Constants constants]
returns [PCTMCODEAnalysis analysis, NumericalPostprocessor postprocessor]
@init{
  Map<String, Object> parameters = new HashMap<String, Object>();
  Map<String, Object> postprocessorParameters = new HashMap<String, Object>();
}:
  ^(INHOMOGENEOUSODES
         (LBRACK
             p1=parameter {parameters.put($p1.name, $p1.value);}
             (COMMA p=parameter
                          {parameters.put($p.name, $p.value);})*
          RBRACK)?
         settings=odeSettings 
         {
		      $analysis = new PCTMCODEAnalysis($pctmc, parameters);
		      ExpressionEvaluatorWithConstants stopEval = new ExpressionEvaluatorWithConstants($constants);
		      $settings.stopTime.accept(stopEval);
		      ExpressionEvaluatorWithConstants stepEval = new ExpressionEvaluatorWithConstants($constants);
		      $settings.stepSize.accept(stepEval);
		      if (postprocessorParameters.isEmpty()) {
		        $postprocessor = new InhomogeneousODEAnalysisNumericalPostprocessor(stopEval.getResult(),
		            stepEval.getResult(),$settings.density);
		      } else {
		        $postprocessor = new InhomogeneousODEAnalysisNumericalPostprocessor(stopEval.getResult(),
		           stepEval.getResult(),$settings.density, postprocessorParameters);
		      }
		      $analysis.addPostprocessor($postprocessor);
      }
         
    )
;

forecastingAnalysis[PCTMC pctmc, Constants constants]
returns [PCTMCODEAnalysis analysis, NumericalPostprocessor postprocessor]
@init{
  Map<String, Object> parameters = new HashMap<String, Object>();
  Map<String, Object> postprocessorParameters = new HashMap<String, Object>();
}:
  ^(FORECASTING
         (LBRACK
             p1=parameter {parameters.put($p1.name, $p1.value);}
             (COMMA p=parameter
                          {parameters.put($p.name, $p.value);})*
          RBRACK)?
         settings=forecastingSettings 
         {
		      $analysis = new PCTMCODEAnalysis($pctmc, parameters);
		      ExpressionEvaluatorWithConstants stepEval = new ExpressionEvaluatorWithConstants($constants);
		      $settings.stepSize.accept(stepEval);
		      if (postprocessorParameters.isEmpty()) {
		        $postprocessor = new ForecastingODEAnalysisNumericalPostprocessor(stepEval.getResult(),
		            $settings.density,$settings.warmup, $settings.forecast, $settings.ibf, $settings.arrState, $settings.startStates,
		            $settings.startDeltas, $settings.tsStep, $settings.arrTS, $settings.depTS);
		      } else {
		        $postprocessor = new ForecastingODEAnalysisNumericalPostprocessor(stepEval.getResult(),
		            $settings.density,$settings.warmup, $settings.forecast, $settings.ibf, $settings.arrState, $settings.startStates,
		            $settings.startDeltas, $settings.tsStep, $settings.arrTS, $settings.depTS, postprocessorParameters);
		      }
		      $analysis.addPostprocessor($postprocessor);
      }
         
    )
;

forecastingSettings returns
  [AbstractExpression stepSize, int density, int warmup, int forecast,
   int ibf, State arrState, List<State> startStates, List<String> startDeltas,
   int tsStep, List<String> arrTS, List<String> depTS]:
	^(FORECASTINGSETTINGS 
	stepSizeTmp=expression COMMA
    densityTmp=INTEGER COMMA
    warmupTmp=INTEGER COMMA
    forecastTmp=INTEGER COMMA
    ibfTmp=INTEGER COMMA
    arrStateTmp=state COMMA
    startStatesTmp=listOfStates COMMA
    startDeltasTmp=listOfStrings COMMA
    tsStepTmp=INTEGER COMMA
    arrTSTmp=listOfStrings COMMA
    depTSTmp=listOfStrings)
  {
      $stepSize = $stepSizeTmp.e;
      $density = Integer.parseInt($densityTmp.text);
      $warmup = Integer.parseInt($warmupTmp.text);
      $forecast = Integer.parseInt($forecastTmp.text);
      $ibf = Integer.parseInt($ibfTmp.text);
      $arrState = $arrStateTmp.t;
      $startStates = $startStatesTmp.l;
      $startDeltas = $startDeltasTmp.l;
      $tsStep = Integer.parseInt($tsStepTmp.text);
      $arrTS = $arrTSTmp.l;
      $depTS = $depTSTmp.l;
  }
;

listOfStrings returns [List<String> l]
@init {l = new LinkedList<String>();}
:
  ^(c1=LOWERCASENAME {l.add($c1.text);} (COMMA c2=LOWERCASENAME {l.add($c2.text);})*) 
| ^(f1=FILENAME {l.add($f1.text.replace("\"",""));} (COMMA f2=FILENAME {l.add($f2.text.replace("\"",""));})*)
;

listOfStates returns [List<State> l]
@init {l = new LinkedList<State>();}
:
  ^(LOS s1=state {l.add(s1);} (COMMA s2=state {l.add(s2);})*)
;

inhomogeneousSimulation[PCTMC pctmc, Constants constants]
returns [PCTMCSimulation analysis, InhomogeneousSimulationAnalysisNumericalPostprocessor postprocessor]
@init{
  Map<String, Object> parameters = new HashMap<String, Object>();
}:
  ^(INHOMOGENEOUSSIMULATION settings=simulationSettings
    (COMMA p=parameter {parameters.put($p.name, $p.value);})*    
    {
      $analysis = new PCTMCSimulation($pctmc);

      ExpressionEvaluatorWithConstants stopEval = new ExpressionEvaluatorWithConstants($constants);
      $settings.stopTime.accept(stopEval);
      ExpressionEvaluatorWithConstants stepEval = new ExpressionEvaluatorWithConstants($constants);
      $settings.stepSize.accept(stepEval);
      if (parameters.isEmpty()) {
        $postprocessor = new InhomogeneousSimulationAnalysisNumericalPostprocessor(stopEval.getResult(),stepEval.getResult(),$settings.replications);
      } else {
        $postprocessor = new InhomogeneousSimulationAnalysisNumericalPostprocessor(
            stopEval.getResult(),stepEval.getResult(),$settings.replications, parameters);
      }
      $analysis.addPostprocessor($postprocessor);
    }
       
   )
;