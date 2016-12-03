package lambda.reduction;

import lambda.ast.ASTApplication;
import lambda.ast.ASTTerm;
import lambda.ast.ASTAbstraction;
import lambda.ast.ASTVariable;

/**
 * A class which can perform weak head normal order reductions to a lambda term.
 */
public class WHNOReduction {

    /**
     * Applies a beta reduction step to an ASTTerm. If no beta reduction step can be performed,
     * the term is left unchanged.
     * @param term the term
     * @return the reduced term
     */
    public static ASTTerm applyBetaReduction(ASTTerm term) {
        if (term instanceof ASTApplication) {
            ASTApplication application = (ASTApplication) term;

            if (application.getFunction() instanceof ASTAbstraction) {
                ASTAbstraction abstraction = (ASTAbstraction) application.getFunction();

                // apply the beta reduction by substituting as follows:
                // (lambda x . t) r -> t [x / r]
                return abstraction.getOutput().substitute(abstraction.getInput(), application.getArgument());
            }
            else {
                return term;
            }
        }
        else {
            return term;
        }
    }
}
