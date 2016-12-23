package haskell.reduction;

import haskell.ast.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Transforms a let term with several declarations to nested let-terms with one declaration each.
 * This transformations also handles the separation of the declarations with entangled functions.
 */
public class SeparateAndNestDecls implements ComplexHaskellTransformation {
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

        // perform the transformation
        List<ASTDecl> decls = node.getDecls();
        ASTExpression exp = node.getExp();

        // we separate pattern declarations from other types of declarations
        List<ASTDecl> nonPatDecls = new ArrayList<>();
        List<ASTPatDecl> patDecls = new ArrayList<>();
        for (ASTDecl decl : decls) {
            if (decl instanceof ASTPatDecl) {
                patDecls.add((ASTPatDecl) decl);
            }
            else {
                nonPatDecls.add(decl);
            }
        }

        // first, we must calculate a separation of pattern declarations so that entangled functions are grouped together
        List<List<ASTPatDecl>> separation = getSeparation(patDecls);

        // then we transform groups of entangled declarations to single declarations
        for (List<ASTPatDecl> group : separation) {
            nonPatDecls.add(fuseEntangledFunctions(group));
        }
        decls = nonPatDecls;
        node.setDecls(decls);

        // we construct the nested let terms as follows:
        /*
                 let { decl1; ...; decln } in exp
        -------------------------------------------------------
        let decl1 in (let decl2 in (... (let decln in exp)...))
         */
        if (decls.size() > 1) {
            ASTExpression nestedLets = exp;
            while (decls.size() > 1) {
                ASTDecl currentDecl = decls.remove(decls.size()-1);
                nestedLets = new ASTLet(Collections.singletonList(currentDecl), nestedLets);
            }
            node.setExp(nestedLets);
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Transforms a group of entangled functions into one single declarations
     * @param decls
     * @return
     */
    private ASTPatDecl fuseEntangledFunctions(List<ASTPatDecl> decls) {
        if (decls.size() < 2) {
            return decls.get(0);
        }
        else {
            // we apply the following transformation to fuse the group together
            /*
              var1 = exp1; ...; varn = expn
            ---------------------------------
            (var1,...,varn) = (exp1,...,expn)
             */
            List<ASTPattern> vars = new ArrayList<>();
            List<ASTExpression> exps = new ArrayList<>();
            for (ASTPatDecl decl : decls) {
                vars.add(decl.getPat());
                exps.add(decl.getExp());
            }

            ASTPatTuple varTuple = new ASTPatTuple(vars);
            ASTExpTuple expTuple = new ASTExpTuple(exps);

            ASTPatDecl fusedGroup = new ASTPatDecl(varTuple, expTuple);
            return fusedGroup;
        }
    }

    /**
     * Calculates a separation of the given list of declarations, so that entangled declarations are grouped together.
     * @param decls
     * @return
     */
    private List<List<ASTPatDecl>> getSeparation(List<ASTPatDecl> decls) {
        // we set up the adjacency matrix representing direct dependence
        int n = decls.size();
        int[][] adjacencyMatrix = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                ASTPatDecl decl1 = decls.get(i);
                ASTPatDecl decl2 = decls.get(j);
                boolean isDepended = isDirectlyDependent(decl1, decl2);
                adjacencyMatrix[i][j] = isDepended ? 1 : 0;
            }
        }

        // we calculate the transitive closure so that we know which functions are [indirectly] dependent of each other
        int[][] transitiveClosure = GraphUtil.getTransitiveClosure(adjacencyMatrix);

        // we need to put those functions into a group which are a clique in the transitive closure
        // because they are all mutually dependent of each other, i.e. they are entangled
        List<Set<Integer>> cliques = GraphUtil.getMaximalCliques(transitiveClosure);

        // afterwards, we need to topologically sort the groups so that functions in a group only refer to
        // function inside the group or earlier defined function, but not function declared afterwards
        cliques = GraphUtil.topologicallySortCliques(cliques, transitiveClosure);
        Collections.reverse(cliques);

        // finally, we convert the indices back to ast nodes
        List<List<ASTPatDecl>> separation = new ArrayList<>();
        for (Set<Integer> clique : cliques) {
            List<ASTPatDecl> group = new ArrayList<>();
            for (int node : clique) {
                group.add(decls.get(node));
            }
            separation.add(group);
        }

        return separation;
    }

    /**
     * Checks whether a pattern declaration is directly dependent of another pattern declaration.
     * That is, it will not check transitivity.
     * @param decl1
     * @param decl2
     * @return
     */
    private boolean isDirectlyDependent(ASTPatDecl decl1, ASTPatDecl decl2) {
        ASTPattern var1 = decl1.getPat();
        ASTPattern var2 = decl2.getPat();

        if (var1.equals(var2)) {
            // if they are the same they are obviously entangled
            return true;
        }
        else {
            // check if var2 appears as a free variable in exp1
            Set<ASTVariable> freeVars = decl1.getExp().getFreeVars();
            return freeVars.contains(var2);
        }
    }
}
