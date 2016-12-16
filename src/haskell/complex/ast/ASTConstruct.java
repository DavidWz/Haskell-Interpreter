package haskell.complex.ast;

import haskell.complex.reduction.ComplexHaskellTransformation;
import haskell.complex.reduction.TooComplexException;
import haskell.simple.ast.ASTConstant;
import haskell.simple.ast.ASTExpression;
import lambda.reduction.WHNOReducer;
import lambda.reduction.delta.ConstructorReduction;

import java.util.*;

/**
 * Represents a construction.
 */
public class ASTConstruct implements ASTPattern {
    private ASTTyConstr type;
    private List<ASTPattern> pats;

    public ASTTyConstr getType() {
        return type;
    }

    public List<ASTPattern> getPats() {
        return pats;
    }

    public ASTConstruct(ASTTyConstr type, List<ASTPattern> pats) {
        assert(type != null);
        assert(pats != null);

        this.type = type;
        this.pats = pats;
    }

    public ASTConstruct(String name) {
        this.type = new ASTTyConstr(name);
        this.pats = Collections.emptyList();
    }

    public ASTConstruct(ASTTyConstr type, ASTPattern... pats) {
        assert(type != null);

        this.type = type;
        this.pats = Arrays.asList(pats);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ASTConstruct that = (ASTConstruct) o;

        if (!getType().equals(that.getType())) return false;
        return getPats().equals(that.getPats());

    }

    @Override
    public int hashCode() {
        int result = getType().hashCode();
        result = 31 * result + getPats().hashCode();
        return result;
    }

    @Override
    public String toString() {
        if (pats.size() == 0) {
            return type.toString();
        }
        else {
            StringBuilder builder = new StringBuilder();
            builder.append("(");
            builder.append(type.toString());
            for (ASTPattern pat : pats) {
                builder.append(" ");
                builder.append(pat.toString());
            }
            builder.append(")");
            return builder.toString();
        }
    }

    @Override
    public Set<ASTVariable> getAllVariables() {
        Set<ASTVariable> vars = new HashSet<>();
        for (ASTPattern pat : pats) {
            vars.addAll(pat.getAllVariables());
        }
        return vars;
    }

    @Override
    public ASTExpression castToSimple() throws TooComplexException {
        if (pats.size() == 0) {
            Optional<lambda.ast.ASTConstant> constant = WHNOReducer.toConst(type.getName());
            if (constant.isPresent()) {
                return new ASTConstant(constant.get().getValue());
            }
            else {
                return new ASTConstant(ConstructorReduction.getConstructor(type.getName()));
            }
        }
        else {
            throw new TooComplexException(this, "Arguments of constructs must be arguments of an application.");
        }
    }

    @Override
    public boolean accept(ComplexHaskellTransformation tr) {
        return tr.visit(this);
    }

    @Override
    public Set<ASTVariable> getFreeVars() {
        Set<ASTVariable> vars = new HashSet<>();
        for (ASTPattern pat : pats) {
            vars.addAll(pat.getFreeVars());
        }
        return vars;
    }
}
