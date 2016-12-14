package lambda.reduction;

import haskell.complex.ast.*;
import lambda.ast.*;
import lambda.ast.ASTApplication;
import lambda.ast.ASTVariable;

import java.util.Optional;

/**
 * Common interface for transformations (beta, delta) on lambda terms.
 * Each visit method returns the transformed node, or an optional empty if the transformation could not be applied.
 * The default behavior is returning an optional empty for every node. The visit method for a general ASTTerm however
 * will call the appropriate visit method for its argument.
 */
public interface LambdaTransformation {
    default Optional<ASTTerm> visit(ASTAbstraction node) {
        return Optional.empty();
    }

    default Optional<ASTTerm> visit(ASTApplication node) {
        return Optional.empty();
    }

    default Optional<ASTTerm> visit(ASTConstant node) {
        return Optional.empty();
    }

    default Optional<ASTTerm> visit(ASTVariable node) {
        return Optional.empty();
    }

    default Optional<ASTTerm> visit(ASTTerm node) {
        if (node instanceof ASTAbstraction) {
            return visit((ASTAbstraction) node);
        }
        else if (node instanceof ASTApplication) {
            return visit((ASTApplication) node);
        }
        else if (node instanceof ASTConstant) {
            return visit((ASTConstant) node);
        }
        else if (node instanceof ASTVariable) {
            return visit((ASTVariable) node);
        }
        else {
            assert(false);
            return Optional.empty();
        }
    }
}
