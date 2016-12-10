package haskell.complex.parser;

import haskell.complex.ast.ASTProgram;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;

public class Test {
    public static void main(String[] args) {
        String sourceCode = "fact 0 = 1\n" +
                "fact x = (times x (fact (decr x))) \n" +
                "decr x = (minus x 1)";

        ComplexHaskellLexer lexer = new ComplexHaskellLexer(new ANTLRInputStream(sourceCode));
        TokenStream tokens = new CommonTokenStream(lexer);
        ComplexHaskellParser parser = new ComplexHaskellParser(tokens);

        ASTGenerator.ProgramVisitor progParser = new ASTGenerator.ProgramVisitor();
        ASTProgram program = progParser.visit(parser.program());

        System.out.println(program);
    }
}
