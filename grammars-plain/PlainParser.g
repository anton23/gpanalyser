parser grammar PlainParser;

options {
  language = Java;
  output = AST; 
  ASTLabelType = CommonTree; 
  tokenVocab = PlainLexer; 
  backtrack = true;
}

import PCTMCParserPrototype;

tokens{
  COMPONENT; 
  CONSTANT;
  VARIABLE; 
  TRANSACTION; 
  TLIST; 
  FUN;
  EVENT; 
  INIT;
  PRODUCT; 
  COMBINEDPRODUCT; 
  RANGE;
  BIKE_FCAST_CFG;
  LOS;
  LOC;
  LOF;
}

@header{
   package uk.ac.imperial.doc.gpa.plain.syntax;
   
   import uk.ac.imperial.doc.pctmc.syntax.ErrorReporter;  
   import uk.ac.imperial.doc.pctmc.syntax.CustomRecognitionException;
   import uk.ac.imperial.doc.pctmc.syntax.ParsingData;
   import uk.ac.imperial.doc.pctmc.syntax.GPAParsingData;
   import uk.ac.imperial.doc.pctmc.syntax.PlainParsingData;
   
   import com.google.common.collect.HashMultiset;
   import com.google.common.collect.Multiset;
   
   import uk.ac.imperial.doc.pctmc.syntax.CustomRecognitionException;
   import uk.ac.imperial.doc.pctmc.syntax.ErrorReporter;  
   import java.util.LinkedList;
   import java.util.HashSet;
   import java.util.Set;
}

//This is a hack until the composite grammars are implemented in a better way
@members{
  public final String fcastSettingsHint =
    "  fcastWarmup=<integer>,\n" +
    "  fcastLen=<integer>,\n" +
    "  fcastFreq=<integer>,\n" +
    "  clDepStates={<state>,...},\n" +
    "  clArrStates={<state>,...},\n" +
    "  depFcastMode=<lowercasename>,\n" +
    "  trainClDepTS={\"file1\",...},\n" +
    "  trainClDepToDestTS={\"file1\",...},\n" +
    "  trainClArrTS={\"file1\",...},\n" +
    "  clDepTS={\"file1\",...},\n" +
    "  clDepToDestTS={\"file1\",...},\n" + 
    "  clArrTS={\"file1\",...}\n";
  protected Stack<String> hint;
  protected ErrorReporter errorReporter;
  protected Set<Multiset<String>> tmpStates = new HashSet<Multiset<String>>();
  protected Set<Multiset<String>> states  = new HashSet<Multiset<String>>();
  
  public void setErrorReporter(ErrorReporter errorReporter) {    
    this.errorReporter = errorReporter;
    gPCTMCParserPrototype.setErrorReporter(errorReporter);
    hint = gPCTMCParserPrototype.hint;
  }

  public ParsingData getParsingData() { 
    return new PlainParsingData(tmpStates);  
  }
  
  public void setParsingData(ParsingData parsingData) {
    if (parsingData instanceof PlainParsingData) {
      states = ((PlainParsingData)parsingData).getStates();            
    }
  }

  public String getErrorHeader(RecognitionException e) {
    return "line " + e.line + ":" + e.charPositionInLine;
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
    String ret = gPCTMCParserPrototype.getModifiedErrorMessage(e, tokenNames);
    if (!hint.isEmpty()) {
     return " (" + hint.peek() + ")";
    }
    if (!gPCTMCParserPrototype.hint.isEmpty()) { 
      return " (" + gPCTMCParserPrototype.hint.peek() + ")";
    } 
    return ret;
  }
}

state: 
  t = transaction | countPattern
;
 
countPattern:
  COUNT UPPERCASENAME -> ^(COUNT UPPERCASENAME)
;

transaction:
  LBRACE cl = componentList RBRACE
  {
    if (gPCTMCParserPrototype.define) {
      tmpStates.add($cl.components);
    } 
    else if (gPCTMCParserPrototype.requireDefinitions) {
      if (!states.contains($cl.components)) {
        reportError(new CustomRecognitionException(
          input, "invalid population {" + $cl.text +"}"
        ));
      }
    }
  }    
-> ^(TRANSACTION componentList);

componentList returns [Multiset<String> components]
  @init {
    $components = HashMultiset.<String>create();
  }:
  n1=UPPERCASENAME {$components.add($n1.text);}
  (COMMA n2=UPPERCASENAME {$components.add($n2.text);})* -> UPPERCASENAME+ 
;

analysis:
  (
      odeAnalysis
    | simulation
    | accurateSimulation
    | compare
    | odeBikeFcast
    | simBikeFcast
    | tsRBikeFcast
  )
  (LBRACE plotDescription* RBRACE)?
;

odeBikeFcast:
  ODE_BIKE_FCAST
  odeParameters?
  {
    hint.push("ODE based bike journey forecasting analysis has syntax\n"+
      "OdeBikeFcast(\n  stepSize=<number>,\n" +
      "  density=<integer>,\n" + fcastSettingsHint + ")"
    );
  }
  LPAR
    STEPSIZE DEF stepSize = expression COMMA
    DENSITY DEF density = INTEGER COMMA
    cfg = bikeFcastConfig
  RPAR
  {
    hint.pop();
  }
  ->
  ^(ODE_BIKE_FCAST
    (odeParameters COMMA)? $stepSize COMMA $density COMMA $cfg
  )
;

simBikeFcast:
  SIM_BIKE_FCAST
  {
    hint.push("Simulation based bike journey forecasting analysis has syntax\n"+
      "SimBikeFcast(\n  stepSize=<number>,\n" +
      "  replications=<integer>,\n" + fcastSettingsHint + ")"
    );
  }
  LPAR
    STEPSIZE DEF stepSize = expression COMMA
    REPLICATIONS DEF replications = INTEGER COMMA
    cfg = bikeFcastConfig
  RPAR
  {
    hint.pop();
  }
  ->
  ^(SIM_BIKE_FCAST
    $stepSize COMMA $replications COMMA $cfg
  )
;

tsRBikeFcast:
  TS_R_BIKE_FCAST
  {
    hint.push(
      "Time series bike journey arrival analysis using R " +
      "has syntax\nTSRBikeFcast(\n  minXreg=<integer>,\n" +
      fcastSettingsHint + ")"
    );
  }
  LPAR
    ARR_FCAST_MODE DEF arrFcastMode = LOWERCASENAME COMMA
    MIN_XREG DEF minXreg = INTEGER COMMA
    cfg = bikeFcastConfig
  RPAR
  {
    hint.pop();
  }
  ->
  ^(TS_R_BIKE_FCAST $arrFcastMode COMMA $minXreg COMMA $cfg)
;


bikeFcastConfig:
  FCAST_WARMUP DEF fcastWarmup = INTEGER COMMA
  FCAST_LEN DEF fcastLen = INTEGER COMMA
  FCAST_FREQ DEF fcastFreq = INTEGER COMMA
  CL_DEP_STATES DEF clDepStates = listOfStates COMMA
  CL_ARR_STATES DEF clArrStates = listOfStates COMMA  
  DEP_FCAST_MODE DEF depFcastMode = LOWERCASENAME COMMA
  TRAIN_CL_DEP_TS DEF trainClDepTS = listOfFiles COMMA
  TRAIN_CL_DEP_TO_DEST_TS DEF trainClDepToDestTS = listOfFiles COMMA
  TRAIN_CL_ARR_TS DEF trainClArrTS = listOfFiles COMMA
  CL_DEP_TS DEF clDepTS = listOfFiles COMMA
  CL_DEP_TO_DEST_TS DEF clDepToDestTS = listOfFiles COMMA  
  CL_ARR_TS DEF clArrTS = listOfFiles
  ->
  ^(BIKE_FCAST_CFG
    $fcastWarmup COMMA $fcastLen COMMA $fcastFreq COMMA
    $clDepStates COMMA $clArrStates COMMA
    $depFcastMode COMMA
    $trainClDepTS COMMA $trainClDepToDestTS COMMA $trainClArrTS COMMA
    $clDepTS COMMA $clDepToDestTS COMMA $clArrTS
  )
;

listOfStates:
  {hint.push("List of states has following syntax {<state>,...}");}
  LBRACE state (COMMA state)* RBRACE
  {hint.pop();}
  ->
  ^(LOS state (COMMA state)*)
;

listOfConsts:
  {hint.push("List of states has following syntax {LOWERCASENAME,...}");}
  LBRACE LOWERCASENAME (COMMA LOWERCASENAME)* RBRACE
  {hint.pop();}
  ->
  ^(LOC LOWERCASENAME (COMMA LOWERCASENAME)*)
;

listOfFiles:
  {hint.push("List of states has following syntax {\"file1\",...}");}
  LBRACE FILENAME (COMMA FILENAME)* RBRACE
  {hint.pop();}
  ->
  ^(LOF FILENAME (COMMA FILENAME)*)
;

