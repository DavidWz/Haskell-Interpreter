package haskell.complex.ast;

import haskell.complex.reduction.SimpleReducer;
import haskell.complex.reduction.VariableManager;

import java.util.*;

/**
 * Represents an anonymous function.
 */
public class ASTLambda implements ASTExpression {
    private List<ASTPattern> pats;
    private ASTExpression exp;

    public ASTLambda(List<ASTPattern> pats, ASTExpression exp) {
        assert(pats != null);
        assert(exp != null);
        assert(pats.size() >= 1);
        this.pats = pats;
        this.exp = exp;
    }

    public ASTLambda(ASTPattern pat, ASTExpression exp) {
        assert(pat != null);
        assert(exp != null);
        this.pats = Collections.singletonList(pat);
        this.exp = exp;
    }

    public List<ASTPattern> getPats() {
        return pats;
    }

    public ASTExpression getExp() {
        return exp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ASTLambda astLambda = (ASTLambda) o;

        if (!getPats().equals(astLambda.getPats())) return false;
        return getExp().equals(astLambda.getExp());

    }

    @Override
    public int hashCode() {
        int result = getPats().hashCode();
        result = 31 * result + getExp().hashCode();
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("(\\");

        for (ASTPattern pat : pats) {
            builder.append(pat);
            builder.append(" ");
        }
        builder.append("-> ");
        builder.append(exp);
        builder.append(")");

        return builder.toString();
    }

    @Override
    public Set<ASTVariable> getAllVariables() {
        Set<ASTVariable> vars = new HashSet<>();
        for (ASTPattern pat : pats) {
            vars.addAll(pat.getAllVariables());
        }
        vars.addAll(exp.getAllVariables());
        return vars;
    }

    @Override
    public Set<ASTVariable> getFreeVars() {
        Set<ASTVariable> vars = new HashSet<>();
        vars.addAll(exp.getFreeVars());
        for (ASTPattern pat : pats) {
            vars.removeAll(pat.getAllVariables());
        }
        return vars;
    }

    @Override
    public boolean nestMultipleLambdas() {
        // first try to apply the transformation as deep as possible
        if (exp.nestMultipleLambdas()) {
            return true;
        }

        // we can only apply the transformation if there are multiple arguments
        if (pats.size() >= 2) {
            ASTExpression nestedLambdas = exp;

            // The transformation looks like this:
            /*
                    \pat1 pat2 ... patn -> exp
            ----------------------------------------------
            \pat1 -> (\pat2 -> (... -> (\patn -> exp)...))
             */

            while (pats.size() >= 2) {
                // take the last pattern and remove it from the argument list
                ASTPattern currentPat = pats.remove(pats.size()-1);

                // create a new lambda term with only that pattern and the current expression
                nestedLambdas = new ASTLambda(currentPat, nestedLambdas);
            }

            // finally, there is only one argument left in this lambda term
            // so replace this lambda term's expression by the nested lambdas
            exp = nestedLambdas;
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public boolean lambdaPatternToCase() {
        // first try to apply the transformation as deep as possible
        if (exp.nestMultipleLambdas()) {
            return true;
        }

        // we can only apply the transformation if there is only one argument
        if (pats.size() == 1) {
            ASTPattern pat = pats.get(0);

            // the transformation looks like this
            /*
                     \pat -> exp
            ----------------------------------
            \var -> case var of { pat -> exp }
             */

            // and the pattern must not be a variable
            if (!(pat instanceof ASTVariable)) {
                // then we can replace that pattern by a variable
                ASTVariable var = VariableManager.getFreshVariable();
                ASTCase caseExp = new ASTCase(var, Collections.singletonList(pat), Collections.singletonList(exp));

                pats = Collections.singletonList(var);
                exp = caseExp;
                return true;
            }
            else {
                return false;
            }
        }
        else {
            return false;
        }
    }

    @Override
    public boolean caseToMatch() {
        if (exp.caseToMatch()) {
            return true;
        }

        if (exp instanceof ASTCase) {
            ASTCase caseExp = (ASTCase) exp;
            exp = SimpleReducer.caseToMatch(caseExp);
            return true;
        }
        return false;
    }

    @Override
    public boolean nestMultipleLets() {
        return exp.nestMultipleLets();
    }

    @Override
    public boolean tuplePatLetToSingleVar() {
        return exp.tuplePatLetToSingleVar();
    }

    @Override
    public haskell.simple.ast.ASTExpression castToSimple() throws SimpleReducer.TooComplexException {
        if (pats.size() == 1) {
            ASTPattern pat = pats.get(0);

            if (pat instanceof ASTVariable) {
                ASTVariable var = (ASTVariable) pat;

                return new haskell.simple.ast.ASTFunction((haskell.simple.ast.ASTVariable) var.castToSimple(), exp.castToSimple());
            }
            else {
                throw new SimpleReducer.TooComplexException(this, "Lambdas must map a variable.");
            }
        }
        else {
            throw new SimpleReducer.TooComplexException(this, "Lambdas must only map one variable.");
        }
    }
}
