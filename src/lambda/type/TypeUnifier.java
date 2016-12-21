package lambda.type;

import haskell.ast.*;

/**
 * This class can unify two types.
 */
public class TypeUnifier {
    private TypeChecker typeChecker;
    
    public TypeUnifier(TypeChecker typeChecker) {
        this.typeChecker = typeChecker;
    }
    
    /**
     * Tries to unify type1 with type2. This method will update the assumptions accordingly.
     * If no unifier could be found, a cannot unify exception will be thrown.
     * @param type1
     * @param type2
     * @throws TypeException.CannotUnifyException
     */
    public void unify(ASTType type1, ASTType type2) throws TypeException.CannotUnifyException {
        // we only need to unify them if they are actually different
        if (type1.equals(type2)) {
            return;
        }

        if (type1 instanceof ASTVariable) {
            unify((ASTVariable) type1, type2);
        }
        else if (type1 instanceof ASTTupleType) {
            unify((ASTTupleType) type1, type2);
        }
        else if (type1 instanceof ASTFuncType) {
            unify((ASTFuncType) type1, type2);
        }
        else if (type1 instanceof ASTTypeConstr) {
            unify((ASTTypeConstr) type1, type2);
        }
        else {
            assert(false);
            throw new RuntimeException();
        }
    }

    public void unify(ASTVariable node, ASTType type2) throws TypeException.CannotUnifyException {
        // a variable can be unified with another type, iff that type does not contain this variable
        // (if both are the same, we can unify them by doing nothing.
        // this case is already covered in the general unify function above)
        if (type2.getAllVariables().contains(node)) {
            throw new TypeException.CannotUnifyException(node, type2);
        }
        else {
            // unify the types: just replace node by type2
            typeChecker.updateAssumption(node, type2);
        }
    }

    public void unify(ASTTupleType node, ASTType type2) throws TypeException.CannotUnifyException {
        // if type2 is a variable, unify that
        if (type2 instanceof ASTVariable) {
            unify((ASTVariable) type2, node);
        }
        // if both types are tuple types of the same length, try to unify them part-wise
        else if(type2 instanceof ASTTupleType && node.getTypes().size() == ((ASTTupleType) type2).getTypes().size()) {
            for (int i = 0; i < node.getTypes().size(); i++) {
                ASTType t1 = node.getTypes().get(i);
                ASTType t2 = ((ASTTupleType) type2).getTypes().get(i);

                // update the types
                t1 = typeChecker.applyAssumptions(t1);
                t2 = typeChecker.applyAssumptions(t2);

                // finally unify them
                unify(t1, t2);
            }
        }
        else {
            throw new TypeException.CannotUnifyException(node, type2);
        }
    }

    public void unify(ASTFuncType node, ASTType type2) throws TypeException.CannotUnifyException {
        // if type2 is a variable, unify that
        if (type2 instanceof ASTVariable) {
            unify((ASTVariable) type2, node);
        }
        // if both types are function types, try to unify them part-wise
        else if(type2 instanceof ASTFuncType) {
            unify(node.getFrom(), ((ASTFuncType) type2).getFrom());

            // update the function output type
            ASTType to1 = typeChecker.applyAssumptions(node.getTo());
            ASTType to2 = typeChecker.applyAssumptions(((ASTFuncType) type2).getTo());
            unify(to1, to2);
        }
        else {
            throw new TypeException.CannotUnifyException(node, type2);
        }
    }

    public void unify(ASTTypeConstr node, ASTType type2) throws TypeException.CannotUnifyException {
        // if type2 is a variable, unify that
        if (type2 instanceof ASTVariable) {
            unify((ASTVariable) type2, node);
        }
        // if both types are type constructors, try to unify them part-wise
        else if(type2 instanceof ASTTypeConstr && node.getTyConstr().equals(((ASTTypeConstr) type2).getTyConstr())) {
            for (int i = 0; i < node.getTypes().size(); i++) {
                ASTType t1 = node.getTypes().get(i);
                ASTType t2 = ((ASTTypeConstr) type2).getTypes().get(i);

                // update the types
                t1 = typeChecker.applyAssumptions(t1);
                t2 = typeChecker.applyAssumptions(t2);

                // finally unify them
                unify(t1, t2);
            }
        }
        else {
            throw new TypeException.CannotUnifyException(node, type2);
        }
    }
}
