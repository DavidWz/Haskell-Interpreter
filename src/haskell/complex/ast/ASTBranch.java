package haskell.complex.ast;

import haskell.complex.reduction.SimpleReducer;

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
    public boolean funcDeclToPatDecl() {
        if (condition.funcDeclToPatDecl()) {
            return true;
        }
        if (ifBranch.funcDeclToPatDecl()) {
            return true;
        }
        return elseBranch.funcDeclToPatDecl();
    }

    @Override
    public boolean nestMultipleLambdas() {
        if (condition.nestMultipleLambdas()) {
            return true;
        }
        if (ifBranch.nestMultipleLambdas()) {
            return true;
        }
        return elseBranch.nestMultipleLambdas();
    }

    @Override
    public boolean lambdaPatternToCase() {
        if (condition.lambdaPatternToCase()) {
            return true;
        }
        if (ifBranch.lambdaPatternToCase()) {
            return true;
        }
        return elseBranch.lambdaPatternToCase();
    }

    @Override
    public boolean caseToMatch() {
        // try to apply it as deep as possible
        if (condition.caseToMatch()) {
            return true;
        }
        if (ifBranch.caseToMatch()) {
            return true;
        }
        if (elseBranch.caseToMatch()) {
            return true;
        }

        // check if one can replace cases here
        if (condition instanceof ASTCase) {
            ASTCase caseExp = (ASTCase) condition;
            condition = SimpleReducer.caseToMatch(caseExp);
            return true;
        }
        if (ifBranch instanceof ASTCase) {
            ASTCase caseExp = (ASTCase) ifBranch;
            condition = SimpleReducer.caseToMatch(caseExp);
            return true;
        }
        if (elseBranch instanceof ASTCase) {
            ASTCase caseExp = (ASTCase) elseBranch;
            condition = SimpleReducer.caseToMatch(caseExp);
            return true;
        }
        return false;
    }

    @Override
    public boolean nestMultipleLets() {
        if (condition.nestMultipleLets()) {
            return true;
        }
        if (ifBranch.nestMultipleLets()) {
            return true;
        }
        return elseBranch.nestMultipleLets();
    }

    @Override
    public haskell.simple.ast.ASTExpression castToSimple() throws SimpleReducer.TooComplexException {
        return new haskell.simple.ast.ASTBranch(condition.castToSimple(), ifBranch.castToSimple(), elseBranch.castToSimple());
    }
}
