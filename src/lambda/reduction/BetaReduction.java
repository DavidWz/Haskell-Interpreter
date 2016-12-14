package lambda.reduction;

import lambda.ast.*;

import java.util.Optional;

/**
 * Implements the beta reduction for lambda terms.
 * I.e.: (lambda x . t) r -> t [x / r]
 */
public class BetaReduction implements LambdaTransformation {
    public Optional<ASTTerm> visit(ASTApplication node) {
        ASTTerm left = node.getLeft();
        if (left instanceof ASTAbstraction) {
            // we can apply a beta reduction step
            ASTAbstraction abstraction = (ASTAbstraction) left;

            // apply the beta reduction as follows: (lambda x . t) r -> t [x / r]
            return Optional.of(abstraction.getOutput().substitute(abstraction.getInput(), node.getRight()));
        }
        else {
            return Optional.empty();
        }
    }
}
