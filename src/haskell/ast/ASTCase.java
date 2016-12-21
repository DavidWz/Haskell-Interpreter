package haskell.ast;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a cases expression, i.e. case exp of { (pat -> exp)+ }
 */
public class ASTCase implements ASTExpression {
    private ASTExpression exp;
    private List<ASTPattern> casePats;
    private List<ASTExpression> caseExps;

    public ASTCase(ASTExpression exp, List<ASTPattern> casePats, List<ASTExpression> caseExps) {
        assert(exp != null);
        assert(casePats != null);
        assert(casePats.size() >= 1);
        assert(caseExps != null);
        assert(casePats.size() == caseExps.size());
        this.exp = exp;
        this.casePats = casePats;
        this.caseExps = caseExps;
    }

    public ASTExpression getExp() {
        return exp;
    }

    public void setExp(ASTExpression exp) {
        assert(exp != null);
        this.exp = exp;
    }

    public List<ASTPattern> getCasePats() {
        return casePats;
    }

    public List<ASTExpression> getCaseExps() {
        return caseExps;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ASTCase astCase = (ASTCase) o;

        if (!getExp().equals(astCase.getExp())) return false;
        if (!getCasePats().equals(astCase.getCasePats())) return false;
        return getCaseExps().equals(astCase.getCaseExps());

    }

    @Override
    public int hashCode() {
        int result = getExp().hashCode();
        result = 31 * result + getCasePats().hashCode();
        result = 31 * result + getCaseExps().hashCode();
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("case ");
        builder.append(exp);
        builder.append(" of {");

        for (int i = 0; i < casePats.size(); i++) {
            builder.append(casePats.get(i));
            builder.append(" -> ");
            builder.append(caseExps.get(i));
            builder.append("; ");
        }
        builder.deleteCharAt(builder.length()-1);
        builder.deleteCharAt(builder.length()-1);
        builder.append("}");
        return builder.toString();
    }

    @Override
    public Set<ASTVariable> getAllVariables() {
        Set<ASTVariable> vars = new HashSet<>();
        vars.addAll(exp.getAllVariables());
        for (ASTPattern pat : casePats) {
            vars.addAll(pat.getAllVariables());
        }
        for (ASTExpression exp : caseExps) {
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
        Set<ASTVariable> vars = new HashSet<>();
        vars.addAll(exp.getFreeVars());
        for (ASTPattern pat : casePats) {
            vars.addAll(pat.getFreeVars());
        }
        for (ASTExpression exp : caseExps) {
            vars.addAll(exp.getFreeVars());
        }
        return vars;
    }
}
