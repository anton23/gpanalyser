parser grammar GPAParser;

options {
  language = Java;
  output = AST; 
  ASTLabelType = CommonTree; 
  tokenVocab = GPALexer; 
  backtrack = true;
  memoize=true;
}

import PCTMCParserPrototype;

tokens{
  COMPONENT;
  LABELLEDGROUP;
  COOP;
  PREFIX;
  MULT;
  COOPCOMP;
  PAIR;
  INGROUP;
  DOT;
  DUMMY;

  //Local

  PASSIVE               ;
  REPETITION            ;
  RL_SINGLE_BRACKETED   ;
  RL_BRACKETED          ;
  RL_SINGLE             ;
  PROBEL                ;
  RLS                   ;
  RL_SINGLE             ;
  RL                    ;
  CHOICE_OP             ;
  BINARY_OP             ;
  UNARY_OP              ;
  COMMA                 ;
  ZERO_ONE              ;
  PLUS                  ;
  NEGATION_OP           ;
  SIGNAL                ;
  EMPTY                 ;
  EVENTUAL				;
  SPECIFIC				;
  ACTION                ;

  //Global

  START                 ;
  BRACKETED             ;
  PROBEG                ;
  RG                    ;
  RGA                   ;
  RGA_ALL               ;
  LOGICAL_NEGATION      ;
  LOGICAL_OR            ;
  LOGICAL_AND           ;
  TRUE                  ;
  FALSE                 ;
  COMPARISON            ;
  EXPRESSION			;
  
  //Probes

  FILE					;
  STEADY				;
  TRANSIENT				;
  GLOBAL				;
  SIGNALS				;
  WHERE					;
  OBSERVES				;
  IN					;
  SUBSTITUTE			;
  LPROBES_DEF			;
  LOCATIONS				;
  PROBE_DEF				;
  SIM_PROBE_DEF			;
  PROBES				;
  MAIN_MODEL            ;
}

@header
{
	package uk.ac.imperial.doc.gpa.syntax; 

	import uk.ac.imperial.doc.pctmc.syntax.ErrorReporter;  
	import uk.ac.imperial.doc.pctmc.syntax.CustomRecognitionException;
	import uk.ac.imperial.doc.pctmc.syntax.ParsingData;
	import uk.ac.imperial.doc.pctmc.syntax.GPAParsingData;
	import uk.ac.imperial.doc.pctmc.syntax.PlainParsingData;

	import java.util.Collection;
	import java.util.Iterator;
	import java.util.LinkedList;
	import java.util.Set;
	import java.util.HashSet;
}

//This is a hack until the composite grammars are implemented in a better way
@members{

  protected Set<String> tmpGroupNames = new HashSet<String>();
  protected Set<String> groupNames = new HashSet<String>();
  protected Set<String> tmpComponentNames = new HashSet<String>();
  protected Set<String> componentNames = new HashSet<String>();
  private boolean checkComponentNames;
  private boolean checkGroupNames;
  private List<String> signals = new ArrayList<String>();
    
  protected Stack<String> hint;
  
  protected ErrorReporter errorReporter;
  
  public void setErrorReporter(ErrorReporter errorReporter) {    
    this.errorReporter = errorReporter;
    gPCTMCParserPrototype.setErrorReporter(errorReporter);
    hint = gPCTMCParserPrototype.hint;
  }
  
  public ParsingData getParsingData() {
       return new GPAParsingData(tmpComponentNames, tmpGroupNames);
  } 
  
  public void setParsingData(ParsingData parsingData) {
       checkComponentNames = true;
       checkGroupNames = true;
       if (parsingData instanceof GPAParsingData) {
            componentNames = ((GPAParsingData)parsingData).getComponentNames();
            groupNames = ((GPAParsingData)parsingData).getGroupNames();
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



start:;

modelDefinition:
  {hint.push("at least one PEPA component definition required");} componentDefinitions {hint.pop();}
  {hint.push("missing system equation");} model {hint.pop();}
  countActions? ;


countActions:
  COUNTACTIONS (LOWERCASENAME)+ SEMI -> ^(COUNTACTIONS LOWERCASENAME+)
;

componentDefinitions:
  componentDefinition+
;

componentDefinition:
  id=UPPERCASENAME DEF {hint.push("invalid definition of component '" + $id.text+"'");} s=component {hint.pop();}
      {hint.push("definition of '" + $id.text + "' must end with a semicolon");} SEMI {hint.pop();}
  {tmpComponentNames.add($id.text);} 
  -> ^(COMPONENT $id $s);

component:   
    choice cooperationSet choice -> ^(COOPCOMP cooperationSet choice DUMMY choice)
   |choice;


//allows only choice between prefixes (not constants)
choice:
  prefix (PLUS {hint.push("summation operator must be followed by a proper prefix, e.g. (a, ra).C");} properPrefix {hint.pop();})* -> prefix properPrefix*
   ;
      
prefix: 
    properPrefix
  | primaryComponent;
  
properPrefix:
  LPAR LOWERCASENAME COMMA expression RPAR DOT prefix -> ^(PREFIX expression LOWERCASENAME prefix)
  | LPAR LOWERCASENAME COMMA PASSIVE (COMMA expression)? RPAR DOT prefix -> ^(PREFIX PASSIVE expression? LOWERCASENAME prefix)
  | LOWERCASENAME DOT prefix -> ^(PREFIX LOWERCASENAME prefix);
  
primaryComponent:
   n = UPPERCASENAME {if (checkComponentNames
                            && gPCTMCParserPrototype.requireDefinitions
                            && !componentNames.contains($n.text)){
      displayRecognitionError(getTokenNames(), new CustomRecognitionException(input, "unknown component '" + $n.text+"'"));}}
 | STOP
 | ANY
 | LPAR component RPAR -> component;
 
model: 
   labelledGroup cooperationSet labelledGroup -> ^(COOP cooperationSet labelledGroup labelledGroup)
  |  labelledGroup 
;
   
labelledGroup:
  l=UPPERCASENAME {tmpGroupNames.add($l.text);}
   {hint.push("group components must be enclosed inside '{' and '}'");} LBRACE {hint.pop();}
     group RBRACE -> ^(LABELLEDGROUP UPPERCASENAME group)
  | LPAR model RPAR -> model;
 
cooperationSet:
   LANGLE {hint.push("expecting a (possibly empty) list of action names");}
      LOWERCASENAME 
       (COMMA {hint.push("cooperation set has to be of the form <a1, a2, ..., >");} LOWERCASENAME {hint.pop();})* 
       {hint.pop();}
      {hint.push("closing bracket '>' missing");}RANGLE {hint.pop();} -> LOWERCASENAME+
  |LANGLE RANGLE -> LOWERCASENAME[""]
  |LANGLE TIMES RANGLE -> TIMES;
   
group:
  groupComponent (PAR {hint.push("group definition has to be of the form G{A[n]|B[m]|...|Z[k]}");}
  	groupComponent {hint.pop();})* -> groupComponent+;
 
groupComponent:
  component (LBRACK expression RBRACK) -> ^(MULT component expression)
 |component -> ^(MULT component REALNUMBER["1.0"]);
 


//states:

state: 
  groupComponentPair
 |actionCount; 

actionCount:
ACOUNT LOWERCASENAME -> ^(ACOUNT LOWERCASENAME)
;

groupComponentPair:
     {hint.push("populations have to be of the form 'Group:Component'");}
     n=UPPERCASENAME      
     INGROUP {if (checkGroupNames
                    && gPCTMCParserPrototype.requireDefinitions
                    && !groupNames.contains($n.text)) {
          reportError(new CustomRecognitionException(input, "invalid group label " + $n.text));
     }} component  
     {hint.pop();} -> ^(PAIR UPPERCASENAME component)
;

extensions
	:	probe_def*
			-> ^(PROBES probe_def*) ;

// Local

probel
	:	f=rl_signal (COMMA rl_signal)* REPETITION?
			-> ^(PROBEL $f rl_signal* REPETITION?) ;

rl_signal
	:	rl_single INGROUP signal
			-> ^(RLS rl_single signal) ;

rl_single
	:	rl_single_bracketed
			-> ^(RL_SINGLE rl_single_bracketed)
		| rl_single_actions
			-> ^(RL_SINGLE rl_single_actions) ;

rl_single_actions
	:	immediateActions rl_un_operators?
			-> ^(ACTION immediateActions rl_un_operators?) ;

rl_single_bracketed
	:	rl_bracketed rl_un_operators?
			-> ^(RL_SINGLE_BRACKETED rl_bracketed rl_un_operators?) ;

rl
	:	rl_single (rl_bin_operators rl)?
			-> ^(RL rl_single (rl rl_bin_operators)?) ;

rl_bin_operators
	:	COMMA -> ^(BINARY_OP COMMA)
		| PAR -> ^(BINARY_OP PAR)
		| SEMI -> ^(BINARY_OP SEMI)
		| DIVIDE -> ^(BINARY_OP DIVIDE)
		| AT -> ^(BINARY_OP AT) ;

rl_un_operators
	:	(LBRACK e1=expression (COMMA e2=expression)? RBRACK)
			-> ^(UNARY_OP $e1 (COMMA $e2)?)
		| ZERO_ONE -> ^(UNARY_OP ZERO_ONE)
		| PLUS -> ^(UNARY_OP PLUS)
		| TIMES -> ^(UNARY_OP TIMES)
		| NEGATION_OP -> ^(UNARY_OP NEGATION_OP) ;

rl_bracketed
	:	LPAR rl RPAR
			-> ^(RL_BRACKETED rl) ;

signal
	:	LOWERCASENAME
			{
            	signals.add ($LOWERCASENAME.text);
			}
			-> ^(SIGNAL LOWERCASENAME) ;

immediateActions
	:	eventual_specific_action
		| subsequent_specific_action
		| DOT | EMPTY ;

eventual_specific_action
	:	LOWERCASENAME
			-> ^(EVENTUAL DOT ^(UNARY_OP TIMES) LOWERCASENAME
			DOT ^(UNARY_OP TIMES)) ;

subsequent_specific_action
	:	MINUS LOWERCASENAME
			-> ^(SPECIFIC LOWERCASENAME) ;

//Global

probeg
	:	start_sync=rg INGROUP START COMMA stop_sync=rg INGROUP STOP REPETITION?
			-> ^(PROBEG $start_sync $stop_sync REPETITION?) ;

rg
	:	(LBRACE pred RBRACE)? rg_sub (rl_bin_operators rg)?
			-> ^(RG rg_sub pred? (rg rl_bin_operators)?) ;

rg_sub
	:	rga_all
		| rl_single ;

rga_all
	:	({$probe_def::mode == 3 && $probe_def::fluid_flow}?=>
			rga (LBRACK expression RBRACK)?)
			-> ^(RGA_ALL rga expression?) ;

rga
	:	a=rga_action (PAR rga_action)*
		-> ^(RGA $a rga_action*) ;

rga_action
	:	eventual_specific_action
		| EMPTY ;

// Predicates for global

pred
	:	(logical_pred | negation) (LOGICAL_OR pred)? ;

negation
	:	LOGICAL_NEGATION logical_pred
			-> ^(LOGICAL_NEGATION logical_pred) ;

logical_pred
	:	TRUE | FALSE | b_expr ;

b_expr
	:	r_expr1=r_expr comparison r_expr2=r_expr
			-> ^(comparison $r_expr1 $r_expr2) ;

comparison
    :   COMPARISON | LANGLE | RANGLE ;

r_expr
	:	concrete_r_expr (binary_op r_expr)?
			-> ^(EXPRESSION concrete_r_expr (binary_op r_expr)?) ;

binary_op
	:	PLUS | MINUS | TIMES | DIVIDE ;

concrete_r_expr
	:	componentCount | expression ;

componentCount
@init
{
    checkGroupNames = false;
    checkComponentNames = false;
}
@after
{
    checkGroupNames = true;
    checkComponentNames = true;
}
	:	groupComponentPair ;

// Probe_spec

probe_def
scope
{
	int mode;
    boolean fluid_flow;
}
	:	PROBE_DEF
			{
				$probe_def::fluid_flow = true;
			}
		out? odeSettings mode? LBRACE probe_spec RBRACE
			{
				$probe_def::fluid_flow = true;
			}
			-> ^(PROBE_DEF out? odeSettings mode? probe_spec)
		| SIM_PROBE_DEF out? simulationSettings mode? LBRACE probe_spec RBRACE
          	-> ^(SIM_PROBE_DEF out? simulationSettings mode? probe_spec) ;

mode
	:	STEADY expression
			{
				$probe_def::mode = 1;
			}
			-> ^(STEADY expression)
		| TRANSIENT expression
			{
				$probe_def::mode = 2;
			}
			-> ^(TRANSIENT expression)
		|	{
        		$probe_def::mode = 3;
        	}
			-> ^(GLOBAL GLOBAL) ;

out
	:	LBRACK FILENAME RBRACK
		-> ^(FILE FILENAME) ;

probe_spec
scope
{
	StringBuilder signalsString;
}
@init
{
	signals = new ArrayList<String> ();
	$probe_spec::signalsString = new StringBuilder ();
}
	:	UPPERCASENAME DEF probeg (OBSERVES local_probes WHERE locations)?
				{
					for (String signal : signals)
					{
						$probe_spec::signalsString.append (signal);
						$probe_spec::signalsString.append (";");
					}
				}
			-> ^(DEF SIGNALS[$probe_spec::signalsString.toString ()]
					UPPERCASENAME (local_probes locations)? probeg) ;

local_probes
	:	LBRACE m=local_probe_ass (COMMA local_probe_ass)* RBRACE
			-> ^(LPROBES_DEF $m local_probe_ass*) ;

local_probe_ass
	:	UPPERCASENAME DEF probel
			-> ^(DEF UPPERCASENAME probel) ;

locations
	:	LBRACE l=location (COMMA location)* RBRACE
			-> ^(LOCATIONS $l location*) ;

location
	:
		model1=model
		{
			checkComponentNames = false;
		}
		SUBSTITUTE
		model2=model
		{
			checkComponentNames = true;
		}
			-> ^(SUBSTITUTE $model1 $model2) ;
