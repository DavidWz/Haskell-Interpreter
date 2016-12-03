package lambda.reduction.delta;

import lambda.ast.ASTConstant;
import lambda.ast.ASTTerm;
import java.util.List;
import java.util.Optional;

/**
 * Represents one or several delta rules, i.e. an actual interpretation of a value or function.
 * However, every delta rule in the same class must take the same number of arguments and the same types of constants.
 */
public abstract class DeltaRule {
    /**
     * Returns the number of arguments needed for this delta rule.
     * @return the number of arguments
     */
    public abstract int getNumberOfArguments();

    /**
     * Returns whether the constant belongs to this delta rule.
     * @return whether the constant belongs to this delta rule
     */
    public abstract boolean isConstantMatching(ASTConstant c);

    /**
     * Checks whether the signature of the constant and arguments is matching with this delta rule.
     * @param constant the constant
     * @param terms the arguments
     * @return whether it's matching
     */
    protected boolean isSignatureMatching(ASTConstant constant, List<ASTTerm> terms) {
        // check the right number of arguments
        if (terms.size() != getNumberOfArguments()) {
            return false;
        }

        // check the constant value
        if (!isConstantMatching(constant)) {
            return false;
        }

        // check if the terms are closed and in normal form
        for (ASTTerm t : terms) {
            if (t.getFreeVars().size() > 0) {
                return false;
            }
            else if (t.isBetaReducible()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns a right-hand side for the given constant with the given list of arguments matches this [class of] delta rule[s].
     * If this delta rule is not applicable to the constant and/or arguments, then an empty optional is returned.
     * @param constant the constant
     * @param terms the list of arguments
     * @return the resulting ast term or empty
     */
    public abstract Optional<ASTTerm> getRHS(ASTConstant constant, List<ASTTerm> terms);
}
