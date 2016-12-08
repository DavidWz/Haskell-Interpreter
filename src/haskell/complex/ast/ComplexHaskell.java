package haskell.complex.ast;

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
}
