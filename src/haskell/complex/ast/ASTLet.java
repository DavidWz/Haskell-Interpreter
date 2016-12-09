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
        // TODO: entangled functions
        // we assume functions are not entangled and that functions only depend on earlier defined other functions

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
