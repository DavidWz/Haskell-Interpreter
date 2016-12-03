package lambda.reduction;

import lambda.ast.*;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Optional;

/**
 * A class which can perform weak head normal order reductions to a lambda term.
 */
public class WHNOReducer {
    public WHNOReducer() {
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
            reduced = term.applyDeltaReduction();
            if (reduced.isPresent()) {
                return reduced;
            }
            else {
                return Optional.empty();
            }
        }
    }
}
