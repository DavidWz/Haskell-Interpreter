package haskell.ast;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a if-then-else branch in complex haskell.
 */
public class ASTBranch implements ASTExpression {
    private ASTExpression condition;
    private ASTExpression ifBranch;
    private ASTExpression elseBranch;

    public ASTBranch(ASTExpression condition, ASTExpression ifBranch, ASTExpression elseBranch) {
        assert(condition != null);
        assert(ifBranch != null);
        assert(elseBranch != null);
        this.condition = condition;
        this.ifBranch = ifBranch;
        this.elseBranch = elseBranch;
    }

    public ASTExpression getCondition() {
        return condition;
    }

    public ASTExpression getIfBranch() {
        return ifBranch;
    }

    public ASTExpression getElseBranch() {
        return elseBranch;
    }

    public void setCondition(ASTExpression condition) {
        assert(condition != null);
        this.condition = condition;
    }

    public void setIfBranch(ASTExpression ifBranch) {
        assert(ifBranch != null);
        this.ifBranch = ifBranch;
    }

    public void setElseBranch(ASTExpression elseBranch) {
        assert(elseBranch != null);
        this.elseBranch = elseBranch;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ASTBranch astBranch = (ASTBranch) o;

        if (!getCondition().equals(astBranch.getCondition())) return false;
        if (!getIfBranch().equals(astBranch.getIfBranch())) return false;
        return getElseBranch().equals(astBranch.getElseBranch());

    }

    @Override
    public int hashCode() {
        int result = getCondition().hashCode();
        result = 31 * result + getIfBranch().hashCode();
        result = 31 * result + getElseBranch().hashCode();
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("if ");
        builder.append(condition);
        builder.append(" then ");
        builder.append(ifBranch);
        builder.append(" else ");
        builder.append(elseBranch);
        return builder.toString();
    }

    @Override
    public Set<ASTVariable> getAllVariables() {
        Set<ASTVariable> vars = new HashSet<>();
        vars.addAll(condition.getAllVariables());
        vars.addAll(ifBranch.getAllVariables());
        vars.addAll(elseBranch.getAllVariables());
        return vars;
    }

    @Override
    public <T> T accept(ComplexHaskellVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public Set<ASTVariable> getFreeVars() {
        HashSet<ASTVariable> vars = new HashSet<>();
        vars.addAll(condition.getFreeVars());
        vars.addAll(ifBranch.getFreeVars());
        vars.addAll(elseBranch.getFreeVars());
        return vars;
    }
}
