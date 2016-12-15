package lambda.reduction.delta;

import lambda.ast.ASTApplication;
import lambda.ast.ASTConstant;
import lambda.ast.ASTTerm;

import java.util.List;
import java.util.Optional;

/**
 * Represents the fixpoint operator.
 */
public class FixReduction extends DeltaReduction {
    @Override
    public int getNumberOfArguments() {
        return 1;
    }

    @Override
    public boolean isConstantMatching(ASTConstant c) {
        return (c.getValue().equals(PredefinedFunction.FIX));
    }

    @Override
    public Optional<ASTTerm> getRHS(ASTConstant constant, List<ASTTerm> terms) {
        // the constant must be FIX and it has only one argument
        if (isSignatureMatching(constant, terms)) {
            ASTTerm t = terms.get(0);
            // fix t -> t (fix t)
            ASTApplication fix = new ASTApplication(t, new ASTApplication(new ASTConstant(PredefinedFunction.FIX), t));
            return Optional.of(fix);
        }
        else {
            return Optional.empty();
        }
    }
}
