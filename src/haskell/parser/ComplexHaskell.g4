grammar ComplexHaskell;

options {  
  language = Java;
}

program : (decl '\n'+)*;
decl : fundecl | patdecl | datadecl;
decls : '{' decl (';' decl)* '}' | '{' '}';
fundecl : var pat+ '=' exp;
patdecl : pat '=' exp;
datadecl : 'data' tyconstr var* '=' constrdecl ('|' constrdecl)*;
constrdecl : tyconstr type*;

exp : var | tyconstr |         integer | bool | floating | character | expTuple | application | branch | let | cases | lambda;
pat : var | tyconstr | joker | integer | bool | floating | character | patTuple | construct;
type : var | typeconstr | functype | tupletype;

expTuple : '(' exp (',' exp)* ')';
patTuple : '(' pat (',' pat)* ')';

application : '(' exp exp+ ')';
branch : 'if' exp 'then' exp 'else' exp;
let : 'let' decls 'in' exp;
cases : 'case' exp 'of' '{' pat '->' exp (';' pat '->' exp)* '}';
lambda : '\\' pat+ '->' exp;
construct : '(' tyconstr pat* ')';

typeconstr : '(' tyconstr type* ')';
functype : '(' type '->' type ')';
tupletype : '(' (type (',' type)*)? ')';

var : VARID;
tyconstr : TYCONSTRID;
integer : INT;
bool : 'True' | 'False';
floating : FLOAT;
character : CHAR;
joker : JOKER;


fragment LOWER_CASE : 'a'..'z' ;
fragment UPPER_CASE : 'A'..'Z' ;
fragment DIGIT : '0'..'9' ;
CHAR : '\'' . '\'';
VARID : LOWER_CASE (LOWER_CASE | UPPER_CASE | DIGIT)*;
TYCONSTRID : UPPER_CASE (LOWER_CASE | UPPER_CASE | DIGIT)*;
INT : ('+'|'-')?DIGIT+;
FLOAT : ('+'|'-')? DIGIT+ '.' DIGIT+;
JOKER : '_';

WS: [ \t\r]+ -> skip;
REMAINDER : (LOWER_CASE | UPPER_CASE | DIGIT)+;