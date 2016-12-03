package lambda.reduction.delta;

import lambda.ast.ASTConstant;
import lambda.ast.ASTTerm;

import java.util.List;
import java.util.Optional;

/**
 * Represents the delta rules for arithmetic operations on integers and booleans.
 */
public class ArithmeticRule extends DeltaRule {
    public enum Operator {
        PLUS,
        MINUS,
        TIMES,
        DIVIDED,
        LESS,
        GREATER,
        LESSEQ,
        GREATEREQ,
        EQUAL,
        INEQUAL,
        POW,
        AND,
        OR
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
                Integer n0 = (Integer) c0.getValue();
                Integer n1 = (Integer) c1.getValue();

                // now we calculate the result
                Object result;
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
                    case POW:
                        result = Math.pow(n0, n1);
                        break;
                    default:
                        return Optional.empty();
                }

                return Optional.of(new ASTConstant(result));
            }
            // or both constants must be booleans
            else if (c0.getValue() instanceof Boolean && c1.getValue() instanceof Boolean) {
                boolean b0 = (Boolean) c0.getValue();
                boolean b1 = (Boolean) c1.getValue();

                // now we calculate the result
                Object result;
                switch(op) {
                    case AND:
                        result = b0 && b1;
                        break;
                    case OR:
                        result = b0 || b1;
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
