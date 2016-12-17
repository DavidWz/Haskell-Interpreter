package lambda.ast;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents an application of a left to an right.
 */
public class ASTApplication extends ASTTerm {
    private ASTTerm left;
    private ASTTerm right;

    public ASTApplication(ASTTerm left, ASTTerm right) {
        assert(left != null);
        assert(right != null);

        this.left = left;
        this.right = right;
    }

    public ASTTerm getLeft() {

        return left;
    }

    public void setLeft(ASTTerm left) {
        assert(left != null);
        this.left = left;
    }

    public ASTTerm getRight() {
        return right;
    }

    public void setRight(ASTTerm right) {
        assert(right != null);
        this.right = right;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ASTApplication that = (ASTApplication) o;

        if (!getLeft().equals(that.getLeft())) return false;
        return getRight().equals(that.getRight());

    }

    @Override
    public int hashCode() {
        int result = getLeft().hashCode();
        result = 31 * result + getRight().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "(" + left.toString() + " " + right.toString() + ")";
    }

    @Override
    public List<ASTTerm> getLMOMArguments() {
        List<ASTTerm> args = left.getLMOMArguments();
        args.add(right);
        return args;
    }

    @Override
    public ASTTerm getLMOMTerm() {
        return left.getLMOMTerm();
    }

    @Override
    public Set<ASTVariable> getFreeVars() {
        // we just combine the free variables of the left and right
        HashSet<ASTVariable> freeVars = new HashSet<>();
        freeVars.addAll(left.getFreeVars());
        freeVars.addAll(right.getFreeVars());
        return freeVars;
    }

    @Override
    public ASTTerm substitute(ASTVariable var, ASTTerm expr) {
        // substituting an application is equivalent to substituting the left and right
        ASTTerm replacedFunciton = left.substitute(var, expr);
        ASTTerm replacedArgument = right.substitute(var, expr);
        return new ASTApplication(replacedFunciton, replacedArgument);
    }

    @Override
    public <T> T accept(LambdaVisitor<T> visitor) {
        return visitor.visit(this);
    }

}
