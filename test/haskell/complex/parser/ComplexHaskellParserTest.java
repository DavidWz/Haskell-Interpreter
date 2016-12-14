package haskell.complex.parser;

import haskell.HaskellInterpreter;
import haskell.complex.ast.ASTExpression;
import haskell.complex.ast.ASTProgram;
import haskell.complex.reduction.TooComplexException;
import lambda.ast.ASTConstant;
import lambda.ast.ASTTerm;
import org.antlr.v4.runtime.ANTLRInputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

/**
 * Tests the complex haskell parser.
 */
public class ComplexHaskellParserTest {
    private static ASTGenerator generator;

    @BeforeClass
    public static void setUp() {
        generator = new ASTGenerator();
    }

    @Test
    public void testParseCompleteInput() {
        String expressionCode = "decrement 5";

        Optional<ASTExpression> eval = generator.parseExpression(new ANTLRInputStream(expressionCode));
        assertFalse(eval.isPresent());
    }

    @Test
    public void testSimpleExpression() {
        String expressionCode = "(plus 2 -5)";

        Optional<ASTExpression> eval = generator.parseExpression(new ANTLRInputStream(expressionCode));
        assertTrue(eval.isPresent());

        System.out.print("eval["+eval.get()+"] = ");

        HaskellInterpreter interpreter = new HaskellInterpreter();
        try {
            ASTTerm result = interpreter.evaluate(eval.get());
            System.out.println(result);

            assertEquals(result, new ASTConstant(-3));
        } catch (TooComplexException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testFunctionDeclarations() {
        String programCode = "fact 0 = 1\n" +
                "fact x = (times x (fact (decr x))) \n" +
                "decr x = (minus x 1)";

        String expressionCode = "(fact (decr 6))";

        Optional<ASTProgram> program = generator.parseProgram(new ANTLRInputStream(programCode));
        Optional<ASTExpression> eval = generator.parseExpression(new ANTLRInputStream(expressionCode));
        assertTrue(program.isPresent());
        assertTrue(eval.isPresent());

        System.out.println(program.get());
        System.out.print("eval["+eval.get()+"] = ");

        HaskellInterpreter interpreter = new HaskellInterpreter(program.get());
        try {
            ASTTerm result = interpreter.evaluate(eval.get());
            System.out.println(result);

            assertEquals(result, new ASTConstant(120));
        } catch (TooComplexException e) {
            fail(e.getMessage());
        }
    }
}