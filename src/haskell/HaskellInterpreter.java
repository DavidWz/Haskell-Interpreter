package haskell;

import haskell.ast.*;
import haskell.reduction.ComplexToSimpleReducer;
import haskell.reduction.SimpleToLambdaReducer;
import lambda.ast.ASTTerm;
import lambda.reduction.WHNOReducer;
import lambda.type.TypeChecker;
import lambda.type.TypeException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This class offers functionality to interpret complex haskell programs.
 * That is, it can evaluate an expression given a haskel program.
 */
public class HaskellInterpreter {
    private haskell.ast.ASTProgram program;
    private ComplexToSimpleReducer complexToSimpleReducer;
    private SimpleToLambdaReducer simpleToLambdaReducer;
    private TypeChecker typeChecker;
    private WHNOReducer whnoReducer;

    /**
     * Creates a new interpreter with no initial functions (except for the predefined ones).
     */
    public HaskellInterpreter() {
        this.program = new ASTProgram();
        this.complexToSimpleReducer = new ComplexToSimpleReducer();
        this.simpleToLambdaReducer = new SimpleToLambdaReducer();
        this.whnoReducer = new WHNOReducer();
        this.typeChecker = new TypeChecker();
    }

    /**
     * Adds a new declaration to this interpreter.
     * @param declaration
     */
    public void addDeclaration(ASTDecl declaration) throws TypeException.InconsistentDataDeclException {
        if (declaration instanceof ASTDataDecl) {
            // add this data declaration to the type checker
            typeChecker.addDataDeclaration((ASTDataDecl) declaration);
        }

        // add this declaration to the program
        this.program.addDeclaration(declaration);
    }

    /**
     * Adds all declarations of the given program to this interpreter.
     * @param program
     */
    public void addProgram(ASTProgram program) throws TypeException.InconsistentDataDeclException {
        for (ASTDecl decl : program.getDecls()) {
            addDeclaration(decl);
        }
    }

    /**
     * Evaluates a complex haskell expression with the given complex haskell program to a non-reducible lambda term.
     * @param expression a complex haskell expression
     * @return a non-reducible lambda term
     */
    public ASTTerm evaluate(ASTExpression expression, boolean verbose) throws TypeException {
        // we only need the function declarations actually used for the reduction
        List<ASTDecl> functionDeclarations = program.getDecls().stream().
                filter(decl -> decl instanceof ASTPatDecl || decl instanceof ASTFunDecl).
                collect(Collectors.toList());

        // init: create the expression: let prog in expr
        ASTExpression letProgInExpr;
        if (functionDeclarations.size() == 0) {
            // empty lets are not supported, so we simply evaluate the expression directly
            letProgInExpr = expression;
        }
        else {
            letProgInExpr = new ASTLet(functionDeclarations, expression);
        }
        if (verbose) {
            System.out.println("\n-- The following expression will be evaluated: ");
            System.out.println(letProgInExpr);
            System.out.println("\n-- In simple haskell, the expression looks like this: ");
        }

        // 1. reduce complex haskell expression to simple haskell expression
        ASTExpression simpleExpr = complexToSimpleReducer.reduceToSimple(letProgInExpr);
        if (verbose) {
            System.out.println(simpleExpr);
            System.out.println("\n-- The corresponding lambda term looks like this: ");
        }

        // 2. reduce simple haskell expression to lambda expression
        lambda.ast.ASTTerm lambdaTerm = simpleExpr.accept(simpleToLambdaReducer);
        if (verbose) {
            System.out.println(lambdaTerm);
            System.out.println("\n-- The type of the expression is: ");
        }

        // 3. do a static type check
        ASTType type = typeChecker.checkType(lambdaTerm);
        // the type checker will throw an exception if something's wrong
        // so at this point we know that the expression is typed correctly
        if (verbose) {
            System.out.println(type);
            System.out.println("\n-- The following reduction steps were applied: ");
        }

        // 4. reduce lambda expression with WHNO
        lambda.ast.ASTTerm result = whnoReducer.reduceToWHNF(lambdaTerm, verbose);
        if (verbose) {
            System.out.println("\n-- The final result is: ");
            System.out.println(result);
        }

        return result;
    }

    public ASTTerm evaluate(ASTExpression expression) throws TypeException {
        return evaluate(expression, false);
    }
}
