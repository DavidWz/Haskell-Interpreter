package haskell.complex.ast;

import haskell.complex.reduction.TooComplexException;
import haskell.simple.ast.ASTExpression;

import java.util.*;

/**
 * Represents the root node of a haskell program.
 */
public class ASTProgram implements ComplexHaskell {
    private List<ASTDecl> decls;

    public ASTProgram(List<ASTDecl> decls) {
        assert(decls != null);
        this.decls = decls;
    }

    public ASTProgram() {
        this.decls = new ArrayList<>();
    }

    public void addDeclaration(ASTDecl decl) {
        decls.add(decl);
    }

    public List<ASTDecl> getDecls() {
        return decls;
    }

    public void setDecls(List<ASTDecl> decls) {
        assert(decls != null);
        this.decls = decls;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ASTProgram that = (ASTProgram) o;

        return getDecls().equals(that.getDecls());

    }

    @Override
    public int hashCode() {
        return getDecls().hashCode();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (ASTDecl decl : decls) {
            builder.append(decl);
            builder.append("\n");
        }
        builder.deleteCharAt(builder.length()-1);
        return builder.toString();
    }

    @Override
    public Set<ASTVariable> getAllVariables() {
        Set<ASTVariable> vars = new HashSet<>();
        for (ASTDecl decl : decls) {
            vars.addAll(decl.getAllVariables());
        }
        return vars;
    }

    @Override
    public Set<ASTVariable> getFreeVars() {
        Set<ASTVariable> vars = new HashSet<>();
        for (ASTDecl decl : decls) {
            vars.addAll(decl.getFreeVars());
        }
        return vars;
    }

    @Override
    public ASTExpression castToSimple() throws TooComplexException {
        throw new TooComplexException(this, "Programs are not part of simple haskell. Please use \"Let [program] in [expression]\" instead.");
    }
}
