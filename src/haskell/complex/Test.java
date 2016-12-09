package haskell.complex;

import haskell.HaskellInterpreter;
import haskell.complex.ast.*;
import haskell.complex.reduction.SimpleReducer;

import java.util.Set;

public class Test {
    public static void main(String[] args) {
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
        ASTVariable times = new ASTVariable("times");
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

        ASTExpression eval = new ASTApplication(fact, new ASTApplication(plus, new ASTApplication(toInt, new ASTApplication(even, new ASTInteger(6))), new ASTInteger(3)));

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
