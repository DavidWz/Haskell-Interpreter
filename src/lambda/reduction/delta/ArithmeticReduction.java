package lambda.reduction.delta;

import lambda.ast.ASTConstant;
import lambda.ast.ASTTerm;

import java.util.List;
import java.util.Optional;

/**
 * Represents the delta rules for arithmetic operations on integers and floats.
 */
public class ArithmeticReduction extends DeltaReduction {
    @Override
    public int getNumberOfArguments() {
        return 2;
    }

    @Override
    public boolean isConstantMatching(ASTConstant c) {
        if (c.getValue() instanceof PredefinedFunction) {
            PredefinedFunction op = (PredefinedFunction) c.getValue();
            switch (op) {
                case PLUS:
                    return true;
                case MINUS:
                    return true;
                case MULT:
                    return true;
                case DIV:
                    return true;
                case MOD:
                    return true;
                case LESS:
                    return true;
                case GREATER:
                    return true;
                case LESSEQ:
                    return true;
                case GREATEREQ:
                    return true;
                case EQUAL:
                    return true;
                case INEQUAL:
                    return true;
                case POW:
                    return true;
                case PLUSF:
                    return true;
                case MINUSF:
                    return true;
                case MULTF:
                    return true;
                case DIVF:
                    return true;
                case POWF:
                    return true;
                case LESSF:
                    return true;
                case GREATERF:
                    return true;
                case LESSEQF:
                    return true;
                case GREATEREQF:
                    return true;
                case EQUALF:
                    return true;
                case INEQUALF:
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

        // and both constants must be of the same type
        if (c0.getValue() instanceof Integer && c1.getValue() instanceof Integer) {
            int n0 = (int) c0.getValue();
            int n1 = (int) c1.getValue();

            // now we calculate the result
            Object result;
            switch (op) {
                case PLUS:
                    result = n0 + n1;
                    break;
                case MINUS:
                    result = n0 - n1;
                    break;
                case MULT:
                    result = n0 * n1;
                    break;
                case DIV:
                    result = n0 / n1;
                    break;
                case MOD:
                    result = n0 % n1;
                    break;
                case POW:
                    result = (int) Math.pow(n0, n1);
                    break;
                case LESS:
                    result = n0 < n1;
                    break;
                case GREATER:
                    result = n0 > n1;
                    break;
                case LESSEQ:
                    result = n0 <= n1;
                    break;
                case GREATEREQ:
                    result = n0 >= n1;
                    break;
                case EQUAL:
                    result = n0 == n1;
                    break;
                case INEQUAL:
                    result = n0 != n1;
                    break;
                default:
                    return Optional.empty();
            }

            return Optional.of(new ASTConstant(result));
        }
        else if (c0.getValue() instanceof Float && c1.getValue() instanceof Float) {
            float n0 = (float) c0.getValue();
            float n1 = (float) c1.getValue();

            // now we calculate the result
            Object result;
            switch (op) {
                case PLUSF:
                    result = n0 + n1;
                    break;
                case MINUSF:
                    result = n0 - n1;
                    break;
                case MULTF:
                    result = n0 * n1;
                    break;
                case DIVF:
                    result = n0 / n1;
                    break;
                case POWF:
                    result = (float) Math.pow(n0, n1);
                    break;
                case LESSF:
                    result = n0 < n1;
                    break;
                case GREATERF:
                    result = n0 > n1;
                    break;
                case LESSEQF:
                    result = n0 <= n1;
                    break;
                case GREATEREQF:
                    result = n0 >= n1;
                    break;
                case EQUALF:
                    result = n0 == n1;
                    break;
                case INEQUALF:
                    result = n0 != n1;
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
