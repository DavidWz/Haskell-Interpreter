package haskell;

import haskell.ast.ASTDecl;
import haskell.ast.ASTExpression;
import haskell.ast.ASTProgram;
import haskell.parser.ASTGenerator;
import lambda.ast.ASTTerm;
import lambda.type.TypeException;
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
    public static final String QUIT_COMMAND = ":quit";
    public static final String LOAD_COMMAND = ":load";
    public static final String HELP_COMMAND = ":help";
    public static final String VERBOSE_COMMAND = ":verbose";
    public static final String HELP_URL = "https://github.com/DavidWz/Haskell-Interpreter";

    private ASTGenerator astGenerator;
    private HaskellInterpreter interpreter;
    private BufferedReader bufferedReader;
    private boolean verbose;

    public InteractiveEnvironment() {
        this.astGenerator = new ASTGenerator();
        this.interpreter = new HaskellInterpreter();
        this.bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        this.verbose = false;
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
                endProgram = handleLine(line);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Handles a user input line.
     * @param line the line
     * @return whether the program should be exited
     */
    private boolean handleLine(String line) {
        if (line.equals(QUIT_COMMAND)) {
            return true;
        }
        else if(line.equals(HELP_COMMAND)) {
            printHelpMessage();
        }
        else if(line.equals(VERBOSE_COMMAND)) {
            verbose = !verbose;
            if (verbose) {
                System.out.println("Verbose: On.");
            }
            else {
                System.out.println("Verbose: Off.");
            }
        }
        else if (line.startsWith(LOAD_COMMAND) && line.length() > LOAD_COMMAND.length()) {
            // +1 because space between :load <filename>
            String fileName = line.substring(LOAD_COMMAND.length()+1);
            loadProgramFromFile(fileName);
        }
        else {
            handleHaskell(line);
        }

        return false;
    }

    /**
     * Prints a help message to the console.
     */
    private void printHelpMessage() {
        StringBuilder msg = new StringBuilder();
        msg.append("Type \"" + QUIT_COMMAND + "\" to exit the interactive environment.\n");
        msg.append("Type \"" + LOAD_COMMAND + " <filename>\" to load a program from a file.\n");
        msg.append("For further information, please refer to " + HELP_URL);
        System.out.println(msg.toString());
    }

    /**
     * Loads the program in the file specified by the fileName.
     * @param fileName
     */
    private void loadProgramFromFile(String fileName) {
        ANTLRFileStream fileStream;

        try {
            fileStream = new ANTLRFileStream(fileName);
        } catch (IOException e) {
            System.out.println("Error: Could not load file.");
            return;
        }

        Optional<ASTProgram> program = astGenerator.parseProgram(fileStream);
        if (program.isPresent()) {
            try {
                interpreter.addProgram(program.get());
            } catch (TypeException.InconsistentDataDeclException e) {
                System.out.println("Error: " + e.getMessage());
            }
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

    /**
     * Handles a haskell expression or declaration.
     * @param line
     */
    private void handleHaskell(String line) {
        CharStream charStream = new ANTLRInputStream(line);

        // try to parse the input as a declaration
        Optional<ASTDecl> declaration = astGenerator.parseDeclaration(charStream);
        if (declaration.isPresent()) {
            try {
                interpreter.addDeclaration(declaration.get());
            } catch (TypeException.InconsistentDataDeclException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        else {
            // rebuild the char stream
            charStream = new ANTLRInputStream(line);

            // try to parse the input as an expression
            Optional<ASTExpression> expression = astGenerator.parseExpression(charStream);
            if (expression.isPresent()) {
                try {
                    ASTTerm result = interpreter.evaluate(expression.get(), verbose);
                    if (!verbose) {
                        System.out.println(result);
                    }
                } catch (TypeException e) {
                    System.out.println("Error: The expression was incorrectly typed. Type \""+HELP_COMMAND+"\" for help.");
                } catch (Exception e) {
                    System.out.println("Unexpected error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            else {
                System.out.println("Syntax error. Type \""+HELP_COMMAND+"\" for help.");
            }
        }
    }

    public static void main(String[] args) {
        InteractiveEnvironment ie = new InteractiveEnvironment();
        ie.start();
    }
}
