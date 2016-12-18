package haskell.complex.ast;

import haskell.complex.reduction.TooComplexException;

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
     * Casts this complex haskell term to a simple haskell term. Throws a TooComplexException if the complex haskell term
     * did not have the same structure as an equivalent simple haskell term.
     * @return the equivalent simple haskell term
     * @throws TooComplexException
     */
    haskell.simple.ast.ASTExpression castToSimple() throws TooComplexException;

    /**
     * Accepts a complex haskell visitor and calls its corresponding visit method.
     * @return
     * @param visitor
     */
    <T> T accept(ComplexHaskellVisitor<T> visitor);
}
