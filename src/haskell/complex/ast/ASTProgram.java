package haskell.complex.ast;

import haskell.complex.reduction.SimpleReducer;
import haskell.simple.ast.*;
import haskell.simple.ast.ASTExpression;

import java.util.*;

/**
 * Represents the root node of a haskell program.
 */
public class ASTProgram implements ComplexHaskell {
    private List<ASTDecl> decls;

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
    public boolean nestMultipleLambdas() {
        for(ASTDecl decl : decls) {
            if (decl.nestMultipleLambdas()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean lambdaPatternToCase() {
        for(ASTDecl decl : decls) {
            if (decl.lambdaPatternToCase()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean caseToMatch() {
        for (ASTDecl decl : decls) {
            if (decl.caseToMatch()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean nestMultipleLets() {
        for(ASTDecl decl : decls) {
            if (decl.nestMultipleLets()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean tuplePatLetToSingleVar() {
        for(ASTDecl decl : decls) {
            if (decl.tuplePatLetToSingleVar()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ASTExpression castToSimple() throws SimpleReducer.TooComplexException {
        throw new SimpleReducer.TooComplexException(this, "Programs are not part of simple haskell. Please use \"Let [program] in [expression]\" instead.");
    }
}
