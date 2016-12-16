package haskell.complex.ast;

import haskell.complex.reduction.ComplexHaskellTransformation;
import haskell.complex.reduction.TooComplexException;

import java.util.Collections;
import java.util.Set;

/**
 * Represents a float.
 */
public class ASTChar implements ASTExpression, ASTPattern {
    private char value;

    public ASTChar(char value) {
        this.value = value;

    }

    public char getValue() {
        return value;
    }

    @Override
    public String toString() {
        return Character.toString(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ASTChar astChar = (ASTChar) o;

        return getValue() == astChar.getValue();

    }

    @Override
    public int hashCode() {
        return (int) getValue();
    }

    @Override
    public Set<ASTVariable> getAllVariables() {
        return Collections.emptySet();
    }

    @Override
    public Set<ASTVariable> getFreeVars() {
        return Collections.emptySet();
    }

    @Override
    public haskell.simple.ast.ASTExpression castToSimple() throws TooComplexException {
        return new haskell.simple.ast.ASTConstant(value);
    }

    @Override
    public boolean accept(ComplexHaskellTransformation tr) {
        return tr.visit(this);
    }
}
