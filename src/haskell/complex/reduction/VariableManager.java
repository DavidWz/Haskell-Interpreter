package haskell.complex.reduction;

import haskell.complex.ast.ASTVariable;
import haskell.complex.ast.ComplexHaskell;

import java.util.Set;

/**
 * A helper class which manages variables in complex haskell programs.
 */
public class VariableManager {
    // the current index for our x_i variables
    private int i = 0;

    public VariableManager(ComplexHaskell complexHaskell) {
        init(complexHaskell);
    }

    /**
     * Initializes this variable manager with the variables found in the given complex haskell term.
     * @param complexHaskell
     */
    public void init(ComplexHaskell complexHaskell) {
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
    public ASTVariable getFreshVariable() {
        i++;
        return new ASTVariable("x"+(i-1));
    }
}
