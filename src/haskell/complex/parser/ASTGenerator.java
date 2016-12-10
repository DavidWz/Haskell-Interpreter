package haskell.complex.parser;

import haskell.complex.ast.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Generates the ast structure of a complex haskell program.
 */
public class ASTGenerator {

    public static class ProgramVisitor extends ComplexHaskellBaseVisitor<ASTProgram> {
        @Override
        public ASTProgram visitProgram(ComplexHaskellParser.ProgramContext ctx) {
            DeclVisitor declVisitor = new DeclVisitor();
            List<ASTDecl> decls = ctx.decl().stream().
                    map(decl -> decl.accept(declVisitor)).
                    collect(Collectors.toList());
            return new ASTProgram(decls);
        }
    }

    public static class DeclVisitor extends ComplexHaskellBaseVisitor<ASTDecl> {
        @Override
        public ASTDecl visitDecl(ComplexHaskellParser.DeclContext ctx) {
            // TODO: generate decl
            return null;
        }
    }

    // TODO: visitors for other classes
}
