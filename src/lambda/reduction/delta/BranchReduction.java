package lambda.reduction.delta;

import lambda.ast.*;

import java.util.List;
import java.util.Optional;

/**
 * Represents delta rules for branches, e.g. if - else statements
 */
public class BranchReduction extends DeltaReduction {
    public enum Operator {
        IF
    }

    @Override
    public int getNumberOfArguments() {
        return 1;
    }

    @Override
    public boolean isConstantMatching(ASTConstant c) {
        return (c.getValue().equals(Operator.IF));
    }

    @Override
    public Optional<ASTTerm> getRHS(ASTConstant constant, List<ASTTerm> terms) {
        // the constant must be FIX and it has only one argument
        if (isSignatureMatching(constant, terms)) {
            ASTTerm cond = terms.get(0);
            boolean condition;

            // condition must be a boolean constant
            if (cond instanceof ASTConstant && ((ASTConstant) cond).getValue() instanceof Boolean) {
                condition = (Boolean) ((ASTConstant) cond).getValue();
            }
            else {
                return Optional.empty();
            }

            // if True -> lambda x.(lambda y.x)
            if (condition) {
                ASTAbstraction result = new ASTAbstraction(new ASTVariable("x"), new ASTAbstraction(new ASTVariable("y"), new ASTVariable("x")));
                return Optional.of(result);
            }
            // if False -> lambda x.(lambda y.y)
            else {
                ASTAbstraction result = new ASTAbstraction(new ASTVariable("x"), new ASTAbstraction(new ASTVariable("y"), new ASTVariable("y")));
                return Optional.of(result);
            }
        }
        else {
            return Optional.empty();
        }
    }
}
