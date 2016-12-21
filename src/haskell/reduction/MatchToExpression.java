package haskell.reduction;

import haskell.ast.*;

/**
 * Helper class which offers functionality to transform applications of the predefined match function.
 */
public class MatchToExpression {
    /**
     * Applies transformation rules which replace a match application by other expressions. The match application has
     * the following form: match pat exp exp1 exp2
     * @param pat
     * @param exp
     * @param exp1
     * @param exp2
     * @return
     */
    public static ASTExpression matchToExpression(ASTPattern pat, ASTExpression exp, ASTExpression exp1, ASTExpression exp2) {
        if (pat instanceof ASTVariable) {
            return matchVarToExp((ASTVariable) pat, exp, exp1, exp2);
        }
        else if (pat instanceof ASTJoker) {
            return matchJokerToExp((ASTJoker) pat, exp, exp1, exp2);
        }
        else if (pat instanceof ASTConstruct) {
            return matchConstrToExp((ASTConstruct) pat, exp, exp1, exp2);
        }
        else if (pat instanceof ASTTyConstr) {
            return matchConstrToExp(new ASTConstruct((ASTTyConstr) pat), exp, exp1, exp2);
        }
        else if (pat instanceof ASTPatTuple) {
            return matchTupleToExp((ASTPatTuple) pat, exp, exp1, exp2);
        }
        else {
            return matchConstToExp(pat, exp, exp1, exp2);
        }
    }

    private static ASTExpression matchVarToExp(ASTVariable var, ASTExpression exp, ASTExpression exp1, ASTExpression exp2) {
        /*
        match var exp exp1 exp2
        -----------------------
           (\var -> exp1) exp
         */
        return new ASTApplication(new ASTLambda(var, exp1), exp);
    }

    private static ASTExpression matchJokerToExp(ASTJoker joker, ASTExpression exp, ASTExpression exp1, ASTExpression exp2) {
        /*
        match _ exp exp1 exp2
        ---------------------
                 exp1
         */
        return exp1;
    }

    private static ASTExpression matchConstrToExp(ASTConstruct constr, ASTExpression exp, ASTExpression exp1, ASTExpression exp2) {
        /*
                          match (constr pat1 ... patn) exp exp1 exp2
        -----------------------------------------------------------------------------------------
        if (isa_constr exp) then (match (pat1, ..., patn) (argof_constr exp) exp1 exp2) else exp2
         */
        ASTApplication isaExp = new ASTApplication(VariableManager.getIsaConstrFunc(constr.getType()), exp);
        ASTPatTuple patTuple = new ASTPatTuple(constr.getPats());
        ASTApplication argofExp = new ASTApplication(VariableManager.getArgofFunc(constr.getType()), exp);
        ASTExpression tupleArgMatch = matchToExpression(patTuple, argofExp, exp1, exp2);

        return new ASTBranch(isaExp, tupleArgMatch, exp2);
    }

    private static ASTExpression matchTupleToExp(ASTPatTuple tuple, ASTExpression exp, ASTExpression exp1, ASTExpression exp2) {
        if (tuple.getPats().size() == 0) {
            /*
                    match () exp exp1 exp2
            ----------------------------------------
            if (isa_0-tuple exp) then exp1 else exp2
             */
            ASTApplication isaExp = new ASTApplication(VariableManager.getIsaTupleFunc(0), exp);
            return new ASTBranch(isaExp, exp1, exp2);
        }
        else if(tuple.getPats().size() >= 2) {
            /*
            match (pat1, ..., patn) exp exp1 exp2
            -------------------------------------------------------
            if (isa_n-tuple exp) then matchTuple else exp2

            where matchTuple is:
            match pat1 (sel_n,1 exp) (match pat2 (sel_n,2 exp) (... match patn (sel_n,n exp) exp1 exp2)...) exp2) exp2
             */
            int n = tuple.getPats().size();

            ASTApplication isaExp = new ASTApplication(VariableManager.getIsaTupleFunc(n), exp);
            ASTExpression matchTuple = exp1;

            int i = n;
            while (i > 0) {
                matchTuple = matchToExpression(
                        tuple.getPats().get(i-1),
                        new ASTApplication(VariableManager.getSelFunc(n,i), exp),
                        matchTuple,
                        exp2);
                i--;
            }

            return new ASTBranch(isaExp, matchTuple, exp2);
        }
        else {
            // 1-tuples are the same as the underlying expression
            return matchToExpression(tuple.getPats().get(0), exp, exp1, exp2);
        }
    }

    private static ASTExpression matchConstToExp(ASTPattern pat, ASTExpression exp, ASTExpression exp1, ASTExpression exp2) {
        /*
                 match CONSTANT exp exp1 exp2
        ----------------------------------------
        if (isa_CONSTANT exp) then exp1 else exp2
         */
        ASTApplication isaExp = new ASTApplication(VariableManager.getIsaFunc(pat), exp);
        return new ASTBranch(isaExp, exp1, exp2);
    }
}
