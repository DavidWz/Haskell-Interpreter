package haskell.simple.ast;

import lambda.ast.ASTTerm;

/**
 * Represents a simple haskell variable.
 */
public class ASTVariable extends ASTExpression {
    private String name;

    public ASTVariable(String name) {
        assert(name != null);
        assert(!name.trim().equals(""));
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        assert(name != null);
        assert(!name.trim().equals(""));

        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ASTVariable that = (ASTVariable) o;

        return getName().equals(that.getName());

    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public ASTTerm toLambdaTerm() {
        return new lambda.ast.ASTVariable(name);
    }
}
