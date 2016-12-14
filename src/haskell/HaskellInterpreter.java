package haskell;

import haskell.complex.ast.ASTDecl;
import haskell.complex.ast.ASTProgram;
import haskell.complex.reduction.ComplexToSimpleReducer;
import haskell.complex.reduction.TooComplexException;
import lambda.reduction.WHNOReducer;

/**
 * This class offers functionality to interpret complex haskell programs.
 * That is, it can evaluate an expression given a haskel program.
 */
public class HaskellInterpreter {
    private haskell.complex.ast.ASTProgram program;
    private WHNOReducer whnoReducer;

    /**
     * Creates a new interpreter with the given initial program.
     * @param initialProgram
     */
    public HaskellInterpreter(ASTProgram initialProgram) {
        assert(initialProgram != null);
        this.program = initialProgram;
        this.whnoReducer = new WHNOReducer();
    }

    /**
     * Creates a new interpreter with no initial functions (except for the predefined ones).
     */
    public HaskellInterpreter() {
        this.program = new ASTProgram();
        this.whnoReducer = new WHNOReducer();
    }

    /**
     * Adds a new declaration to this interpreter.
     * @param declaration
     */
    public void addDeclaration(ASTDecl declaration) {
        program.addDeclaration(declaration);
    }

    /**
     * Adds all declarations of the given program to this interpreter.
     * @param program
     */
    public void addProgram(ASTProgram program) {
        for (ASTDecl decl : program.getDecls()) {
            addDeclaration(decl);
        }
    }

    /**
     * Evaluates a complex haskell expression with the given complex haskell program to a non-reducible lambda term.
     * @param expression a complex haskell expression
     * @return a non-reducible lambda term
     */
    public lambda.ast.ASTTerm evaluate(haskell.complex.ast.ASTExpression expression, boolean verbose) throws TooComplexException {
        // init: create the expression: let prog in expr
        haskell.complex.ast.ASTExpression letProgInExpr;
        if (program.getDecls().size() == 0) {
            // empty lets are not supported, so we simply evaluate the expression directly
            letProgInExpr = expression;
        }
        else {
            letProgInExpr = new haskell.complex.ast.ASTLet(program.getDecls(), expression);
        }
        if (verbose) {
            System.out.println("\n-- The following expression will be evaluated: ");
            System.out.println(letProgInExpr);
        }

        // 1. reduce complex haskell expression to simple haskell expression
        ComplexToSimpleReducer complexToSimpleReducer = new ComplexToSimpleReducer(letProgInExpr);
        haskell.simple.ast.ASTExpression simpleExpr = complexToSimpleReducer.reduceToSimple();
        if (verbose) {
            System.out.println("\n-- In simple haskell, the expression looks like this: ");
            System.out.println(simpleExpr);
        }

        // 2. reduce simple haskell expression to lambda expression
        lambda.ast.ASTTerm lambdaTerm = simpleExpr.toLambdaTerm();
        if (verbose) {
            System.out.println("\n-- The corresponding lambda term looks like this: ");
            System.out.println(lambdaTerm);
            System.out.println("\n-- The following reduction steps were applied: ");
        }

        // 3. reduce lambda expression with WHNO
        lambda.ast.ASTTerm result = whnoReducer.reduceToWHNF(lambdaTerm, verbose);
        if (verbose) {
            System.out.println("\n-- The final result is: ");
            System.out.println(result);
        }

        return result;
    }

    public lambda.ast.ASTTerm evaluate(haskell.complex.ast.ASTExpression expression) throws TooComplexException {
        return evaluate(expression, false);
    }
}
