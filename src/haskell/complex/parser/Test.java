package haskell.complex.parser;

import haskell.HaskellInterpreter;
import haskell.complex.ast.ASTExpression;
import haskell.complex.ast.ASTProgram;
import haskell.complex.reduction.TooComplexException;
import org.antlr.v4.runtime.ANTLRInputStream;

public class Test {
    public static void main(String[] args) {
        ASTGenerator gen = new ASTGenerator();

        String sourceCode = "fact 0 = 1\n" +
                "fact x = (times x (fact (decr x))) \n" +
                "decr x = (minus x 1)";

        String evalCode = "(fact (decr 6))";

        ASTProgram program = gen.parseProgram(new ANTLRInputStream(sourceCode)).get();
        ASTExpression eval = gen.parseExpression(new ANTLRInputStream(evalCode)).get();

        System.out.println(program);
        System.out.print("eval["+eval+"] = ");

        HaskellInterpreter interpreter = new HaskellInterpreter(program);
        try {
            System.out.println(interpreter.evaluate(eval));
        } catch (TooComplexException e) {
            System.out.println("\n"+e.getMessage());
        }
    }
}
