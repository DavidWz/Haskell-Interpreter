package haskell.complex.ast;

import haskell.complex.reduction.SimpleReducer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    /**
     * Creates a new function declaration.
     * @param exp The right-hand side expression
     * @param var The function variable
     * @param pats The function argument patterns
     */
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

    @Override
    public Set<ASTVariable> getAllVariables() {
        Set<ASTVariable> vars = new HashSet<>();
        vars.add(var);
        for (ASTPattern pat : pats) {
            vars.addAll(pat.getAllVariables());
        }
        vars.addAll(exp.getAllVariables());
        return vars;
    }

    @Override
    public boolean funcDeclToPatDecl() {
        return exp.funcDeclToPatDecl();
    }

    @Override
    public boolean nestMultipleLambdas() {
        return exp.nestMultipleLambdas();
    }

    @Override
    public boolean lambdaPatternToCase() {
        return exp.lambdaPatternToCase();
    }

    @Override
    public boolean caseToMatch() {
        if (exp.caseToMatch()) {
            return true;
        }

        if (exp instanceof ASTCase) {
            ASTCase caseExp = (ASTCase) exp;
            exp = SimpleReducer.caseToMatch(caseExp);
            return true;
        }
        return false;
    }

    @Override
    public boolean nestMultipleLets() {
        return exp.nestMultipleLets();
    }
}
