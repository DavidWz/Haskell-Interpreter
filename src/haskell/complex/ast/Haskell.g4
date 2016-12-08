grammar Haskell;

options {  
  language = Java;
}

topdecl : (decl | datadecl)*;
datadecl : 'data' TYCONSTR VAR* '=' TYCONSTR type* ('|' TYCONSTR type*)*;
decl : fundecl | patdecl;
fundecl : funlhs '=' rhs;
funlhs : VAR pats+;
rhs : exp;
patdecl : pats '=' rhs;
decls : '{' decl (';' decl)* '}';

exp : VAR |       INT | constr | tuple | application | branch | let | cases | lambda;
pats : VAR | '_' | INT | constr | tuple;

tuple : '(' (exp (',' exp)*)? ')';
constr : TYCONSTR | '(' TYCONSTR pats* ')';
application : '(' exp exp+ ')';
branch : 'if' exp 'then' exp 'else' exp;
let : 'let' decls 'in' exp;
cases : 'case' exp 'of' '{' pats '->' exp (';' pats '->' exp)* '}';
lambda : '\\' pats+ '->' exp;

type : functype | tyconstrval | tupletype | VAR;
tyconstrval : TYCONSTR | '(' TYCONSTR type* ')';
functype : '(' type '->' type ')';
tupletype : '(' (type (',' type)*)? ')';


fragment LOWER_CASE : 'a'..'z' ;
fragment UPPER_CASE : 'A'..'Z' ;
fragment DIGIT : '0'..'9' ;
INT : '-'? DIGIT+;
VAR : LOWER_CASE (LOWER_CASE | UPPER_CASE | DIGIT)*;
TYCONSTR : UPPER_CASE (LOWER_CASE | UPPER_CASE | DIGIT)*;

WS: [ \n\t\r]+ -> skip;
IDENTIFIER : (LOWER_CASE | UPPER_CASE | DIGIT)+;