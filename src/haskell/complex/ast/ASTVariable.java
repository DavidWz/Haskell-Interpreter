package haskell.complex.ast;

import haskell.complex.reduction.SimpleReducer;
import lambda.reduction.WHNOReducer;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Represents a variable, i.e. a name that starts with a lower case.
 */
public class ASTVariable implements ASTExpression, ASTPattern {
    private String name;

    public ASTVariable(String name) {
        assert(name != null);
        assert(!name.trim().equals(""));
        assert(Character.isLowerCase(name.charAt(0)));
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ASTVariable that = (ASTVariable) o;

        return getName().equals(that.getName());

    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public Set<ASTVariable> getAllVariables() {
        Set<ASTVariable> vars = new HashSet<>();
        vars.add(this);
        return vars;
    }

    @Override
    public Set<ASTVariable> getFreeVars() {
        Set<ASTVariable> vars = new HashSet<>();
        vars.add(this);
        return vars;
    }

    @Override
    public boolean funcDeclToPatDecl() {
        return false;
    }

    @Override
    public boolean nestMultipleLambdas() {
        return false;
    }

    @Override
    public boolean lambdaPatternToCase() {
        return false;
    }

    @Override
    public boolean caseToMatch() {
        return false;
    }

    @Override
    public boolean nestMultipleLets() {
        return false;
    }

    @Override
    public boolean tuplePatLetToSingleVar() {
        return false;
    }

    @Override
    public haskell.simple.ast.ASTExpression castToSimple() throws SimpleReducer.TooComplexException {
        Optional<lambda.ast.ASTConstant> constant = WHNOReducer.toConst(name);
        if (constant.isPresent()) {
            return new haskell.simple.ast.ASTConstant(constant.get().getValue());
        }
        else {
            return new haskell.simple.ast.ASTVariable(name);
        }
    }
}
