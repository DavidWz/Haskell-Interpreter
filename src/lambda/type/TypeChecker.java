package lambda.type;

import haskell.complex.ast.*;
import haskell.complex.ast.ASTVariable;
import lambda.ast.*;
import lambda.ast.ASTApplication;

import java.util.*;

/**
 * This class can check the type of a lambda term.
 * It will return the type of a lambda term if it is correctly typed,
 * and it will throw a type exception if the lambda term is incorrectly typed.
 */
public class TypeChecker implements LambdaVisitor<Optional<ASTType>> {
    // list of data declarations
    private List<ASTDataDecl> dataDeclarations;

    // map of type assumptions
    private Map<ASTVariable, ASTType> assumptions;

    // the exception in case there was one
    private Optional<TypeException> error;

    private int freshVarIndex;

    public TypeChecker(){
        this.dataDeclarations = new ArrayList<>();
        this.assumptions = new HashMap<>();
        this.error = Optional.empty();
        this.freshVarIndex = 0;
    }

    /**
     * Adds a new data declaration.
     * @param decl the declaration
     */
    public void addDataDeclaration(ASTDataDecl decl) {
        dataDeclarations.add(decl);
    }

    /**
     * Checks the type of a lambda term. Will return the type of the lambda term if it is correctly typed.
     * It will throw a TypeException if the lambda term is not correctly typed.
     * @return the type of the lambda term
     * @throws TypeException
     */
    public ASTType checkType(ASTTerm term) throws TypeException {
        init(term);

        Optional<ASTType> type = term.accept(this);
        return type.orElseThrow(() -> error.get());
    }

    private void init(ASTTerm term) {
        // clear previous results
        this.assumptions = new HashMap<>();
        this.error = Optional.empty();

        // make sure our fresh variables do not conflict with any variables in the term
        Set<lambda.ast.ASTVariable> vars = term.getFreeVars();

        // all our fresh variables are simply called "b_i", so we need to determine the smallest i which is not used yet
        freshVarIndex = 0;
        for(lambda.ast.ASTVariable var : vars) {
            String varName = var.getName();
            if (varName.length() >= 2 && varName.charAt(0) == 'b') {

                try {
                    int index = Integer.parseInt(varName.substring(1));
                    if (index >= freshVarIndex) {
                        freshVarIndex = index + 1;
                    }
                }
                catch (Exception e) {
                    continue;
                }
            }
        }
    }

    @Override
    public Optional<ASTType> visit(ASTAbstraction node) {
        // we replace the abstraction variable with a fresh variable
        ASTVariable argument = getFreshVariable();
        updateAssumption(new ASTVariable(node.getInput().getName()), argument);

        // then we determine the type of the abstraction output
        Optional<ASTType> outputType = node.getOutput().accept(this);
        if (!outputType.isPresent()) {
            // abort if the type could not be determined.
            return outputType;
        }

        // the type of this abstraction then is: (type[argument] -> type[output])
        ASTFuncType result = new ASTFuncType(getAssumedType(argument), outputType.get());
        return Optional.of(result);
    }

    @Override
    public Optional<ASTType> visit(ASTApplication node) {
        // determine the type of the left side
        Optional<ASTType> leftType = node.getLeft().accept(this);
        if (!leftType.isPresent()) {
            return leftType;
        }

        // determine the type of the right side
        Optional<ASTType> rightType = node.getRight().accept(this);
        if (!rightType.isPresent()) {
            return rightType;
        }

        // try to unify leftType  with (rightType -> var)
        ASTVariable resultVar = getFreshVariable();
        try {
            unify(leftType.get(), new ASTFuncType(rightType.get(), resultVar));
        }
        catch(TypeException.CannotUnifyException e) {
            error = Optional.of(e);
            return Optional.empty();
        }

        // everything worked, so return the resulting type
        return Optional.of(getAssumedType(resultVar));
    }

    @Override
    public Optional<ASTType> visit(ASTConstant node) {
        Optional<ASTType> type = ConstantTypes.getType(node, dataDeclarations);
        if (!type.isPresent()) {
            error = Optional.of(new TypeException.TypeNotFoundException(node));
        }
        // TODO: replace used variables with fresh variables

        return type;
    }

    @Override
    public Optional<ASTType> visit(lambda.ast.ASTVariable node) {
        ASTVariable var = new ASTVariable(node.getName());

        // look up if this variable already has a type assumption
        if (assumptions.containsKey(var)) {
            return Optional.of(assumptions.get(var));
        }

        // if not, assign a fresh variable type to this variable
        ASTType varType = getFreshVariable();
        updateAssumption(var, varType);
        return Optional.of(varType);
    }

    /**
     * Returns a fresh variable which does not occur in the assumptions or in predefined functions
     * @return
     */
    private ASTVariable getFreshVariable() {
        // we choose "b" because predefined functions only use "a..." as type variables
        String varName = "b";

        ASTVariable var = new ASTVariable(varName + freshVarIndex);
        freshVarIndex++;

        return var;
    }

    /**
     * Updates the type assumptions.
     * @param var the variable
     * @param type it's new type
     */
    private void updateAssumption(ASTVariable var, ASTType type) {
        assumptions.put(var, type);
    }

    /**
     * Returns the assumed type of the given variable.
     * Default assumption is the identity.
     * @param var
     * @return
     */
    private ASTType getAssumedType(ASTVariable var) {
        if (assumptions.containsKey(var)) {
            return assumptions.get(var);
        }
        else {
            return var;
        }
    }

    /**
     * Tries to unify type1 with type2. This method will update the assumptions accordingly.
     * If no unifier could be found, a cannot unify exception will be thrown.
     * @param type1
     * @param type2
     * @throws TypeException.CannotUnifyException
     */
    private void unify(ASTType type1, ASTType type2) throws TypeException.CannotUnifyException {
        // TODO
    }
}
