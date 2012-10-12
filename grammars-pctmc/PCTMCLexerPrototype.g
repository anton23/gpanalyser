lexer grammar PCTMCLexerPrototype;

options {
  language = Java;
  backtrack = true;
}


AT: '@';
VAR:'$'; 
LPAR: '('; 
RPAR: ')';  
LBRACE: '{';
RBRACE: '}';
LBRACK: '[';
RBRACK: ']';

COMMA: ',';
 
PLUS: '+'; 
MINUS:'-';
SEMI: ';'; 
TIMES	:	'*';
DIVIDE	:	'/';
DEF: '='; 
PATTERN:'%';


MIN: 'min';
MAX: 'max';


POWER: '^';
STOPTIME: 'stopTime'; 
STEPSIZE: 'stepSize';
DENSITY: 'density';
REPLICATIONS:'replications';
CI:'CI';
MAXRELCIWIDTH:'maxRelCIWidth';
BATCHSIZE:'batchSize';

ACC: 'acc';

TO: '->';

MEAN: 'E';  
GENEXPECTATION: 'Eg';
CENTRAL: 'CM';
SCENTRAL: 'SCM';
VARIANCE: 'Var';
MOMENT: 'Moment';
COV: 'Cov';

ODES: 'ODEs';
SIMULATION: 'Simulation';
CISIMULATION: 'CISimulation';
ACCURATESIMULATION: 'AccurateSimulation';
COMPARE: 'Compare';
ITERATE: 'Iterate';
TRANSIENT_ITERATE: 'TransientIterate';
DISTRIBUTION_SIMULATION: 'DistributionSimulation';
MINIMISE: 'Minimise';

FROMVALUE: 'from'; 
TOVALUE:'to'; 
IN:'in';
STEPS:'steps';
STEP:'with step';
PLOT:'plot';
ATTIME:'at';
WHERE:'where';
WHEN: 'when'; 
AND:'and';
GEQ:'>=';
RANGLE:'>';
LANGLE:'<';

DERMEAN: 'dE';
DT: 'dt';

UPPERCASENAME : ('A'..'Z')('A'..'Z'|'a'..'z'|'0'..'9'|'_')*('\'')*;
LOWERCASENAME  : ('a'..'z'|'_')('a'..'z'|'0'..'9'|'_'|'A'..'Z'|'\'')*;
INTEGER : ('0'..'9')+;
REALNUMBER : ('0'..'9')+('.'('0'..'9')+)?;
FILENAME  : '"' ('a'..'z'|'A'..'Z'|'_'|'0'..'9'|'.'|'/'|'-')* '"';

WHITESPACE : ( '\t' | ' ' | '\r' | '\n'| '\u000C' )+  { $channel = HIDDEN; } ;
COMMENT : '//' (~('\n' | '\r'))* ('\r'|'\n')?  { $channel=HIDDEN;} ;


MULTI_COMMENT
  : '/*' (options {greedy=false;} : . )* '*/' ('\n')? { skip(); };
