package haskell;

import haskell.complex.ast.ASTDecl;
import haskell.complex.ast.ASTExpression;
import haskell.complex.parser.ASTGenerator;
import haskell.complex.reduction.TooComplexException;
import lambda.ast.ASTTerm;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;

/**
 * An interactive environment to write haskell programs and evaluate expressions.
 */
public class InteractiveEnvironment {
    private ASTGenerator astGenerator;
    private HaskellInterpreter interpreter;
    private BufferedReader bufferedReader;
    private final String QUIT_COMMAND = ":quit";

    public InteractiveEnvironment() {
        this.astGenerator = new ASTGenerator();
        this.interpreter = new HaskellInterpreter();
        this.bufferedReader = new BufferedReader(new InputStreamReader(System.in));
    }

    /**
     * Starts the interactive environment.
     */
    public void start() {
        boolean endProgram = false;

        while(!endProgram) {
            // read a line of text
            System.out.print("> ");
            String line;

            try {
                line = readLine();

                // check if the user wants to quit
                if (line.equals(QUIT_COMMAND)) {
                    endProgram = true;
                }
                // TODO: add: load program from file
                else {
                    handleLine(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Reads a line from the console.
     * @return the line
     */
    public String readLine() throws IOException {
        return bufferedReader.readLine();
    }

    private void handleLine(String line) {
        CharStream charStream = new ANTLRInputStream(line);

        // try to parse the input as a declaration
        Optional<ASTDecl> declaration = astGenerator.parseDeclaration(charStream);
        if (declaration.isPresent()) {
            interpreter.addDeclaration(declaration.get());
        }
        else {
            // rebuild the char stream
            charStream = new ANTLRInputStream(line);

            // try to parse the input as an expression
            Optional<ASTExpression> expression = astGenerator.parseExpression(charStream);
            if (expression.isPresent()) {
                try {
                    ASTTerm result = interpreter.evaluate(expression.get());
                    System.out.println(result);
                } catch (TooComplexException e) {
                    System.out.println("Error: Could not evaluate the expression.");
                }
            }
            else {
                System.out.println("Error: Input must be a declaration or expression.");
            }
        }
    }

    public static void main(String[] args) {
        InteractiveEnvironment ie = new InteractiveEnvironment();
        ie.start();
    }
}
