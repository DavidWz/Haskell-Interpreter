package lambda.ast;

import java.util.Optional;
import java.util.Set;

/**
 * Abstract super class for all lambda terms.
 */
public abstract class ASTTerm {
    /**
     * Applies a beta reduction step to the left-most outer-most node where such a step can be applied.
     * @return an optional containing the reduced term or empty if no reduction could be applied
     */
    public abstract Optional<ASTTerm> applyBetaReduction();

    public Optional<ASTTerm> applyDeltaReduction() {
        // TODO
        return Optional.empty();
    }

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
