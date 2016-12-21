package lambda.type;

import haskell.ast.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Helper class which can substitute variables with another type.
 */
public class TypeSubstituter implements TypeVisitor<ASTType> {
    private ASTVariable var;
    private ASTType type;

    public TypeSubstituter() {
    }

    /**
     * Substitutes var by type in the target expression.
     * @param var the variable which should be replaced
     * @param type the type it should be replaced with
     * @param target the target type on which the substitution should be done
     * @return
     */
    public ASTType substituteVariable(ASTVariable var, ASTType type, ASTType target) {
        this.var = var;
        this.type = type;
        return target.accept(this);
    }

    @Override
    public ASTType visit(ASTVariable node) {
        if (node.equals(var)) {
            return type;
        }
        else {
            return node;
        }
    }

    @Override
    public ASTType visit(ASTTupleType node) {
        List<ASTType> types = node.getTypes().stream().map(type -> type.accept(this)).collect(Collectors.toList());
        return new ASTTupleType(types);
    }

    @Override
    public ASTType visit(ASTFuncType node) {
        ASTType from = node.getFrom().accept(this);
        ASTType to = node.getTo().accept(this);
        return new ASTFuncType(from, to);
    }

    @Override
    public ASTType visit(ASTTypeConstr node) {
        List<ASTType> types = node.getTypes().stream().map(type -> type.accept(this)).collect(Collectors.toList());
        return new ASTTypeConstr(node.getTyConstr(), types);
    }
}
