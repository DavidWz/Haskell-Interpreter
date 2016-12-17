package lambda.ast;

/**
 * Interface for lambda visitors.
 */
public interface LambdaVisitor<T> {
    T visit(ASTAbstraction node);
    T visit(ASTApplication node);
    T visit(ASTConstant node);
    T visit(ASTVariable node);
}
