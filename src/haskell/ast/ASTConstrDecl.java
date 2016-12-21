package haskell.ast;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a type constructor declaration.
 */
public class ASTConstrDecl implements ComplexHaskell {
    private ASTTyConstr tyConstr;
    private List<ASTType> types;

    public ASTConstrDecl(ASTTyConstr tyConstr, List<ASTType> types) {
        assert(tyConstr != null);
        assert(types != null);
        this.tyConstr = tyConstr;
        this.types = types;
    }

    public ASTConstrDecl(ASTTyConstr tyConstr, ASTType... types) {
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

        ASTConstrDecl that = (ASTConstrDecl) o;

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
        StringBuilder builder = new StringBuilder();
        builder.append(tyConstr).append(" ");
        for (ASTType type : types) {
            builder.append(type).append(" ");
        }
        builder.deleteCharAt(builder.length()-1);
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
}
