package haskell.complex.reduction;

import haskell.complex.ast.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A class which can reduce complex haskell to simple haskell.
 * Note: The following functions are predefined, so you cannot overwrite them.
 * match and all functions defined in the delta rules
 */
public class SimpleReducer {
    public static class TooComplexException extends Exception {
        private ComplexHaskell exp;
        private String msg;

        public TooComplexException(ComplexHaskell exp, String msg) {
            this.exp = exp;
            this.msg = msg;
        }

        @Override
        public String getMessage() {
            StringBuilder builder = new StringBuilder();
            builder.append("The complex haskell expression cannot be fully reduced to a simple haskell expression, because:\n");
            builder.append(msg);
            builder.append("\nThe expression was: \n");
            builder.append(exp);
            return builder.toString();
        }
    }

    private ASTExpression expression;

    public SimpleReducer(ASTExpression expression) {
        assert(expression != null);
        this.expression = expression;
        VariableManager.init(expression);
    }

    /**
     * Transforms function declarations for one specific function into a pattern declaration.
     * @param decls a list of declarations
     * @return an optional containing a new list of declarations where one function was replaced by a pattern declaration.
     *         if the rule could not be applied, an empty optional is returned.
     */
    public static Optional<List<ASTDecl>> funcDeclToPatDecl(List<ASTDecl> decls) {
        // filter out the function declarations
        List<ASTFunDecl> funDecls = new ArrayList<>();
        List<ASTDecl> otherDecls = new ArrayList<>();

        for (ASTDecl decl : decls) {
            if (decl instanceof ASTFunDecl) {
                funDecls.add((ASTFunDecl) decl);
            }
            else {
                otherDecls.add(decl);
            }
        }

        // now we want to transform all declarations for one specific function
        if (funDecls.size() > 0) {
            // we pick the first function we encounter
            ASTVariable targetFunction = funDecls.get(0).getVar();

            // then we collect all declarations for that function
            List<ASTFunDecl> targetFunDecls = funDecls.stream().
                    filter(decl -> decl.getVar().equals(targetFunction)).
                    collect(Collectors.toList());
            funDecls.removeAll(targetFunDecls);

            // now we can actually apply a transformation to those function
            ASTPatDecl patDecl = SimpleReducer.getPatDeclForFunctionDecls(targetFunDecls);

            // and then we recombine the results
            otherDecls.add(patDecl);
            otherDecls.addAll(funDecls);
            return Optional.of(otherDecls);
        }
        else {
            return Optional.empty();
        }
    }

    /**
     * Transforms a series of function declarations for the same function into a single pattern declaration.
     * @param decls a list of function declarations for the same function
     * @return the equivalent pattern declaration
     */
    private static ASTPatDecl getPatDeclForFunctionDecls(List<ASTFunDecl> decls) {
        assert(decls != null);
        assert(decls.size() > 0);

        // we assume all functions in the list have the same function name
        ASTVariable functionName = decls.get(0).getVar();

        // and we assume they all have the same number of arguments
        int numArgs = decls.get(0).getPats().size();

        // we construct the pattern as follows:
        /*
        var pat1 ... patn = exp; ...; var pat1 ... patn = exp;
        ------------------------------------------------------
        var = \x1 ... xn -> case (x1,...,xn) of { (pat1,...,patn) -> exp; ...;}
         */

        // create the lambda variables
        List<ASTPattern> lambdaVarsPat = new ArrayList<>();
        List<ASTExpression> lambdaVarsExp = new ArrayList<>();
        for (int i = 0; i < numArgs; i++) {
            ASTVariable var = VariableManager.getFreshVariable();
            lambdaVarsPat.add(var);
            lambdaVarsExp.add(var);
        }

        // create the case expression
        ASTExpTuple varsTuple = new ASTExpTuple(lambdaVarsExp);

        // create the case patterns and expression
        List<ASTPattern> casePats = new ArrayList<>();
        List<ASTExpression> caseExps = new ArrayList<>();

        for (ASTFunDecl funDecl : decls) {
            ASTPatTuple argsTuple = new ASTPatTuple(funDecl.getPats());
            ASTExpression funExp = funDecl.getExp();
            casePats.add(argsTuple);
            caseExps.add(funExp);
        }

        ASTCase cases = new ASTCase(varsTuple, casePats, caseExps);

        // create the lambda and return the resulting pattern
        ASTLambda lambda = new ASTLambda(lambdaVarsPat, cases);
        ASTPatDecl patDecl = new ASTPatDecl(functionName, lambda);
        return patDecl;
    }

    /**
     * Transforms the given case expression to a nested application of the predefined match function.
     * @param caseExpr the case expression
     * @return
     */
    public static ASTExpression caseToMatch(ASTCase caseExpr) {
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
            nestedMatches = matchToExpression(currentPat, exp, currentExp, nestedMatches);
        }

        // we can cast because there is always at least one case (@see ASTCase assertions)
        return nestedMatches;
    }

    /**
     * Applies transformation rules which replace a match application by other expressions. The match application has
     * the following form: match pat exp exp1 exp2
     * @param pat
     * @param exp
     * @param exp1
     * @param exp2
     * @return
     */
    private static ASTExpression matchToExpression(ASTPattern pat, ASTExpression exp, ASTExpression exp1, ASTExpression exp2) {
        if (pat instanceof ASTVariable) {
           return matchVarToExp((ASTVariable) pat, exp, exp1, exp2);
        }
        else if (pat instanceof ASTJoker) {
            return matchJokerToExp((ASTJoker) pat, exp, exp1, exp2);
        }
        else if (pat instanceof ASTConstruct) {
            return matchConstrToExp((ASTConstruct) pat, exp, exp1, exp2);
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
        if (pat instanceof ASTInteger) {
            ASTInteger intPat = (ASTInteger) pat;
            /*
                     match INT exp exp1 exp2
            ----------------------------------------
            if (isa_int_INT exp) then exp1 else exp2
             */
            ASTApplication isaExp = new ASTApplication(VariableManager.getIsaIntFunc(intPat.getValue()), exp);
            return new ASTBranch(isaExp, exp1, exp2);
        }
        else if (pat instanceof ASTBoolean) {
            ASTBoolean boolPat = (ASTBoolean) pat;
            /*
                     match BOOL exp exp1 exp2
            ----------------------------------------
            if (isa_bool_INT exp) then exp1 else exp2
             */
            ASTApplication isaExp = new ASTApplication(VariableManager.getIsaBoolFunc(boolPat.getValue()), exp);
            return new ASTBranch(isaExp, exp1, exp2);
        }
        else {
            // unknown constant, leave it as is
            ASTExpression constant = (ASTExpression) pat;
            return new ASTApplication(VariableManager.getMatchFunc(), constant, exp, exp1, exp2);
        }
    }

    /**
     * Converts a complex haskell expression to a simple haskell expression.
     * @return an equivalent simple haskell expression
     * @throws TooComplexException
     */
    public haskell.simple.ast.ASTExpression reduceToSimple() throws TooComplexException {
        // first we must transform multiple function declarations for the same function to single declarations
        applyFuncDeclToPatDecl();

        // then we apply basic transformation rules as long as possible
        applyBasicTransformationRules();

        // after this, we apply the declarations according to entaglement
        // and then nest them in multiple let expressions
        applyDeclarationSplit();

        // then we again apply basic transformation rules as long as possible
        applyBasicTransformationRules();

        // after this, the complex haskell expression should be in simple haskell form
        return expression.castToSimple();
    }

    private void applyFuncDeclToPatDecl() {
        boolean transformed;

        do {
            transformed = false;
            if (expression.funcDeclToPatDecl()) {
                // the rule was successfully applied
                transformed = true;
            }
        } while(transformed);
    }

    private void applyBasicTransformationRules() {
        boolean transformed;

        // apply rules as long as they still change something
        do {
            transformed = false;

            // we try to apply every rule in succession
            for (int i=0; i < getNumBasicRules(); i++) {
                if (applyRule(i)) {
                    // the rule was successfully applied
                    transformed = true;
                }
            }
        } while(transformed);
    }

    private void applyDeclarationSplit() {
        // TODO: actually split them according to entanglement
        expression.nestMultipleLets();
    }

    private int getNumBasicRules() {
        return 3;
    }

    /**
     * Tries to apply a transformation rule to a complex haskell expression.
     * @param ruleNumber the number of the rule. 0 <= ruleNumber < getNumRules()
     * @return whether a transformation was successfully applied
     */
    private boolean applyRule(int ruleNumber) {
        switch(ruleNumber) {
            case 0:
                return expression.nestMultipleLambdas();
            case 1:
                return expression.lambdaPatternToCase();
            case 2:
                return expression.caseToMatch();
            default:
                return false;
        }
    }
}
