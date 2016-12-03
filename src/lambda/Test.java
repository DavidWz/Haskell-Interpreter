package lambda;

import lambda.ast.*;
import lambda.reduction.WHNOReducer;

import java.util.Optional;

public class Test {
    public static void main(String[] args) {
        WHNOReducer reducer = new WHNOReducer();

        ASTTerm lambda = new ASTApplication(new ASTAbstraction(new ASTVariable("x"), new ASTVariable("x")), new ASTConstant("Zero"));
        System.out.println(lambda);
        Optional<ASTTerm> result = reducer.applyWHNOReduction(lambda);
        do {
            System.out.println(" => " + result.get());
            result = reducer.applyWHNOReduction(result.get());
        } while(result.isPresent());

        ASTAbstraction b = new ASTAbstraction(new ASTVariable("y"), new ASTApplication(new ASTVariable("x"), new ASTVariable("y")));
        ASTAbstraction a = new ASTAbstraction(new ASTVariable("x"), b);
        ASTApplication c = new ASTApplication(a, new ASTVariable("y"));
        lambda = new ASTApplication(c, new ASTConstant("4"));
        System.out.println(lambda);
        result = reducer.applyWHNOReduction(lambda);
        do {
            System.out.println(" => " + result.get());
            result = reducer.applyWHNOReduction(result.get());
        } while(result.isPresent());

        ASTTerm plus = new ASTApplication(new ASTApplication(new ASTConstant("plus"), new ASTConstant("40")), new ASTVariable("x"));
        a = new ASTAbstraction(new ASTVariable("x"), plus);
        ASTTerm times = new ASTApplication(new ASTApplication(new ASTConstant("times"), new ASTVariable("y")), new ASTVariable("y"));
        b = new ASTAbstraction(new ASTVariable("y"), times);
        c = new ASTApplication(b, new ASTConstant("3"));
        lambda = new ASTApplication(a, c);

        System.out.println(lambda);
        result = reducer.applyWHNOReduction(lambda);
        do {
            System.out.println(" => " + result.get());
            result = reducer.applyWHNOReduction(result.get());
        } while(result.isPresent());
    }
}
