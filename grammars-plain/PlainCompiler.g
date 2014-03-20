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
  // Events for time inhomogenous PCTMCs 
  protected TimedEvents mTimedEvents = new TimedEvents(); 
  
  public void setErrorReporter(ErrorReporter errorReporter) {
    this.errorReporter = errorReporter;
  }
     
  public String getErrorHeader(RecognitionException e) {
  	return "line "+e.line+":"+e.charPositionInLine;
  }
     
	public void displayRecognitionError(
    String[] tokenNames,
    RecognitionException e
  ) {
    String hdr = getErrorHeader(e);
    String msg = getErrorMessage(e, tokenNames);
    if (errorReporter != null) {
      errorReporter.addError("[" + hdr + "] " + msg);
    }
  }

	public String getErrorMessage(
	  RecognitionException e,
    String[] tokenNames
  ) {
    if (!hint.isEmpty()) {
      return hint.peek();
    }
    return super.getErrorMessage(e, tokenNames);
	}

  public PCTMC genNewPCTMC(
    Map<State,AbstractExpression> initMap,
    List<EvolutionEvent> events
  ) {
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
| ^(COUNT c=UPPERCASENAME) {
    $t = new CountingState($c.text);
  }
;

rateFileDefinitions[]:
  LOADRATES f=FILENAME INTO fun=LOWERCASENAME {
    mTimedEvents.addRateEventsFromFile($fun.text,$f.text.replace("\"",""));
  } SEMI
;

jumpFileDefinitions[]:
  LOADJUMPS f=FILENAME INTO t=state {
    mTimedEvents.addJumpEventsFromFile($t.t,$f.text.replace("\"",""));
  } SEMI
;

resetFileDefinitions[]:
  LOADRESETS f=FILENAME INTO t=state {
    mTimedEvents.addResetEventsFromFile($t.t,$f.text.replace("\"",""));
  } SEMI
;

analysis[PCTMC pctmc, Constants constants, Multimap<AbstractPCTMCAnalysis,PlotDescription> plots]
returns [AbstractPCTMCAnalysis analysis, NumericalPostprocessor postprocessor]:
  (
      o=odeAnalysis[pctmc,constants] {$analysis=$o.analysis; $postprocessor=$o.postprocessor;}
    | accs=accurateSimulation[pctmc,constants] {$analysis=$s.analysis; $postprocessor=$s.postprocessor;}
    | c=compare[pctmc, constants, plots] {$analysis=$c.analysis; $postprocessor=$c.postprocessor;}
    | s=simulation[pctmc,constants] {$analysis=$s.analysis; $postprocessor=$s.postprocessor;}
    | f=odeBikeFcastAnalysis[pctmc,constants] {$analysis=$f.analysis; $postprocessor=$f.postprocessor;}
    | fs=simBikeFcastAnalysis[pctmc,constants] {$analysis=$fs.analysis; $postprocessor=$fs.postprocessor;}
    | ls=tsRBikeFcastAnalysis[pctmc,constants] {$analysis=$ls.analysis; $postprocessor=$ls.postprocessor;}
  )
  (LBRACE ps = plotDescriptions RBRACE
    {
      if ($plots!=null) $plots.putAll($analysis,$ps.p);
    }
  )? 
;

odeBikeFcastAnalysis[PCTMC pctmc, Constants constants]
returns [
  PCTMCODEAnalysis analysis,
  NumericalPostprocessor postprocessor
]
@init{
  Map<String, Object> parameters = new HashMap<String, Object>();
  Map<String, Object> postprocessorParameters = new HashMap<String, Object>();
}:
  ^(ODE_BIKE_FCAST
    (
      LBRACK
      p1 = parameter {parameters.put($p1.name, $p1.value);}
      (COMMA p = parameter {parameters.put($p.name, $p.value);})*
      RBRACK COMMA
    )?
    stepSize = expression COMMA 
    density = INTEGER COMMA
    cfg = bikeFcastConfig
  )
  {
    $analysis = new PCTMCODEAnalysis($pctmc, parameters);
    ExpressionEvaluatorWithConstants stepEval =
      new ExpressionEvaluatorWithConstants($constants);
    $stepSize.e.accept(stepEval);
    if (postprocessorParameters.isEmpty()) {
      $postprocessor = new BikeArrivalODEPostprocessor(
        stepEval.getResult(), Integer.parseInt($density.text), $cfg.cfg
      );
    } else {
      $postprocessor = new BikeArrivalODEPostprocessor(
        stepEval.getResult(), Integer.parseInt($density.text), $cfg.cfg,
        postprocessorParameters
      );
    }
    $analysis.addPostprocessor($postprocessor);
  }
;

simBikeFcastAnalysis[PCTMC pctmc, Constants constants]
returns [
  PCTMCSimulation analysis,
  NumericalPostprocessor postprocessor
]
@init{
  Map<String, Object> parameters = new HashMap<String, Object>();
  Map<String, Object> postprocessorParameters = new HashMap<String, Object>();
}:
  ^(SIM_BIKE_FCAST
    stepSize = expression COMMA
    replications = INTEGER COMMA
    cfg = bikeFcastConfig
  )
  {
    $analysis = new PCTMCSimulation($pctmc);
    ExpressionEvaluatorWithConstants stepEval =
      new ExpressionEvaluatorWithConstants($constants);
    $stepSize.e.accept(stepEval);
    if (postprocessorParameters.isEmpty()) {
      $postprocessor = new BikeArrivalSimPostprocessor(
        stepEval.getResult(), Integer.parseInt($replications.text), $cfg.cfg
      );
    } else {
      $postprocessor = new BikeArrivalSimPostprocessor(
        stepEval.getResult(), Integer.parseInt($replications.text), $cfg.cfg,
        postprocessorParameters
      );
    }
    $analysis.addPostprocessor($postprocessor);
  }
;

tsRBikeFcastAnalysis[PCTMC pctmc, Constants constants]
returns [
  PCTMCTSR analysis,
  NumericalPostprocessor postprocessor
]
@init{
  Map<String, Object> parameters = new HashMap<String, Object>();
  Map<String, Object> postprocessorParameters = new HashMap<String, Object>();
}:
  ^(TS_R_BIKE_FCAST
    arrFcastMode = LOWERCASENAME COMMA
    minXreg = INTEGER COMMA
    cfg = bikeFcastConfig
  )
  {
    $analysis = new PCTMCTSR($pctmc, $arrFcastMode.text);
    if (postprocessorParameters.isEmpty()) {
      $postprocessor = new BikeArrivalTSRPostprocessor(
        $arrFcastMode.text, Integer.parseInt($minXreg.text), $cfg.cfg
      );
    } else {
      $postprocessor = new BikeArrivalTSRPostprocessor(
        $arrFcastMode.text, Integer.parseInt($minXreg.text), $cfg.cfg,
        postprocessorParameters
      );
    }
    $analysis.addPostprocessor($postprocessor);
  }
;

bikeFcastConfig returns [
  BikeModelRBridge cfg
]:
  ^(BIKE_FCAST_CFG 
    fcastWarmupTmp = INTEGER COMMA
    fcastLenTmp = INTEGER COMMA
    fcastFreqTmp = INTEGER COMMA
    clDepStatesTmp = listOfStates COMMA
    clArrStatesTmp = listOfStates COMMA
    depFcastModeTmp = LOWERCASENAME COMMA
    trainClDepTSTmp = listOfFiles COMMA
    trainClDepToDestTSTmp = listOfFiles COMMA    
    trainClArrTSTmp = listOfFiles COMMA
    clDepTSTmp = listOfFiles COMMA
    clDepToDestTSTmp = listOfFiles COMMA    
    clArrTSTmp = listOfFiles
  ) {
    $cfg = new BikeModelRBridge(
      Integer.parseInt($fcastWarmupTmp.text),
      Integer.parseInt($fcastLenTmp.text),
      Integer.parseInt($fcastFreqTmp.text),
      $clDepStatesTmp.l,
      $clArrStatesTmp.l,
      $depFcastModeTmp.text,
      $trainClDepTSTmp.l,
      $trainClDepToDestTSTmp.l,
      $trainClArrTSTmp.l,
      $clDepTSTmp.l,
      $clDepToDestTSTmp.l,
      $clArrTSTmp.l
    );
  }
;

listOfStates returns [List<State> l]
@init {l = new LinkedList<State>();}
:
  ^(LOS
    s1=state {l.add(s1);}
    (COMMA s2=state {l.add(s2);})*
  )
;

listOfConsts returns [List<String> l]
@init {l = new LinkedList<String>();}
:
  ^(LOC
    c1=LOWERCASENAME {l.add($c1.text);}
    (COMMA c2=LOWERCASENAME {l.add($c2.text);})*
  )
;

listOfFiles returns [List<String> l]
@init {l = new LinkedList<String>();}
:
  ^(LOF
    f1=FILENAME {l.add($f1.text.replace("\"",""));}
    (COMMA f2=FILENAME {l.add($f2.text.replace("\"",""));})*
  )
;