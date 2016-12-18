package haskell.complex.ast;

import haskell.complex.reduction.TooComplexException;
import haskell.simple.ast.ASTExpression;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a function type.
 */
public class ASTFuncType implements ASTType {
    private ASTType from;
    private ASTType to;

    public ASTFuncType(ASTType from, ASTType to) {
        assert(from != null);
        assert(to != null);

        this.from = from;
        this.to = to;
    }

    public ASTType getFrom() {
        return from;
    }

    public ASTType getTo() {
        return to;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ASTFuncType that = (ASTFuncType) o;

        if (!getFrom().equals(that.getFrom())) return false;
        return getTo().equals(that.getTo());

    }

    @Override
    public int hashCode() {
        int result = getFrom().hashCode();
        result = 31 * result + getTo().hashCode();
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("(").append(from).append(" -> ");
        builder.append(to).append(")");
        return builder.toString();
    }

    @Override
    public Set<ASTVariable> getAllVariables() {
        Set<ASTVariable> vars = new HashSet<>(from.getAllVariables());
        vars.addAll(to.getAllVariables());
        return vars;
    }

    @Override
    public Set<ASTVariable> getFreeVars() {
        Set<ASTVariable> vars = new HashSet<>(from.getFreeVars());
        vars.addAll(to.getFreeVars());
        return vars;
    }

    @Override
    public ASTExpression castToSimple() throws TooComplexException {
        throw new TooComplexException(this, "Types are not part of simple haskell.");
    }

    @Override
    public <T> T accept(ComplexHaskellVisitor<T> visitor) {
        return visitor.accept(this);
    }

    @Override
    public <T> T accept(TypeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
