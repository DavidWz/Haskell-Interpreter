package lambda.reduction;

import lambda.ast.*;
import lambda.reduction.delta.ArithmeticReduction;
import lambda.reduction.delta.BranchReduction;
import lambda.reduction.delta.FixReduction;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests the WHNO Reducer.
 */
public class WHNOReducerTest {
    private static WHNOReducer reducer;
    private static ASTVariable x, y;
    private static ASTConstant plus;

    /**
     * Returns the following factorial function: fact x = if x <= 0 then 1 else fact(x-1)
     * @return
     */
    public static ASTTerm getFactFunction() {
        ASTVariable x = new ASTVariable("x");
        ASTConstant times = new ASTConstant(ArithmeticReduction.Operator.TIMES);
        ASTConstant minus = new ASTConstant(ArithmeticReduction.Operator.MINUS);
        ASTConstant lesseq = new ASTConstant(ArithmeticReduction.Operator.LESSEQ);
        ASTConstant fix = new ASTConstant(FixReduction.Operator.FIX);
        ASTConstant _if = new ASTConstant(BranchReduction.Operator.IF);

        // fact x = if x <= 0 then 1 else fact(x-1)
        ASTTerm decrementX = new ASTApplication(new ASTApplication(minus, x), new ASTConstant(1));
        ASTTerm recCall = new ASTApplication(new ASTVariable("fact"), decrementX);

        ASTTerm lessEqCond = new ASTApplication(new ASTApplication(lesseq, x), new ASTConstant(0));
        ASTTerm cond = new ASTApplication(_if, lessEqCond);
        ASTTerm condIf = new ASTApplication(cond, new ASTConstant(1));

        ASTTerm elseVal = new ASTApplication(new ASTApplication(times, recCall), x);
        ASTTerm branch = new ASTApplication(condIf, elseVal);
        ASTTerm fact = new ASTAbstraction(new ASTVariable("fact"), new ASTAbstraction(x, branch));
        ASTTerm fixFact = new ASTApplication(fix, fact);

        return fixFact;
    }

    @BeforeClass
    public static void setUp() throws Exception {
        reducer = new WHNOReducer();
        x = new ASTVariable("x");
        y = new ASTVariable("y");
        plus = new ASTConstant(ArithmeticReduction.Operator.PLUS);
    }

    @Test
    public void testIdentityFunction() {
        // (\x.x) Zero => Zero
        ASTConstant zero = new ASTConstant("Zero");

        ASTTerm lambda = new ASTApplication(new ASTAbstraction(x, x), zero);
        ASTTerm result = reducer.reduceToWHNF(lambda, true);

        assertEquals(zero, result);
    }

    @Test
    public void testVariableReplacement() {
        // (\xy.xy) y 4 => y 4
        ASTTerm b = new ASTAbstraction(y, new ASTApplication(x, y));
        ASTTerm a = new ASTAbstraction(x, b);
        ASTTerm c = new ASTApplication(a, y);
        ASTTerm lambda = new ASTApplication(c, new ASTConstant(4));
        ASTTerm result = reducer.reduceToWHNF(lambda, true);

        ASTTerm y4 = new ASTApplication(y, new ASTConstant(4));

        assertEquals(result, y4);
    }

    @Test
    public void testIntegerArithmetic() {
        // \x.(plus 40 x) (\y.(times y y) 3) => 49
        ASTTerm plus40X = new ASTApplication(new ASTApplication(plus, new ASTConstant(40)), x);
        ASTTerm add40 = new ASTAbstraction(x, plus40X);

        ASTTerm timesYY = new ASTApplication(new ASTApplication(new ASTConstant(ArithmeticReduction.Operator.TIMES), y), y);
        ASTTerm square = new ASTAbstraction(y, timesYY);

        ASTTerm square3 = new ASTApplication(square, new ASTConstant(3));
        ASTTerm lambda = new ASTApplication(add40, square3);

        ASTTerm result = reducer.reduceToWHNF(lambda, true);

        assertEquals(result, new ASTConstant(49));
    }

    @Test
    public void testRecursion() {
        ASTTerm fixFact = getFactFunction();

        // fact 5 = 120
        ASTTerm lambda = new ASTApplication(fixFact, new ASTConstant(5));
        ASTTerm result = reducer.reduceToWHNF(lambda, true);
        assertEquals(result, new ASTConstant(120));
    }

    @Test
    public void testWeakHeadNormalOrder() {
        // (\x.y) (\x.xx) (\x.xx) => y (this would not terminate if one didn't use WHNO)
        ASTTerm xy = new ASTAbstraction(x, y);
        ASTTerm xXX = new ASTAbstraction(x, new ASTApplication(x, x));
        ASTTerm lambda = new ASTApplication(xy, new ASTApplication(xXX, xXX));
        ASTTerm result = reducer.reduceToWHNF(lambda, true);

        assertEquals(result, y);
    }
}