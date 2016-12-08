package haskell.complex.ast;

import java.util.Arrays;
import java.util.List;

/**
 * Represents a function declaration:
 * var pat ... pat = exp
 */
public class ASTFunDecl extends ASTDecl {
    private ASTVariable var;
    private List<ASTPattern> pats;
    private ASTExpression exp;

    public ASTFunDecl(ASTVariable var, List<ASTPattern> pats, ASTExpression exp) {
        assert(var != null);
        assert(pats != null);
        assert(pats.size() >= 1);
        assert(exp != null);
        this.var = var;
        this.pats = pats;
        this.exp = exp;
    }

    public ASTFunDecl(ASTExpression exp, ASTVariable var, ASTPattern... pats) {
        assert(var != null);
        assert(exp != null);
        assert(pats.length >= 1);
        this.var = var;
        this.exp = exp;
        this.pats = Arrays.asList(pats);
    }

    public ASTVariable getVar() {
        return var;
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

        ASTFunDecl that = (ASTFunDecl) o;

        if (!getVar().equals(that.getVar())) return false;
        if (!getPats().equals(that.getPats())) return false;
        return getExp().equals(that.getExp());

    }

    @Override
    public int hashCode() {
        int result = getVar().hashCode();
        result = 31 * result + getPats().hashCode();
        result = 31 * result + getExp().hashCode();
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(var);
        for (ASTPattern pat : pats) {
            builder.append(" ");
            builder.append(pat);
        }
        builder.append(" = ");
        builder.append(exp);
        return builder.toString();
    }
}
