package lambda.ast;

import java.util.List;
import java.util.Set;

/**
 * Abstract super class for all lambda terms.
 */
public abstract class ASTTerm {
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
     * Substitutes all free occurrences of var in this term by expr.
     * @param var the variable which will be substituted
     * @param expr the generated expression
     * @return the substituted term
     */
    public abstract ASTTerm substitute(ASTVariable var, ASTTerm expr);

    /**
     * Accepts a lambda visitor and calls its visit method for the corresponding node.
     * @param visitor
     * @return
     */
    public abstract <T> T accept(LambdaVisitor<T> visitor);
}
