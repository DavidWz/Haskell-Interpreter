package lambda.ast;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a constant.
 */
public class ASTConstant extends ASTTerm {
    private String name;

    public ASTConstant(String name) {
        assert(name != null);
        assert(!name.trim().equals(""));
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        assert(name != null);
        assert(!name.trim().equals(""));
        this.name = name;
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
    public Set<ASTVariable> getFreeVars() {
        // a constant is never free
        return new HashSet<>();
    }

    @Override
    public ASTTerm substitute(ASTVariable var, ASTTerm expr) {
        // constants cannot be substituted because they're not free
        return this;
    }
}