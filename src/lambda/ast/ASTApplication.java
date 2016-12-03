package lambda.ast;

import lambda.reduction.delta.DeltaRule;

import java.lang.reflect.Array;
import java.util.*;

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
    protected Optional<ASTTerm> applyBetaReduction() {
        if (left instanceof ASTAbstraction) {
            // we can apply a beta reduction step
            ASTAbstraction abstraction = (ASTAbstraction) left;

            // apply the beta reduction as follows: (lambda x . t) r -> t [x / r]
            return Optional.of(abstraction.getOutput().substitute(abstraction.getInput(), right));
        }
        else {
            return Optional.empty();
        }
    }

    @Override
    protected Optional<ASTTerm> applyDeltaReduction(DeltaRule delta) {
        List<ASTTerm> arguments = getLMOMArguments();
        ASTTerm lmomTerm = getLMOMTerm();
        if (lmomTerm instanceof ASTConstant && arguments.size() == delta.getNumberOfArguments()) {
            // this is a constant with the right number of arguments, so try to apply the delta rule
            Optional<ASTTerm> result = delta.getRHS((ASTConstant) lmomTerm, arguments);
            if (result.isPresent()) {
                return result;
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<ASTTerm> applyWHNOReduction(List<DeltaRule> deltaRules) {
        // first, try to do a beta reduction
        Optional<ASTTerm> reduced = applyBetaReduction();
        if(reduced.isPresent()) {
            return reduced;
        }
        else {
            // if not possible, try to do a delta reduction
            for (DeltaRule delta : deltaRules) {
                reduced = applyDeltaReduction(delta);
                if (reduced.isPresent()) {
                    return reduced;
                }
            }
        }

        // try to apply the reduction in a left-most inner-most fashion
        Optional<ASTTerm> result = left.applyWHNOReduction(deltaRules);
        if (result.isPresent()) {
            return Optional.of(new ASTApplication(result.get(), right));
        }
        else {
            result = right.applyWHNOReduction(deltaRules);
            if (result.isPresent()) {
                return Optional.of(new ASTApplication(left, result.get()));
            }
            else {
                return Optional.empty();
            }
        }
    }

    @Override
    protected List<ASTTerm> getLMOMArguments() {
        List<ASTTerm> args = left.getLMOMArguments();
        args.add(right);
        return args;
    }

    @Override
    protected ASTTerm getLMOMTerm() {
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

}
