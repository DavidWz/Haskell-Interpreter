package haskell.complex.ast;

import haskell.complex.reduction.SimpleReducer;
import haskell.complex.reduction.VariableManager;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a let expression, i.e. let decls in expr
 */
public class ASTLet implements ASTExpression {
    private List<ASTDecl> decls;
    private ASTExpression exp;

    public ASTLet(List<ASTDecl> decls, ASTExpression exp) {
        assert(decls != null);
        assert(exp != null);
        this.decls = decls;
        this.exp = exp;
    }

    public List<ASTDecl> getDecls() {
        return decls;
    }

    public ASTExpression getExp() {
        return exp;
    }

    @Override

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ASTLet astLet = (ASTLet) o;

        if (!getDecls().equals(astLet.getDecls())) return false;
        return getExp().equals(astLet.getExp());

    }

    @Override
    public int hashCode() {
        int result = getDecls().hashCode();
        result = 31 * result + getExp().hashCode();
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("let {\n\t");
        for (ASTDecl decl : decls) {
            builder.append(decl);
            builder.append(";\n\t");
        }
        builder.deleteCharAt(builder.length()-1);
        builder.append("} in ");
        builder.append(exp);
        return builder.toString();
    }

    @Override
    public Set<ASTVariable> getAllVariables() {
        Set<ASTVariable> vars = new HashSet<>();
        for (ASTDecl decl : decls) {
            vars.addAll(decl.getAllVariables());
        }
        vars.addAll(exp.getAllVariables());
        return vars;
    }

    @Override
    public Set<ASTVariable> getFreeVars() {
        Set<ASTVariable> vars = new HashSet<>();
        vars.addAll(exp.getFreeVars());
        for (ASTDecl decl : decls) {
            if (decl instanceof ASTFunDecl) {
                vars.addAll(((ASTFunDecl) decl).getExp().getFreeVars());
            }
        }
        for (ASTDecl decl : decls) {
            if (decl instanceof ASTFunDecl) {
                vars.remove(((ASTFunDecl) decl).getVar());
                for (ASTPattern pat : ((ASTFunDecl) decl).getPats()) {
                    vars.removeAll(pat.getFreeVars());
                }
            }
        }
        return vars;
    }

    @Override
    public boolean funcDeclToPatDecl() {
        // first, try to apply this transformation as deep as possible
        for (ASTDecl decl : decls) {
            if (decl.funcDeclToPatDecl()) {
                return true;
            }
        }
        if (exp.funcDeclToPatDecl()) {
            return true;
        }

        // try to apply the transformation to the declarations stored in this let-term
        Optional<List<ASTDecl>> transformedDecls = SimpleReducer.funcDeclToPatDecl(decls);
        if (transformedDecls.isPresent()) {
            decls = transformedDecls.get();
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public boolean nestMultipleLambdas() {
        for (ASTDecl decl : decls) {
            if (decl.nestMultipleLambdas()) {
                return true;
            }
        }
        return exp.nestMultipleLambdas();
    }

    @Override
    public boolean lambdaPatternToCase() {
        for (ASTDecl decl : decls) {
            if (decl.lambdaPatternToCase()) {
                return true;
            }
        }
        return exp.lambdaPatternToCase();
    }

    @Override
    public boolean caseToMatch() {
        for (ASTDecl decl : decls) {
            if (decl.caseToMatch()) {
                return true;
            }
        }
        if (exp.caseToMatch()) {
            return true;
        }

        if (exp instanceof ASTCase) {
            ASTCase caseExp = (ASTCase) exp;
            exp = SimpleReducer.caseToMatch(caseExp);
            return true;
        }
        return false;
    }

    @Override
    public boolean nestMultipleLets() {
        // we seperate pattern declarations from other types of declarations
        List<ASTDecl> nonPatDecls = new ArrayList<>();
        List<ASTPatDecl> patDecls = new ArrayList<>();
        for (ASTDecl decl : decls) {
            if (decl instanceof ASTPatDecl) {
                patDecls.add((ASTPatDecl) decl);
            }
            else {
                nonPatDecls.add(decl);
            }
        }

        // first, we must calculate a seperation of pattern declarations so that entangled functions are grouped together
        List<List<ASTPatDecl>> seperation = SimpleReducer.getSeperation(patDecls);

        System.out.println("The seperation is as follows: ");
        for (List<ASTPatDecl> group : seperation) {
            System.out.println("Group: ");
            for (ASTPatDecl decl : group) {
                System.out.println("\t" + decl);
            }
        }

        // then we transform groups of entangled declarations to single declarations
        for (List<ASTPatDecl> group : seperation) {
            nonPatDecls.add(fuseEntangledFunctions(group));
        }
        decls = nonPatDecls;

        // we construct the nested let terms as follows:
        /*
                 let { decl1; ...; decln } in exp
        -------------------------------------------------------
        let decl1 in (let decl2 in (... (let decln in exp)...))
         */
        if (decls.size() > 0) {
            ASTExpression nestedLets = exp;
            while (decls.size() > 1) {
                ASTDecl currentDecl = decls.remove(decls.size()-1);
                nestedLets = new ASTLet(Collections.singletonList(currentDecl), nestedLets);
            }
            exp = nestedLets;
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Transforms a group of entangled functions into one single declarations
     * @param decls
     * @return
     */
    private ASTPatDecl fuseEntangledFunctions(List<ASTPatDecl> decls) {
        if (decls.size() < 2) {
            return decls.get(0);
        }
        else {
            // we apply the following transformation to fuse the group together
            /*
              var1 = exp1; ...; varn = expn
            ---------------------------------
            (var1,...,varn) = (exp1,...,expn)
             */
            List<ASTPattern> vars = new ArrayList<>();
            List<ASTExpression> exps = new ArrayList<>();
            for (ASTPatDecl decl : decls) {
                vars.add(decl.getPat());
                exps.add(decl.getExp());
            }

            ASTPatTuple varTuple = new ASTPatTuple(vars);
            ASTExpTuple expTuple = new ASTExpTuple(exps);

            ASTPatDecl fusedGroup = new ASTPatDecl(varTuple, expTuple);
            return fusedGroup;
        }
    }

    @Override
    public haskell.simple.ast.ASTExpression castToSimple() throws SimpleReducer.TooComplexException {
        if (decls.size() != 1) {
            throw new SimpleReducer.TooComplexException(this, "Let expressions in simple haskel must only contain one declaration.");
        }

        ASTDecl decl = decls.get(0);
        if (!(decl instanceof ASTPatDecl)) {
            throw new SimpleReducer.TooComplexException(this, "Declarations in let expressions must be pattern declarations.");
        }

        ASTPatDecl patDecl = (ASTPatDecl) decl;
        ASTPattern pat = patDecl.getPat();
        if (!(pat instanceof ASTVariable)) {
            throw new SimpleReducer.TooComplexException(this, "The pattern of a pattern declaration in let expressions must be a variable.");
        }

        return new haskell.simple.ast.ASTLet((haskell.simple.ast.ASTVariable) pat.castToSimple(),
                patDecl.getExp().castToSimple(),
                exp.castToSimple());
    }
}
