package haskell.simple.ast;

import lambda.ast.ASTTerm;
import lambda.reduction.delta.BranchRule;

/**
 * Represents a simple haskell if-then-else branch.
 */
public class ASTBranch extends ASTExpression {
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

    public void setCondition(ASTExpression condition) {
        assert(condition != null);
        this.condition = condition;
    }

    public ASTExpression getIfBranch() {
        return ifBranch;
    }

    public void setIfBranch(ASTExpression ifBranch) {
        assert(ifBranch != null);
        this.ifBranch = ifBranch;
    }

    public ASTExpression getElseBranch() {
        return elseBranch;
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
        return "if " + condition + " then " + ifBranch + " else " + elseBranch;
    }

    @Override
    public ASTTerm toLambdaTerm() {
        lambda.ast.ASTTerm result = new lambda.ast.ASTApplication(new lambda.ast.ASTConstant(BranchRule.Operator.IF), condition.toLambdaTerm());
        result = new lambda.ast.ASTApplication(result, ifBranch.toLambdaTerm());
        result = new lambda.ast.ASTApplication(result, elseBranch.toLambdaTerm());
        return result;
    }
}
