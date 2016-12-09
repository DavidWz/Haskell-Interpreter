package haskell.complex;

import haskell.HaskellInterpreter;
import haskell.complex.ast.*;
import haskell.complex.reduction.SimpleReducer;

public class Test {
    public static void main(String[] args) {
        ASTProgram prog = new ASTProgram();

        // square 0 = 0
        ASTVariable square = new ASTVariable("square");
        ASTFunDecl squareBasis = new ASTFunDecl(new ASTInteger(0), square, new ASTInteger(0));
        prog.addDeclaration(squareBasis);

        // square x = times x x
        ASTVariable x = new ASTVariable("x");
        ASTVariable times = new ASTVariable("times");
        ASTFunDecl squareFunc = new ASTFunDecl(new ASTApplication(times, x, x), square, x);
        prog.addDeclaration(squareFunc);

        // fact x = if x <= 0 then 1 else fact(x-1)*x
        ASTVariable fact = new ASTVariable("fact");
        ASTExpression lesseq_x_0 = new ASTApplication(new ASTVariable("lesseq"), x, new ASTInteger(0));
        ASTExpression fact_decr_x = new ASTApplication(fact, new ASTApplication(new ASTVariable("minus"), x, new ASTInteger(1)));
        ASTExpression factRec = new ASTApplication(times, fact_decr_x, x);
        ASTFunDecl factFunc = new ASTFunDecl(new ASTBranch(lesseq_x_0, new ASTInteger(1), factRec), fact, x);
        prog.addDeclaration(factFunc);

        // len Nil = 0
        ASTVariable len = new ASTVariable("len");
        ASTTypeConstr Nil = new ASTTypeConstr("Nil");
        ASTFunDecl lenNil = new ASTFunDecl(new ASTInteger(0), len, new ASTConstruct(Nil));
        prog.addDeclaration(lenNil);

        // append Nil z = (Cons z Nil)
        ASTTypeConstr Cons = new ASTTypeConstr("Cons");
        ASTVariable append = new ASTVariable("append");
        ASTVariable z = new ASTVariable("z");
        ASTFunDecl appendFuncNil = new ASTFunDecl(new ASTApplication(Cons, z, Nil), append, new ASTConstruct(Nil), z);
        prog.addDeclaration(appendFuncNil);

        // len (Cons _ xs) = (len xs) + 1
        ASTVariable xs = new ASTVariable("xs");
        ASTVariable plus = new ASTVariable("plus");
        ASTFunDecl lenCons = new ASTFunDecl(new ASTApplication(plus, new ASTApplication(len, xs), new ASTInteger(1)), len, new ASTConstruct(Cons, new ASTJoker(), xs));
        prog.addDeclaration(lenCons);

        // append (Cons x y) z = Cons x (append y z)
        ASTVariable y = new ASTVariable("y");
        ASTFunDecl appendFuncCons = new ASTFunDecl(new ASTApplication(Cons, x, new ASTApplication(append, y, z)), append, new ASTConstruct(Cons, x, y), z);
        prog.addDeclaration(appendFuncCons);

        // list3 = (Cons 3) ((Cons 2) ((Cons 1) Nil)))
        ASTExpression list1 = new ASTApplication(new ASTApplication(Cons, new ASTInteger(1)), Nil);
        ASTExpression list2 = new ASTApplication(new ASTApplication(Cons, new ASTInteger(2)), list1);
        ASTExpression list3 = new ASTApplication(new ASTApplication(Cons, new ASTInteger(3)), list2);

        // list 4 = append list3 4
        ASTExpression list4 = new ASTApplication(append, list3, new ASTInteger(4));

        // several test expressions
        ASTExpression square2 = new ASTApplication(square, new ASTInteger(2));
        ASTExpression lenList3 = new ASTApplication(len, list3);
        ASTExpression lenList1 = new ASTApplication(len, list1);
        ASTExpression lenList4 = new ASTApplication(len, list4);
        ASTExpression squareLenList3 = new ASTApplication(square, lenList3);
        ASTExpression squareLenList4 = new ASTApplication(square, lenList4);
        ASTExpression factLenList3 = new ASTApplication(fact, lenList3);

        ASTExpression eval = factLenList3;

        System.out.println(prog);
        System.out.print("\neval[" + eval + "] = ");

        // evaluate the expression
        HaskellInterpreter interpreter = new HaskellInterpreter(prog);
        try {
            System.out.println(interpreter.evaluate(eval));
        } catch (SimpleReducer.TooComplexException e) {
            System.out.println("\n"+e.getMessage());
        }
    }
}
