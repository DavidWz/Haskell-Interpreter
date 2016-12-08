grammar Haskell98;

options {
  language = Java;
}

topdecls : topdecl*;
topdecl : datadecl | decl;
decls : '{' decl (';' decl)* '}';

datadecl : 'data' simpletype '=' constrs;
decl : gendecl | funlhs rhs;
gendecl : vars '::' type;
vars : var+;

type : atype;
atype : gtycon | var | tupletype | parconstr | functype;
tupletype : '(' type (',' type)+ ')';
parconstr : '(' type ')';
functype : '(' type '->' type ')';

gtycon : con | unittype | functionconstr | tupleconstr;
unittype : '(' ')';
functionconstr : '(' '->' ')';
tupleconstr : '(' ','+ ')';

simpletype : con var*;
constrs : constr ('|' constr)*;
constr : con atype*;

funlhs : var apat+;
rhs : '=' exp;
exp : lambda | letexp | conditional | caseexp | fexp;
lambda : '\\' apat+ '->' exp;
letexp : 'let' decls 'in' exp;
conditional : 'if' exp 'then' exp 'else' exp;
caseexp : 'case' exp 'of' '{' alts '}';
fexp : '(' fexp? aexp ')';
aexp : var | gcon | literal | parexp | tupleexp;
parexp : '(' exp ')';
tupleexp : '(' exp (',' exp)+ ')';
alts : alt (';' alt)*;
alt : pat '->' exp;

pat : apat | gcon apat+;
apat : var | gcon | literal | '_' | parpat | tuplepat;
parpat : '(' pat ')';
tuplepat : '(' pat (',' pat)+ ')';

gcon : unittype | tupleconstr | con;

var : VARID;
con : CONID;
literal : INT | CHAR;

fragment LOWER_CASE : 'a'..'z' ;
fragment UPPER_CASE : 'A'..'Z' ;
fragment DIGIT : '0'..'9' ;
INT : '-'? DIGIT+;
CHAR : '\'' (LOWER_CASE | UPPER_CASE | DIGIT) '\'';

VARID : LOWER_CASE (LOWER_CASE | UPPER_CASE | DIGIT)*;
CONID : UPPER_CASE (LOWER_CASE | UPPER_CASE | DIGIT)*;

WS: [ \n\t\r]+ -> skip;
IDENTIFIER : (LOWER_CASE | UPPER_CASE | DIGIT)+;