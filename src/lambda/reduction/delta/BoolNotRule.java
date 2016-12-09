package lambda.reduction.delta;

import lambda.ast.ASTConstant;
import lambda.ast.ASTTerm;

import java.util.List;
import java.util.Optional;

/**
 * Represents the boolean not operator.
 */
public class BoolNotRule extends DeltaRule {
    public enum Operator {
        NOT
    }

    @Override
    public int getNumberOfArguments() {
        return 1;
    }

    @Override
    public boolean isConstantMatching(ASTConstant c) {
        return (c.getValue().equals(Operator.NOT));
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

    public static Optional<ASTConstant> toConst(String name) {
        if (name.equals("true")) {
            return Optional.of(new ASTConstant(true));
        }
        else if (name.equals("false")) {
            return Optional.of(new ASTConstant(false));
        }
        else {
            return Optional.empty();
        }
    }
}
