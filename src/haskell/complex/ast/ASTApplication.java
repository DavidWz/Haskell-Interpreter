package haskell.complex.ast;

import haskell.complex.reduction.SimpleReducer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a application, e.g. f x
 */
public class ASTApplication implements ASTExpression {
    private List<ASTExpression> exps;

    public ASTApplication(List<ASTExpression> exps) {
        assert(exps != null);
        assert(exps.size() > 1);
        this.exps = exps;
    }

    public ASTApplication(ASTExpression... exps) {
        assert(exps.length > 1);
        this.exps = Arrays.asList(exps);
    }

    public List<ASTExpression> getExps() {
        return exps;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ASTApplication that = (ASTApplication) o;

        return getExps().equals(that.getExps());

    }

    @Override
    public int hashCode() {
        return getExps().hashCode();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        for (ASTExpression exp : exps) {
            builder.append(exp);
            builder.append(" ");
        }
        builder.deleteCharAt(builder.length()-1);
        builder.append(")");
        return builder.toString();
    }

    @Override
    public Set<ASTVariable> getAllVariables() {
        Set<ASTVariable> vars = new HashSet<>();
        for (ASTExpression exp : exps) {
            vars.addAll(exp.getAllVariables());
        }
        return vars;
    }

    @Override
    public boolean funcDeclToPatDecl() {
        for (ASTExpression exp : exps) {
            if (exp.funcDeclToPatDecl()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean nestMultipleLambdas() {
        for (ASTExpression exp : exps) {
            if (exp.nestMultipleLambdas()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean lambdaPatternToCase() {
        for (ASTExpression exp : exps) {
            if (exp.lambdaPatternToCase()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean caseToMatch() {
        // try to apply it as deep as possible
        for (ASTExpression exp : exps) {
            if (exp.caseToMatch()) {
                return true;
            }
        }

        // check if one can replace cases here
        for (int i = 0; i < exps.size(); i++) {
            if (exps.get(i) instanceof ASTCase) {
                ASTCase caseExp = (ASTCase) exps.get(i);
                exps.set(i, SimpleReducer.caseToMatch(caseExp));
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean nestMultipleLets() {
        for (ASTExpression exp : exps) {
            if (exp.nestMultipleLets()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public haskell.simple.ast.ASTExpression castToSimple() throws SimpleReducer.TooComplexException {
        haskell.simple.ast.ASTExpression simpleExp = exps.get(0).castToSimple();

        for (int i = 1; i < exps.size(); i++) {
            simpleExp = new haskell.simple.ast.ASTApplication(simpleExp, exps.get(i).castToSimple());
        }

        return simpleExp;
    }
}
