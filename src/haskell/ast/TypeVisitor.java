package haskell.ast;

/**
 * Visitor for ASTTypes.
 */
public interface TypeVisitor<T> {
    T visit(ASTVariable node);

    T visit(ASTTupleType node);

    T visit(ASTFuncType node);

    T visit(ASTTypeConstr node);
}
