package haskell.complex.ast;

import java.util.Optional;
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
     * Transforms a series of function declarations for one specific function
     * into an equivalent pattern declaration.
     * @return whether the transformation was successful or not
     */
    boolean funcDeclToPatDecl();
}
