package haskell.complex.ast;

/**
 * Represents a type.
 */
public interface ASTType extends ComplexHaskell {
    <T> T accept(TypeVisitor<T> visitor);
}
