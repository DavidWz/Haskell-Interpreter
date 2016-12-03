package haskell.simple.ast;

import lambda.ast.ASTTerm;

/**
 * Represents a simple haskel application.
 */
public class ASTApplication extends ASTExpression {
    private ASTExpression left;
    private ASTExpression right;

    public ASTApplication(ASTExpression left, ASTExpression right) {
        assert(left != null);
        assert(right != null);
        this.left = left;
        this.right = right;
    }

    public ASTExpression getLeft() {
        return left;
    }

    public void setLeft(ASTExpression left) {
        assert(left != null);
        this.left = left;
    }

    public ASTExpression getRight() {
        return right;
    }

    public void setRight(ASTExpression right) {
        assert(right != null);
        this.right = right;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ASTApplication that = (ASTApplication) o;

        if (!getLeft().equals(that.getLeft())) return false;
        return getRight().equals(that.getRight());

    }

    @Override
    public int hashCode() {
        int result = getLeft().hashCode();
        result = 31 * result + getRight().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "(" + left + " " + right +")";
    }

    @Override
    public ASTTerm toLambdaTerm() {
        return new lambda.ast.ASTApplication(left.toLambdaTerm(), right.toLambdaTerm());
    }
}
