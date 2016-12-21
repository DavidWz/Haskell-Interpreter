package haskell.reduction;

import haskell.ast.ASTExpression;
import haskell.ast.ASTLambda;
import haskell.ast.ASTPattern;

import java.util.List;

/**
 * Transforms a lambda expression with multiple arguments to nested lambda terms with only one argument each.
 */
public class NestMultipleLambdas implements ComplexHaskellTransformation {
    @Override
    public Boolean visit(ASTLambda node) {
        // first try to apply the transformation as deep as possible
        if (node.getExp().accept(this)) {
            return true;
        }

        // we can only apply the transformation if there are multiple arguments
        List<ASTPattern> pats = node.getPats();
        if (pats.size() >= 2) {
            ASTExpression nestedLambdas = node.getExp();

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
            node.setExp(nestedLambdas);
            return true;
        }
        else {
            return false;
        }
    }
}
