package lambda;

import lambda.ast.*;
import lambda.reduction.WHNOReduction;

public class Test {
    public static void main(String[] args) {
        ASTTerm lambda = new ASTApplication(new ASTAbstraction(new ASTVariable("x"), new ASTVariable("x")), new ASTConstant("Zero"));
        System.out.println(lambda);
        System.out.println(" => " + WHNOReduction.applyBetaReduction(lambda));

        ASTAbstraction b = new ASTAbstraction(new ASTVariable("y"), new ASTApplication(new ASTVariable("x"), new ASTVariable("y")));
        ASTAbstraction a = new ASTAbstraction(new ASTVariable("x"), b);
        lambda = new ASTApplication(a, new ASTVariable("y"));
        System.out.println(lambda);
        System.out.println(" => " + WHNOReduction.applyBetaReduction(lambda));

        ASTTerm plus = new ASTApplication(new ASTApplication(new ASTConstant("plus"), new ASTVariable("x")), new ASTConstant("1"));
        a = new ASTAbstraction(new ASTVariable("x"), plus);
        ASTTerm times = new ASTApplication(new ASTApplication(new ASTConstant("times"), new ASTVariable("y")), new ASTVariable("y"));
        b = new ASTAbstraction(new ASTVariable("y"), times);
        ASTTerm c = new ASTApplication(b, new ASTConstant("3"));
        lambda = new ASTApplication(a, c);
        System.out.println(lambda);
        System.out.println(" => " + WHNOReduction.applyBetaReduction(lambda));
    }
}
