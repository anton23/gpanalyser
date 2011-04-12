parser grammar PCTMCParserPrototype;

options {
  language = Java;
  output = AST; 
  ASTLabelType = CommonTree;  
  backtrack = true;
}

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
}

@members{
protected Object recoverFromMismatchedToken(IntStream input,
                                            int ttype,
                                            BitSet follow)
    throws RecognitionException
{   
    throw new MismatchedTokenException(ttype, input);
}   

}


//stops the parser on errors instead of recovering
@rulecatch {
  catch (RecognitionException re) {
    reportError(re);
  }
}

system:constantDefinition* varDefinition* modelDefinition analysis* experiment*;

modelDefinition:
  eventDefinition*
  initDef*
;


analysis:
  odeAnalysis
 |simulation
 |compare
;

compare:
  COMPARE LPAR
    analysis COMMA
    analysis
  RPAR LBRACE
      plotDescription*
  RBRACE -> ^(COMPARE analysis analysis plotDescription*)
;

experiment:
  ITERATE 
    rangeSpecification+ 
    (WHERE
      constantReEvaluation+)?
    analysis PLOT LBRACE
      plotAtSpecification+
    RBRACE
  -> ^(ITERATE rangeSpecification+ (WHERE constantReEvaluation+)? analysis plotAtSpecification+)
;

constantReEvaluation:
  id=LOWERCASENAME DEF rhs=expression SEMI ->  $id $rhs
;

rangeSpecification:
  constant=LOWERCASENAME FROMVALUE from=REALNUMBER TOVALUE to=REALNUMBER IN steps=INTEGER STEPS 
  -> ^(RANGE $constant $from $to $steps)
;



plotAtSpecification:
 plotAt (WHEN constraints)? (TO FILENAME)? SEMI -> 
  ^(PLOT plotAt (WHEN constraints)? FILENAME?)
;

constraints:
  constraint (AND constraint)* -> constraint* 
;

constraint:
 plotAt GEQ REALNUMBER -> plotAt GEQ REALNUMBER
;

plotAt:
  expression ATTIME REALNUMBER -> expression REALNUMBER
;


odeAnalysis:
  ODES LPAR
  STOPTIME DEF stopTime = REALNUMBER COMMA
  STEPSIZE DEF stepSize = REALNUMBER COMMA
  DENSITY DEF density=INTEGER   
  RPAR LBRACE
    plotDescription*
  RBRACE
  -> ^(ODES $stopTime $stepSize $density LBRACE plotDescription* RBRACE )
;

simulation:
  SIMULATION LPAR
  STOPTIME DEF stopTime = REALNUMBER COMMA
  STEPSIZE DEF stepSize = REALNUMBER COMMA
  REPLICATIONS DEF replications=INTEGER   
  RPAR LBRACE
    plotDescription*
  RBRACE
  -> ^(SIMULATION $stopTime $stepSize $replications LBRACE plotDescription* RBRACE )
;

plotDescription:
  (expressionList (TO FILENAME)? SEMI)
;

analysesSpec:
  STOPTIME DEF REALNUMBER SEMI
  STEPSIZE DEF REALNUMBER SEMI
  DENSITY DEF INTEGER SEMI
  REPLICATIONS DEF INTEGER SEMI
;

var: VAR LOWERCASENAME -> ^(VAR LOWERCASENAME); 


state: UPPERCASENAME; 

//-----Rules for definitions----- 

constantDefinition:
  id=LOWERCASENAME DEF (rate=REALNUMBER) SEMI -> ^(CONSTANT $id $rate);
  
varDefinition:
  var DEF expression SEMI -> ^(VARIABLE var expression);
  
//-----Rules for events

eventDefinition:
dec=stateSum TO inc=stateSum AT expression SEMI -> ^(EVENT $dec TO $inc AT expression)
;

stateSum:
 state (PLUS state)* -> state+
;
     
     
//-----Initial values

initDef:state DEF expression SEMI -> ^(INIT state DEF expression);      
    
//-----Rules for expressions-----    

     
expression:   
  multExpression ((PLUS|MINUS) multExpression)*;    

multExpression
  : 
  powerExpression ((TIMES|DIVIDE) powerExpression)*;
  
powerExpression
  :
  signExpression (POWER signExpression)*;
  
signExpression
  : 
  (MINUS)? primaryExpression;
  


primaryExpression:    
      combinedPowerProduct
     | var 
     |REALNUMBER
     |INTEGER
     | LPAR expression RPAR 
     | MIN LPAR expression COMMA expression RPAR -> ^(MIN expression COMMA expression)
     | LOWERCASENAME LPAR expressionList RPAR -> ^(FUN LOWERCASENAME expressionList) 
     | LOWERCASENAME     
     | mean 
     | central
     | PATTERN state -> ^(PATTERN state)
;


mean:
  MEAN LBRACE expression RBRACE -> ^(MEAN expression)
;

central:
   CENTRAL LBRACE expression COMMA INTEGER RBRACE -> ^(CENTRAL expression INTEGER)
  |VARIANCE LBRACE expression RBRACE -> ^(CENTRAL expression INTEGER["2"])
;


combinedPowerProduct:
   powerProduct? accPower*  ->
    ^(COMBINEDPRODUCT powerProduct? accPower*);

powerProduct:
  power+ -> ^(PRODUCT power+);
  
power:
  state (POWER INTEGER)? -> state INTEGER?;
  
acc:
 ACC LPAR powerProduct RPAR -> ^(ACC powerProduct); 
 
accPower:
  acc (POWER INTEGER)? -> acc INTEGER?; 

expressionList:
    expression (COMMA expression)*; 
   
