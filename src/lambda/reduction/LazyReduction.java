package lambda.reduction;

import lambda.ast.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Represents the lazy evaluation of lambda terms.
 * I.e. previous results get stored, and application results get looked up before they get calculated.
 */
public class LazyReduction implements LambdaTransformation {
    private Map<ASTTerm, ASTTerm> reductionResults;

    public LazyReduction() {
        reductionResults = new HashMap<>();
    }

    public void rememberResult(ASTTerm previous, ASTTerm result) {
        // remember the current result
        reductionResults.put(previous, result);

        // ensure transitivity is stored
        // i.e.: if (a => b) and b == previous, then update it to (a => result)
        for (Map.Entry<ASTTerm, ASTTerm> entry : reductionResults.entrySet()) {
            ASTTerm otherResult = entry.getValue();
            if (otherResult.equals(previous)) {
                entry.setValue(result);
            }
        }
    }

    public Optional<ASTTerm> visit(ASTApplication node) {
        // try to reduce this application
        if (reductionResults.containsKey(node)) {
            return Optional.of(reductionResults.get(node));
        }
        return Optional.empty();
    }
}
