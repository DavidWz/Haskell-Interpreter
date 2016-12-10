grammar ComplexHaskell;

options {  
  language = Java;
}

program : decl*;
decl : fundecl | patdecl;
decls : '{' decl (';' decl)* '}' | '{' '}';
fundecl : VAR pat+ '=' exp;
patdecl : pat '=' exp;

exp : VAR | TYCONSTR |       INT | BOOL | expTuple | application | branch | let | cases | lambda;
pat : VAR | TYCONSTR | '_' | INT | BOOL | patTuple | construct;

expTuple : '(' exp (',' exp)* ')';
patTuple : '(' pat (',' pat)* ')';

application : '(' exp exp+ ')';
branch : 'if' exp 'then' exp 'else' exp;
let : 'let' decls 'in' exp;
cases : 'case' exp 'of' '{' caseExp (';' caseExp)* '}';
caseExp : pat '->' exp;
lambda : '\\' pat+ '->' exp;
construct : '(' TYCONSTR pat* ')';

INT : DIGIT+;
BOOL : 'True' | 'False';

fragment LOWER_CASE : 'a'..'z' ;
fragment UPPER_CASE : 'A'..'Z' ;
fragment DIGIT : '0'..'9' ;
VAR : LOWER_CASE (LOWER_CASE | UPPER_CASE | DIGIT)*;
TYCONSTR : UPPER_CASE (LOWER_CASE | UPPER_CASE | DIGIT)*;

WS: [ \n\t\r]+ -> skip;
IDENTIFIER : (LOWER_CASE | UPPER_CASE | DIGIT)+;