package lambda.reduction.delta;

import haskell.ast.*;
import lambda.type.PredefinedType;

/**
 * Enum of all predefined functions. (except for isa_, argof_, and sel_ functions)
 */
public enum PredefinedFunction {
    // operations on integers
    PLUS(PredefinedType.INTEGER, PredefinedType.INTEGER, PredefinedType.INTEGER),
    MINUS(PredefinedType.INTEGER, PredefinedType.INTEGER, PredefinedType.INTEGER),
    MULT(PredefinedType.INTEGER, PredefinedType.INTEGER, PredefinedType.INTEGER),
    DIV(PredefinedType.INTEGER, PredefinedType.INTEGER, PredefinedType.INTEGER),
    MOD(PredefinedType.INTEGER, PredefinedType.INTEGER, PredefinedType.INTEGER),
    POW(PredefinedType.INTEGER, PredefinedType.INTEGER, PredefinedType.INTEGER),
    LESS(PredefinedType.INTEGER, PredefinedType.INTEGER, PredefinedType.BOOL),
    GREATER(PredefinedType.INTEGER, PredefinedType.INTEGER, PredefinedType.BOOL),
    LESSEQ(PredefinedType.INTEGER, PredefinedType.INTEGER, PredefinedType.BOOL),
    GREATEREQ(PredefinedType.INTEGER, PredefinedType.INTEGER, PredefinedType.BOOL),
    EQUAL(PredefinedType.INTEGER, PredefinedType.INTEGER, PredefinedType.BOOL),
    INEQUAL(PredefinedType.INTEGER, PredefinedType.INTEGER, PredefinedType.BOOL),

    // operations on floating point numbers
    PLUSF(PredefinedType.FLOAT, PredefinedType.FLOAT, PredefinedType.FLOAT),
    MINUSF(PredefinedType.FLOAT, PredefinedType.FLOAT, PredefinedType.FLOAT),
    MULTF(PredefinedType.FLOAT, PredefinedType.FLOAT, PredefinedType.FLOAT),
    DIVF(PredefinedType.FLOAT, PredefinedType.FLOAT, PredefinedType.FLOAT),
    POWF(PredefinedType.FLOAT, PredefinedType.FLOAT, PredefinedType.FLOAT),
    LESSF(PredefinedType.FLOAT, PredefinedType.FLOAT, PredefinedType.BOOL),
    GREATERF(PredefinedType.FLOAT, PredefinedType.FLOAT, PredefinedType.BOOL),
    LESSEQF(PredefinedType.FLOAT, PredefinedType.FLOAT, PredefinedType.BOOL),
    GREATEREQF(PredefinedType.FLOAT, PredefinedType.FLOAT, PredefinedType.BOOL),
    EQUALF(PredefinedType.FLOAT, PredefinedType.FLOAT, PredefinedType.BOOL),
    INEQUALF(PredefinedType.FLOAT, PredefinedType.FLOAT, PredefinedType.BOOL),

    // operations on characters
    EQUALC(PredefinedType.CHAR, PredefinedType.CHAR, PredefinedType.BOOL),
    INEQUALC(PredefinedType.CHAR, PredefinedType.CHAR, PredefinedType.BOOL),

    // operations on booleans
    NOT(PredefinedType.BOOL, PredefinedType.BOOL),
    AND(PredefinedType.BOOL, PredefinedType.BOOL, PredefinedType.BOOL),
    OR(PredefinedType.BOOL, PredefinedType.BOOL, PredefinedType.BOOL),
    EQUIV(PredefinedType.BOOL, PredefinedType.BOOL, PredefinedType.BOOL),
    XOR(PredefinedType.BOOL, PredefinedType.BOOL, PredefinedType.BOOL),
    IMPLIES(PredefinedType.BOOL, PredefinedType.BOOL, PredefinedType.BOOL),

    // other predefined functions

    // bot :: a
    BOT(new ASTVariable("a")),

    // if :: Bool -> a -> a -> a
    IF(new ASTFuncType(PredefinedType.BOOL.getType(),
            new ASTFuncType(new ASTVariable("a"),
                    new ASTFuncType(new ASTVariable("a"), new ASTVariable("a"))))),

    // fix :: (a -> a) -> a
    FIX(new ASTFuncType(new ASTFuncType(new ASTVariable("a"), new ASTVariable("a")),
            new ASTVariable("a")));

    private ASTType type;

    PredefinedFunction(PredefinedType input1, PredefinedType input2, PredefinedType output) {
        this.type = new ASTFuncType(input1.getType(), new ASTFuncType(input2.getType(), output.getType()));
    }

    PredefinedFunction(PredefinedType input, PredefinedType output) {
        this.type = new ASTFuncType(input.getType(), output.getType());
    }

    PredefinedFunction(ASTType type) {
        this.type = type;
    }

    public ASTType getType() {
        return type;
    }
}
