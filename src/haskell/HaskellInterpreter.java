package haskell;

import haskell.complex.reduction.SimpleReducer;
import lambda.reduction.WHNOReducer;

/**
 * This class offers functionality to interpret complex haskell programs.
 * That is, it can evaluate an expression given a haskel program.
 */
public class HaskellInterpreter {
    private haskell.complex.ast.ASTProgram program;

    public HaskellInterpreter(haskell.complex.ast.ASTProgram program) {
        assert(program != null);
        this.program = program;
    }

    /**
     * Evaluates a complex haskell expression with the given complex haskell program to a non-reducible lambda term.
     * @param expression a complex haskell expression
     * @return a non-reducible lambda term
     */
    public lambda.ast.ASTTerm evaluate(haskell.complex.ast.ASTExpression expression, boolean verbose) throws SimpleReducer.TooComplexException {
        // init: create the expression: let prog in expr
        haskell.complex.ast.ASTExpression letProgInExpr = new haskell.complex.ast.ASTLet(program.getDecls(), expression);
        if (verbose) {
            System.out.println("\n-- The following expression will be evaluated: ");
            System.out.println(letProgInExpr);
        }

        // 1. reduce complex haskell expression to simple haskell expression
        SimpleReducer simpleReducer = new SimpleReducer(letProgInExpr);
        haskell.simple.ast.ASTExpression simpleExpr = simpleReducer.reduceToSimple();
        if (verbose) {
            System.out.println("\n-- In simple haskell, the expression looks like this: ");
            System.out.println(simpleExpr);
        }

        // 2. reduce simple haskell expression to lambda expression
        lambda.ast.ASTTerm lambdaTerm = simpleExpr.toLambdaTerm();
        if (verbose) {
            System.out.println("\n-- The corresponding lambda term looks like this: ");
            System.out.println(lambdaTerm);
        }

        // 3. reduce lambda expression with WHNO
        WHNOReducer whnoReducer = new WHNOReducer();
        lambda.ast.ASTTerm result = whnoReducer.reduceToWHNF(lambdaTerm, verbose);
        if (verbose) {
            System.out.println("\n-- The final result is: ");
            System.out.println(result);
        }

        return result;
    }

    public lambda.ast.ASTTerm evaluate(haskell.complex.ast.ASTExpression expression) throws SimpleReducer.TooComplexException {
        return evaluate(expression, false);
    }
}
