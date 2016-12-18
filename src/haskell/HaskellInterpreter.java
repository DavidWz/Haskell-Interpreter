package haskell;

import haskell.complex.ast.*;
import haskell.complex.reduction.ComplexToSimpleReducer;
import haskell.complex.reduction.TooComplexException;
import lambda.reduction.WHNOReducer;
import lambda.type.TypeChecker;
import lambda.type.TypeException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class offers functionality to interpret complex haskell programs.
 * That is, it can evaluate an expression given a haskel program.
 */
public class HaskellInterpreter {
    private haskell.complex.ast.ASTProgram program;
    private ComplexToSimpleReducer complexToSimpleReducer;
    private TypeChecker typeChecker;
    private WHNOReducer whnoReducer;

    /**
     * Creates a new interpreter with the given initial program.
     * @param initialProgram
     */
    public HaskellInterpreter(ASTProgram initialProgram) {
        assert(initialProgram != null);
        init();
        addProgram(initialProgram);
    }

    /**
     * Creates a new interpreter with no initial functions (except for the predefined ones).
     */
    public HaskellInterpreter() {
        init();
    }

    private void init() {
        this.program = new ASTProgram();
        this.complexToSimpleReducer = new ComplexToSimpleReducer();
        this.whnoReducer = new WHNOReducer();
        this.typeChecker = new TypeChecker();
    }

    /**
     * Adds a new declaration to this interpreter.
     * @param declaration
     */
    public void addDeclaration(ASTDecl declaration) {
        addProgram(new ASTProgram(declaration));
    }

    /**
     * Adds all declarations of the given program to this interpreter.
     * @param program
     */
    public void addProgram(ASTProgram program) {
        // add the data declarations to the type checker
        List<ASTDataDecl> dataDeclarations = program.getDecls().stream().
                filter(decl -> decl instanceof ASTDataDecl).
                map(decl -> (ASTDataDecl) decl).
                collect(Collectors.toList());
        for (ASTDataDecl dataDecl : dataDeclarations) {
            typeChecker.addDataDeclaration(dataDecl);
        }

        // add all declarations to the program
        for (ASTDecl decl : program.getDecls()) {
            this.program.addDeclaration(decl);
        }
    }

    /**
     * Evaluates a complex haskell expression with the given complex haskell program to a non-reducible lambda term.
     * @param expression a complex haskell expression
     * @return a non-reducible lambda term
     */
    public lambda.ast.ASTTerm evaluate(haskell.complex.ast.ASTExpression expression, boolean verbose) throws TooComplexException, TypeException {
        // we only need the function declarations actually used for the reduction
        List<ASTDecl> functionDeclarations = program.getDecls().stream().
                filter(decl -> decl instanceof ASTPatDecl || decl instanceof ASTFunDecl).
                collect(Collectors.toList());

        // init: create the expression: let prog in expr
        haskell.complex.ast.ASTExpression letProgInExpr;
        if (functionDeclarations.size() == 0) {
            // empty lets are not supported, so we simply evaluate the expression directly
            letProgInExpr = expression;
        }
        else {
            letProgInExpr = new haskell.complex.ast.ASTLet(functionDeclarations, expression);
        }
        if (verbose) {
            System.out.println("\n-- The following expression will be evaluated: ");
            System.out.println(letProgInExpr);
        }

        // 1. reduce complex haskell expression to simple haskell expression
        haskell.simple.ast.ASTExpression simpleExpr = complexToSimpleReducer.reduceToSimple(letProgInExpr);
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

        // 3. do a static type check
        ASTType type = typeChecker.checkType(lambdaTerm);
        // the type checker will throw an exception if something's wrong
        // so at this point we know that the expression is typed correctly
        if (verbose) {
            System.out.println("\n-- The type of the expression is: ");
            System.out.println(type);
        }

        // 4. reduce lambda expression with WHNO
        if (verbose) {
            System.out.println("\n-- The following reduction steps were applied: ");
        }
        lambda.ast.ASTTerm result = whnoReducer.reduceToWHNF(lambdaTerm, verbose);
        if (verbose) {
            System.out.println("\n-- The final result is: ");
            System.out.println(result);
        }

        return result;
    }

    public lambda.ast.ASTTerm evaluate(haskell.complex.ast.ASTExpression expression) throws TooComplexException, TypeException {
        return evaluate(expression, false);
    }
}
