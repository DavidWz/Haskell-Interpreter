package haskell.ast;

/**
 * Interface for complex haskell visitors.
 */
public interface ComplexHaskellVisitor<T> {
    T visit(ASTApplication node);

    T visit(ASTBoolean node);

    T visit(ASTBranch node);

    T visit(ASTCase node);

    T visit(ASTChar node);

    T visit(ASTConstruct node);

    T visit(ASTExpTuple node);

    T visit(ASTFloat node);

    T visit(ASTFunDecl node);

    T visit(ASTInteger node);

    T visit(ASTJoker node);

    T visit(ASTLambda node);

    T visit(ASTLet node);

    T visit(ASTPatDecl node);

    T visit(ASTPatTuple node);

    T visit(ASTProgram node);

    T visit(ASTTyConstr node);

    T visit(ASTVariable node);

    T visit(ASTDataDecl node);

    T visit(ASTConstrDecl node);

    T visit(ASTTypeConstr node);

    T visit(ASTFuncType node);

    T visit(ASTTupleType node);
}
