package haskell.ast;

import java.util.*;

/**
 * Represents a complex haskell tuple of expressions. (exp, ..., exp)
 */
public class ASTExpTuple implements ASTExpression {
    private List<ASTExpression> exps;

    public ASTExpTuple(List<ASTExpression> exps) {
        assert(exps != null);
        this.exps = exps;
    }

    public List<ASTExpression> getExps() {
        return exps;
    }

    @Override
    public String toString() {
        if (exps.size() == 0) {
            return "()";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("(");
        for (ASTExpression exp : exps) {
            builder.append(exp);
            builder.append(", ");
        }
        builder.deleteCharAt(builder.length()-1);
        builder.deleteCharAt(builder.length()-1);
        builder.append(")");
        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ASTExpTuple that = (ASTExpTuple) o;

        return getExps().equals(that.getExps());

    }

    @Override
    public int hashCode() {
        return getExps().hashCode();
    }

    @Override
    public Set<ASTVariable> getAllVariables() {
        Set<ASTVariable> vars = new HashSet<>();
        for (ASTExpression exp : exps) {
            vars.addAll(exp.getAllVariables());
        }
        return vars;
    }

    @Override
    public Set<ASTVariable> getFreeVars() {
        Set<ASTVariable> vars = new HashSet<>();
        for (ASTExpression exp : exps) {
            vars.addAll(exp.getFreeVars());
        }
        return vars;
    }

    @Override
    public <T> T accept(ComplexHaskellVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
