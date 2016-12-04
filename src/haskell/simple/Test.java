package haskell.simple;

import haskell.simple.ast.*;
import lambda.reduction.WHNOReducer;
import lambda.reduction.delta.ArithmeticRule;
import lambda.reduction.delta.TupleRule;

import java.util.ArrayList;

public class Test {
    public static void main(String[] args) {
        WHNOReducer reducer = new WHNOReducer();
        TupleRule tr3 = new TupleRule(3);
        TupleRule tr2 = new TupleRule(2);
        reducer.addDeltaRule(tr3);
        reducer.addDeltaRule(tr2);

        // ###############################
        String programText = "let fact = \\x -> if x <= 0 then 1 else fact(x − 1) ∗ x in fact 5";

        ASTExpression cond = new ASTApplication(new ASTApplication(new ASTConstant(ArithmeticRule.Operator.LESSEQ), new ASTVariable("x")), new ASTConstant(0));
        ASTExpression ifBranch = new ASTConstant(1);
        ASTExpression minus = new ASTApplication(new ASTApplication(new ASTConstant(ArithmeticRule.Operator.MINUS), new ASTVariable("x")), new ASTConstant(1));
        ASTExpression rec = new ASTApplication(new ASTVariable("fact"), minus);
        ASTExpression elseBranch = new ASTApplication(new ASTApplication(new ASTConstant(ArithmeticRule.Operator.TIMES), rec), new ASTVariable("x"));
        ASTExpression fact = new ASTBranch(cond, ifBranch, elseBranch);
        ASTExpression factFunc = new ASTFunction(new ASTVariable("x"), fact);
        ASTExpression program = new ASTLet(new ASTVariable("fact"), factFunc, new ASTApplication(new ASTVariable("fact"), new ASTConstant(5)));

        System.out.println(program);
        lambda.ast.ASTTerm lambda = program.toLambdaTerm();
        System.out.println("Lam = " + lambda);
        System.out.println(" => " +reducer.reduceToWHNF(lambda));

        // ################################
        programText = "isa_3-tuple (5, \\x -> x, 'c')";

        ArrayList<ASTExpression> expressions = new ArrayList<>();
        expressions.add(new ASTConstant(5));
        expressions.add(new ASTFunction(new ASTVariable("x"), new ASTVariable("x")));
        expressions.add(new ASTConstant('c'));
        ASTExpression tuple = new ASTTuple(expressions);
        program = new ASTApplication(new ASTConstant(tr3.getIsaOperator()), tuple);

        System.out.println(program);
        lambda = program.toLambdaTerm();
        System.out.println("Lam = " + lambda);
        System.out.println(" => " +reducer.reduceToWHNF(lambda));

        // #################################
        programText = "sel_3,2 (5, \\x -> x, 'c')";

        program = new ASTApplication(new ASTConstant(tr3.getSelOperator(2)), tuple);
        System.out.println(program);
        lambda = program.toLambdaTerm();
        System.out.println("Lam = " + lambda);
        System.out.println(" => " +reducer.reduceToWHNF(lambda));
    }
}
