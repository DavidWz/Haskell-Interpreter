package haskell.complex.ast;

import haskell.complex.reduction.SimpleReducer;

import java.util.Set;

/**
 * A super-interface for complex haskell ast nodes.
 */
public interface ComplexHaskell {
    /**
     * Returns all variables within this complex haskell node.
     * @return a set of all variables
     */
    Set<ASTVariable> getAllVariables();

    /**
     * Returns a set of free variables of this term.
     * @return
     */
    Set<ASTVariable> getFreeVars();

    /**
     * Transforms a series of function declarations for one specific function
     * into an equivalent pattern declaration.
     * @return whether the transformation was successful
     */
    boolean funcDeclToPatDecl();

    /**
     * Transforms a lambda expression with multiple arguments to nested lambda terms with only one argument each.
     * @return whether the transformation was successful
     */
    boolean nestMultipleLambdas();

    /**
     * Transforms a lambda expression with a pattern which is not a variable to a case expression.
     * @return whether the transformation was successful
     */
    boolean lambdaPatternToCase();

    /**
     * Transforms a case term to nested applications of the predefined match function.
     * @return whether the transformation was successful
     */
    boolean caseToMatch();

    /**
     * Transforms a let term with several declarations to nested let-terms with one declaration each.
     * @return
     */
    boolean nestMultipleLets();

    /**
     * Casts this complex haskell term to a simple haskell term. Throws a TooComplexException if the complex haskell term
     * did not have the same structure as an equivalent simple haskell term.
     * @return the equivalent simple haskell term
     * @throws SimpleReducer.TooComplexException
     */
    haskell.simple.ast.ASTExpression castToSimple() throws SimpleReducer.TooComplexException;
}
