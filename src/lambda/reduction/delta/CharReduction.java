package lambda.reduction.delta;

import lambda.ast.ASTConstant;
import lambda.ast.ASTTerm;

import java.util.List;
import java.util.Optional;

/**
 * Represents delta rules for operations on characters.
 */
public class CharReduction extends DeltaReduction {
    @Override
    public int getNumberOfArguments() {
        return 2;
    }

    @Override
    public boolean isConstantMatching(ASTConstant c) {
        if (c.getValue() instanceof PredefinedFunction) {
            PredefinedFunction op = (PredefinedFunction) c.getValue();
            switch (op) {
                case EQUALC:
                    return true;
                case INEQUALC:
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

        // and both constants must be characters
        if (c0.getValue() instanceof Character && c1.getValue() instanceof Character) {
            char ch1 = (char) c0.getValue();
            char ch2 = (char) c1.getValue();

            // now we calculate the result
            Object result;
            switch (op) {
                case EQUALC:
                    result = ch1 == ch2;
                    break;
                case INEQUALC:
                    result = ch1 != ch2;
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
