package haskell.complex.ast;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the root node of a haskell program.
 */
public class ASTProgram {
    private List<ASTDecl> decls;

    public ASTProgram() {
        this.decls = new ArrayList<>();
    }

    public void addDeclaration(ASTDecl decl) {
        decls.add(decl);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (ASTDecl decl : decls) {
            builder.append(decl);
            builder.append("\n");
        }
        return builder.toString();
    }
}
