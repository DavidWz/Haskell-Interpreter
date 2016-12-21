package haskell.parser;

import haskell.ast.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;

import java.util.BitSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Generates the ast structure of a complex haskell program.
 */
public class ASTGenerator implements ANTLRErrorListener {
    public ASTGenerator() {
    }

    /**
     * Returns a parser for the given char stream.
     * The parser won't output any errors to the console but instead throw exceptions.
     * @param charStream
     * @return
     */
    private ComplexHaskellParser getParser(CharStream charStream) {
        ComplexHaskellLexer lexer = new ComplexHaskellLexer(charStream);
        TokenStream tokens = new CommonTokenStream(lexer);

        lexer.removeErrorListener(ConsoleErrorListener.INSTANCE);
        lexer.addErrorListener(this);

        ComplexHaskellParser parser = new ComplexHaskellParser(tokens);

        parser.removeErrorListener(ConsoleErrorListener.INSTANCE);
        parser.addErrorListener(this);

        return parser;
    }

    /**
     * Parses program code to an ASTProgram.
     * @param charStream the input char stream
     * @return the ast of the program or empty if it could not be parsed
     */
    public Optional<ASTProgram> parseProgram(CharStream charStream) {
        ComplexHaskellParser parser = getParser(charStream);

        ASTGenerator.ProgramVisitor progParser = new ASTGenerator.ProgramVisitor();
        try {
            ASTProgram program = progParser.visit(parser.program());
            if (parser.getCurrentToken().getType() != parser.getTokenType("EOF")) {
                // make sure the whole input was successfully parsed
                return Optional.empty();
            }
            return Optional.of(program);
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
        ComplexHaskellParser parser = getParser(charStream);

        ASTGenerator.DeclVisitor declParser = new ASTGenerator.DeclVisitor();
        try {
            ASTDecl decl = declParser.visit(parser.decl());
            if (parser.getCurrentToken().getType() != parser.getTokenType("EOF")) {
                // make sure the whole input was successfully parsed
                return Optional.empty();
            }
            return Optional.of(decl);
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
        ComplexHaskellParser parser = getParser(charStream);

        ASTGenerator.ExpVisitor expParser = new ASTGenerator.ExpVisitor();
        try {
            ASTExpression exp = expParser.visit(parser.exp());
            if (parser.getCurrentToken().getType() != parser.getTokenType("EOF")) {
                // make sure the whole input was successfully parsed
                return Optional.empty();
            }
            return Optional.of(exp);
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
            } else if (ctx.datadecl() != null) {
                DataDeclVisitor dataDeclVisitor = new DataDeclVisitor();
                return ctx.datadecl().accept(dataDeclVisitor);
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
                return new ASTTyConstr(tyConstrID);
            } else if (ctx.joker() != null) {
                return new ASTJoker();
            } else if (ctx.integer() != null) {
                String intId = ctx.integer().getText();
                int n = Integer.parseInt(intId);
                return new ASTInteger(n);
            } else if (ctx.floating() != null) {
                String floatId = ctx.floating().getText();
                float f = Float.parseFloat(floatId);
                return new ASTFloat(f);
            } else if (ctx.character() != null) {
                String charId = ctx.character().getText();
                char c = charId.charAt(1);
                return new ASTChar(c);
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
            ASTTyConstr typeConstr = new ASTTyConstr(tyConstrID);

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
                return new ASTTyConstr(tyConstrID);
            } else if (ctx.integer() != null) {
                String intId = ctx.integer().getText();
                int n = Integer.parseInt(intId);
                return new ASTInteger(n);
            } else if (ctx.floating() != null) {
                String floatId = ctx.floating().getText();
                float f = Float.parseFloat(floatId);
                return new ASTFloat(f);
            } else if (ctx.character() != null) {
                String charId = ctx.character().getText();
                char c = charId.charAt(1);
                return new ASTChar(c);
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
        public ASTLambda visitLambda(ComplexHaskellParser.LambdaContext ctx) {
            PatVisitor patVisitor = new PatVisitor();
            List<ASTPattern> pats = ctx.pat().stream().
                    map(pat -> pat.accept(patVisitor)).
                    collect(Collectors.toList());

            ExpVisitor expVisitor = new ExpVisitor();
            ASTExpression exp = ctx.exp().accept(expVisitor);

            return new ASTLambda(pats, exp);
        }
    }

    private static class DataDeclVisitor extends ComplexHaskellBaseVisitor<ASTDataDecl> {
        @Override
        public ASTDataDecl visitDatadecl(ComplexHaskellParser.DatadeclContext ctx) {
            String tyConstrID = ctx.tyconstr().getText();
            ASTTyConstr tyConstr =  new ASTTyConstr(tyConstrID);

            List<ASTVariable> vars = ctx.var().stream().
                    map(var -> new ASTVariable(var.getText())).
                    collect(Collectors.toList());

            ConstrDeclVisitor constrDeclVisitor = new ConstrDeclVisitor();
            List<ASTConstrDecl> constrDecls = ctx.constrdecl().stream().
                    map(constrDecl -> constrDecl.accept(constrDeclVisitor)).
                    collect(Collectors.toList());

            return new ASTDataDecl(tyConstr, vars, constrDecls);
        }
    }

    private static class ConstrDeclVisitor extends ComplexHaskellBaseVisitor<ASTConstrDecl> {
        @Override
        public ASTConstrDecl visitConstrdecl(ComplexHaskellParser.ConstrdeclContext ctx) {
            String tyConstrID = ctx.tyconstr().getText();
            ASTTyConstr tyConstr =  new ASTTyConstr(tyConstrID);

            TypeVisitor typeVisitor = new TypeVisitor();
            List<ASTType> types = ctx.type().stream().
                    map(type -> type.accept(typeVisitor)).
                    collect(Collectors.toList());

            return new ASTConstrDecl(tyConstr, types);
        }
    }

    private static class TypeVisitor extends ComplexHaskellBaseVisitor<ASTType> {
        @Override
        public ASTType visitType(ComplexHaskellParser.TypeContext ctx) {
            if (ctx.var() != null) {
                String varID = ctx.var().getText();
                return new ASTVariable(varID);
            } else if (ctx.typeconstr() != null) {
                TypeConstrVisitor typeConstrVisitor = new TypeConstrVisitor();
                return ctx.typeconstr().accept(typeConstrVisitor);
            } else if (ctx.functype() != null) {
                FuncTypeVisitor funcTypeVisitor = new FuncTypeVisitor();
                return ctx.functype().accept(funcTypeVisitor);
            } else if (ctx.tupletype() != null) {
                TupleTypeVisitor tupleTypeVisitor = new TupleTypeVisitor();
                return ctx.tupletype().accept(tupleTypeVisitor);
            } else {
                throw new RuntimeException();
            }
        }
    }

    private static class TypeConstrVisitor extends ComplexHaskellBaseVisitor<ASTTypeConstr> {
        @Override
        public ASTTypeConstr visitTypeconstr(ComplexHaskellParser.TypeconstrContext ctx) {
            String tyConstrID = ctx.tyconstr().getText();
            ASTTyConstr tyConstr =  new ASTTyConstr(tyConstrID);

            TypeVisitor typeVisitor = new TypeVisitor();
            List<ASTType> types = ctx.type().stream().
                    map(type -> type.accept(typeVisitor)).
                    collect(Collectors.toList());

            return new ASTTypeConstr(tyConstr, types);
        }
    }

    private static class FuncTypeVisitor extends ComplexHaskellBaseVisitor<ASTFuncType> {
        @Override
        public ASTFuncType visitFunctype(ComplexHaskellParser.FunctypeContext ctx) {
            TypeVisitor typeVisitor = new TypeVisitor();

            ASTType type1 = ctx.type(0).accept(typeVisitor);
            ASTType type2 = ctx.type(1).accept(typeVisitor);

            return new ASTFuncType(type1, type2);
        }
    }

    private static class TupleTypeVisitor extends ComplexHaskellBaseVisitor<ASTTupleType> {
        @Override
        public ASTTupleType visitTupletype(ComplexHaskellParser.TupletypeContext ctx) {
            TypeVisitor typeVisitor = new TypeVisitor();
            List<ASTType> types = ctx.type().stream().
                    map(type -> type.accept(typeVisitor)).
                    collect(Collectors.toList());
            return new ASTTupleType(types);
        }
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object o, int i, int i1, String s, RecognitionException e) {
        throw new RuntimeException();
    }

    @Override
    public void reportAmbiguity(Parser parser, DFA dfa, int i, int i1, boolean b, BitSet bitSet, ATNConfigSet atnConfigSet) {
        throw new RuntimeException();
    }

    @Override
    public void reportAttemptingFullContext(Parser parser, DFA dfa, int i, int i1, BitSet bitSet, ATNConfigSet atnConfigSet) {
        throw new RuntimeException();
    }

    @Override
    public void reportContextSensitivity(Parser parser, DFA dfa, int i, int i1, int i2, ATNConfigSet atnConfigSet) {
        throw new RuntimeException();
    }
}
