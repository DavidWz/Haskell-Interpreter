package haskell.ast;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a complex haskell tuple of patterns. (pat, ..., pat)
 */
public class ASTPatTuple implements ASTPattern {
    private List<ASTPattern> pats;

    public ASTPatTuple(List<ASTPattern> pats) {
        assert(pats != null);
        this.pats = pats;
    }

    public List<ASTPattern> getPats() {
        return pats;
    }

    @Override
    public String toString() {
        if (pats.size() == 0) {
            return "()";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("(");
        for (ASTPattern pat : pats) {
            builder.append(pat);
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

        ASTPatTuple that = (ASTPatTuple) o;

        return getPats().equals(that.getPats());

    }

    @Override
    public int hashCode() {
        return getPats().hashCode();
    }

    @Override
    public Set<ASTVariable> getAllVariables() {
        Set<ASTVariable> vars = new HashSet<>();
        for (ASTPattern pat : pats) {
            vars.addAll(pat.getAllVariables());
        }
        return vars;
    }

    @Override
    public Set<ASTVariable> getFreeVars() {
        Set<ASTVariable> vars = new HashSet<>();
        for (ASTPattern pat : pats) {
            vars.addAll(pat.getFreeVars());
        }
        return vars;
    }

    @Override
    public <T> T accept(ComplexHaskellVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
