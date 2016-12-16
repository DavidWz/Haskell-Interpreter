package haskell.complex.ast;

import haskell.complex.reduction.ComplexHaskellTransformation;
import haskell.complex.reduction.TooComplexException;

import java.util.*;

/**
 * Represents a let expression, i.e. let decls in expr
 */
public class ASTLet implements ASTExpression {
    private List<ASTDecl> decls;
    private ASTExpression exp;

    public ASTLet(List<ASTDecl> decls, ASTExpression exp) {
        assert(decls != null);
        assert(exp != null);
        this.decls = decls;
        this.exp = exp;
    }

    public List<ASTDecl> getDecls() {
        return decls;
    }

    public void setDecls(List<ASTDecl> decls) {
        assert(decls != null);
        this.decls = decls;
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

        ASTLet astLet = (ASTLet) o;

        if (!getDecls().equals(astLet.getDecls())) return false;
        return getExp().equals(astLet.getExp());

    }

    @Override
    public int hashCode() {
        int result = getDecls().hashCode();
        result = 31 * result + getExp().hashCode();
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("let {\n\t");
        for (ASTDecl decl : decls) {
            builder.append(decl);
            builder.append(";\n\t");
        }
        builder.deleteCharAt(builder.length()-1);
        builder.append("} in ");
        builder.append(exp);
        return builder.toString();
    }

    @Override
    public Set<ASTVariable> getAllVariables() {
        Set<ASTVariable> vars = new HashSet<>();
        for (ASTDecl decl : decls) {
            vars.addAll(decl.getAllVariables());
        }
        vars.addAll(exp.getAllVariables());
        return vars;
    }

    @Override
    public Set<ASTVariable> getFreeVars() {
        Set<ASTVariable> vars = new HashSet<>();
        vars.addAll(exp.getFreeVars());
        for (ASTDecl decl : decls) {
            if (decl instanceof ASTFunDecl) {
                vars.addAll(((ASTFunDecl) decl).getExp().getFreeVars());
            }
        }
        for (ASTDecl decl : decls) {
            if (decl instanceof ASTFunDecl) {
                vars.remove(((ASTFunDecl) decl).getVar());
                for (ASTPattern pat : ((ASTFunDecl) decl).getPats()) {
                    vars.removeAll(pat.getFreeVars());
                }
            }
        }
        return vars;
    }

    @Override
    public haskell.simple.ast.ASTExpression castToSimple() throws TooComplexException {
        if (decls.size() != 1) {
            throw new TooComplexException(this, "Let expressions in simple haskel must only contain one declaration.");
        }

        ASTDecl decl = decls.get(0);
        if (!(decl instanceof ASTPatDecl)) {
            throw new TooComplexException(this, "Declarations in let expressions must be pattern declarations.");
        }

        ASTPatDecl patDecl = (ASTPatDecl) decl;
        ASTPattern pat = patDecl.getPat();
        if (!(pat instanceof ASTVariable)) {
            throw new TooComplexException(this, "The pattern of a pattern declaration in let expressions must be a variable.");
        }

        return new haskell.simple.ast.ASTLet((haskell.simple.ast.ASTVariable) pat.castToSimple(),
                patDecl.getExp().castToSimple(),
                exp.castToSimple());
    }

    @Override
    public boolean accept(ComplexHaskellTransformation tr) {
        return tr.visit(this);
    }
}
