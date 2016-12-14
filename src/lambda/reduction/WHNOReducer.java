package lambda.reduction;

import lambda.ast.*;
import lambda.reduction.delta.*;

import java.util.*;

/**
 * A class which can perform weak head normal order reductions to a lambda term.
 */
public class WHNOReducer implements LambdaTransformation {
    private List<LambdaTransformation> transformations;

    /**
     * This standard constructor creates a WHNO reducer with standard beta and delta rules.
     */
    public WHNOReducer() {
        transformations = new ArrayList<>();

        transformations.add(new BetaReduction());

        transformations.add(new ArithmeticRule());
        transformations.add(new BoolNotRule());
        transformations.add(new BotRule());
        transformations.add(new BranchRule());
        transformations.add(new FixRule());
        transformations.add(new TupleRule());
        transformations.add(new ConstructorRule());
    }

    @Override
    public Optional<ASTTerm> visit(ASTApplication node) {
        // first, try to do apply a reduction on this term
        Optional<ASTTerm> reduced;
        for (LambdaTransformation transformation : transformations) {
            reduced = transformation.visit(node);
            if (reduced.isPresent()) {
                return reduced;
            }
        }

        // try to apply the reduction in a left-most inner-most fashion
        Optional<ASTTerm> result = visit(node.getLeft());
        if (result.isPresent()) {
            return Optional.of(new ASTApplication(result.get(), node.getRight()));
        }
        else {
            result = visit(node.getRight());
            if (result.isPresent()) {
                return Optional.of(new ASTApplication(node.getLeft(), result.get()));
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

        constant = ArithmeticRule.toConst(name);
        if (constant.isPresent()) {
            return constant;
        }

        constant = BoolNotRule.toConst(name);
        if (constant.isPresent()) {
            return constant;
        }

        constant = BotRule.toConst(name);
        if (constant.isPresent()) {
            return constant;
        }

        constant = ConstructorRule.toConst(name);
        if (constant.isPresent()) {
            return constant;
        }

        constant = TupleRule.toConst(name);
        if (constant.isPresent()) {
            return constant;
        }

        return Optional.empty();
    }
}
