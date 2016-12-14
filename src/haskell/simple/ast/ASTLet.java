package haskell.simple.ast;


import lambda.ast.ASTTerm;
import lambda.reduction.delta.FixReduction;

/**
 * Represents a simple haskell let definition.
 */
public class ASTLet extends ASTExpression {
    private ASTVariable var;
    private ASTExpression expr;
    private ASTExpression target;

    public ASTLet(ASTVariable var, ASTExpression expr, ASTExpression target) {
        assert(var != null);
        assert(expr != null);
        assert(target != null);
        this.var = var;
        this.expr = expr;
        this.target = target;
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

    public ASTExpression getTarget() {
        return target;
    }

    public void setTarget(ASTExpression target) {
        assert(target != null);

        this.target = target;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ASTLet astLet = (ASTLet) o;

        if (!getVar().equals(astLet.getVar())) return false;
        if (!getExpr().equals(astLet.getExpr())) return false;
        return getTarget().equals(astLet.getTarget());

    }

    @Override
    public int hashCode() {
        int result = getVar().hashCode();
        result = 31 * result + getExpr().hashCode();
        result = 31 * result + getTarget().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "let " + var + " = " + expr + " in " + target;
    }

    @Override
    public ASTTerm toLambdaTerm() {
        lambda.ast.ASTTerm basis = target.toLambdaTerm();
        lambda.ast.ASTVariable variable = (lambda.ast.ASTVariable) var.toLambdaTerm();
        lambda.ast.ASTTerm func = new lambda.ast.ASTAbstraction(variable, expr.toLambdaTerm());
        lambda.ast.ASTTerm replacement = new lambda.ast.ASTApplication(new lambda.ast.ASTConstant(FixReduction.Operator.FIX), func);
        return basis.substitute(variable, replacement);
    }
}
