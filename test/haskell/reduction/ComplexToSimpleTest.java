package haskell.reduction;

import haskell.HaskellInterpreter;
import haskell.ast.ASTExpression;
import haskell.ast.ASTProgram;
import haskell.parser.ASTGenerator;
import lambda.ast.ASTConstant;
import lambda.ast.ASTTerm;
import lambda.type.TypeException;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

/**
 * Tests the complex haskell to simple haskell transformation.
 */
public class ComplexToSimpleTest {
    private static ASTGenerator generator;

    @BeforeClass
    public static void setUp() {
        generator = new ASTGenerator();
    }

    @Test
    public void testLetInExpr() {
        String programCode = "squareDouble x = (let { square x = (mult x x); double x = (mult 2 x); res = (square (double x)) } in res)\n";
        Optional<ASTProgram> program = generator.parseProgram(new ANTLRInputStream(programCode));
        assertTrue(program.isPresent());

        String expCode = "(squareDouble 5)";
        Optional<ASTExpression> exp = generator.parseExpression(new ANTLRInputStream(expCode));
        assertTrue(exp.isPresent());

        HaskellInterpreter interpreter = new HaskellInterpreter();
        try {
            interpreter.addProgram(program.get());
        } catch (TypeException.InconsistentDataDeclException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        try {
            ASTTerm result = interpreter.evaluate(exp.get(), true);
            System.out.println(result);

            assertEquals(result, new ASTConstant(100));
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
}