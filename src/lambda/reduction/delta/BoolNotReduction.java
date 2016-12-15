package lambda.reduction.delta;

import lambda.ast.ASTConstant;
import lambda.ast.ASTTerm;

import java.util.List;
import java.util.Optional;

/**
 * Represents the boolean not operator.
 */
public class BoolNotReduction extends DeltaReduction {
    @Override
    public int getNumberOfArguments() {
        return 1;
    }

    @Override
    public boolean isConstantMatching(ASTConstant c) {
        return (c.getValue().equals(PredefinedFunction.NOT));
    }

    @Override
    public Optional<ASTTerm> getRHS(ASTConstant constant, List<ASTTerm> terms) {
        // the constant must be NOT and it has one argument
        if (isSignatureMatching(constant, terms)) {
            if (terms.get(0) instanceof ASTConstant) {
                ASTConstant c = (ASTConstant) terms.get(0);

                // the result is the negated boolean
                if (c.getValue() instanceof Boolean) {
                    Boolean b = (Boolean) c.getValue();
                    return Optional.of(new ASTConstant(!b));
                }
            }
        }

        return Optional.empty();
    }
}
