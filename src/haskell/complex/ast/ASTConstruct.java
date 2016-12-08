package haskell.complex.ast;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a construction.
 */
public class ASTConstruct implements ASTExpression, ASTPattern {
    private ASTTypeConstr type;
    private List<ASTPattern> pats;

    public ASTTypeConstr getType() {
        return type;
    }

    public List<ASTPattern> getPats() {
        return pats;
    }

    public ASTConstruct(ASTTypeConstr type, List<ASTPattern> pats) {
        assert(type != null);
        assert(pats != null);

        this.type = type;
        this.pats = pats;
    }

    public ASTConstruct(ASTTypeConstr type, ASTPattern... pats) {
        assert(type != null);

        this.type = type;
        this.pats = Arrays.asList(pats);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ASTConstruct that = (ASTConstruct) o;

        if (!getType().equals(that.getType())) return false;
        return getPats().equals(that.getPats());

    }

    @Override
    public int hashCode() {
        int result = getType().hashCode();
        result = 31 * result + getPats().hashCode();
        return result;
    }

    @Override
    public String toString() {
        if (pats.size() == 0) {
            return type.toString();
        }
        else {
            StringBuilder builder = new StringBuilder();
            builder.append("(");
            builder.append(type.toString());
            for (ASTPattern pat : pats) {
                builder.append(" ");
                builder.append(pat.toString());
            }
            builder.append(")");
            return builder.toString();
        }
    }

    @Override
    public Set<ASTVariable> getAllVariables() {
        Set<ASTVariable> vars = new HashSet<>();
        for (ASTPattern pat : pats) {
            vars.addAll(pat.getAllVariables());
        }
        return vars;
    }
}
