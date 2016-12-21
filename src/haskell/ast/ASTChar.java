package haskell.ast;

import java.util.Collections;
import java.util.Set;

/**
 * Represents a float.
 */
public class ASTChar implements ASTExpression, ASTPattern {
    private char value;

    public ASTChar(char value) {
        this.value = value;

    }

    public char getValue() {
        return value;
    }

    @Override
    public String toString() {
        return Character.toString(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ASTChar astChar = (ASTChar) o;

        return getValue() == astChar.getValue();

    }

    @Override
    public int hashCode() {
        return (int) getValue();
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
    public <T> T accept(ComplexHaskellVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
