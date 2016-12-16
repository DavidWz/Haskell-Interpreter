package haskell.complex.reduction;

import haskell.complex.ast.*;

/**
 * Interface for visitors which transform complex haskell expressions.
 * Transformations are done in-place, i.e. they don't return a result node but just change the current visited node.
 * Every visit method returns whether the visitor was successful in doing what it was supposed to do for that node.
 * The default behavior for visiting a node is that the visit method is simply called for every sub-expression in that node.
 */
public interface ComplexHaskellTransformation {
    default boolean visit(ASTApplication node) {
        for (ASTExpression exp : node.getExps()) {
            if (exp.accept(this)) {
                return true;
            }
        }
        return false;
    }

    default boolean visit(ASTBoolean node) {
        return false;
    }

    default boolean visit(ASTBranch node) {
        if (node.getCondition().accept(this)) {
            return true;
        }
        if (node.getIfBranch().accept(this)) {
            return true;
        }
        return node.getElseBranch().accept(this);
    }

    default boolean visit(ASTCase node) {
        if (node.getExp().accept(this)) {
            return true;
        }
        for (ASTExpression exp : node.getCaseExps()) {
            if (exp.accept(this)) {
                return true;
            }
        }
        return false;
    }

    default boolean visit(ASTChar node) {
        return false;
    }

    default boolean visit(ASTConstruct node) {
        return false;
    }

    default boolean visit(ASTExpTuple node) {
        for (ASTExpression exp : node.getExps()) {
            if (exp.accept(this)) {
                return true;
            }
        }
        return false;
    }

    default boolean visit(ASTFloat node) {
        return false;
    }

    default boolean visit(ASTFunDecl node) {
        return node.getExp().accept(this);
    }

    default boolean visit(ASTInteger node) {
        return false;
    }

    default boolean visit(ASTJoker node) {
        return false;
    }

    default boolean visit(ASTLambda node) {
        return node.getExp().accept(this);
    }

    default boolean visit(ASTLet node) {
        for (ASTDecl decl : node.getDecls()) {
            if (decl.accept(this)) {
                return true;
            }
        }
        return node.getExp().accept(this);
    }

    default boolean visit(ASTPatDecl node) {
        return node.getExp().accept(this);
    }

    default boolean visit(ASTPatTuple node) {
        return false;
    }

    default boolean visit(ASTProgram node) {
        for(ASTDecl decl : node.getDecls()) {
            if (decl.accept(this)) {
                return true;
            }
        }
        return false;
    }

    default boolean visit(ASTTyConstr node) {
        return false;
    }

    default boolean visit(ASTVariable node) {
        return false;
    }

    default boolean visit(ASTDataDecl node) {
        return false;
    }

    default boolean visit(ASTConstrDecl node) {
        return false;
    }

    default boolean visit(ASTTypeConstr node) {
        return false;
    }

    default boolean accept(ASTFuncType node) {
        return false;
    }

    default boolean visit(ASTTupleType node) {
        return false;
    }
}
