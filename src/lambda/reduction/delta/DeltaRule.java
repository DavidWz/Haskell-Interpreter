package lambda.reduction.delta;

import lambda.ast.ASTConstant;
import lambda.ast.ASTTerm;
import java.util.List;
import java.util.Optional;

/**
 * Represents one or several delta rules, i.e. an actual interpretation of a value or function.
 */
public abstract class DeltaRule {
    /**
     * Checks whether the list of terms is reducible. I.e., whether no argument contains free variables and
     * every argument is in beta-normalform.
     * @param terms
     * @return
     */
    public static boolean isReducible(List<ASTTerm> terms) {
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
