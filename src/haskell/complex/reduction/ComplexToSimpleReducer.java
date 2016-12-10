package haskell.complex.reduction;

import haskell.complex.ast.*;

import java.util.*;

/**
 * A class which can reduce complex haskell to simple haskell.
 * Note: The following functions are predefined, so you cannot overwrite them.
 * match and all functions defined in the delta rules
 */
public class ComplexToSimpleReducer {
    private ASTExpression expression;
    private List<ComplexHaskellVisitor> basicTransformations;
    private FunDeclToPatDecl funDeclToPatDecl;
    private SeparateAndNestDecls separateAndNestDecls;

    public ComplexToSimpleReducer(ASTExpression expression) {
        assert(expression != null);
        this.expression = expression;

        // we need to init the variable manager so that it knows all variables of this expression
        VariableManager.init(expression);

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
    public haskell.simple.ast.ASTExpression reduceToSimple() throws TooComplexException {
        // first we must transform multiple function declarations for the same function to single declarations
        applyFuncDeclToPatDecl();

        // then we apply basic transformation rules as long as possible
        applyBasicTransformationRules();

        // after this, we split the declarations according to entaglement
        // and then nest them in multiple let expressions
        separateAndNestDecls.visit(expression);

        // then we again apply basic transformation rules as long as possible
        applyBasicTransformationRules();

        // after this, the complex haskell expression should be in simple haskell form
        return expression.castToSimple();
    }

    private void applyFuncDeclToPatDecl() {
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

    private void applyBasicTransformationRules() {
        boolean transformed;

        // apply rules as long as they still change something
        do {
            transformed = false;

            // we try to apply every rule in succession
            for (ComplexHaskellVisitor tr : basicTransformations) {
                if (tr.visit(expression)) {
                    // the rule was successfully applied
                    transformed = true;
                }
            }
        } while(transformed);
    }
}
