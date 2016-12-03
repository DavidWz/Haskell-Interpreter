package haskell.simple.ast;

/**
 * Represents a simple haskell expression.
 */
public abstract class ASTExpression {
    /**
     * Transforms this simple haskell program into an equivalent lambda term.
     * @return a lambda term
     */
    public abstract lambda.ast.ASTTerm toLambdaTerm();
}
