package haskell.complex;

import haskell.complex.ast.*;

import java.util.Arrays;
import java.util.List;

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

        // append Nil ys = ys
        ASTVariable append = new ASTVariable("append");
        ASTVariable ys = new ASTVariable("ys");
        ASTFunDecl appendFuncNil = new ASTFunDecl(ys, append, Nil, ys);
        prog.addDeclaration(appendFuncNil);

        // append (Cons x ys) ys = Cons x (append xs ys)
        ASTVariable xs = new ASTVariable("xs");
        ASTTypeConstr Cons = new ASTTypeConstr("Cons");
        ASTFunDecl appendFuncCons = new ASTFunDecl(new ASTApplication(Cons, x, new ASTApplication(append, xs, ys)), append, new ASTConstruct(Cons, x, xs), ys);
        prog.addDeclaration(appendFuncCons);

        System.out.println(prog);
    }
}
