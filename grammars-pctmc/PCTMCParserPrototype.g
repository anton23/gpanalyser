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
    ir=rangeSpecifications 
  (MINIMISE minSpec=plotAtSpecificationNoFile mr=rangeSpecifications)?
    (WHERE
      constantReEvaluation+)?
    analysis PLOT LBRACE
      plots=plotAtSpecifications
    RBRACE
  -> ^(ITERATE $ir (MINIMISE ^(PLOT $minSpec) $mr)? (WHERE constantReEvaluation+)? analysis $plots)
;

rangeSpecifications:
rangeSpecification+
;

constantReEvaluation:
  id=LOWERCASENAME DEF rhs=expression SEMI ->  $id $rhs
;

rangeSpecification:
  constant=LOWERCASENAME FROMVALUE from=REALNUMBER TOVALUE to=REALNUMBER steps=stepSpecification 
  -> ^(RANGE $constant $from $to $steps)
;

stepSpecification:
    IN INTEGER STEPS 
  | STEP REALNUMBER 
;

plotAtSpecifications:
  plotAtSpecification+
;

plotAtSpecification:
 plotAtSpecificationNoFile (TO FILENAME)? SEMI -> 
  ^(PLOT plotAtSpecificationNoFile FILENAME?)
;

plotAtSpecificationNoFile:
  plotAt (WHEN constraints)?
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
  MEAN LBRACK expression RBRACK -> ^(MEAN expression)
;

central:
   CENTRAL LBRACK expression COMMA INTEGER RBRACK -> ^(CENTRAL expression INTEGER)
  |VARIANCE LBRACK expression RBRACK -> ^(CENTRAL expression INTEGER["2"])
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
   
