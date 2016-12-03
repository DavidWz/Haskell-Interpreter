package lambda.reduction.delta;

import lambda.ast.ASTApplication;
import lambda.ast.ASTConstant;
import lambda.ast.ASTTerm;

import java.util.List;
import java.util.Optional;

/**
 * Represents the fixpoint operator.
 */
public class FixRule extends DeltaRule {
    public enum Operator {
        FIX
    }

    @Override
    public Optional<ASTTerm> getRHS(ASTConstant constant, List<ASTTerm> terms) {
        // the constant must be FIX and it has only one argument
        if (constant.getValue().equals(Operator.FIX) && terms.size() == 1) {
            ASTTerm t = terms.get(0);
            // fix t -> t (fix t)
            ASTApplication fix = new ASTApplication(t, new ASTApplication(new ASTConstant(Operator.FIX), t));
            return Optional.of(fix);
        }
        else {
            return Optional.empty();
        }
    }
}
