package haskell.reduction;

import haskell.ast.*;

import java.util.Collections;
import java.util.List;

/**
 * Transforms a lambda expression with a pattern which is not a variable to a case expression.
 */
public class LambdaPatternToCase implements ComplexHaskellTransformation {
    @Override
    public Boolean visit(ASTLambda node) {
        // first try to apply the transformation as deep as possible
        ASTExpression exp = node.getExp();
        if (exp.accept(this)) {
            return true;
        }

        // we can only apply the transformation if there is only one argument
        List<ASTPattern> pats = node.getPats();
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

                node.setPats(Collections.singletonList(var));
                node.setExp(caseExp);
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
}
