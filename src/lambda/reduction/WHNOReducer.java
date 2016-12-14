package lambda.reduction;

import lambda.ast.*;
import lambda.reduction.delta.*;

import java.util.*;

/**
 * A class which can perform weak head normal order reductions to a lambda term.
 */
public class WHNOReducer implements LambdaTransformation {
    private List<LambdaTransformation> transformations;
    private Map<ASTTerm, ASTTerm> reductionResults;

    /**
     * This standard constructor creates a WHNO reducer with standard beta and delta rules.
     */
    public WHNOReducer() {
        transformations = new ArrayList<>();

        transformations.add(new BetaReduction());

        transformations.add(new ArithmeticReduction());
        transformations.add(new BoolNotReduction());
        transformations.add(new BotReduction());
        transformations.add(new BranchReduction());
        transformations.add(new FixReduction());
        transformations.add(new TupleReduction());
        transformations.add(new ConstructorReduction());

        reductionResults = new HashMap<>();
    }

    @Override
    public Optional<ASTTerm> visit(ASTApplication node) {
        // lazy evaluation: try to look up the result of this application from previous reductions
        if (reductionResults.containsKey(node)) {
            return Optional.of(reductionResults.get(node));
        }

        // first, try to do apply a reduction on this term
        Optional<ASTTerm> reduced;
        for (LambdaTransformation transformation : transformations) {
            reduced = transformation.visit(node);
            if (reduced.isPresent()) {
                // remember the result of this reduction
                reductionResults.put(node, reduced.get());
                // and return it, of course
                return reduced;
            }
        }

        // try to apply the reduction in a left-most inner-most fashion
        Optional<ASTTerm> result = visit(node.getLeft());
        if (result.isPresent()) {
            ASTApplication reducedApplication = new ASTApplication(result.get(), node.getRight());
            reductionResults.put(node, reducedApplication);
            return Optional.of(reducedApplication);
        }
        else {
            result = visit(node.getRight());
            if (result.isPresent()) {
                ASTApplication reducedApplication = new ASTApplication(node.getLeft(), result.get());
                reductionResults.put(node, reducedApplication);
                return Optional.of(reducedApplication);
            }
            else {
                return Optional.empty();
            }
        }
    }

    /**
     * Reduces a term to weak head order normal form.
     * @param term the term
     * @param verbose whether reduction steps should be printed
     * @return the WHNF
     */
    public ASTTerm reduceToWHNF(ASTTerm term, boolean verbose) {
        if (verbose) {
            System.out.println(term);
        }

        ASTTerm whnf = term;
        Optional<ASTTerm> result = visit(term);
        while(result.isPresent()) {
            whnf = result.get();
            reductionResults.put(term, whnf);

            if (verbose) {
                System.out.println(" => " + result.get());
            }

            result = visit(result.get());
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


    /**
     * Converts a predefined variable name to the actual ast constant that represents it. If the given name is not
     * predefined, then a new constant with a corresponding constructor is created.
     * @param name
     * @return
     */
    public static Optional<ASTConstant> toConst(String name) {
        Optional<ASTConstant> constant;

        constant = ArithmeticReduction.toConst(name);
        if (constant.isPresent()) {
            return constant;
        }

        constant = BoolNotReduction.toConst(name);
        if (constant.isPresent()) {
            return constant;
        }

        constant = BotReduction.toConst(name);
        if (constant.isPresent()) {
            return constant;
        }

        constant = ConstructorReduction.toConst(name);
        if (constant.isPresent()) {
            return constant;
        }

        constant = TupleReduction.toConst(name);
        if (constant.isPresent()) {
            return constant;
        }

        return Optional.empty();
    }
}
