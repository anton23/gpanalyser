parser grammar MASSPAParser;

options {
  language = Java;
  output = AST; 
  ASTLabelType = CommonTree; 
  tokenVocab = MASSPALexer; 
  backtrack = true;
}

import PCTMCParserPrototype;

tokens
{
	// Tokens for sequential operators
	COMPONENT;
	PREFIX;
	SEND;
	RECV;
	TAU;

	// Tokens for parallel composition
	LOCATION;
	LOCATIONS;
	AGENTPOP;
	INITVAL;
	COUNTACTIONS;
}

@members{

    protected Stack<String> hint = new Stack<String>();

    public void displayRecognitionError(String[] tokenNames,
                                        RecognitionException e) {
        String hdr = getErrorHeader(e);
        String msg = getErrorMessage(e, tokenNames);
        if (errorReporter != null) {
          errorReporter.addError("["+hdr + "] " + msg);
        }
    }
    
    protected ErrorReporter errorReporter;
    
    public boolean requireDefinitions = false;
    
    public void setErrorReporter(ErrorReporter errorReporter) {
       this.errorReporter = errorReporter;
    }
    
  public boolean define = false;
  
  public ParsingData getParsingData() { 
    return null;  
  } 
  
  public void setParsingData(ParsingData parsingData) {
      
  }
    
     
  public String getErrorHeader(RecognitionException e) {
    return "line "+e.line+":"+e.charPositionInLine;
  }
    
	   public String getErrorMessage(RecognitionException e,
                              String[] tokenNames) {
        String ret = "";
        
          ret += getModifiedErrorMessage(e, tokenNames);
                
        if (!hint.isEmpty()) {
          ret += " (" + hint.peek() + ")";
        } 
        
        return ret;
      }
	  
	   public String getTokenErrorDisplay(Token t) {
    String s = t.getText();
    if (s.equals("<EOF>")) {
       return "the end of file";
    }
    if ( s==null ) {
      if ( t.getType()==Token.EOF ) {
        return "the end of file";
      }
      else {
        s = "<"+t.getType()+">";
      }
    }
    s = s.replaceAll("\n","\\\\n");
    s = s.replaceAll("\r","\\\\r");
    s = s.replaceAll("\t","\\\\t");
    return "'"+s+"'";
  }
	  
	  
	  private String[] customTokenNames(String[] tokenNames) {
	     String[] ret = new String[tokenNames.length];
	     Map<String, String> map = new HashMap<String, String>();
	     map.put("SEMI", "';'");
	     map.put("LBRACE", "'{'");
	     map.put("RBRACE", "'}'");
	     map.put("EOF", "the end of file");
	     map.put("STOPTIME","'stopTime'");
	     map.put("STEPSIZE","'stepSize'");
	     map.put("DENSITY","'density'");
	     map.put("REPLICATIONS","'replications'");
	     map.put("INTEGER","an integer");
	     map.put("AT","@");
	     map.put("DEF","=");
	     map.put("LPAR","(");
	     map.put("RPAR",")");
	     map.put("FILENAME","filename of the form \"filename\"");
	     for (int i = 0; i<tokenNames.length; i++) {
	         ret[i] = tokenNames[i]; 
	         if (map.containsKey(ret[i])) {
	           ret[i] = map.get(ret[i]);
	         }
	     }
	     return ret;
	  }
	  
	  public String getModifiedErrorMessage(RecognitionException e, String[] tokenNames) {
	  tokenNames = customTokenNames(tokenNames);
    String msg = e.getMessage();
    if ( e instanceof UnwantedTokenException ) {
      UnwantedTokenException ute = (UnwantedTokenException)e;
      String tokenName="<unknown>";
      if ( ute.expecting== Token.EOF ) {
        tokenName = "EOF";
      }
      else {
        tokenName = tokenNames[ute.expecting];
      }
      msg = "extraneous input "+getTokenErrorDisplay(ute.getUnexpectedToken())+
        " expecting "+tokenName;
    }
    else if ( e instanceof MissingTokenException ) {
      MissingTokenException mte = (MissingTokenException)e;
      String tokenName="<unknown>";
      if ( mte.expecting== Token.EOF ) {
        tokenName = "EOF";
        msg = "unknown command " + getTokenErrorDisplay(e.token);
      }
      else {
        tokenName = tokenNames[mte.expecting];
        msg = "missing "+tokenName+" at "+getTokenErrorDisplay(e.token);
      }
    }
    else if ( e instanceof MismatchedTokenException ) {
      MismatchedTokenException mte = (MismatchedTokenException)e;
      String tokenName="<unknown>";
      if ( mte.expecting== Token.EOF ) {
        tokenName = "EOF";
      }
      else {
        tokenName = tokenNames[mte.expecting];
      }  
      
      msg = "mismatched input "+getTokenErrorDisplay(e.token)+
        " expecting "+tokenName;
    }
    else if ( e instanceof MismatchedTreeNodeException ) {
      MismatchedTreeNodeException mtne = (MismatchedTreeNodeException)e;
      String tokenName="<unknown>";
      if ( mtne.expecting==Token.EOF ) {
        tokenName = "EOF";
      }
      else {
        tokenName = tokenNames[mtne.expecting];
      } 

      msg = "mismatched tree node: "+mtne.node+
        " expecting "+tokenName;
    }
    else if ( e instanceof NoViableAltException ) {
      //NoViableAltException nvae = (NoViableAltException)e;
      // for development, can add "decision=<<"+nvae.grammarDecisionDescription+">>"
      // and "(decision="+nvae.decisionNumber+") and
      // "state "+nvae.stateNumber
      msg = "no viable alternative at "+getTokenErrorDisplay(e.token);
    }
    else if ( e instanceof EarlyExitException ) {
      //EarlyExitException eee = (EarlyExitException)e;
      // for development, can add "(decision="+eee.decisionNumber+")"
      msg = "required list did not match anything at input "+
        getTokenErrorDisplay(e.token);
    }
    else if ( e instanceof MismatchedSetException ) {
      MismatchedSetException mse = (MismatchedSetException)e;
      msg = "mismatched input "+getTokenErrorDisplay(e.token)+
        " expecting set "+mse.expecting;
    }
    else if ( e instanceof MismatchedNotSetException ) {
      MismatchedNotSetException mse = (MismatchedNotSetException)e;
      msg = "mismatched input "+getTokenErrorDisplay(e.token)+
        " expecting set "+mse.expecting;
    }
    else if ( e instanceof FailedPredicateException ) {
      FailedPredicateException fpe = (FailedPredicateException)e;
      msg = "rule "+fpe.ruleName+" failed predicate: {"+
        fpe.predicateText+"}?";
    } else if ( e instanceof CustomRecognitionException ) {
      CustomRecognitionException mse = (CustomRecognitionException)e;
      msg = mse.getMessage();
    }
    return msg;
  }
  
  boolean requiresExpectation = false;
  boolean insideExpectation = false;
  
  Set<String> constants = new HashSet<String>();
}

@header
{
package uk.ac.imperial.doc.masspa.syntax;

import uk.ac.imperial.doc.masspa.language.Messages;
import uk.ac.imperial.doc.masspa.util.MASSPALogging;
import uk.ac.imperial.doc.pctmc.syntax.ErrorReporter;  
import uk.ac.imperial.doc.pctmc.syntax.CustomRecognitionException;
import uk.ac.imperial.doc.pctmc.syntax.ParsingData;

import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;
}

// ****************************************************
// *    			PCTMC Overwrites			      *
// ****************************************************
constant:
  id=LOWERCASENAME {hint.push(String.format(Messages.s_PARSER_INVALID_LOCATION_IN_CONST_NAME,$id.text));} AT location {hint.pop();}
  -> ^(CONSTANT LOWERCASENAME location)
;

variable:
  VAR id=LOWERCASENAME {hint.push(String.format(Messages.s_PARSER_INVALID_LOCATION_IN_VAR_NAME,$id.text));} AT location {hint.pop();}
  -> ^(VARIABLE LOWERCASENAME location)
; 

state: 
  agentPopulation -> ^(STATE agentPopulation)
| actionCount -> ^(STATE actionCount)
;

// ****************************************************
// *    Syntax for definition of sequential agents    *
// ****************************************************
start:;

modelDefinition:
  {hint.push(Messages.s_PARSER_AGENT_DEFINITION_REQUIRED);} agentDefinition+ {hint.pop();}
  {hint.push(Messages.s_PARSER_MODEL_DEFINITION_REQUIRED);} model {hint.pop();}
;

agentDefinition:
  {hint.push(String.format(Messages.s_PARSER_INVALID_AGENT_SCOPE,$scopeName.text));} AGENT scopeName=UPPERCASENAME {hint.pop();}
  {hint.push(String.format(Messages.s_PARSER_INVALID_AGENT_MISSING_LBRACE,$scopeName.text));} LBRACE {hint.pop();}
  componentDefinition+ 
  {hint.push(String.format(Messages.s_PARSER_INVALID_AGENT_MISSING_RBRACE,$scopeName.text));} RBRACE {hint.pop();}
  {hint.push(String.format(Messages.s_PARSER_INVALID_AGENT_MISSING_SEMI,$scopeName.text));} SEMI {hint.pop();}
  -> ^(AGENT $scopeName componentDefinition+)
;

componentDefinition:
  id=UPPERCASENAME DEF {hint.push(String.format(Messages.s_PARSER_INVALID_AGENT_STATE_DEFINITION,$id.text));} s=component {hint.pop();}
  {hint.push(String.format(Messages.s_PARSER_INVALID_AGENT_STATE_MISSING_SEMI,$id.text));} SEMI {hint.pop();}
  -> ^(COMPONENT $id $s)
;

component:
  choice
;

choice:
  prefix (PLUS {hint.push(String.format(Messages.s_PARSER_CHOICE_INVALID_PREFIX));} prefix {hint.pop();})*
  -> prefix+
;

prefix:
  {hint.push(String.format(Messages.s_PARSER_INVALID_TAU_PREFIX_DEFINITION));} LPAR r=expression RPAR DOT s=primaryComponent {hint.pop();} -> ^(PREFIX TAU $r $s)
| {hint.push(String.format(Messages.s_PARSER_INVALID_PREFIX_DEFINITION));} LPAR action=LOWERCASENAME COMMA r=expression RPAR DOT s=primaryComponent {hint.pop();} -> ^(PREFIX $action $r $s)
| SENDMSG {hint.push(String.format(Messages.s_PARSER_INVALID_TAU_SEND_PREFIX_DEFINITION));} LPAR r=expression COMMA msg=UPPERCASENAME COMMA nofmsg=expression RPAR DOT s=primaryComponent {hint.pop();} -> ^(SEND TAU $r $msg $nofmsg $s)
| SENDMSG {hint.push(String.format(Messages.s_PARSER_INVALID_SEND_PREFIX_DEFINITION));} LPAR action=LOWERCASENAME COMMA r=expression COMMA msg=UPPERCASENAME COMMA nofmsg=expression RPAR DOT s=primaryComponent {hint.pop();} -> ^(SEND $action $r $msg $nofmsg $s)
| RECVMSG {hint.push(String.format(Messages.s_PARSER_INVALID_TAU_RECV_PREFIX_DEFINITION));} LPAR msg=UPPERCASENAME COMMA msgaccprob=expression RPAR DOT s=primaryComponent {hint.pop();} -> ^(RECV TAU $msg $msgaccprob $s) 
| RECVMSG {hint.push(String.format(Messages.s_PARSER_INVALID_RECV_PREFIX_DEFINITION));}  LPAR action=LOWERCASENAME COMMA msg=UPPERCASENAME COMMA msgaccprob=expression RPAR DOT s=primaryComponent {hint.pop();} -> ^(RECV $action $msg $msgaccprob $s) 
| {hint.push(String.format(Messages.s_PARSER_INVALID_PRIMARY_COMPONENT));} primaryComponent {hint.pop();}
;

primaryComponent:
  UPPERCASENAME
| STOP
;

// ****************************************************
// *      Syntax for definition of spatial model      *
// ****************************************************
model:
  {hint.push(String.format(Messages.s_PARSER_MISSING_LOCATION_DEF));} locationDef {hint.pop();}
  {hint.push(String.format(Messages.s_PARSER_MISSING_INITIAL_VALUES));} initVal+ {hint.pop();}
  channel*
;

location: 
  LPAR INTEGER (COMMA INTEGER)* RPAR -> ^(LOCATION INTEGER+)
| LPAR LOC_ALL RPAR -> ^(LOCATION LOC_ALL)
| LPAR LOC_VAR RPAR -> ^(LOCATION LOC_VAR)
;

locationDef:
  LOCS DEF
  {hint.push(String.format(Messages.s_PARSER_INVALID_LOCATION_DEF_MISSING_LBRACE));} LBRACE {hint.pop();}
  {hint.push(String.format(Messages.s_PARSER_INVALID_LOCATION_DEF_INVALID_LOC));} location (COMMA location)* {hint.pop();}
  {hint.push(String.format(Messages.s_PARSER_INVALID_LOCATION_DEF_MISSING_RBRACE));} RBRACE {hint.pop();}
  {hint.push(String.format(Messages.s_PARSER_INVALID_LOCATION_DEF_MISSING_SEMI));} SEMI {hint.pop();}
  -> ^(LOCATIONS location+)
;

agentPopulation:
  id=UPPERCASENAME {hint.push(String.format(Messages.s_PARSER_INVALID_AGENT_POPULATION_LOCATION,$id.text));} AT location {hint.pop();}
  -> ^(AGENTPOP $id location)
;

actionCount:
  ACOUNT id=LOWERCASENAME {hint.push(String.format(Messages.s_PARSER_INVALID_ACTION_COUNT_LOCATION,$id.text));} AT location {hint.pop();}
  -> ^(ACOUNT $id location?)
;

initVal:
  agentPopulation DEF {hint.push(String.format(Messages.s_PARSER_INVALID_AGENT_POPULATION_DEF));} expression SEMI {hint.pop();} -> ^(INITVAL agentPopulation expression)
| actionCount DEF {hint.push(String.format(Messages.s_PARSER_INVALID_ACTION_COUNT_DEF));} expression SEMI {hint.pop();} -> ^(INITVAL actionCount expression)
;

channel:
  CHANNEL 
  {hint.push(String.format(Messages.s_PARSER_CHANNEL_MISSING_LPAR));} LPAR {hint.pop();}
  {hint.push(String.format(Messages.s_PARSER_CHANNEL_INVALID_SENDER));} sender=agentPopulation {hint.pop();}
  COMMA 
  {hint.push(String.format(Messages.s_PARSER_CHANNEL_INVALID_RECEIVER));} receiver=agentPopulation {hint.pop();}
  COMMA 
  {hint.push(String.format(Messages.s_PARSER_CHANNEL_INVALID_MSG));} msg=UPPERCASENAME {hint.pop();}
  {hint.push(String.format(Messages.s_PARSER_CHANNEL_MISSING_RPAR));} RPAR {hint.pop();} 
  {hint.push(String.format(Messages.s_PARSER_CHANNEL_MISSING_DEF));} DEF {hint.pop();}
  {hint.push(String.format(Messages.s_PARSER_CHANNEL_MISSING_EXPR));} intensity=expression {hint.pop();}
  {hint.push(String.format(Messages.s_PARSER_CHANNEL_MISSING_SEMI));} SEMI {hint.pop();}
  -> ^(CHANNEL $sender $receiver $msg $intensity) 
;
