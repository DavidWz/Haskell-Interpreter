package lambda.reduction;

import lambda.ast.*;
import lambda.reduction.delta.*;

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
        deltaRules.add(new BoolNotRule());
        deltaRules.add(new BotRule());
        deltaRules.add(new BranchRule());
        deltaRules.add(new FixRule());
        deltaRules.add(new TupleRule());
    }

    public void addDeltaRule(DeltaRule deltaRule) {
        deltaRules.add(deltaRule);
    }

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

    /**
     * Reduces a term to weak head order normal form.
     * @param term the term
     * @return the WHNF
     */
    public ASTTerm reduceToWHNF(ASTTerm term) {
        return reduceToWHNF(term, false);
    }
}
