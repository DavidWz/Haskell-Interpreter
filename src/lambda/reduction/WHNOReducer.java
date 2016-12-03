package lambda.reduction;

import lambda.ast.*;
import lambda.reduction.delta.ArithmeticRule;
import lambda.reduction.delta.BranchRule;
import lambda.reduction.delta.DeltaRule;
import lambda.reduction.delta.FixRule;

import java.util.*;

/**
 * A class which can perform weak head normal order reductions to a lambda term.
 */
public class WHNOReducer {
    protected List<DeltaRule> deltaRules;

    /**
     * This standard constructor initializes the delta rules with standard delta rules.
     */
    public WHNOReducer() {
        deltaRules = new ArrayList<>();
        deltaRules.add(new ArithmeticRule());
        deltaRules.add(new FixRule());
        deltaRules.add(new BranchRule());
    }

    public WHNOReducer(List<DeltaRule> deltaRules) {
        assert(deltaRules != null);
        this.deltaRules = deltaRules;
    }

    /**
     * Reduces a term to weak head order normal form.
     * @param term the term
     * @return the WHNF
     */
    public ASTTerm reduceToWHNF(ASTTerm term, boolean verbose) {
        if (verbose) {
            System.out.println(term);
        }

        ASTTerm whnf = term;
        Optional<ASTTerm> result = term.applyWHNOReduction(deltaRules);
        while(result.isPresent()) {
            whnf = result.get();

            if (verbose) {
                System.out.println(" => " + result.get());
            }

            result = result.get().applyWHNOReduction(deltaRules);
        }

        return whnf;
    }

    public ASTTerm reduceToWHNF(ASTTerm term) {
        return reduceToWHNF(term, false);
    }
}
