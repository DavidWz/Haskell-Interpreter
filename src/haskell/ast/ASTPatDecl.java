package haskell.ast;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a pattern declaration.
 */
public class ASTPatDecl extends ASTDecl {
    private ASTPattern pat;
    private ASTExpression exp;

    public ASTPatDecl(ASTPattern pat, ASTExpression exp) {
        assert(pat != null);
        assert(exp != null);
        this.pat = pat;
        this.exp = exp;
    }

    public ASTPattern getPat() {
        return pat;
    }

    public ASTExpression getExp() {
        return exp;
    }

    public void setExp(ASTExpression exp) {
        assert(exp != null);
        this.exp = exp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ASTPatDecl that = (ASTPatDecl) o;

        if (!getPat().equals(that.getPat())) return false;
        return getExp().equals(that.getExp());

    }

    @Override
    public int hashCode() {
        int result = getPat().hashCode();
        result = 31 * result + getExp().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return pat + " = " + exp;
    }

    @Override
    public Set<ASTVariable> getAllVariables() {
        Set<ASTVariable> vars = new HashSet<>();
        vars.addAll(pat.getAllVariables());
        vars.addAll(exp.getAllVariables());
        return vars;
    }

    @Override
    public Set<ASTVariable> getFreeVars() {
        Set<ASTVariable> vars = new HashSet<>();
        vars.addAll(exp.getFreeVars());
        vars.removeAll(pat.getFreeVars());
        return vars;
    }

    @Override
    public <T> T accept(ComplexHaskellVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
