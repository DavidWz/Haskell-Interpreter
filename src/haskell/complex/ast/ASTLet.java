package haskell.complex.ast;

import haskell.complex.reduction.SimpleReducer;
import haskell.complex.reduction.VariableManager;

import java.util.*;
import java.util.stream.Collectors;

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

    public ASTExpression getExp() {
        return exp;
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
    public boolean funcDeclToPatDecl() {
        // first, try to apply this transformation as deep as possible
        for (ASTDecl decl : decls) {
            if (decl.funcDeclToPatDecl()) {
                return true;
            }
        }
        if (exp.funcDeclToPatDecl()) {
            return true;
        }

        // try to apply the transformation to the declarations stored in this let-term
        Optional<List<ASTDecl>> transformedDecls = SimpleReducer.funcDeclToPatDecl(decls);
        if (transformedDecls.isPresent()) {
            decls = transformedDecls.get();
            return true;
        }
        else {
            return false;
        }
    }
}
