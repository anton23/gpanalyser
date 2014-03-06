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
    "fcastMode=<string>,\n"+
    "fcastWarmup=<integer>,\n"+
    "fcastLen=<integer>,\n"+
    "fcastFreq=<integer>,\n"+
    "clDepStates={<state>,...},\n"+
    "clDepTS={<StringFilename>,...}){}"+
    "clArrStates={<state>,...},\n"+
    "clArrTS={<StringFilename>,...},\n";
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
  )
  (LBRACE plotDescription* RBRACE)?
;

odeBikeFcast:
  ODE_BIKE_FCAST
  odeParameters?
  {
    hint.push("ODE based bike journey forecasting analysis has syntax\n"+
      "OdeBikeFcast(BikeFcastSimu=<number>,\n" +
      "density=<integer>,\n" + fcastSettingsHint
    );
  }
  LPAR
    STEPSIZE DEF stepSize = expression COMMA
    DENSITY DEF density = INTEGER COMMA
    bikeFcastConfig
  RPAR
  {
    hint.pop();
  }
  ->
  ^(ODE_BIKE_FCAST
    (odeParameters COMMA)? $stepSize COMMA $density COMMA bikeFcastConfig
  )
;

simBikeFcast:
  SIM_BIKE_FCAST
  {
    hint.push("Simulation based bike journey forecasting analysis has syntax\n"+
      "Forecasting(BikeFcastSimu=<number>,\n" +
      "replications=<integer>,\n" + fcastSettingsHint
    );
  }
  LPAR
    STEPSIZE DEF stepSize = expression COMMA
    REPLICATIONS DEF replications = INTEGER COMMA
    bikeFcastConfig
  RPAR
  {
    hint.pop();
  }
  ->
  ^(SIM_BIKE_FCAST
    $stepSize COMMA $replications COMMA bikeFcastConfig
  )
;

bikeFcastConfig:
  FCAST_MODE DEF fcastMode = STRING COMMA
  FCAST_WARMUP DEF fcastWarmup = INTEGER COMMA
  FCAST_LEN DEF fcastLen = INTEGER COMMA
  FCAST_FREQ DEF fcastFreq = INTEGER COMMA
  CL_DEP_STATES DEF LBRACE clDepStates = listOfStates RBRACE COMMA
  CL_DEP_TS DEF LBRACE clDepTS = listOfFiles RBRACE
  CL_ARR_STATES DEF LBRACE clArrStates = listOfStates RBRACE COMMA
  CL_ARR_TS DEF LBRACE clArrTS = listOfFiles RBRACE
  ->
  ^(BIKE_FCAST_CFG
    $fcastMode COMMA $fcastWarmup COMMA $fcastLen COMMA $fcastFreq COMMA
    $clDepStates COMMA $clDepTS COMMA $clArrStates COMMA $clArrTS
  )
;

listOfStates:
  state (COMMA state)* -> ^(LOS state (COMMA state)*)
;

listOfConsts:
  LOWERCASENAME (COMMA LOWERCASENAME)* -> ^(LOWERCASENAME (COMMA LOWERCASENAME)*)
;

listOfFiles:
  FILENAME (COMMA FILENAME)* -> ^(FILENAME (COMMA FILENAME)*)
;

