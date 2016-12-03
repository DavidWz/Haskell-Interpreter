package haskell.simple.ast;

import lambda.ast.ASTTerm;

import java.util.List;

/**
 * Represents a simple haskell tuple.
 */
public class ASTTuple extends ASTExpression {
    private List<ASTExpression> expr;

    public ASTTuple(List<ASTExpression> expr) {
        assert(expr != null);
        this.expr = expr;
    }

    public List<ASTExpression> getExpr() {
        return expr;
    }

    public void setExpr(List<ASTExpression> expr) {
        assert(expr != null);
        this.expr = expr;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ASTTuple astTuple = (ASTTuple) o;

        return getExpr().equals(astTuple.getExpr());

    }

    @Override
    public int hashCode() {
        return getExpr().hashCode();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        for (ASTExpression e : expr) {
            builder.append(e.toString());
            builder.append(", ");
        }
        if (expr.size() > 0) {
            builder.deleteCharAt(builder.length()-1);
            builder.deleteCharAt(builder.length()-1);
        }
        builder.append(")");
        return builder.toString();
    }

    @Override
    public ASTTerm toLambdaTerm() {
        int n = expr.size();

        // there are no tupels of size 1
        if (n == 1) {
            return expr.get(0).toLambdaTerm();
        }
        else {
            lambda.ast.ASTTerm result = new lambda.ast.ASTConstant(new lambda.ast.TupelConstant(n));

            for (ASTExpression e : expr) {
                result = new lambda.ast.ASTApplication(result, e.toLambdaTerm());
            }

            return result;
        }
    }
}
