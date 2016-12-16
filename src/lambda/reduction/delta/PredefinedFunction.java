package lambda.reduction.delta;

/**
 * Enum of all predefined functions. (except for isa_, argof_, and sel_ functions)
 */
public enum PredefinedFunction {
    // operations on integers
    PLUS,
    MINUS,
    MULT,
    DIV,
    MOD,
    POW,
    LESS,
    GREATER,
    LESSEQ,
    GREATEREQ,
    EQUAL,
    INEQUAL,
    // operations on floating point numbers
    PLUSF,
    MINUSF,
    MULTF,
    DIVF,
    POWF,
    LESSF,
    GREATERF,
    LESSEQF,
    GREATEREQF,
    EQUALF,
    INEQUALF,
    // operations on characters
    EQUALC,
    INEQUALC,
    // operations on booleans
    NOT,
    AND,
    OR,
    EQUIV,
    XOR,
    IMPLIES,
    // other predefined functions
    BOT,
    IF,
    FIX
}
