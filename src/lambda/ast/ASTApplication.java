package lambda.ast;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents an application of a function to an argument.
 */
public class ASTApplication extends ASTTerm {
    private ASTTerm function;
    private ASTTerm argument;

    public ASTApplication(ASTTerm function, ASTTerm argument) {
        assert(function != null);
        assert(argument != null);

        this.function = function;
        this.argument = argument;
    }

    public ASTTerm getFunction() {

        return function;
    }

    public void setFunction(ASTTerm function) {
        assert(function != null);
        this.function = function;
    }

    public ASTTerm getArgument() {
        return argument;
    }

    public void setArgument(ASTTerm argument) {
        assert(argument != null);
        this.argument = argument;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ASTApplication that = (ASTApplication) o;

        if (!getFunction().equals(that.getFunction())) return false;
        return getArgument().equals(that.getArgument());

    }

    @Override
    public int hashCode() {
        int result = getFunction().hashCode();
        result = 31 * result + getArgument().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "(" + function.toString() + " " + argument.toString() + ")";
    }

    @Override
    public Set<ASTVariable> getFreeVars() {
        // we just combine the free variables of the function and argument
        HashSet<ASTVariable> freeVars = new HashSet<>();
        freeVars.addAll(function.getFreeVars());
        freeVars.addAll(argument.getFreeVars());
        return freeVars;
    }

    @Override
    public ASTTerm substitute(ASTVariable var, ASTTerm expr) {
        // substituting an application is equivalent to substituting the function and argument
        ASTTerm replacedFunciton = function.substitute(var, expr);
        ASTTerm replacedArgument = argument.substitute(var, expr);
        return new ASTApplication(replacedFunciton, replacedArgument);
    }

}
