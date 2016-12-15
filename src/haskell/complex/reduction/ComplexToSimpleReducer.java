package haskell.complex.reduction;

import haskell.complex.ast.*;

import java.util.*;

/**
 * A class which can reduce complex haskell to simple haskell.
 * For predefined functions, refer to @see PredefinedFunction enum.
 */
public class ComplexToSimpleReducer {
    private List<ComplexHaskellTransformation> basicTransformations;
    private FunDeclToPatDecl funDeclToPatDecl;
    private SeparateAndNestDecls separateAndNestDecls;

    public ComplexToSimpleReducer() {
        // set up all basicTransformations
        basicTransformations = new ArrayList<>();
        basicTransformations.add(new NestMultipleLambdas());
        basicTransformations.add(new LambdaPatternToCase());
        basicTransformations.add(new CaseToMatch());
        basicTransformations.add(new TuplePatLetToSingleVar());
        funDeclToPatDecl = new FunDeclToPatDecl();
        separateAndNestDecls = new SeparateAndNestDecls();
    }

    /**
     * Converts a complex haskell expression to a simple haskell expression.
     * @return an equivalent simple haskell expression
     * @throws TooComplexException
     */
    public haskell.simple.ast.ASTExpression reduceToSimple(ASTExpression expression) throws TooComplexException {
        // we need to init the variable manager so that it knows all variables of this expression
        VariableManager.init(expression);

        // first we must transform multiple function declarations for the same function to single declarations
        applyFuncDeclToPatDecl(expression);

        // then we apply basic transformation rules as long as possible
        applyBasicTransformationRules(expression);

        // after this, we split the declarations according to entaglement
        // and then nest them in multiple let expressions
        separateAndNestDecls.visit(expression);

        // then we again apply basic transformation rules as long as possible
        applyBasicTransformationRules(expression);

        // after this, the complex haskell expression should be in simple haskell form
        return expression.castToSimple();
    }

    private void applyFuncDeclToPatDecl(ASTExpression expression) {
        boolean transformed;

        // apply the function declaration to pattern declaration rule as long as it still changes something
        do {
            transformed = false;
            if (funDeclToPatDecl.visit(expression)) {
                // the rule was successfully applied
                transformed = true;
            }
        } while(transformed);
    }

    private void applyBasicTransformationRules(ASTExpression expression) {
        boolean transformed;

        // apply rules as long as they still change something
        do {
            transformed = false;

            // we try to apply every rule in succession
            for (ComplexHaskellTransformation tr : basicTransformations) {
                if (tr.visit(expression)) {
                    // the rule was successfully applied
                    transformed = true;
                }
            }
        } while(transformed);
    }
}
