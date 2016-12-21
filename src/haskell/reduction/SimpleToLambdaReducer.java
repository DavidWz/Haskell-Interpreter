package haskell.reduction;

import haskell.ast.*;
import lambda.ast.ASTAbstraction;
import lambda.ast.ASTConstant;
import lambda.ast.ASTTerm;
import lambda.reduction.WHNOReducer;
import lambda.reduction.delta.ConstructorReduction;
import lambda.reduction.delta.PredefinedFunction;
import lambda.reduction.delta.TupleReduction;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Casts a simple haskell expression to a lambda term.
 */
public class SimpleToLambdaReducer implements ComplexHaskellVisitor<ASTTerm> {
    public SimpleToLambdaReducer() {
    }

    @Override
    public ASTTerm visit(ASTApplication node) {
        // (a b c...) => (...((a b) c) ...)
        ASTTerm lambdaTerm = node.getExps().get(0).accept(this);
        for (int i = 1; i < node.getExps().size(); i++) {
            lambdaTerm = new lambda.ast.ASTApplication(lambdaTerm, node.getExps().get(i).accept(this));
        }

        return lambdaTerm;
    }

    @Override
    public ASTTerm visit(ASTBoolean node) {
        // c => c
        return new ASTConstant(node.getValue());
    }

    @Override
    public ASTTerm visit(ASTBranch node) {
        // if a then b else c => (((if cond) a) b)
        lambda.ast.ASTTerm result = new lambda.ast.ASTApplication(
                new lambda.ast.ASTConstant(PredefinedFunction.IF),
                node.getCondition().accept(this));
        result = new lambda.ast.ASTApplication(result, node.getIfBranch().accept(this));
        result = new lambda.ast.ASTApplication(result, node.getElseBranch().accept(this));
        return result;
    }

    @Override
    public ASTTerm visit(ASTCase node) {
        throw new RuntimeException("Complex to Simple reduction is incomplete: Cases cannot be cast to lambda terms.");
    }

    @Override
    public ASTTerm visit(ASTChar node) {
        // c => c
        return new ASTConstant(node.getValue());
    }

    @Override
    public ASTTerm visit(ASTConstruct node) {
        if (node.getPats().size() == 0) {
            // C => C
            Optional<ASTConstant> constant = WHNOReducer.toConst(node.getType().getName());
            if (constant.isPresent()) {
                return new ASTConstant(constant.get().getValue());
            }
            else {
                return new ASTConstant(ConstructorReduction.getConstructor(node.getType().getName()));
            }
        }
        else {
            throw new RuntimeException("Complex to Simple reduction is incomplete: Arguments of constructs must be arguments of an application.");
        }
    }

    @Override
    public ASTTerm visit(ASTExpTuple node) {
        // (a, b, ..., z) => (...((tuple_n a) b) ... z)
        List<ASTTerm> terms = node.getExps().stream().map(exp -> exp.accept(this)).collect(Collectors.toList());
        int n = terms.size();

        // there are no tupels of size 1
        if (n == 1) {
            return terms.get(0);
        }
        else {
            ASTTerm result = new lambda.ast.ASTConstant(new TupleReduction.TupleConstant(n));
            for (ASTTerm t : terms) {
                result = new lambda.ast.ASTApplication(result,t);
            }
            return result;
        }
    }

    @Override
    public ASTTerm visit(ASTFloat node) {
        // c => c
        return new ASTConstant(node.getValue());
    }

    @Override
    public ASTTerm visit(ASTFunDecl node) {
        throw new RuntimeException("Complex to Simple reduction is incomplete: Function declarations cannot be cast to lambda terms.");
    }

    @Override
    public ASTTerm visit(ASTInteger node) {
        // c => c
        return new ASTConstant(node.getValue());
    }

    @Override
    public ASTTerm visit(ASTJoker node) {
        throw new RuntimeException("Complex to Simple reduction is incomplete: The joker pattern is not part of the lambda calculus.");
    }

    @Override
    public ASTTerm visit(ASTLambda node) {
        if (node.getPats().size() == 1) {
            // \x -> exp = (\x.exp)
            ASTPattern pat = node.getPats().get(0);

            if (pat instanceof ASTVariable) {
                ASTVariable var = (ASTVariable) pat;

                return new ASTAbstraction((lambda.ast.ASTVariable) var.accept(this), node.getExp().accept(this));
            }
            else {
                throw new RuntimeException("Complex to Simple reduction is incomplete: Lambdas must map a variable.");
            }
        }
        else {
            throw new RuntimeException("Complex to Simple reduction is incomplete: Lambdas must only map one variable.");
        }
    }

    @Override
    public ASTTerm visit(ASTLet node) {
        if (node.getDecls().size() != 1) {
            throw new RuntimeException("Complex to Simple reduction is incomplete: Let expressions in simple haskel must only contain one declaration.");
        }

        ASTDecl decl = node.getDecls().get(0);
        if (!(decl instanceof ASTPatDecl)) {
            throw new RuntimeException("Complex to Simple reduction is incomplete: Declarations in let expressions must be pattern declarations.");
        }

        ASTPatDecl patDecl = (ASTPatDecl) decl;
        ASTPattern pat = patDecl.getPat();
        if (!(pat instanceof ASTVariable)) {
            throw new RuntimeException("Complex to Simple reduction is incomplete: The pattern of a pattern declaration in let expressions must be a variable.");
        }

        // let var = exp in target => exp[var / (fix \var.exp)]
        ASTTerm basis = node.getExp().accept(this);
        lambda.ast.ASTVariable variable = (lambda.ast.ASTVariable) pat.accept(this);
        ASTTerm func = new lambda.ast.ASTAbstraction(variable, patDecl.getExp().accept(this));
        ASTTerm replacement = new lambda.ast.ASTApplication(new lambda.ast.ASTConstant(PredefinedFunction.FIX), func);
        return basis.substitute(variable, replacement);
    }

    @Override
    public ASTTerm visit(ASTPatDecl node) {
        throw new RuntimeException("Complex to Simple reduction is incomplete: Pattern declarations are not part of simple haskell.");
    }

    @Override
    public ASTTerm visit(ASTPatTuple node) {
        throw new RuntimeException("Complex to Simple reduction is incomplete: Pattern tuples are not part of simple haskell.");
    }

    @Override
    public ASTTerm visit(ASTProgram node) {
        throw new RuntimeException("Complex to Simple reduction is incomplete: Programs are not part of simple haskell. Please use \"Let [program] in [expression]\" instead.");
    }

    @Override
    public ASTTerm visit(ASTTyConstr node) {
        // T => T
        Optional<ASTConstant> constant = WHNOReducer.toConst(node.getName());
        if (constant.isPresent()) {
            return constant.get();
        }
        else {
            return new ASTConstant(ConstructorReduction.getConstructor(node.getName()));
        }
    }

    @Override
    public ASTTerm visit(ASTVariable node) {
        // var => var
        Optional<ASTConstant> constant = WHNOReducer.toConst(node.getName());
        if (constant.isPresent()) {
            return constant.get();
        }
        else {
            return new lambda.ast.ASTVariable(node.getName());
        }
    }

    @Override
    public ASTTerm visit(ASTDataDecl node) {
        throw new RuntimeException("Complex to Simple reduction is incomplete: Data declarations are not part of simple haskell.");
    }

    @Override
    public ASTTerm visit(ASTConstrDecl node) {
        throw new RuntimeException("Complex to Simple reduction is incomplete: Data declarations are not part of simple haskell.");
    }

    @Override
    public ASTTerm visit(ASTTypeConstr node) {
        throw new RuntimeException("Complex to Simple reduction is incomplete: Data declarations are not part of simple haskell.");
    }

    @Override
    public ASTTerm visit(ASTFuncType node) {
        throw new RuntimeException("Complex to Simple reduction is incomplete: Data declarations are not part of simple haskell.");
    }

    @Override
    public ASTTerm visit(ASTTupleType node) {
        throw new RuntimeException("Complex to Simple reduction is incomplete: Data declarations are not part of simple haskell.");
    }
}
