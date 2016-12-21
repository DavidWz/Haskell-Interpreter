package haskell.ast;

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
     * Accepts a complex haskell visitor and calls its corresponding visit method.
     * @return
     * @param visitor
     */
    <T> T accept(ComplexHaskellVisitor<T> visitor);
}
