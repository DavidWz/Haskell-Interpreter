package haskell.reduction;

import haskell.ast.*;

import java.util.Set;

/**
 * A helper class which manages variables in complex haskell programs.
 */
public class VariableManager {
    // the current index for our x_i variables
    private static int i = 0;

    // the bottom function variable
    private static ASTVariable botFunc = new ASTVariable("bot");

    /**
     * Initializes this variable manager with the variables found in the given complex haskell term.
     * @param complexHaskell
     */
    public static void init(ComplexHaskell complexHaskell) {
        Set<ASTVariable> vars = complexHaskell.getAllVariables();

        // all our fresh variables are simply called "x_i", so we need to determine the smallest i which is not used yet
        i = 0;
        for(ASTVariable var : vars) {
            String varName = var.getName();
            if (varName.length() >= 2 && varName.charAt(0) == 'x') {

                // determine whether there's a number after x
                boolean isNumbered = true;
                for (int k = 1; k < varName.length(); k++) {
                    if (!Character.isDigit(varName.charAt(k))) {
                        isNumbered = false;
                        break;
                    }
                }

                // if there's a number after x, set i to the next value if it's actually bigger
                if (isNumbered) {
                    int index = Integer.parseInt(varName.substring(1));
                    if (index >= i) {
                        i = index + 1;
                    }
                }
            }
        }
    }

    /**
     * Returns a fresh variable.
     */
    public static ASTVariable getFreshVariable() {
        i++;
        return new ASTVariable("x"+(i-1));
    }

    /**
     * Returns a variable representing the bottom function.
     * @return
     */
    public static ASTVariable getBot() {
        return botFunc;
    }

    /**
     * Returns a variable representing the isa_constr function.
     * @param type the constructor
     * @return
     */
    public static ASTVariable getIsaConstrFunc(ASTTyConstr type) {
        return new ASTVariable("isa_constr_"+type.getName());
    }

    /**
     * Returns a variable representing the isa_n-tuple function.
     * @param n
     * @return
     */
    public static ASTVariable getIsaTupleFunc(int n) {
        return new ASTVariable("isa_tuple_"+n);
    }

    /**
     * Returns a variable representing the argof_constr function.
     * @param type the constructor
     * @return
     */
    public static ASTVariable getArgofFunc(ASTTyConstr type) {
        return new ASTVariable("argof_"+type.getName());
    }

    /**
     * Returns a variable representing the sel_{n,i} function.
     * @param n
     * @param i
     * @return
     */
    public static ASTVariable getSelFunc(int n, int i) {
        return new ASTVariable("sel_"+n+"_"+i);
    }

    private static ASTVariable getIsaIntFunc(int value) {
        return new ASTVariable("isa_int_"+value);
    }

    private static ASTVariable getIsaFloatFunc(float value) {
        return new ASTVariable("isa_float_"+value);
    }

    private static ASTVariable getIsaCharFunc(char value) {
        return new ASTVariable("isa_char_"+value);
    }

    private static ASTVariable getIsaBoolFunc(boolean value) {
        return new ASTVariable("isa_bool_"+value);
    }

    public static ASTVariable getIsaFunc(ASTPattern pat) {
        if (pat instanceof ASTInteger) {
            ASTInteger intPat = (ASTInteger) pat;
            return getIsaIntFunc(intPat.getValue());
        }
        else if (pat instanceof ASTFloat) {
            ASTFloat floatPat = (ASTFloat) pat;
            return getIsaFloatFunc(floatPat.getValue());
        }
        else if (pat instanceof ASTBoolean) {
            ASTBoolean boolPat = (ASTBoolean) pat;
            return getIsaBoolFunc(boolPat.getValue());
        }
        else if (pat instanceof ASTChar) {
            ASTChar charPat = (ASTChar) pat;
            return getIsaCharFunc(charPat.getValue());
        }
        else {
            // unknown constant, this is undefined behavior and should not happen
            assert(false);
            throw new RuntimeException();
        }
    }
}
