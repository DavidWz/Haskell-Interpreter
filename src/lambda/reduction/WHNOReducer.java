package lambda.reduction;

import lambda.ast.*;
import lambda.reduction.delta.ArithmeticRules;
import lambda.reduction.delta.DeltaRule;
import lambda.reduction.delta.FixRule;

import java.util.*;

/**
 * A class which can perform weak head normal order reductions to a lambda term.
 */
public class WHNOReducer {
    private List<DeltaRule> deltaRules;

    public WHNOReducer() {
        deltaRules = new ArrayList<>();
        deltaRules.add(new ArithmeticRules());
        deltaRules.add(new FixRule());
    }

    /**
     * Applies a weak head normal order reduction step to an ASTTerm. If no reduction step can be performed, an empty
     * optional is returned.
     * @param term the term
     * @return the reduced term
     */
    public Optional<ASTTerm> applyWHNOReduction(ASTTerm term) {
        // first, try to do a beta reduction
        Optional<ASTTerm> reduced = term.applyBetaReduction();
        if(reduced.isPresent()) {
            return reduced;
        }
        else {
            // if not possible, try to do a delta reduction
            for (DeltaRule delta : deltaRules) {
                reduced = term.applyDeltaReduction(delta);
                if (reduced.isPresent()) {
                    return reduced;
                }
            }
            return Optional.empty();
        }
    }
}
