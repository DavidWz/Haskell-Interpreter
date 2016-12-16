package haskell;

import haskell.complex.ast.*;
import haskell.complex.reduction.TooComplexException;
import lambda.ast.ASTConstant;
import lambda.ast.ASTTerm;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests the complex haskell interpreter.
 */
public class HaskellInterpreterTest {
    private static HaskellInterpreter interpreter;
    private static ASTApplication list1;
    private static ASTApplication list2;
    private static ASTApplication list3;
    private static ASTApplication list4;
    private static ASTApplication square2;
    private static ASTApplication lenList3;
    private static ASTApplication lenList4;
    private static ASTApplication squareLenList3;
    private static ASTApplication squareLenList4;
    private static ASTApplication factLenList3;
    private static ASTApplication factPlusToIntEven63;
    private static ASTApplication factLenAppend;

    @BeforeClass
    public static void setUp() {
        ASTProgram prog = new ASTProgram();

        // toInt true = 1
        // toInt false = 0
        ASTVariable toInt = new ASTVariable("toInt");
        ASTFunDecl toIntTrue = new ASTFunDecl(new ASTInteger(1), toInt, new ASTBoolean(true));
        ASTFunDecl toIntFalse = new ASTFunDecl(new ASTInteger(0), toInt, new ASTBoolean(false));
        prog.addDeclaration(toIntTrue);
        prog.addDeclaration(toIntFalse);

        // decr x = x - 1
        ASTVariable decr = new ASTVariable("decr");
        ASTVariable minus = new ASTVariable("minus");
        ASTVariable x = new ASTVariable("x");
        ASTExpression decrX = new ASTApplication(minus, x, new ASTInteger(1));
        ASTFunDecl decrFunc = new ASTFunDecl(decrX, decr, x);
        prog.addDeclaration(decrFunc);

        // even 0 = true
        // even x = odd(x-1)
        ASTVariable even = new ASTVariable("even");
        ASTVariable odd = new ASTVariable("odd");
        ASTFunDecl evenBase = new ASTFunDecl(new ASTBoolean(true), even, new ASTInteger(0));
        ASTFunDecl evenRec = new ASTFunDecl(new ASTApplication(odd, new ASTApplication(decr, x)), even, x);
        prog.addDeclaration(evenBase);
        prog.addDeclaration(evenRec);

        // odd 0 = false
        // odd x = even(x-1)
        ASTFunDecl oddBase = new ASTFunDecl(new ASTBoolean(false), odd, new ASTInteger(0));
        ASTFunDecl oddRec = new ASTFunDecl(new ASTApplication(even, new ASTApplication(decr, x)), odd, x);
        prog.addDeclaration(oddBase);
        prog.addDeclaration(oddRec);

        // square 0 = 0
        ASTVariable square = new ASTVariable("square");
        ASTFunDecl squareBasis = new ASTFunDecl(new ASTInteger(0), square, new ASTInteger(0));
        prog.addDeclaration(squareBasis);

        // square x = times x x
        ASTVariable times = new ASTVariable("mult");
        ASTFunDecl squareFunc = new ASTFunDecl(new ASTApplication(times, x, x), square, x);
        prog.addDeclaration(squareFunc);

        // fact 0 = 1
        // fact x = fact(x-1)*x
        ASTVariable fact = new ASTVariable("fact");
        ASTFunDecl factBase = new ASTFunDecl(new ASTInteger(1), fact, new ASTInteger(0));
        ASTApplication factRec = new ASTApplication(times,
                new ASTApplication(fact, new ASTApplication(decr, x)),
                x);
        ASTFunDecl factFunc = new ASTFunDecl(factRec, fact, x);
        prog.addDeclaration(factBase);
        prog.addDeclaration(factFunc);

        // len Nil = 0
        ASTVariable len = new ASTVariable("len");
        ASTTyConstr Nil = new ASTTyConstr("Nil");
        ASTFunDecl lenNil = new ASTFunDecl(new ASTInteger(0), len, new ASTConstruct(Nil));
        prog.addDeclaration(lenNil);

        // append z Nil = (Cons z Nil)
        ASTTyConstr Cons = new ASTTyConstr("Cons");
        ASTVariable append = new ASTVariable("append");
        ASTVariable z = new ASTVariable("z");
        ASTFunDecl appendFuncNil = new ASTFunDecl(new ASTApplication(Cons, z, Nil), append, z, new ASTConstruct(Nil));
        prog.addDeclaration(appendFuncNil);

        // len (Cons _ xs) = (len xs) + 1
        ASTVariable xs = new ASTVariable("xs");
        ASTVariable plus = new ASTVariable("plus");
        ASTFunDecl lenCons = new ASTFunDecl(new ASTApplication(plus, new ASTApplication(len, xs), new ASTInteger(1)), len, new ASTConstruct(Cons, new ASTJoker(), xs));
        prog.addDeclaration(lenCons);

        // append z (Cons x y) = Cons x (append z y)
        ASTVariable y = new ASTVariable("y");
        ASTFunDecl appendFuncCons = new ASTFunDecl(
                new ASTApplication(Cons, x, new ASTApplication(append, z, y)),
                append,
                z,
                new ASTConstruct(Cons, x, y));
        prog.addDeclaration(appendFuncCons);

        // genList 0 = Nil
        ASTVariable genList = new ASTVariable("genList");
        ASTFunDecl genList0 = new ASTFunDecl(Nil, genList, new ASTInteger(0));
        prog.addDeclaration(genList0);

        // genList x = (Cons x (genList (decr x)))
        ASTFunDecl genListX = new ASTFunDecl(new ASTApplication(Cons, x,
                new ASTApplication(genList, new ASTApplication(decr, x))), genList, x);
        prog.addDeclaration(genListX);

        // List a = Nil | Cons a (List a)
        ASTTyConstr List = new ASTTyConstr("List");
        ASTVariable a = new ASTVariable("a");
        ASTConstrDecl ConsDecl = new ASTConstrDecl(Cons, a, new ASTTypeConstr(List, a));
        ASTDataDecl listDecl = new ASTDataDecl(List, a, new ASTConstrDecl(Nil), ConsDecl);
        prog.addDeclaration(listDecl);

        // create the interpreter
        interpreter = new HaskellInterpreter(prog);

        // list3 = (Cons 3) ((Cons 2) ((Cons 1) Nil)))
        list1 = new ASTApplication(new ASTApplication(Cons, new ASTInteger(1)), Nil);
        list2 = new ASTApplication(new ASTApplication(Cons, new ASTInteger(2)), list1);
        list3 = new ASTApplication(new ASTApplication(Cons, new ASTInteger(3)), list2);

        // list 4 = append 4 list3
        list4 = new ASTApplication(append, new ASTInteger(4), list3);

        // several test expressions
        square2 = new ASTApplication(square, new ASTInteger(2));
        lenList3 = new ASTApplication(len, list3);
        lenList4 = new ASTApplication(len, list4);
        squareLenList3 = new ASTApplication(square, lenList3);
        squareLenList4 = new ASTApplication(square, lenList4);
        factLenList3 = new ASTApplication(fact, lenList3);
        factPlusToIntEven63 = new ASTApplication(fact, new ASTApplication(plus, new ASTApplication(toInt, new ASTApplication(even, new ASTInteger(6))), new ASTInteger(3)));
        ASTApplication appendList4 = new ASTApplication(append, new ASTInteger(5), new ASTApplication(genList, new ASTInteger(4)));
        factLenAppend = new ASTApplication(fact, new ASTApplication(len, appendList4));
    }

    /**
     * Tests if the given expression evaluates to the given expected result.
     * @param exp
     * @param expectedResult
     * @throws TooComplexException
     */
    private void testExpression(ASTExpression exp, ASTTerm expectedResult) {
        System.out.print("eval[" + exp + "] = ");
        ASTTerm result = null;
        try {
            result = interpreter.evaluate(exp);
        } catch (TooComplexException e) {
            fail(e.getMessage());
        }
        System.out.println(result);

        assertEquals(expectedResult, result);
    }

    @Test
    public void testSquare2() {
        testExpression(square2, new ASTConstant(4));
    }

    @Test
    public void testLenList3() {
        testExpression(lenList3, new ASTConstant(3));
    }

    @Test
    public void testLenList4() {
        testExpression(lenList4, new ASTConstant(4));
    }

    @Test
    public void testSquareLenList3() {
        testExpression(squareLenList3, new ASTConstant(9));
    }

    @Test
    public void testSquareLenList4() {
        testExpression(squareLenList4, new ASTConstant(16));
    }

    @Test
    public void testFactLenList3() {
        testExpression(factLenList3, new ASTConstant(6));
    }

    @Test
    public void testFactPlusToIntEven63() {
        testExpression(factPlusToIntEven63, new ASTConstant(24));
    }

    @Test
    public void testFactLenAppend() {
        testExpression(factLenAppend, new ASTConstant(120));
    }
}