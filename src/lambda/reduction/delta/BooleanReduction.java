package lambda.reduction.delta;

import lambda.ast.ASTConstant;
import lambda.ast.ASTTerm;

import java.util.List;
import java.util.Optional;

/**
 * Represents the delta rules for operations on booleans.
 */
public class BooleanReduction extends DeltaReduction {
    @Override
    public int getNumberOfArguments() {
        return 2;
    }

    @Override
    public boolean isConstantMatching(ASTConstant c) {
        if (c.getValue() instanceof PredefinedFunction) {
            PredefinedFunction op = (PredefinedFunction) c.getValue();
            switch (op) {
                case AND:
                    return true;
                case OR:
                    return true;
                case EQUIV:
                    return true;
                case XOR:
                    return true;
                case IMPLIES:
                    return true;
                default:
                    return false;
            }
        }
        else {
            return false;
        }
    }

    @Override
    public Optional<ASTTerm> getRHS(ASTConstant constant, List<ASTTerm> terms) {
        if (!isSignatureMatching(constant, terms)) {
            return Optional.empty();
        }

        // the signature is matching, so it's an operator with 2 arguments
        PredefinedFunction op = (PredefinedFunction) constant.getValue();

        // the two arguments must be constants
        if (!(terms.get(0) instanceof ASTConstant) || !(terms.get(1) instanceof ASTConstant)) {
            return Optional.empty();
        }
        ASTConstant c0 = (ASTConstant) terms.get(0);
        ASTConstant c1 = (ASTConstant) terms.get(1);

        // and both constants must be booleans
        if (c0.getValue() instanceof Boolean && c1.getValue() instanceof Boolean) {
            boolean b0 = (boolean) c0.getValue();
            boolean b1 = (boolean) c1.getValue();

            // now we calculate the result
            Object result;
            switch (op) {
                case AND:
                    result = b0 && b1;
                    break;
                case OR:
                    result = b0 || b1;
                    break;
                case EQUIV:
                    result = b0 == b1;
                    break;
                case XOR:
                    result = b0 != b1;
                    break;
                case IMPLIES:
                    result = !b0 || b1;
                    break;
                default:
                    return Optional.empty();
            }

            return Optional.of(new ASTConstant(result));
        }
        else {
            return Optional.empty();
        }
    }
}
