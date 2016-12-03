package lambda.ast;

import java.util.Set;

/**
 * Abstract super class for all lambda terms.
 */
public abstract class ASTTerm {
    /**
     * Returns a set of free variables of this term.
     * @return a set of free variables
     */
    public abstract Set<ASTVariable> getFreeVars();

    /**
     * Substitutes all free occurences of var in this term by expr.
     * @param var the variable which will be substituted
     * @param expr the generated expression
     * @return the substituted term
     */
    public abstract ASTTerm substitute(ASTVariable var, ASTTerm expr);
}
