package lambda.type;

import haskell.ast.*;
import lambda.ast.ASTConstant;
import lambda.reduction.delta.ConstructorReduction;
import lambda.reduction.delta.PredefinedFunction;
import lambda.reduction.delta.TupleReduction;

import java.util.List;
import java.util.Optional;

/**
 * Helper class which can return the type of constants. (both predefined and custom)
 */
public class ConstantTypeResolver {
    /**
     * Returns the type of a lambda constant. Because it is a constant, theoretically, its type should be known.
     * If the type of this constant cannot be determined, this means the corresponding data declaration has not been
     * added to this type checker yet.
     *
     * @param node the constant
     * @return its type or empty if the type could not be determined
     */
    public static Optional<ASTType> getType(ASTConstant node, List<ASTDataDecl> dataDecls) {
        Object value = node.getValue();
        ASTType type;

        if (value instanceof Integer) {
            type = PredefinedType.INTEGER.getType();
        }
        else if (value instanceof Float) {
            type = PredefinedType.FLOAT.getType();
        }
        else if (value instanceof Character) {
            type = PredefinedType.CHAR.getType();
        }
        else if (value instanceof Boolean) {
            type = PredefinedType.BOOL.getType();
        }
        else if (value instanceof PredefinedFunction) {
            PredefinedFunction f = (PredefinedFunction) value;
            type = f.getType();
        }
        else if (value instanceof TupleReduction.TupleConstant) {
            TupleReduction.TupleConstant tuple = (TupleReduction.TupleConstant) value;
            type = tuple.getType();
        }
        else if (value instanceof TupleReduction.IsATuple) {
            TupleReduction.IsATuple isaTuple = (TupleReduction.IsATuple) value;
            type = isaTuple.getType();
        }
        else if (value instanceof TupleReduction.Sel) {
            TupleReduction.Sel selTuple = (TupleReduction.Sel) value;
            type = selTuple.getType();
        }
        else if (value instanceof ConstructorReduction.Constructor){
            ConstructorReduction.Constructor constr = (ConstructorReduction.Constructor) value;
            return getType(constr, dataDecls);
        }
        else if (value instanceof ConstructorReduction.ArgOf) {
            ConstructorReduction.ArgOf argof = (ConstructorReduction.ArgOf) value;
            return getType(argof, dataDecls);
        }
        else if (value instanceof ConstructorReduction.IsA) {
            ConstructorReduction.IsA isaConstr = (ConstructorReduction.IsA) value;
            return getType(isaConstr, dataDecls);
        }
        else {
            return Optional.empty();
        }

        return Optional.of(type);
    }

    private static Optional<ASTType> getType(ConstructorReduction.Constructor constr, List<ASTDataDecl> dataDecls) {
        // data tyconstr a1 ... am = constr type1 ... typen
        // constr :: type1 -> ... -> typen -> (tyconstr a1 ... am)
        Optional<ASTType> dataType = getDataType(constr, dataDecls);
        Optional<List<ASTType>> constrTypes = getConstrTypes(constr, dataDecls);

        if (!dataType.isPresent() || !constrTypes.isPresent()) {
            return Optional.empty();
        }

        ASTType type = dataType.get();
        for (int i = constrTypes.get().size()-1; i >= 0; i--) {
            type = new ASTFuncType(constrTypes.get().get(i), type);
        }

        return Optional.of(type);
    }

    private static Optional<ASTType> getType(ConstructorReduction.ArgOf argof, List<ASTDataDecl> dataDecls) {
        // data tyconstr a1 ... am = constr type1 ... typen
        // argof_constr :: (tyconstr a1 ... am) -> (type1, ..., typen)
        Optional<ASTType> dataType = getDataType(argof.getConstr(), dataDecls);
        Optional<List<ASTType>> constrTypes = getConstrTypes(argof.getConstr(), dataDecls);

        if (!dataType.isPresent() || !constrTypes.isPresent()) {
            return Optional.empty();
        }

        ASTType type;
        if (constrTypes.get().size() == 1) {
            type = new ASTFuncType(dataType.get(), constrTypes.get().get(0));
        }
        else {
            ASTTupleType tupleType = new ASTTupleType(constrTypes.get());
            type = new ASTFuncType(dataType.get(), tupleType);
        }

        return Optional.of(type);
    }

    private static Optional<ASTType> getType(ConstructorReduction.IsA isaConstr, List<ASTDataDecl> dataDecls) {
        ASTType type;
        if (isaConstr.getValue() instanceof Integer) {
            // isa_INT :: Integer -> Bool
            type = new ASTFuncType(PredefinedType.INTEGER.getType(), PredefinedType.BOOL.getType());
        }
        else if (isaConstr.getValue() instanceof Float) {
            // isa_FLOAT :: Float -> Bool
            type = new ASTFuncType(PredefinedType.FLOAT.getType(), PredefinedType.BOOL.getType());
        }
        else if (isaConstr.getValue() instanceof Character) {
            // isa_CHAR :: Char -> Bool
            type = new ASTFuncType(PredefinedType.CHAR.getType(), PredefinedType.BOOL.getType());
        }
        else if (isaConstr.getValue() instanceof Boolean) {
            // isa_BOOL:: Bool -> Bool
            type = new ASTFuncType(PredefinedType.BOOL.getType(), PredefinedType.BOOL.getType());
        }
        else if (isaConstr.getValue() instanceof ConstructorReduction.Constructor) {
            ConstructorReduction.Constructor constr = (ConstructorReduction.Constructor) isaConstr.getValue();

            // data tyconstr a1 ... am = constr type1 ... typen
            // isa_constr :: (tyconstr a1 ... am) -> Bool
            Optional<ASTType> dataType = getDataType(constr, dataDecls);
            if (!dataType.isPresent()) {
                return Optional.empty();
            }
            type = new ASTFuncType(dataType.get(), PredefinedType.BOOL.getType());
        }
        else {
            return Optional.empty();
        }

        return Optional.of(type);
    }

    /**
     * Returns the data type of a constructor.
     * E.g.: data List a = Cons a b => (List a)
     * @param constr the constructor
     * @return its data type
     */
    private static Optional<ASTType> getDataType(ConstructorReduction.Constructor constr, List<ASTDataDecl> dataDecls) {
        for (ASTDataDecl dataDecl : dataDecls) {
            for (ASTConstrDecl constrDecl : dataDecl.getConstrDecls()) {
                if (constrDecl.getTyConstr().getName().equals(constr.getName())) {
                    return Optional.of(dataDecl.getType());
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Returns the type arguments of a constructor.
     * E.g.: data List a = Cons a b => [a, b]
     * @param constr
     * @return
     */
    private static Optional<List<ASTType>> getConstrTypes(ConstructorReduction.Constructor constr, List<ASTDataDecl> dataDecls) {
        for (ASTDataDecl dataDecl : dataDecls) {
            for (ASTConstrDecl constrDecl : dataDecl.getConstrDecls()) {
                if (constrDecl.getTyConstr().getName().equals(constr.getName())) {
                    return Optional.of(constrDecl.getTypes());
                }
            }
        }
        return Optional.empty();
    }
}
