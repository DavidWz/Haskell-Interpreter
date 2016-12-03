package haskell.simple.ast;

import lambda.ast.ASTTerm;

/**
 * Represents a simple haskell constant.
 */
public class ASTConstant extends ASTExpression {
    private Object value;

    public ASTConstant(Object value) {
        assert(value != null);
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        assert(value != null);
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ASTConstant that = (ASTConstant) o;

        return getValue().equals(that.getValue());

    }

    @Override
    public int hashCode() {
        return getValue().hashCode();
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public ASTTerm toLambdaTerm() {
        return new lambda.ast.ASTConstant(value);
    }
}
