package haskell.simple.ast;

import lambda.ast.ASTTerm;

/**
 * Represents a simple haskell function.
 */
public class ASTFunction extends ASTExpression {
    private ASTVariable var;
    private ASTExpression expr;

    public ASTFunction(ASTVariable var, ASTExpression expr) {
        assert(var != null);
        assert(expr != null);
        this.var = var;
        this.expr = expr;
    }

    public ASTVariable getVar() {
        return var;
    }

    public void setVar(ASTVariable var) {
        assert(var != null);
        this.var = var;
    }

    public ASTExpression getExpr() {
        return expr;
    }

    public void setExpr(ASTExpression expr) {
        assert(expr != null);
        this.expr = expr;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ASTFunction that = (ASTFunction) o;

        if (!getVar().equals(that.getVar())) return false;
        return getExpr().equals(that.getExpr());

    }

    @Override
    public int hashCode() {
        int result = getVar().hashCode();
        result = 31 * result + getExpr().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "\\" + var + " -> " + expr;
    }

    @Override
    public ASTTerm toLambdaTerm() {
        return new lambda.ast.ASTAbstraction((lambda.ast.ASTVariable) var.toLambdaTerm(), expr.toLambdaTerm());
    }
}
