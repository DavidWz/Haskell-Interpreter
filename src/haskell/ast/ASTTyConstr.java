package haskell.ast;

import java.util.Collections;
import java.util.Set;

/**
 * Represents a type constructor, i.e. a name that starts with an upper case.
 */
public class ASTTyConstr implements ASTExpression, ASTPattern {
    private String name;

    public ASTTyConstr(String name) {
        assert(name != null);
        assert(!name.trim().equals(""));
        assert(Character.isUpperCase(name.charAt(0)));
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ASTTyConstr that = (ASTTyConstr) o;

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
        return Collections.emptySet();
    }

    @Override
    public Set<ASTVariable> getFreeVars() {
        return Collections.emptySet();
    }

    @Override
    public <T> T accept(ComplexHaskellVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
