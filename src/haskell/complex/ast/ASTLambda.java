package haskell.complex.ast;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents an anonymous function.
 */
public class ASTLambda implements ASTExpression {
    private List<ASTPattern> pats;
    private ASTExpression exp;

    public ASTLambda(List<ASTPattern> pats, ASTExpression exp) {
        assert(pats != null);
        assert(exp != null);
        assert(pats.size() >= 1);
        this.pats = pats;
        this.exp = exp;
    }

    public List<ASTPattern> getPats() {
        return pats;
    }

    public ASTExpression getExp() {
        return exp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ASTLambda astLambda = (ASTLambda) o;

        if (!getPats().equals(astLambda.getPats())) return false;
        return getExp().equals(astLambda.getExp());

    }

    @Override
    public int hashCode() {
        int result = getPats().hashCode();
        result = 31 * result + getExp().hashCode();
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\\");

        for (ASTPattern pat : pats) {
            builder.append(pat);
            builder.append(" ");
        }
        builder.append("-> ");
        builder.append(exp);

        return builder.toString();
    }

    @Override
    public Set<ASTVariable> getAllVariables() {
        Set<ASTVariable> vars = new HashSet<>();
        for (ASTPattern pat : pats) {
            vars.addAll(pat.getAllVariables());
        }
        vars.addAll(exp.getAllVariables());
        return vars;
    }
}
