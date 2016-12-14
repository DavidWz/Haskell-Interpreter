grammar ComplexHaskell;

options {  
  language = Java;
}

program : decl*;
decl : fundecl | patdecl;
decls : '{' decl (';' decl)* '}' | '{' '}';
fundecl : var pat+ '=' exp;
patdecl : pat '=' exp;

exp : var | tyconstr |         integer | bool | expTuple | application | branch | let | cases | lambda;
pat : var | tyconstr | joker | integer | bool | patTuple | construct;

expTuple : '(' exp (',' exp)* ')';
patTuple : '(' pat (',' pat)* ')';

application : '(' exp exp+ ')';
branch : 'if' exp 'then' exp 'else' exp;
let : 'let' decls 'in' exp;
cases : 'case' exp 'of' '{' pat '->' exp (';' pat '->' exp)* '}';
lambda : '\\' pat+ '->' exp;
construct : '(' tyconstr pat* ')';

var : VARID;
tyconstr : TYCONSTRID;
integer : INT;
bool : 'True' | 'False';
joker : JOKER;


fragment LOWER_CASE : 'a'..'z' ;
fragment UPPER_CASE : 'A'..'Z' ;
fragment DIGIT : '0'..'9' ;
VARID : LOWER_CASE (LOWER_CASE | UPPER_CASE | DIGIT)*;
TYCONSTRID : UPPER_CASE (LOWER_CASE | UPPER_CASE | DIGIT)*;
INT : DIGIT+;
JOKER : '_';

WS: [ \n\t\r]+ -> skip;
REMAINDER : (LOWER_CASE | UPPER_CASE | DIGIT)+;