package lambda.ast;

import lambda.reduction.delta.DeltaRule;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Abstract super class for all lambda terms.
 */
public abstract class ASTTerm {
    /**
     * Applies a weak head normal order reduction step to this ASTTerm. If no reduction step can be performed, an empty
     * optional is returned.
     * @param deltaRules the delta rules
     * @return an optional containing the reduced term or empty if no reduction could be applied
     */
    public abstract Optional<ASTTerm> applyWHNOReduction(List<DeltaRule> deltaRules);

    /**
     * Applies a beta reduction step on the root node.
     * @return an optional containing the reduced term or empty if no reduction could be applied
     */
    protected abstract Optional<ASTTerm> applyBetaReduction();

    /**
     * Applies a delta reduction step on the root node.
     * @param delta the delta rule for this step
     * @return an optional containing the reduced term or empty if no reduction could be applied
     */
    protected abstract Optional<ASTTerm> applyDeltaReduction(DeltaRule delta);

    /**
     * Returns the arguments of left-most outer-most applications inside this lambda term. For example, the term
     * (((a 1) (b a)) 2) results in {1, (b a), 2}.
     * @return the left-most outer-most depth of this term
     */
    public abstract List<ASTTerm> getLMOMArguments();

    /**
     * Returns the left-most outer-most term (this will be something other than an application).
     * @return the left-most outer-most term
     */
    public abstract ASTTerm getLMOMTerm();

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
