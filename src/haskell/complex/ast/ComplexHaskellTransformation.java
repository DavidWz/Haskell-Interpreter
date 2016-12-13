package haskell.complex.ast;

/**
 * Interface for visitors which transform complex haskell expressions.
 * Transformations are done in-place, i.e. they don't return a result node but just change the current visited node.
 * Every visit method returns whether the visitor was successful in doing what it was supposed to do for that node.
 * The default behavior for visiting a node is that the visit method is simply called for every sub-expression in that node.
 */
public interface ComplexHaskellTransformation {
    default boolean visit(ASTApplication node) {
        for (ASTExpression exp : node.getExps()) {
            if (visit(exp)) {
                return true;
            }
        }
        return false;
    }

    default boolean visit(ASTBoolean node) {
        return false;
    }

    default boolean visit(ASTBranch node) {
        if (visit(node.getCondition())) {
            return true;
        }
        if (visit(node.getIfBranch())) {
            return true;
        }
        return visit(node.getElseBranch());
    }

    default boolean visit(ASTCase node) {
        if (visit(node.getExp())) {
            return true;
        }
        for (ASTExpression exp : node.getCaseExps()) {
            if (visit(exp)) {
                return true;
            }
        }
        return false;
    }

    default boolean visit(ASTConstruct node) {
        return false;
    }

    default boolean visit(ASTExpTuple node) {
        for (ASTExpression exp : node.getExps()) {
            if (visit(exp)) {
                return true;
            }
        }
        return false;
    }

    default boolean visit(ASTFunDecl node) {
        return visit(node.getExp());
    }

    default boolean visit(ASTInteger node) {
        return false;
    }

    default boolean visit(ASTJoker node) {
        return false;
    }

    default boolean visit(ASTLambda node) {
        return visit(node.getExp());
    }

    default boolean visit(ASTLet node) {
        for (ASTDecl decl : node.getDecls()) {
            if (visit(decl)) {
                return true;
            }
        }
        return visit(node.getExp());
    }

    default boolean visit(ASTPatDecl node) {
        return visit(node.getExp());
    }

    default boolean visit(ASTPatTuple node) {
        return false;
    }

    default boolean visit(ASTProgram node) {
        for(ASTDecl decl : node.getDecls()) {
            if (visit(decl)) {
                return true;
            }
        }
        return false;
    }

    default boolean visit(ASTTypeConstr node) {
        return false;
    }

    default boolean visit(ASTVariable node) {
        return false;
    }

    default boolean visit(ASTDecl node) {
        if (node instanceof ASTFunDecl) {
            return visit((ASTFunDecl) node);
        }
        else if (node instanceof ASTPatDecl) {
            return visit((ASTPatDecl) node);
        }
        else {
            assert(false);
            return false;
        }
    }

    default boolean visit(ASTExpression node) {
        if (node instanceof ASTApplication) {
            return visit((ASTApplication) node);
        }
        else if (node instanceof ASTBoolean) {
            return visit((ASTBoolean) node);
        }
        else if (node instanceof ASTBranch) {
            return visit((ASTBranch) node);
        }
        else if (node instanceof ASTCase) {
            return visit((ASTCase) node);
        }
        else if (node instanceof ASTExpTuple) {
            return visit((ASTExpTuple) node);
        }
        else if (node instanceof ASTInteger) {
            return visit((ASTInteger) node);
        }
        else if (node instanceof ASTLambda) {
            return visit((ASTLambda) node);
        }
        else if (node instanceof ASTLet) {
            return visit((ASTLet) node);
        }
        else if (node instanceof ASTTypeConstr) {
            return visit((ASTTypeConstr) node);
        }
        else if (node instanceof ASTVariable) {
            return visit((ASTVariable) node);
        }
        else {
            assert(false);
            return false;
        }
    }

    default boolean visit(ASTPattern node) {
        if (node instanceof ASTBoolean) {
            return visit((ASTBoolean) node);
        }
        else if (node instanceof ASTConstruct) {
            return visit((ASTConstruct) node);
        }
        else if (node instanceof ASTInteger) {
            return visit((ASTInteger) node);
        }
        else if (node instanceof ASTJoker) {
            return visit((ASTJoker) node);
        }
        else if (node instanceof ASTPatTuple) {
            return visit((ASTPatTuple) node);
        }
        else if (node instanceof ASTTypeConstr) {
            return visit((ASTTypeConstr) node);
        }
        else if (node instanceof ASTVariable) {
            return visit((ASTVariable) node);
        }
        else {
            assert(false);
            return false;
        }
    }
}
