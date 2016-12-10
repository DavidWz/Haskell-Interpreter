package haskell.complex.reduction;

import haskell.complex.ast.ComplexHaskell;

/**
 * Exceptions thrown when a complex haskell term cannot be cast to a simple haskell term.
 */
public class TooComplexException extends Exception {
    private ComplexHaskell exp;
    private String msg;

    /**
     * @param exp the expression which cannot be cast to simple haskell
     * @param msg an error message explaining why it cannot be cast to simple haskell
     */
    public TooComplexException(ComplexHaskell exp, String msg) {
        this.exp = exp;
        this.msg = msg;
    }

    @Override
    public String getMessage() {
        StringBuilder builder = new StringBuilder();
        builder.append("The complex haskell expression cannot be fully reduced to a simple haskell expression, because:\n");
        builder.append(msg);
        builder.append("\nThe expression was: \n");
        builder.append(exp);
        return builder.toString();
    }
}
