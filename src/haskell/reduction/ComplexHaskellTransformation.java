package haskell.reduction;

import haskell.ast.*;

/**
 * Interface for visitors which transform complex haskell expressions.
 * Transformations are done in-place, i.e. they don't return a result node but just change the current visited node.
 * Every visit method returns whether the visitor was successful in doing what it was supposed to do for that node.
 * The default behavior for visiting a node is that the visit method is simply called for every sub-expression in that node.
 */
public interface ComplexHaskellTransformation extends ComplexHaskellVisitor<Boolean> {
    default Boolean visit(ASTApplication node) {
        for (ASTExpression exp : node.getExps()) {
            if (exp.accept(this)) {
                return true;
            }
        }
        return false;
    }

    default Boolean visit(ASTBoolean node) {
        return false;
    }

    default Boolean visit(ASTBranch node) {
        if (node.getCondition().accept(this)) {
            return true;
        }
        if (node.getIfBranch().accept(this)) {
            return true;
        }
        return node.getElseBranch().accept(this);
    }

    default Boolean visit(ASTCase node) {
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

    default Boolean visit(ASTChar node) {
        return false;
    }

    default Boolean visit(ASTConstruct node) {
        return false;
    }

    default Boolean visit(ASTExpTuple node) {
        for (ASTExpression exp : node.getExps()) {
            if (exp.accept(this)) {
                return true;
            }
        }
        return false;
    }

    default Boolean visit(ASTFloat node) {
        return false;
    }

    default Boolean visit(ASTFunDecl node) {
        return node.getExp().accept(this);
    }

    default Boolean visit(ASTInteger node) {
        return false;
    }

    default Boolean visit(ASTJoker node) {
        return false;
    }

    default Boolean visit(ASTLambda node) {
        return node.getExp().accept(this);
    }

    default Boolean visit(ASTLet node) {
        for (ASTDecl decl : node.getDecls()) {
            if (decl.accept(this)) {
                return true;
            }
        }
        return node.getExp().accept(this);
    }

    default Boolean visit(ASTPatDecl node) {
        return node.getExp().accept(this);
    }

    default Boolean visit(ASTPatTuple node) {
        return false;
    }

    default Boolean visit(ASTProgram node) {
        for(ASTDecl decl : node.getDecls()) {
            if (decl.accept(this)) {
                return true;
            }
        }
        return false;
    }

    default Boolean visit(ASTTyConstr node) {
        return false;
    }

    default Boolean visit(ASTVariable node) {
        return false;
    }

    default Boolean visit(ASTDataDecl node) {
        return false;
    }

    default Boolean visit(ASTConstrDecl node) {
        return false;
    }

    default Boolean visit(ASTTypeConstr node) {
        return false;
    }

    default Boolean visit(ASTFuncType node) {
        return false;
    }

    default Boolean visit(ASTTupleType node) {
        return false;
    }
}
