package lambda;

import lambda.ast.*;
import lambda.reduction.WHNOReducer;
import lambda.reduction.delta.*;

public class Test {
    public static void main(String[] args) {
        WHNOReducer reducer = new WHNOReducer();

        // ##########################################
        ASTTerm lambda = new ASTApplication(new ASTAbstraction(new ASTVariable("x"), new ASTVariable("x")), new ASTConstant("Zero"));
        reducer.reduceToWHNF(lambda, true);

        // ##########################################
        ASTTerm b = new ASTAbstraction(new ASTVariable("y"), new ASTApplication(new ASTVariable("x"), new ASTVariable("y")));
        ASTTerm a = new ASTAbstraction(new ASTVariable("x"), b);
        ASTTerm c = new ASTApplication(a, new ASTVariable("y"));
        lambda = new ASTApplication(c, new ASTConstant(4));
        reducer.reduceToWHNF(lambda, true);

        // ##########################################
        ASTTerm plus = new ASTApplication(new ASTApplication(new ASTConstant(ArithmeticRule.Operator.PLUS), new ASTConstant(40)), new ASTVariable("x"));
        a = new ASTAbstraction(new ASTVariable("x"), plus);
        ASTTerm times = new ASTApplication(new ASTApplication(new ASTConstant(ArithmeticRule.Operator.TIMES), new ASTVariable("y")), new ASTVariable("y"));
        b = new ASTAbstraction(new ASTVariable("y"), times);
        c = new ASTApplication(b, new ASTConstant(3));
        lambda = new ASTApplication(a, c);
        reducer.reduceToWHNF(lambda, true);

        // ##########################################
        ASTTerm sub = new ASTApplication(new ASTApplication(new ASTConstant(ArithmeticRule.Operator.MINUS), new ASTVariable("x")), new ASTConstant(1));
        ASTTerm rec = new ASTApplication(new ASTVariable("fact"), sub);
        ASTTerm less = new ASTApplication(new ASTConstant(BoolNotRule.Operator.NOT), new ASTApplication(new ASTApplication(new ASTConstant(ArithmeticRule.Operator.GREATER), new ASTVariable("x")), new ASTConstant(0)));
        ASTTerm cond = new ASTApplication(new ASTConstant(BranchRule.Operator.IF), less);
        ASTTerm condIf = new ASTApplication(cond, new ASTConstant(1));
        ASTTerm val = new ASTApplication(new ASTApplication(new ASTConstant(ArithmeticRule.Operator.TIMES), rec), new ASTVariable("x"));
        ASTTerm branch = new ASTApplication(condIf, val);
        ASTTerm fact = new ASTAbstraction(new ASTVariable("fact"), new ASTAbstraction(new ASTVariable("x"), branch));
        ASTTerm fixFact = new ASTApplication(new ASTConstant(FixRule.Operator.FIX), fact);
        lambda = new ASTApplication(fixFact, new ASTConstant(5));
        System.out.println(lambda);
        lambda = reducer.reduceToWHNF(lambda);
        System.out.println(" => " + lambda);

        // ############################################
        a = new ASTAbstraction(new ASTVariable("x"), new ASTVariable("y"));
        b = new ASTAbstraction(new ASTVariable("x"), new ASTApplication(new ASTVariable("x"), new ASTVariable("x")));
        c = new ASTAbstraction(new ASTVariable("x"), new ASTApplication(new ASTVariable("x"), new ASTVariable("x")));
        ASTTerm d = new ASTApplication(b, c);
        lambda = new ASTApplication(a, d);
        reducer.reduceToWHNF(lambda, true);
    }
}
