package haskell.ast;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a tuple type.
 */
public class ASTTupleType implements ASTType {
    private List<ASTType> types;

    public ASTTupleType(List<ASTType> types) {
        assert(types != null);
        this.types = types;
    }

    public List<ASTType> getTypes() {
        return types;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ASTTupleType that = (ASTTupleType) o;

        return getTypes().equals(that.getTypes());

    }

    @Override
    public int hashCode() {
        return getTypes().hashCode();
    }

    @Override
    public String toString() {
        if (types.size() == 0) {
            return "()";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("(");
        for (ASTType type : types) {
            builder.append(type);
            builder.append(", ");
        }
        builder.deleteCharAt(builder.length()-1);
        builder.deleteCharAt(builder.length()-1);
        builder.append(")");
        return builder.toString();
    }

    @Override
    public Set<ASTVariable> getAllVariables() {
        Set<ASTVariable> vars = new HashSet<>();
        for (ASTType type : types) {
            vars.addAll(type.getAllVariables());
        }
        return vars;
    }

    @Override
    public Set<ASTVariable> getFreeVars() {
        Set<ASTVariable> vars = new HashSet<>();
        for (ASTType type : types) {
            vars.addAll(type.getFreeVars());
        }
        return vars;
    }

    @Override
    public <T> T accept(ComplexHaskellVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public <T> T accept(TypeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
