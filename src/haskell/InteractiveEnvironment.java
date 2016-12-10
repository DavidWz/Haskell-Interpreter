package haskell;

import haskell.complex.ast.ASTDecl;
import haskell.complex.ast.ASTExpression;
import haskell.complex.ast.ASTProgram;
import haskell.complex.parser.ASTGenerator;
import haskell.complex.reduction.TooComplexException;
import lambda.ast.ASTTerm;
import org.antlr.v4.runtime.ANTLRFileStream;
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
    private final String LOAD_COMMAND = ":load";

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
                // check if the user wants to load a program
                else if (line.startsWith(LOAD_COMMAND)) {
                    // +1 because space between :load <filename>
                    String fileName = line.substring(LOAD_COMMAND.length()+1);
                    load(fileName);
                }
                else {
                    handleLine(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Loads the program in the file specifiec by the fileName.
     * @param fileName
     */
    private void load(String fileName) {
        ANTLRFileStream fileStream;

        try {
            fileStream = new ANTLRFileStream(fileName);
        } catch (IOException e) {
            System.out.println("Error: Could not load file.");
            return;
        }

        Optional<ASTProgram> program = astGenerator.parseProgram(fileStream);
        if (program.isPresent()) {
            interpreter.addProgram(program.get());
        }
        else {
            System.out.println("Error: The file did not contain a syntactically correct program.");
        }
    }

    /**
     * Reads a line from the console.
     * @return the line
     */
    private String readLine() throws IOException {
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
