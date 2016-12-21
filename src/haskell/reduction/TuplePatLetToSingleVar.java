package haskell.reduction;


import haskell.ast.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Transforms a let expresison with a tuple pattern to a let expression with a single variable as pattern.
 */
public class TuplePatLetToSingleVar implements ComplexHaskellTransformation {
    @Override
    public Boolean visit(ASTLet node) {
        List<ASTDecl> decls = node.getDecls();
        ASTExpression exp = node.getExp();

        // first, try to apply this transformation as deep as possible
        for (ASTDecl decl : decls) {
            if (decl.accept(this)) {
                return true;
            }
        }
        if (exp.accept(this)) {
            return true;
        }

        // we apply the following transformation:
        /*
                            let (var1, ..., varn) = exp in exp'
        --------------------------------------------------------------------------
        let var = match (var1, ..., varn) (sel_n,1 var, ..., sel_n,n var) exp  bot
               in match (var1, ..., varn) (sel_n,1 var, ..., sel_n,n var) exp' bot
         */
        // it only works if this let has only one pattern declaration though
        if (decls.size() != 1) {
            return false;
        }
        if (!(decls.get(0) instanceof ASTPatDecl)) {
            return false;
        }
        ASTPatDecl patDecl = (ASTPatDecl) decls.get(0);
        if (!(patDecl.getPat() instanceof ASTPatTuple)) {
            return false;
        }
        ASTPatTuple varTuple = (ASTPatTuple) patDecl.getPat();
        // lastly, check if the variable tuple actually contains only variables
        for (ASTPattern var : varTuple.getPats()) {
            if (!(var instanceof ASTVariable)) {
                return false;
            }
        }
        ASTVariable var = VariableManager.getFreshVariable();

        List<ASTExpression> sels = new ArrayList<>();
        int n = varTuple.getPats().size();
        for (int i = 1; i <= n; i++) {
            sels.add(new ASTApplication(VariableManager.getSelFunc(n, i), var));
        }
        ASTExpTuple selTuple = new ASTExpTuple(sels);

        ASTExpression matchExpBot = MatchToExpression.matchToExpression(
                varTuple,
                selTuple,
                patDecl.getExp(),
                VariableManager.getBot());

        ASTExpression matchExpPrimeBot = MatchToExpression.matchToExpression(
                varTuple,
                selTuple,
                exp,
                VariableManager.getBot());

        ASTPatDecl newDecl = new ASTPatDecl(var, matchExpBot);
        ASTExpression newExp = matchExpPrimeBot;

        node.setDecls(Collections.singletonList(newDecl));
        node.setExp(newExp);
        return true;
    }
}
