package haskell.complex.ast;

/**
 * Represents the joker pattern "_"
 */
public class ASTJoker implements ASTPattern {
    public ASTJoker() {
    }

    @Override
    public String toString() {
        return "_";
    }
}
