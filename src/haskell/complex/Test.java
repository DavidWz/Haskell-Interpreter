package haskell.complex;

import haskell.HaskellInterpreter;
import haskell.complex.ast.*;
import haskell.complex.reduction.SimpleReducer;

public class Test {
    public static void main(String[] args) {
        ASTProgram prog = new ASTProgram();

        // square x = times x x
        ASTVariable x = new ASTVariable("x");
        ASTVariable times = new ASTVariable("times");
        ASTVariable square = new ASTVariable("square");
        ASTFunDecl squareFunc = new ASTFunDecl(new ASTApplication(times, x, x), square, x);
        prog.addDeclaration(squareFunc);

        // len Nil = 0
        ASTVariable len = new ASTVariable("len");
        ASTTypeConstr Nil = new ASTTypeConstr("Nil");
        ASTFunDecl lenNil = new ASTFunDecl(new ASTInteger(0), len, Nil);
        prog.addDeclaration(lenNil);

        // append Nil z = z
        ASTVariable append = new ASTVariable("append");
        ASTVariable z = new ASTVariable("z");
        ASTFunDecl appendFuncNil = new ASTFunDecl(z, append, Nil, z);
        prog.addDeclaration(appendFuncNil);

        // len (Cons x xs) = (len xs) + 1
        ASTTypeConstr Cons = new ASTTypeConstr("Cons");
        ASTVariable xs = new ASTVariable("xs");
        ASTVariable plus = new ASTVariable("plus");
        ASTFunDecl lenCons = new ASTFunDecl(new ASTApplication(plus, new ASTApplication(len, xs), new ASTInteger(1)), len, new ASTConstruct(Cons, x, xs));
        prog.addDeclaration(lenCons);

        // append (Cons x y) z = Cons x (append y z)
        ASTVariable y = new ASTVariable("y");
        ASTFunDecl appendFuncCons = new ASTFunDecl(new ASTApplication(Cons, x, new ASTApplication(append, y, z)), append, new ASTConstruct(Cons, x, y), z);
        prog.addDeclaration(appendFuncCons);

        // list3 = (Cons 3) ((Cons 2) ((Cons 1) Nil)))
        ASTExpression list1 = new ASTApplication(new ASTApplication(Cons, new ASTInteger(1)), Nil);
        ASTExpression list2 = new ASTApplication(new ASTApplication(Cons, new ASTInteger(2)), list1);
        ASTExpression list3 = new ASTApplication(new ASTApplication(Cons, new ASTInteger(3)), list2);

        // square the length of the list: square (len list3)
        ASTExpression squareLenList3 = new ASTApplication(square, new ASTApplication(len, list3));
        System.out.println(prog);

        // evaluate the expression
        HaskellInterpreter interpreter = new HaskellInterpreter(prog);
        try {
            interpreter.evaluate(squareLenList3, true);
        } catch (SimpleReducer.TooComplexException e) {
            System.out.println(e.getMessage());
        }
    }
}
