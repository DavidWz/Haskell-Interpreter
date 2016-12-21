package haskell.reduction;

import haskell.ast.*;

import java.util.List;

/**
 * Transforms a case term to nested applications of the predefined match function.
 */
public class CaseToMatch implements ComplexHaskellTransformation {
    /**
     * Transforms the given case expression to a nested application of the predefined match function.
     * @param caseExpr the case expression
     * @return
     */
    private ASTExpression caseToMatch(ASTCase caseExpr) {
        ASTExpression exp = caseExpr.getExp();
        List<ASTPattern> casePats = caseExpr.getCasePats();
        List<ASTExpression> caseExps = caseExpr.getCaseExps();

        // we start with bot
        ASTExpression nestedMatches = VariableManager.getBot();

        // then we iteratively apply match as follows:
        /*
                 case exp of { pat1 -> exp1; ...; patn -> expn }
        ----------------------------------------------------------------------------
        match pat1 exp exp1 (match pat2 exp exp2 (... (match patn exp expn bot)...))
         */
        while (casePats.size() > 0) {
            ASTPattern currentPat = casePats.remove(casePats.size()-1);
            ASTExpression currentExp = caseExps.remove(caseExps.size()-1);

            // An application can only contain expressions, but at this point there is still a pattern (currentPat).
            // In order to avoid problems with the type system, we need to immediatly apply other transformation rules
            // which transform a match application with patterns to some other expression.
            nestedMatches = MatchToExpression.matchToExpression(currentPat, exp, currentExp, nestedMatches);
        }

        // we can cast because there is always at least one case (@see ASTCase assertions)
        return nestedMatches;
    }

    @Override
    public Boolean visit(ASTApplication node) {
        // try to apply it as deep as possible
        List<ASTExpression> exps = node.getExps();
        for (ASTExpression exp : exps) {
            if (exp.accept(this)) {
                return true;
            }
        }

        // check if one can replace cases here
        for (int i = 0; i < exps.size(); i++) {
            if (exps.get(i) instanceof ASTCase) {
                ASTCase caseExp = (ASTCase) exps.get(i);
                exps.set(i, caseToMatch(caseExp));
                return true;
            }
        }
        return false;
    }

    @Override
    public Boolean visit(ASTBranch node) {
        ASTExpression condition = node.getCondition();
        ASTExpression ifBranch = node.getIfBranch();
        ASTExpression elseBranch = node.getElseBranch();

        // try to apply it as deep as possible
        if (condition.accept(this)) {
            return true;
        }
        if (ifBranch.accept(this)) {
            return true;
        }
        if (elseBranch.accept(this)) {
            return true;
        }

        // check if one can replace cases here
        if (condition instanceof ASTCase) {
            ASTCase caseExp = (ASTCase) condition;
            node.setCondition(caseToMatch(caseExp));
            return true;
        }
        if (ifBranch instanceof ASTCase) {
            ASTCase caseExp = (ASTCase) ifBranch;
            node.setIfBranch(caseToMatch(caseExp));
            return true;
        }
        if (elseBranch instanceof ASTCase) {
            ASTCase caseExp = (ASTCase) elseBranch;
            node.setElseBranch(caseToMatch(caseExp));
            return true;
        }

        return false;
    }

    @Override
    public Boolean visit(ASTCase node) {
        ASTExpression exp = node.getExp();
        List<ASTExpression> caseExps = node.getCaseExps();

        // try to apply it as deep as possible
        if (exp.accept(this)) {
            return true;
        }
        for (ASTExpression e : caseExps) {
            if (e.accept(this)) {
                return true;
            }
        }

        // check if one can replace cases here
        if (exp instanceof ASTCase) {
            ASTCase caseExp = (ASTCase) exp;
            node.setExp(caseToMatch(caseExp));
            return true;
        }
        for (int i = 0; i < caseExps.size(); i++) {
            if (caseExps.get(i) instanceof ASTCase) {
                ASTCase caseExp = (ASTCase) caseExps.get(i);
                caseExps.set(i, caseToMatch(caseExp));
                return true;
            }
        }
        return false;
    }

    @Override
    public Boolean visit(ASTExpTuple node) {
        List<ASTExpression> exps = node.getExps();

        // try to apply it as deep as possible
        for (ASTExpression exp : exps) {
            if (exp.accept(this)) {
                return true;
            }
        }

        // check if one can replace cases here
        for (int i = 0; i < exps.size(); i++) {
            if (exps.get(i) instanceof ASTCase) {
                ASTCase caseExp = (ASTCase) exps.get(i);
                exps.set(i, caseToMatch(caseExp));
                return true;
            }
        }
        return false;
    }


    @Override
    public Boolean visit(ASTFunDecl node) {
        ASTExpression exp = node.getExp();

        if (exp.accept(this)) {
            return true;
        }

        if (exp instanceof ASTCase) {
            ASTCase caseExp = (ASTCase) exp;
            node.setExp(caseToMatch(caseExp));
            return true;
        }
        return false;
    }


    @Override
    public Boolean visit(ASTLambda node) {
        ASTExpression exp = node.getExp();

        if (exp.accept(this)) {
            return true;
        }

        if (exp instanceof ASTCase) {
            ASTCase caseExp = (ASTCase) exp;
            node.setExp(caseToMatch(caseExp));
            return true;
        }
        return false;
    }

    @Override
    public Boolean visit(ASTLet node) {
        List<ASTDecl> decls = node.getDecls();
        ASTExpression exp = node.getExp();

        for (ASTDecl decl : decls) {
            if (decl.accept(this)) {
                return true;
            }
        }
        if (exp.accept(this)) {
            return true;
        }

        if (exp instanceof ASTCase) {
            ASTCase caseExp = (ASTCase) exp;
            node.setExp(caseToMatch(caseExp));
            return true;
        }
        return false;
    }

    @Override
    public Boolean visit(ASTPatDecl node) {
        ASTExpression exp = node.getExp();

        if (exp.accept(this)) {
            return true;
        }

        if (exp instanceof ASTCase) {
            ASTCase caseExp = (ASTCase) exp;
            node.setExp(caseToMatch(caseExp));
            return true;
        }
        return false;
    }


    @Override
    public Boolean visit(ASTProgram node) {
        for (ASTDecl decl : node.getDecls()) {
            if (decl.accept(this)) {
                return true;
            }
        }
        return false;
    }
}
