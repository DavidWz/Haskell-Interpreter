package haskell.reduction;

import haskell.ast.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Transforms a series of function declarations for one specific function
 * into an equivalent pattern declaration.
 */
public class FunDeclToPatDecl implements ComplexHaskellTransformation {

    public FunDeclToPatDecl() {
    }

    /**
     * Transforms function declarations for one specific function into a pattern declaration.
     * @param decls a list of declarations
     * @return an optional containing a new list of declarations where one function was replaced by a pattern declaration.
     *         if the rule could not be applied, an empty optional is returned.
     */
    public Optional<List<ASTDecl>> funcDeclToPatDecl(List<ASTDecl> decls) {
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
            ASTPatDecl patDecl = getPatDeclForFunctionDecls(targetFunDecls);

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
    private ASTPatDecl getPatDeclForFunctionDecls(List<ASTFunDecl> decls) {
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

    @Override
    public Boolean visit(ASTLet node) {
        // first, try to apply this transformation as deep as possible
        for (ASTDecl decl : node.getDecls()) {
            if (decl.accept(this)) {
                return true;
            }
        }
        if (node.getExp().accept(this)) {
            return true;
        }

        // try to apply the transformation to the declarations stored in this let-term
        Optional<List<ASTDecl>> transformedDecls = funcDeclToPatDecl(node.getDecls());
        if (transformedDecls.isPresent()) {
            node.setDecls(transformedDecls.get());
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public Boolean visit(ASTProgram node) {
        // first, we try to apply the transformation as deep as possible
        for (ASTDecl decl : node.getDecls()) {
            if (decl.accept(this)) {
                return true;
            }
        }

        // try to apply the transformation to the declarations stored in this program-term
        Optional<List<ASTDecl>> transformedDecls = funcDeclToPatDecl(node.getDecls());
        if (transformedDecls.isPresent()) {
            node.setDecls(transformedDecls.get());
            return true;
        }
        else {
            return false;
        }
    }
}
