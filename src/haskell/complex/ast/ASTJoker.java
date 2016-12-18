package haskell.complex.ast;

import haskell.complex.reduction.TooComplexException;
import haskell.simple.ast.ASTExpression;

import java.util.Collections;
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
        return Collections.emptySet();
    }

    @Override
    public Set<ASTVariable> getFreeVars() {
        return Collections.emptySet();
    }

    @Override
    public ASTExpression castToSimple() throws TooComplexException {
        throw new TooComplexException(this, "The joker pattern is not part of simple haskell.");
    }

    @Override
    public <T> T accept(ComplexHaskellVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
