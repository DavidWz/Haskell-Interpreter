package haskell;

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
    public lambda.ast.ASTTerm evaluate(haskell.complex.ast.ASTExpression expression) {
        // init: create the expression: let prog in expr
        haskell.complex.ast.ASTExpression letProgInExpr = new haskell.complex.ast.ASTLet(program.getDecls(), expression);
        System.out.println(letProgInExpr);

        // 1. reduce complex haskell expression to simple haskell expression
        // haskell.simple.ast.ASTExpression simpleExprs = letProgInExpr.toSimpleHaskell();

        // 2. reduce simple haskell expression to lambda expression
        // lambda.ast.ASTTerm lambdaTerm = simpleExprs.toLambdaTerm();

        // 3. reduce lambda expression with WHNO
        // WHNOReducer whnoReducer = new WHNOReducer();
        // return whnoReducer.reduceToWHNF(lambdaTerm);

        return null;
    }
}
