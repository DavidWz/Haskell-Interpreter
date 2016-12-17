package lambda.type;

import haskell.complex.ast.*;
import lambda.ast.ASTConstant;
import lambda.ast.ASTTerm;

import java.util.*;

/**
 * This class can check the type of a lambda term.
 * It will return the type of a lambda term if it is correctly typed,
 * and it will throw a type exception if the lambda term is incorrectly typed.
 */
public class TypeChecker {
    List<ASTDataDecl> dataDeclarations;

    public TypeChecker(){
        this.dataDeclarations = new ArrayList<>();
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
        // TODO
        if (term instanceof ASTConstant) {
            return ConstantTypes.getType((ASTConstant) term, dataDeclarations);
        }
        else {
            throw new RuntimeException("Not implemented yet.");
        }
    }
}
