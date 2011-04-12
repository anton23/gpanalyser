lexer grammar PCTMCLexerPrototype;

options {
  language = Java;
}

AT: '@';
VAR:'$'; 
LPAR: '('; 
RPAR: ')';  
LBRACE: '{';
RBRACE: '}';

COMMA: ',';
 
PLUS: '+'; 
MINUS:'-';
SEMI: ';'; 
TIMES	:	'*';
DIVIDE	:	'/';
DEF: '='; 
PATTERN:'%';


MIN: 'min';


POWER: '^';
STOPTIME: 'stopTime'; 
STEPSIZE: 'stepSize';
DENSITY: 'density';
REPLICATIONS:'replications';

ACC: 'acc';

TO: '->';

MEAN: 'E';  
CENTRAL: 'CM';
VARIANCE: 'Var';

ODES: 'ODEs';
SIMULATION: 'Simulation';
COMPARE: 'Compare';
ITERATE: 'Iterate';

FROMVALUE: 'from'; 
TOVALUE:'to'; 
IN:'in';
STEPS:'steps';
PLOT:'plot';
ATTIME:'at';
WHERE:'where';
WHEN: 'when'; 
AND:'and';
GEQ:'>=';



UPPERCASENAME : ('A'..'Z')('A'..'Z'|'a'..'z'|'0'..'9'|'_')*('\'')*;
LOWERCASENAME  : ('a'..'z')('a'..'z'|'0'..'9'|'_'|'A'..'Z'|'\'')*;
INTEGER : ('0'..'9')+;
REALNUMBER : ('0'..'9')+('.'('0'..'9')+)?;
FILENAME  : '"' ('a'..'z'|'A'..'Z'|'_'|'0'..'9'|'.'|'/'|'-')* '"';


WHITESPACE : ( '\t' | ' ' | '\r' | '\n'| '\u000C' )+  { $channel = HIDDEN; } ;
COMMENT : '//' (~('\n' | '\r'))* ('\r'|'\n')?  { $channel=HIDDEN;} ;


MULTI_COMMENT options { greedy = false; }
  : '/*' .* '*/' ('\n')? { skip(); };
