package haskell.complex.ast;

import haskell.complex.reduction.ComplexHaskellTransformation;
import haskell.complex.reduction.TooComplexException;
import haskell.simple.ast.ASTExpression;

import java.util.*;

/**
 * Represents a data declaration.
 */
public class ASTDataDecl extends ASTDecl {
    private ASTTyConstr tyConstr;
    private List<ASTVariable> vars;
    private List<ASTConstrDecl> constrDecls;

    public ASTDataDecl(ASTTyConstr tyConstr, List<ASTVariable> vars, List<ASTConstrDecl> constrDecls) {
        assert(tyConstr != null);
        assert(vars != null);
        assert(constrDecls != null);
        assert(constrDecls.size() >= 1);
        this.tyConstr = tyConstr;
        this.vars = vars;
        this.constrDecls = constrDecls;
    }

    public ASTDataDecl(ASTTyConstr tyConstr, ASTVariable var, ASTConstrDecl... constrDecls) {
        assert(tyConstr != null);
        assert(var != null);
        this.tyConstr = tyConstr;
        this.vars = Collections.singletonList(var);
        this.constrDecls = Arrays.asList(constrDecls);
    }

    public ASTTyConstr getTyConstr() {
        return tyConstr;
    }

    public List<ASTVariable> getVars() {
        return vars;
    }

    public List<ASTConstrDecl> getConstrDecls() {
        return constrDecls;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ASTDataDecl that = (ASTDataDecl) o;

        if (!getTyConstr().equals(that.getTyConstr())) return false;
        if (!getVars().equals(that.getVars())) return false;
        return getConstrDecls().equals(that.getConstrDecls());

    }

    @Override
    public int hashCode() {
        int result = getTyConstr().hashCode();
        result = 31 * result + getVars().hashCode();
        result = 31 * result + getConstrDecls().hashCode();
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("data ").append(tyConstr);
        for (ASTVariable var : vars) {
            builder.append(" ").append(var);
        }
        builder.append(" = ");
        for (ASTConstrDecl constrDecl : constrDecls) {
            builder.append(constrDecl).append(" | ");
        }
        builder.setLength(builder.length()-2);
        return builder.toString();
    }

    @Override
    public Set<ASTVariable> getAllVariables() {
        return new HashSet<>(vars);
    }

    @Override
    public Set<ASTVariable> getFreeVars() {
        return new HashSet<>(vars);
    }

    @Override
    public ASTExpression castToSimple() throws TooComplexException {
        throw new TooComplexException(this, "Data declarations are not part of simple haskell.");
    }

    @Override
    public boolean accept(ComplexHaskellTransformation tr) {
        return tr.visit(this);
    }
}
