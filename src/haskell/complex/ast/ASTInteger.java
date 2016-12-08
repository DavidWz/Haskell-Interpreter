package haskell.complex.ast;

/**
 * Represents an integer.
 */
public class ASTInteger implements ASTExpression, ASTPattern {
    private int value;

    public ASTInteger(int value) {
        this.value = value;

    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }

    public int getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ASTInteger that = (ASTInteger) o;

        return getValue() == that.getValue();

    }

    @Override
    public int hashCode() {
        return getValue();
    }

}
