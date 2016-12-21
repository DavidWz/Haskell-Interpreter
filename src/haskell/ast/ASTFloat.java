package haskell.ast;

import java.util.Collections;
import java.util.Set;

/**
 * Represents a float.
 */
public class ASTFloat implements ASTExpression, ASTPattern {
    private float value;

    public ASTFloat(float value) {
        this.value = value;

    }

    public float getValue() {
        return value;
    }

    @Override
    public String toString() {
        return Float.toString(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ASTFloat astFloat = (ASTFloat) o;

        return Float.compare(astFloat.getValue(), getValue()) == 0;

    }

    @Override
    public int hashCode() {
        return (getValue() != +0.0f ? Float.floatToIntBits(getValue()) : 0);
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
