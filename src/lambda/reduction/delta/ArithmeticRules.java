package lambda.reduction.delta;

import lambda.ast.ASTConstant;
import lambda.ast.ASTTerm;

import java.util.List;
import java.util.Optional;

/**
 * Represents the delta rules for arithmetic operations on integers.
 */
public class ArithmeticRules extends DeltaRule {
    public enum Operator {
        PLUS,
        MINUS,
        TIMES,
        DIVIDED
    }

    @Override
    public int getNumberOfArguments() {
        return 2;
    }

    @Override
    public boolean isConstantMatching(ASTConstant c) {
        return (c.getValue() instanceof Operator);
    }

    @Override
    public Optional<ASTTerm> getRHS(ASTConstant constant, List<ASTTerm> terms) {
        if (!isSignatureMatching(constant, terms)) {
            return Optional.empty();
        }

        // the signature is matching, so it's an operator with 2 arguments
        Operator op = (Operator) constant.getValue();
        if (terms.get(0) instanceof ASTConstant && terms.get(1) instanceof ASTConstant) {
            ASTConstant c0 = (ASTConstant) terms.get(0);
            ASTConstant c1 = (ASTConstant) terms.get(1);

            // and both constants must be numbers
            if (c0.getValue() instanceof Integer && c1.getValue() instanceof Integer) {
                int n0 = (Integer) c0.getValue();
                int n1 = (Integer) c1.getValue();

                // now we calculate the result
                Number result;
                switch (op) {
                    case PLUS:
                        result = n0 + n1;
                        break;
                    case MINUS:
                        result = n0 - n1;
                        break;
                    case TIMES:
                        result = n0 * n1;
                        break;
                    case DIVIDED:
                        result = n0 / n1;
                        break;
                    default:
                        return Optional.empty();
                }

                return Optional.of(new ASTConstant(result));
            }
        }

        return Optional.empty();
    }
}
