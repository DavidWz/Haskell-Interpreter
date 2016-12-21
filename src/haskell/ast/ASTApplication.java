package haskell.ast;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a application, e.g. f x
 */
public class ASTApplication implements ASTExpression {
    private List<ASTExpression> exps;

    public ASTApplication(List<ASTExpression> exps) {
        assert(exps != null);
        assert(exps.size() > 1);
        this.exps = exps;
    }

    public ASTApplication(ASTExpression... exps) {
        assert(exps.length > 1);
        this.exps = Arrays.asList(exps);
    }

    public List<ASTExpression> getExps() {
        return exps;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ASTApplication that = (ASTApplication) o;

        return getExps().equals(that.getExps());

    }

    @Override
    public int hashCode() {
        return getExps().hashCode();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        for (ASTExpression exp : exps) {
            builder.append(exp);
            builder.append(" ");
        }
        builder.deleteCharAt(builder.length()-1);
        builder.append(")");
        return builder.toString();
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
    public <T> T accept(ComplexHaskellVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public Set<ASTVariable> getFreeVars() {
        HashSet<ASTVariable> vars = new HashSet<>();
        for (ASTExpression exp : exps) {
            vars.addAll(exp.getFreeVars());
        }
        return vars;
    }
}
