package haskell;

import haskell.ast.*;
import haskell.reduction.SimpleToLambdaReducer;
import lambda.ast.ASTAbstraction;
import lambda.ast.ASTTerm;
import lambda.reduction.WHNOReducer;
import lambda.reduction.WHNOReducerTest;
import lambda.reduction.delta.*;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 * Tests the transformation from simple haskell to lambda terms.
 */
public class SimpleToLambdaReducerTest {
    private static WHNOReducer reducer;
    private static SimpleToLambdaReducer simpleToLambdaReducer;
    private static ASTVariable x;
    private static ASTVariable times, minus, lesseq;
    private static ConstructorReduction.Constructor list, tree;

    @BeforeClass
    public static void setUp() throws Exception {
        reducer = new WHNOReducer();
        simpleToLambdaReducer = new SimpleToLambdaReducer();
        x = new ASTVariable("x");
        times = new ASTVariable("mult");
        minus = new ASTVariable("minus");
        lesseq = new ASTVariable("lesseq");
        list = ConstructorReduction.getConstructor("List");
        tree = ConstructorReduction.getConstructor("Tree");
    }

    @Test
    public void testRecursion() {
        // fact = \x -> if x <= 0 then 1 else fact(x − 1) ∗ x
        ASTVariable fact = new ASTVariable("fact");
        ASTExpression cond = new ASTApplication(new ASTApplication(lesseq, x), new ASTInteger(0));
        ASTExpression ifBranch = new ASTInteger(1);
        ASTExpression decrementX = new ASTApplication(new ASTApplication(minus, x), new ASTInteger(1));
        ASTExpression rec = new ASTApplication(fact, decrementX);
        ASTExpression elseBranch = new ASTApplication(new ASTApplication(times, rec), x);
        ASTExpression ifThenElse = new ASTBranch(cond, ifBranch, elseBranch);
        ASTExpression factFunc = new ASTLambda(x, ifThenElse);

        // let fact = ... in fact 5
        ASTExpression program = new ASTLet(Collections.singletonList(new ASTPatDecl(fact, factFunc)),
                new ASTApplication(fact, new ASTInteger(5)));

        System.out.println(program);
        lambda.ast.ASTTerm result = program.accept(simpleToLambdaReducer);
        System.out.println("Lam = " + result);

        // now the same expression in lambda terms
        ASTTerm fixFact = WHNOReducerTest.getFactFunction();
        ASTTerm fixFact5 = new lambda.ast.ASTApplication(fixFact, new lambda.ast.ASTConstant(5));

        assertEquals(fixFact5, result);
    }

    @Test
    public void testIsaTuple() {
        // isa_3-tuple (5, \\x -> x, 'c')
        ArrayList<ASTExpression> expressions = new ArrayList<>();
        expressions.add(new ASTInteger(5));
        expressions.add(new ASTLambda(x, x));
        expressions.add(new ASTChar('c'));
        ASTExpression tuple = new ASTExpTuple(expressions);
        ASTExpression program = new ASTApplication(new ASTVariable("isa_tuple_3"), tuple);

        System.out.println(program);
        lambda.ast.ASTTerm result = program.accept(simpleToLambdaReducer);
        System.out.println("Lam = " + result);

        // in lambda terms: isa_3-tuple (((tuple_3 5) \\x -> x) 'c')
        lambda.ast.ASTApplication lambdaTuple = new lambda.ast.ASTApplication(
                new lambda.ast.ASTApplication(new lambda.ast.ASTApplication(
                        new lambda.ast.ASTConstant(TupleReduction.getTupleConstructor(3)),
                        new lambda.ast.ASTConstant(5)),
                    new ASTAbstraction(new lambda.ast.ASTVariable("x"), new lambda.ast.ASTVariable("x"))),
                new lambda.ast.ASTConstant('c'));
        lambda.ast.ASTApplication lambdaIsaTuple = new lambda.ast.ASTApplication(
                new lambda.ast.ASTConstant(TupleReduction.getIsaOperator(3)),
                lambdaTuple);

        assertEquals(result, lambdaIsaTuple);

        // also test if that reduces to true
        ASTTerm reducedResult = reducer.reduceToWHNF(result);
        System.out.println("=> " + reducedResult);
        assertEquals(reducedResult, new lambda.ast.ASTConstant(true));
    }

    @Test
    public void testSelTuple() {
        // sel_2,2 ('a', 7)
        ArrayList<ASTExpression> expressions = new ArrayList<>();
        expressions.add(new ASTChar('a'));
        expressions.add(new ASTInteger(7));
        ASTExpression tuple = new ASTExpTuple(expressions);

        ASTExpression selTuple = new ASTApplication(new ASTVariable("sel_2_2"), tuple);
        System.out.println(selTuple);
        ASTTerm result = selTuple.accept(simpleToLambdaReducer);
        System.out.println("Lam = " + result);

        // in lambda terms:  sel_2,2 ((tuple_2 'a') 7)
        lambda.ast.ASTApplication lambdaTuple = new lambda.ast.ASTApplication(
                new lambda.ast.ASTApplication(new lambda.ast.ASTConstant(
                        TupleReduction.getTupleConstructor(2)),
                        new lambda.ast.ASTConstant('a')),
                new lambda.ast.ASTConstant(7));
        lambda.ast.ASTApplication lambdaSelTuple = new lambda.ast.ASTApplication(
                new lambda.ast.ASTConstant(TupleReduction.getSelOperator(2, 2)),
                lambdaTuple);

        assertEquals(result, lambdaSelTuple);

        // also test if that reduces to 7
        ASTTerm reducedResult = reducer.reduceToWHNF(result);
        System.out.println("=> " + reducedResult);
        assertEquals(reducedResult, new lambda.ast.ASTConstant(7));
    }

    @Test
    public void testIsaConstr() {
        // isa_List ((Tree BOT) 5)
        ASTExpression constr = new ASTApplication(
                new ASTApplication(new ASTTyConstr("Tree"), new ASTVariable("bot")),
                new ASTInteger(5));
        ASTExpression program = new ASTApplication(new ASTVariable("isa_constr_List"), constr);

        System.out.println(program);
        lambda.ast.ASTTerm result = program.accept(simpleToLambdaReducer);
        System.out.println("Lam = " + result);

        // in lambda terms: isa_List ((Tree BOT) 5)
        lambda.ast.ASTApplication lambdaConstr = new lambda.ast.ASTApplication(
                new lambda.ast.ASTApplication(new lambda.ast.ASTConstant(tree),
                        new lambda.ast.ASTConstant(PredefinedFunction.BOT)),
                new lambda.ast.ASTConstant(5));
        lambda.ast.ASTApplication lambdaIsaConstr = new lambda.ast.ASTApplication(
                new lambda.ast.ASTConstant(ConstructorReduction.getIsaOperator(list)),
                lambdaConstr);

        assertEquals(result, lambdaIsaConstr);

        // also test if that reduces to false
        ASTTerm reducedResult = reducer.reduceToWHNF(result);
        System.out.println("=> " + reducedResult);
        assertEquals(reducedResult, new lambda.ast.ASTConstant(false));
    }

    @Test
    public void testArgofConstr() {
        // argof_Tree ((Tree BOT) 5)
        ASTExpression constr = new ASTApplication(
                new ASTApplication(new ASTTyConstr("Tree"), new ASTVariable("bot")),
                new ASTInteger(5));
        ASTExpression program = new ASTApplication(new ASTVariable("argof_Tree"), constr);

        System.out.println(program);
        lambda.ast.ASTTerm result = program.accept(simpleToLambdaReducer);
        System.out.println("Lam = " + result);

        // in lambda terms: argof_Tree ((Tree BOT) 5)
        lambda.ast.ASTApplication lambdaConstr = new lambda.ast.ASTApplication(
                new lambda.ast.ASTApplication(new lambda.ast.ASTConstant(tree),
                        new lambda.ast.ASTConstant(PredefinedFunction.BOT)),
                new lambda.ast.ASTConstant(5));
        lambda.ast.ASTApplication lambdaIsaConstr = new lambda.ast.ASTApplication(
                new lambda.ast.ASTConstant(ConstructorReduction.getArgOfOperator(tree)),
                lambdaConstr);

        assertEquals(result, lambdaIsaConstr);

        // also test if that reduces to (BOT, 5)
        ASTTerm reducedResult = reducer.reduceToWHNF(result);
        System.out.println("=> " + reducedResult);

        ASTTerm bot5Tuple = new lambda.ast.ASTApplication(
                new lambda.ast.ASTApplication(
                        new lambda.ast.ASTConstant(TupleReduction.getTupleConstructor(2)),
                        new lambda.ast.ASTConstant(PredefinedFunction.BOT)),
                new lambda.ast.ASTConstant(5));
        assertEquals(reducedResult, bot5Tuple);
    }
}