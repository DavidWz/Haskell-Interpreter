package haskell.complex.parser;

import haskell.complex.ast.*;
import org.antlr.v4.runtime.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Generates the ast structure of a complex haskell program.
 */
public class ASTGenerator {
    public ASTGenerator() {
    }

    /**
     * Parses program code to an ASTProgram.
     * @param charStream the input char stream
     * @return the ast of the program or empty if it could not be parsed
     */
    public Optional<ASTProgram> parseProgram(CharStream charStream) {
        ComplexHaskellLexer lexer = new ComplexHaskellLexer(charStream);
        TokenStream tokens = new CommonTokenStream(lexer);
        lexer.removeErrorListener(ConsoleErrorListener.INSTANCE);
        ComplexHaskellParser parser = new ComplexHaskellParser(tokens);
        parser.removeErrorListener(ConsoleErrorListener.INSTANCE);

        ASTGenerator.ProgramVisitor progParser = new ASTGenerator.ProgramVisitor();
        try {
            return Optional.of(progParser.visit(parser.program()));
        }
        catch(Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Parses a declaration to an ASTDecl.
     * @param charStream the input char stream
     * @return the ast of the declaration or empty if it could not be parsed
     */
    public Optional<ASTDecl> parseDeclaration(CharStream charStream) {
        ComplexHaskellLexer lexer = new ComplexHaskellLexer(charStream);
        lexer.removeErrorListener(ConsoleErrorListener.INSTANCE);
        TokenStream tokens = new CommonTokenStream(lexer);
        ComplexHaskellParser parser = new ComplexHaskellParser(tokens);
        parser.removeErrorListener(ConsoleErrorListener.INSTANCE);

        ASTGenerator.DeclVisitor declParser = new ASTGenerator.DeclVisitor();
        try {
            return Optional.of(declParser.visit(parser.decl()));
        }
        catch(Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Parses an expression to an ASTExpression.
     * @param charStream the input char stream
     * @return the ast of the expression or empty if it could not be parsed
     */
    public Optional<ASTExpression> parseExpression(CharStream charStream) {
        ComplexHaskellLexer lexer = new ComplexHaskellLexer(charStream);
        TokenStream tokens = new CommonTokenStream(lexer);
        lexer.removeErrorListener(ConsoleErrorListener.INSTANCE);
        ComplexHaskellParser parser = new ComplexHaskellParser(tokens);
        parser.removeErrorListener(ConsoleErrorListener.INSTANCE);

        ASTGenerator.ExpVisitor expParser = new ASTGenerator.ExpVisitor();
        try {
            return Optional.of(expParser.visit(parser.exp()));
        }
        catch(Exception e) {
            return Optional.empty();
        }
    }

    private static class ProgramVisitor extends ComplexHaskellBaseVisitor<ASTProgram> {
        @Override
        public ASTProgram visitProgram(ComplexHaskellParser.ProgramContext ctx) {
            DeclVisitor declVisitor = new DeclVisitor();
            List<ASTDecl> decls = ctx.decl().stream().
                    map(decl -> decl.accept(declVisitor)).
                    collect(Collectors.toList());
            return new ASTProgram(decls);
        }
    }

    private static class DeclVisitor extends ComplexHaskellBaseVisitor<ASTDecl> {
        @Override
        public ASTDecl visitDecl(ComplexHaskellParser.DeclContext ctx) {
            if (ctx.fundecl() != null) {
                FunDeclVisitor funDeclVisitor = new FunDeclVisitor();
                return ctx.fundecl().accept(funDeclVisitor);
            } else if (ctx.patdecl() != null) {
                PatDeclVisitor patDeclVisitor = new PatDeclVisitor();
                return ctx.patdecl().accept(patDeclVisitor);
            } else {
                throw new RuntimeException();
            }
        }
    }

    private static class FunDeclVisitor extends ComplexHaskellBaseVisitor<ASTFunDecl> {
        @Override
        public ASTFunDecl visitFundecl(ComplexHaskellParser.FundeclContext ctx) {
            String varID = ctx.var().getText();
            ASTVariable var = new ASTVariable(varID);

            PatVisitor patVisitor = new PatVisitor();
            List<ASTPattern> pats = ctx.pat().stream().
                    map(pat -> pat.accept(patVisitor)).
                    collect(Collectors.toList());

            ExpVisitor expVisitor = new ExpVisitor();
            ASTExpression exp = ctx.exp().accept(expVisitor);

            return new ASTFunDecl(var, pats, exp);
        }
    }

    private static class PatDeclVisitor extends ComplexHaskellBaseVisitor<ASTPatDecl> {
        @Override
        public ASTPatDecl visitPatdecl(ComplexHaskellParser.PatdeclContext ctx) {
            PatVisitor patVisitor = new PatVisitor();
            ASTPattern pat = ctx.pat().accept(patVisitor);

            ExpVisitor expVisitor = new ExpVisitor();
            ASTExpression exp = ctx.exp().accept(expVisitor);

            return new ASTPatDecl(pat, exp);
        }
    }

    private static class PatVisitor extends ComplexHaskellBaseVisitor<ASTPattern> {
        @Override
        public ASTPattern visitPat(ComplexHaskellParser.PatContext ctx) {
            if (ctx.var() != null) {
                String varID = ctx.var().getText();
                return new ASTVariable(varID);
            } else if (ctx.tyconstr() != null) {
                String tyConstrID = ctx.tyconstr().getText();
                return new ASTTypeConstr(tyConstrID);
            } else if (ctx.joker() != null) {
                return new ASTJoker();
            } else if (ctx.integer() != null) {
                String intId = ctx.integer().getText();
                int n = Integer.parseInt(intId);
                return new ASTInteger(n);
            } else if (ctx.bool() != null) {
                String bool = ctx.bool().getText();
                boolean b = bool.equals("True");
                return new ASTBoolean(b);
            } else if (ctx.patTuple() != null) {
                PatTupleVisitor patTupleVisitor = new PatTupleVisitor();
                return ctx.patTuple().accept(patTupleVisitor);
            } else if (ctx.construct() != null) {
                ConstructVisitor constructVisitor = new ConstructVisitor();
                return ctx.construct().accept(constructVisitor);
            } else {
                throw new RuntimeException();
            }
        }
    }

    private static class PatTupleVisitor extends ComplexHaskellBaseVisitor<ASTPatTuple> {
        @Override
        public ASTPatTuple visitPatTuple(ComplexHaskellParser.PatTupleContext ctx) {
            PatVisitor patVisitor = new PatVisitor();
            List<ASTPattern> pats = ctx.pat().stream().
                    map(pat -> pat.accept(patVisitor)).
                    collect(Collectors.toList());
            return new ASTPatTuple(pats);
        }
    }

    private static class ConstructVisitor extends ComplexHaskellBaseVisitor<ASTConstruct> {
        @Override
        public ASTConstruct visitConstruct(ComplexHaskellParser.ConstructContext ctx) {
            String tyConstrID = ctx.tyconstr().getText();
            ASTTypeConstr typeConstr = new ASTTypeConstr(tyConstrID);

            PatVisitor patVisitor = new PatVisitor();
            List<ASTPattern> pats = ctx.pat().stream().
                    map(pat -> pat.accept(patVisitor)).
                    collect(Collectors.toList());

            return new ASTConstruct(typeConstr, pats);
        }
    }

    private static class ExpVisitor extends ComplexHaskellBaseVisitor<ASTExpression> {
        @Override
        public ASTExpression visitExp(ComplexHaskellParser.ExpContext ctx) {
            if (ctx.var() != null) {
                String varID = ctx.var().getText();
                return new ASTVariable(varID);
            } else if (ctx.tyconstr() != null) {
                String tyConstrID = ctx.tyconstr().getText();
                return new ASTTypeConstr(tyConstrID);
            } else if (ctx.integer() != null) {
                String intId = ctx.integer().getText();
                int n = Integer.parseInt(intId);
                return new ASTInteger(n);
            } else if (ctx.bool() != null) {
                String bool = ctx.bool().getText();
                boolean b = bool.equals("True");
                return new ASTBoolean(b);
            } else if (ctx.expTuple() != null) {
                ExpTupleVisitor expTupleVisitor = new ExpTupleVisitor();
                return ctx.expTuple().accept(expTupleVisitor);
            } else if (ctx.application() != null) {
                ApplicationVisitor applicationVisitor = new ApplicationVisitor();
                return ctx.application().accept(applicationVisitor);
            } else if (ctx.branch() != null) {
                BranchVisitor branchVisitor = new BranchVisitor();
                return ctx.branch().accept(branchVisitor);
            } else if (ctx.let() != null) {
                LetVisitor letVisitor = new LetVisitor();
                return ctx.let().accept(letVisitor);
            } else if (ctx.cases() != null) {
                CasesVisitor casesVisitor = new CasesVisitor();
                return ctx.cases().accept(casesVisitor);
            } else if (ctx.lambda() != null) {
                LambdaVisitor lambdaVisitor = new LambdaVisitor();
                return ctx.lambda().accept(lambdaVisitor);
            } else {
                throw new RuntimeException();
            }
        }
    }

    private static class ExpTupleVisitor extends ComplexHaskellBaseVisitor<ASTExpTuple> {
        @Override
        public ASTExpTuple visitExpTuple(ComplexHaskellParser.ExpTupleContext ctx) {
            ExpVisitor expVisitor = new ExpVisitor();
            List<ASTExpression> exps = ctx.exp().stream().
                    map(exp -> exp.accept(expVisitor)).
                    collect(Collectors.toList());
            return new ASTExpTuple(exps);
        }
    }

    private static class ApplicationVisitor extends ComplexHaskellBaseVisitor<ASTApplication> {
        @Override
        public ASTApplication visitApplication(ComplexHaskellParser.ApplicationContext ctx) {
            ExpVisitor expVisitor = new ExpVisitor();
            List<ASTExpression> exps = ctx.exp().stream().
                    map(exp -> exp.accept(expVisitor)).
                    collect(Collectors.toList());
            return new ASTApplication(exps);
        }
    }

    private static class BranchVisitor extends ComplexHaskellBaseVisitor<ASTBranch> {
        @Override
        public ASTBranch visitBranch(ComplexHaskellParser.BranchContext ctx) {
            ExpVisitor expVisitor = new ExpVisitor();
            ASTExpression condition = ctx.exp(0).accept(expVisitor);
            ASTExpression ifBranch = ctx.exp(1).accept(expVisitor);
            ASTExpression elseBranch = ctx.exp(2).accept(expVisitor);
            return new ASTBranch(condition, ifBranch, elseBranch);
        }
    }

    private static class LetVisitor extends ComplexHaskellBaseVisitor<ASTLet> {
        @Override
        public ASTLet visitLet(ComplexHaskellParser.LetContext ctx) {
            DeclsVisitor declsVisitor = new DeclsVisitor();
            List<ASTDecl> decls = ctx.decls().accept(declsVisitor);

            ExpVisitor expVisitor = new ExpVisitor();
            ASTExpression exp = ctx.exp().accept(expVisitor);

            return new ASTLet(decls, exp);
        }
    }

    private static class DeclsVisitor extends ComplexHaskellBaseVisitor<List<ASTDecl>> {
        @Override
        public List<ASTDecl> visitDecls(ComplexHaskellParser.DeclsContext ctx) {
            DeclVisitor declVisitor = new DeclVisitor();
            List<ASTDecl> decls = ctx.decl().stream().
                    map(decl -> decl.accept(declVisitor)).
                    collect(Collectors.toList());
            return decls;
        }
    }

    private static class CasesVisitor extends ComplexHaskellBaseVisitor<ASTCase> {
        @Override
        public ASTCase visitCases(ComplexHaskellParser.CasesContext ctx) {
            PatVisitor patVisitor = new PatVisitor();
            List<ASTPattern> pats = ctx.pat().stream().
                    map(pat -> pat.accept(patVisitor)).
                    collect(Collectors.toList());

            ExpVisitor expVisitor = new ExpVisitor();
            List<ASTExpression> exps = ctx.exp().stream().
                    map(exp -> exp.accept(expVisitor)).
                    collect(Collectors.toList());

            ASTExpression exp = exps.remove(0);

            return new ASTCase(exp, pats, exps);
        }
    }

    private static class LambdaVisitor extends ComplexHaskellBaseVisitor<ASTLambda> {
        @Override
        public ASTLambda visitFundecl(ComplexHaskellParser.FundeclContext ctx) {
            PatVisitor patVisitor = new PatVisitor();
            List<ASTPattern> pats = ctx.pat().stream().
                    map(pat -> pat.accept(patVisitor)).
                    collect(Collectors.toList());

            ExpVisitor expVisitor = new ExpVisitor();
            ASTExpression exp = ctx.exp().accept(expVisitor);

            return new ASTLambda(pats, exp);
        }
    }
}
