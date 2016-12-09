package haskell.complex.ast;

import haskell.complex.reduction.SimpleReducer;

import java.util.*;

/**
 * Represents a complex haskell tuple of expressions. (exp, ..., exp)
 */
public class ASTExpTuple implements ASTExpression {
    private List<ASTExpression> exps;

    public ASTExpTuple(List<ASTExpression> exps) {
        assert(exps != null);
        this.exps = exps;
    }

    public List<ASTExpression> getExps() {
        return exps;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        for (ASTExpression exp : exps) {
            builder.append(exp);
            builder.append(", ");
        }
        builder.deleteCharAt(builder.length()-1);
        builder.deleteCharAt(builder.length()-1);
        builder.append(")");
        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ASTExpTuple that = (ASTExpTuple) o;

        return getExps().equals(that.getExps());

    }

    @Override
    public int hashCode() {
        return getExps().hashCode();
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
        List<haskell.simple.ast.ASTExpression> simpleExps = new ArrayList<>();
        for (ASTExpression exp : exps) {
            simpleExps.add(exp.castToSimple());
        }
        return new haskell.simple.ast.ASTTuple(simpleExps);
    }
}
