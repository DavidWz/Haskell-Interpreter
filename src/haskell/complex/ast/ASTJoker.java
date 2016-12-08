package haskell.complex.ast;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents the joker pattern "_"
 */
public class ASTJoker implements ASTPattern {
    public ASTJoker() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public String toString() {
        return "_";
    }

    @Override
    public Set<ASTVariable> getAllVariables() {
        return new HashSet<>();
    }
}
