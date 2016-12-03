package lambda.ast;

import lambda.reduction.delta.DeltaRule;

import java.util.*;

/**
 * Represents a variable.
 */
public class ASTVariable extends ASTTerm {
    private String name;

    public ASTVariable(String name) {
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
    protected Optional<ASTTerm> applyBetaReduction() {
        return Optional.empty();
    }

    @Override
    protected Optional<ASTTerm> applyDeltaReduction(DeltaRule delta) {
        return Optional.empty();
    }

    @Override
    public Optional<ASTTerm> applyWHNOReduction(List<DeltaRule> deltaRules) {
        return Optional.empty();
    }

    @Override
    protected List<ASTTerm> getLMOMArguments() {
        return new ArrayList<>();
    }

    @Override
    protected ASTTerm getLMOMTerm() {
        return this;
    }

    @Override
    public Set<ASTVariable> getFreeVars() {
        // a variable is always free
        HashSet<ASTVariable> freeVars = new HashSet<>();
        freeVars.add(this);
        return freeVars;
    }

    @Override
    public ASTTerm substitute(ASTVariable var, ASTTerm expr) {
        // if this variable should be replaced, replace it by expr
        if (var.equals(this)) {
            return expr;
        }
        // otherwise leave it untouched
        else {
            return this;
        }
    }
}
