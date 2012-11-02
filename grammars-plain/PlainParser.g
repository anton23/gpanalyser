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
  FORECASTINGSETTINGS;
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
   protected Stack<String> hint;
  
  protected ErrorReporter errorReporter;
  
  public void setErrorReporter(ErrorReporter errorReporter) {    
    this.errorReporter = errorReporter;
    gPCTMCParserPrototype.setErrorReporter(errorReporter);
    hint = gPCTMCParserPrototype.hint;
  }
  
  
  protected Set<Multiset<String>> tmpStates = new HashSet<Multiset<String>>();
  protected Set<Multiset<String>> states  = new HashSet<Multiset<String>>();
  
  public ParsingData getParsingData() { 
    return new PlainParsingData(tmpStates);  
  }
  
    public void setParsingData(ParsingData parsingData) {
      if (parsingData instanceof PlainParsingData) {
           states = ((PlainParsingData)parsingData).getStates();            
      }
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
        String ret = "";
        
          ret += gPCTMCParserPrototype.getModifiedErrorMessage(e, tokenNames);
                
        if (!hint.isEmpty()) {
          ret += " (" + hint.peek() + ")";
        } else 
        if (!gPCTMCParserPrototype.hint.isEmpty()) { 
          ret += " (" + gPCTMCParserPrototype.hint.peek() + ")";
        } 
        
        return ret;
      }
}




state: 
  t=transaction
 |countPattern
 ;
 
countPattern:
 COUNT UPPERCASENAME -> ^(COUNT UPPERCASENAME)
;

transaction: LBRACE cl=componentList RBRACE
{if (gPCTMCParserPrototype.define) {
  tmpStates.add($cl.components);
 } else if (gPCTMCParserPrototype.requireDefinitions) {
  if (!states.contains($cl.components)) {
     reportError(new CustomRecognitionException(input, "invalid population {" + $cl.text +"}"));
  }
 }
 }    
-> ^(TRANSACTION componentList);

componentList returns [Multiset<String> components]
@init{
  $components = HashMultiset.<String>create();
}:
n1=UPPERCASENAME {$components.add($n1.text);}(COMMA n2=UPPERCASENAME {$components.add($n2.text);})* -> UPPERCASENAME+ 
;

analysis:
 (odeAnalysis
 |inhomogeneousODEAnalysis
 |forecasting
 |simulation
 |inhomogeneousSimulation
 |accuratesimulation
 |compare)

 (LBRACE
    plotDescription*
  RBRACE)?
;

inhomogeneousODEAnalysis:
  INHOMOGENEOUSODES
  odeParameters?
  {hint.push("ODE analysis has to be of the form\n   ODEs(stopTime=<number>, stepSize=<number>, density=<integer>){}'");}
  odeSettings
  {hint.pop();}
  -> ^(INHOMOGENEOUSODES odeParameters? odeSettings)
;

forecasting:
  FORECASTING
  odeParameters?
  {hint.push("Forecasting analysis has to be of the form\n"+
  			 "Forecasting(stepSize=<number>,\n"+
  			 "density=<integer>, warmup=<integer>,\n"+
  			 "forecast=<integer>, intvlBetweenForecasts=<integer>,\n"+
  			 "arrState=<state>, startStates={<state>,...},\n"+
  			 "startDeltas={<LOWERCASENAME>,...}, TSStep=<integer>,\n"+
  			 "arrTS={<StringFilename>,...},depTS={<StringFilename>,...}){}'");}
  forecastingSettings
  {hint.pop();}
  -> ^(FORECASTING odeParameters? forecastingSettings)
;

forecastingSettings:
  LPAR
    STEPSIZE DEF stepSize = expression COMMA
    DENSITY DEF density = INTEGER COMMA
    WARMUP DEF warmup = INTEGER COMMA
    FORECAST DEF forecast = INTEGER COMMA
    INTVLBETWEENFORECASTS DEF ibf = INTEGER COMMA
    ARRSTATE DEF arrState = state COMMA
    STARTSTATES DEF LBRACE startStates = listOfStates RBRACE COMMA
    STARTDELTAS DEF LBRACE startDeltas = listOfConsts RBRACE COMMA
    TSSTEP DEF tsStep = INTEGER COMMA
    ARRTS DEF LBRACE arrTS = listOfFiles RBRACE COMMA
    DEPTS DEF LBRACE depTS = listOfFiles RBRACE
  RPAR
  -> ^(FORECASTINGSETTINGS $stepSize COMMA $density COMMA $warmup COMMA
  	   $forecast COMMA $ibf COMMA $arrState COMMA $startStates COMMA
  	   $startDeltas COMMA $tsStep COMMA $arrTS COMMA $depTS)
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


inhomogeneousSimulation:
  INHOMOGENEOUSSIMULATION
  simulationSettings
  -> ^(INHOMOGENEOUSSIMULATION simulationSettings)
;


