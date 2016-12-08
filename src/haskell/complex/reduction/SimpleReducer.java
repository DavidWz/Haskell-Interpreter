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
    private VariableManager variableManager;

    public SimpleReducer(ASTExpression expression) {
        assert(expression != null);
        this.expression = expression;
        this.variableManager = new VariableManager(expression);
    }

    public int getNumRules() {
        return 1;
    }

    /**
     * Transforms a series of function declarations for the same function into a single patter declaration.
     * @param decls
     * @return
     */
    private ASTPatDecl funcDeclToPatDecl(List<ASTFunDecl> decls) {
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
        List<ASTPattern> lambdaVars = new ArrayList<>();
        for (int i = 0; i < numArgs; i++) {
            lambdaVars.add(variableManager.getFreshVariable());
        }

        // create the case expression
        // TODO

        ASTLambda lambda = new ASTLambda(lambdaVars, new ASTVariable("todo"));

        ASTPatDecl patDecl = new ASTPatDecl(functionName, lambda);
        return patDecl;
    }

    /**
     * Transforms a series of function declarations inside the top-level let expression
     * for one specific function into an equivalent pattern declaration.
     * @param exp
     * @return
     */
    private Optional<ASTExpression> funcDeclToPatDecl(ASTExpression exp) {
        if (exp instanceof ASTLet) {
            ASTLet let = (ASTLet) exp;
            List<ASTDecl> decls = let.getDecls();

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
                ASTPatDecl patDecl = funcDeclToPatDecl(targetFunDecls);

                // and then we recombine the results
                otherDecls.add(patDecl);
                let.setDecls(otherDecls);
                return Optional.of(let);
            }
            else {
                return Optional.empty();
            }
        }
        else {
            return Optional.empty();
        }
    }

    /**
     * Converts a complex haskell expression to a simple haskell expression.
     * @return an equivalent simple haskell expression
     * @throws TooComplexException
     */
    public haskell.simple.ast.ASTExpression reduceToSimple() throws TooComplexException {
        boolean transformed;

        Optional<ASTExpression> transformedExp;
        // apply rules as long as they still change something
        do {
            {
                // DEBUG
                System.out.println("Current simple haskell program: \n");
                System.out.println(expression);
            }
            transformed = false;

            // we try to apply every rule in succession
            for (int i=0; i < getNumRules(); i++) {
                transformedExp = applyRule(i);
                if (transformedExp.isPresent()) {
                    // the rule was successfully applied
                    transformed = true;
                    expression = transformedExp.get();
                }
            }
        } while(transformed);

        return castToSimple(expression);
    }

    /**
     * Tries to apply a transformation rule to a complex haskell expression.
     * @param ruleNumber the number of the rule. 0 <= ruleNumber < getNumRules()
     * @return An optional containing the transformed expression or empty if the rule was not applicable
     */
    private Optional<ASTExpression> applyRule(int ruleNumber) {
        switch(ruleNumber) {
            case 0:
                return funcDeclToPatDecl(expression);
            default:
                return Optional.empty();
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
