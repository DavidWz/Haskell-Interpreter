package haskell.complex.reduction;

import haskell.complex.ast.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A class which can reduce complex haskell to simple haskell.
 */
public class SimpleReducer {
    public static class TooComplexException extends Exception {
        private ASTExpression exp;

        public TooComplexException(ASTExpression exp) {
            this.exp = exp;
        }

        @Override
        public String getMessage() {
            StringBuilder builder = new StringBuilder();
            builder.append("The complex haskell expression cannot be fully reduced to a simple haskell expression.\n");
            builder.append("The expression was: \n");
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

    public int getNumRules() {
        return 1;
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

        // now we want to transformed all declaration for one specific function
        if (funDecls.size() > 0) {
            // we pick the first function we encounter
            ASTVariable targetFunction = funDecls.get(0).getVar();

            // then we collect all declarations for that function
            List<ASTFunDecl> targetFunDecls = funDecls.stream().
                    filter(decl -> decl.getVar().equals(targetFunction)).
                    collect(Collectors.toList());
            otherDecls.addAll(funDecls.stream().
                    filter(decl -> !decl.getVar().equals(targetFunction)).
                    collect(Collectors.toList()));

            // now we can actually apply a transformation to those function
            ASTPatDecl patDecl = SimpleReducer.getPatDeclForFunctionDecls(targetFunDecls);

            // and then we recombine the results
            otherDecls.add(patDecl);
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
     * Converts a complex haskell expression to a simple haskell expression.
     * @return an equivalent simple haskell expression
     * @throws TooComplexException
     */
    public haskell.simple.ast.ASTExpression reduceToSimple() throws TooComplexException {
        boolean transformed;

        // apply rules as long as they still change something
        do {
            {
                // DEBUG
                System.out.println("\nCurrent simple haskell program:");
                System.out.println(expression);
            }
            transformed = false;

            // we try to apply every rule in succession
            for (int i=0; i < getNumRules(); i++) {
                if (applyRule(i)) {
                    // the rule was successfully applied
                    transformed = true;
                }
            }
        } while(transformed);

        return castToSimple(expression);
    }

    /**
     * Tries to apply a transformation rule to a complex haskell expression.
     * @param ruleNumber the number of the rule. 0 <= ruleNumber < getNumRules()
     * @return whether a transformation was successfully applied
     */
    private boolean applyRule(int ruleNumber) {
        switch(ruleNumber) {
            case 0:
                return expression.funcDeclToPatDecl();
            // TODO: implement other transformation rules
            default:
                return false;
        }
    }

    /**
     * Casts a complex haskell expression to a simple haskell expression. The complex haskell expression must already be
     * in a simple haskell format, otherwise a TooComplexException is thrown.
     * @param exp the complex haskell expression
     * @return the equivalent simple haskell expression
     * @throws TooComplexException
     */
    private haskell.simple.ast.ASTExpression castToSimple(ASTExpression exp) throws TooComplexException {
        throw new TooComplexException(exp);
    }
}
