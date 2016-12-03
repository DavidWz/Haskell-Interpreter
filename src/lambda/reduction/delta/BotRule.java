package lambda.reduction.delta;

import lambda.ast.ASTConstant;
import lambda.ast.ASTTerm;

import java.util.List;
import java.util.Optional;

/**
 * Represents the bottom constant (i.e. non-termination)
 */
public class BotRule extends DeltaRule {
    public enum Operator {
        BOT
    }

    @Override
    public int getNumberOfArguments() {
        return 0;
    }

    @Override
    public boolean isConstantMatching(ASTConstant c) {
        return (c.getValue().equals(Operator.BOT));
    }

    @Override
    public Optional<ASTTerm> getRHS(ASTConstant constant, List<ASTTerm> terms) {
        // the constant must be BOT and it has no arguments
        if (isSignatureMatching(constant, terms)) {
            // bot -> bot
            return Optional.of(constant);
        }
        else {
            return Optional.empty();
        }
    }
}
