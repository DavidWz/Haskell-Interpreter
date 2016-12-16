package lambda.reduction;

import lambda.ast.*;
import lambda.reduction.delta.*;

import java.util.*;

/**
 * A class which can perform weak head normal order reductions to a lambda term.
 */
public class WHNOReducer implements LambdaTransformation {
    private List<LambdaTransformation> transformations;
    private LazyReduction lazyReduction;

    /**
     * This standard constructor creates a WHNO reducer with standard beta and delta rules.
     */
    public WHNOReducer() {
        transformations = new ArrayList<>();

        transformations.add(new BetaReduction());

        transformations.add(new ArithmeticReduction());
        transformations.add(new BooleanReduction());
        transformations.add(new BoolNotReduction());
        transformations.add(new CharReduction());
        transformations.add(new BotReduction());
        transformations.add(new BranchReduction());
        transformations.add(new FixReduction());
        transformations.add(new TupleReduction());
        transformations.add(new ConstructorReduction());

        lazyReduction = new LazyReduction();
    }

    @Override
    public Optional<ASTTerm> visit(ASTApplication node) {
        // lazy evaluation: try to look up the result of this application from previous reductions
        Optional<ASTTerm> previousResult = node.accept(lazyReduction);
        if (previousResult.isPresent()) {
            return previousResult;
        }

        // first, try to do apply a reduction on this term
        Optional<ASTTerm> reduced;
        for (LambdaTransformation transformation : transformations) {
            reduced = node.accept(transformation);
            if (reduced.isPresent()) {
                // remember the result of this reduction
                lazyReduction.rememberResult(node, reduced.get());
                // and return it, of course
                return reduced;
            }
        }

        // try to apply the reduction in a left-most inner-most fashion
        Optional<ASTTerm> result = node.getLeft().accept(this);
        if (result.isPresent()) {
            ASTApplication reducedApplication = new ASTApplication(result.get(), node.getRight());
            lazyReduction.rememberResult(node, reducedApplication);
            return Optional.of(reducedApplication);
        }
        else {
            result = node.getRight().accept(this);
            if (result.isPresent()) {
                ASTApplication reducedApplication = new ASTApplication(node.getLeft(), result.get());
                lazyReduction.rememberResult(node, reducedApplication);
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

        ASTTerm currentTerm = term;

        // try to reduce the term as long as possible
        Optional<ASTTerm> reducedTerm = currentTerm.accept(this);
        while(reducedTerm.isPresent()) {
            currentTerm = reducedTerm.get();

            if (verbose) {
                System.out.println(" => " + currentTerm);
            }

            reducedTerm = currentTerm.accept(this);
        }

        return currentTerm;
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

        constant = ConstructorReduction.toConst(name);
        if (constant.isPresent()) {
            return constant;
        }

        constant = TupleReduction.toConst(name);
        if (constant.isPresent()) {
            return constant;
        }

        try {
            PredefinedFunction operator = PredefinedFunction.valueOf(name.toUpperCase());
            return Optional.of(new ASTConstant(operator));
        }
        catch (Exception e) {
            return Optional.empty();
        }
    }
}
