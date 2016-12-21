package haskell.ast;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a type constructor.
 * Do not confuse with ASTTyConstr, which just represents the name of a type constructor.
 */
public class ASTTypeConstr implements ASTType {
    private ASTTyConstr tyConstr;
    private List<ASTType> types;

    public ASTTypeConstr(ASTTyConstr tyConstr, List<ASTType> types) {
        assert(tyConstr != null);
        assert(types != null);
        this.tyConstr = tyConstr;
        this.types = types;
    }

    public ASTTypeConstr(ASTTyConstr tyConstr, ASTType... types) {
        assert(tyConstr != null);
        this.tyConstr = tyConstr;
        this.types = Arrays.asList(types);
    }

    public ASTTyConstr getTyConstr() {
        return tyConstr;
    }

    public List<ASTType> getTypes() {
        return types;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ASTTypeConstr that = (ASTTypeConstr) o;

        if (!getTyConstr().equals(that.getTyConstr())) return false;
        return getTypes().equals(that.getTypes());

    }

    @Override
    public int hashCode() {
        int result = getTyConstr().hashCode();
        result = 31 * result + getTypes().hashCode();
        return result;
    }

    @Override
    public String toString() {
        if (types.size() == 0) {
            return tyConstr.toString();
        }

        StringBuilder builder = new StringBuilder();
        builder.append("(").append(tyConstr);
        for (ASTType type : types) {
            builder.append(" ").append(type);
        }
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
