package lambda.type;

import haskell.ast.*;
import haskell.parser.ASTGenerator;
import haskell.reduction.ComplexToSimpleReducer;
import haskell.reduction.SimpleToLambdaReducer;
import lambda.ast.ASTAbstraction;
import lambda.ast.ASTConstant;
import lambda.ast.ASTTerm;
import lambda.reduction.WHNOReducerTest;
import lambda.ast.ASTApplication;
import lambda.reduction.delta.ConstructorReduction;
import lambda.reduction.delta.PredefinedFunction;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Tests the type checker.
 */
public class TypeCheckerTest {
    private static TypeChecker typeChecker;
    private static ASTGenerator astGenerator;

    private static ASTTyConstr List;
    private static ASTTyConstr Maybe;

    @BeforeClass
    public static void setUp() {
        typeChecker = new TypeChecker();
        astGenerator = new ASTGenerator();

        // list type: data List a = Nil | Cons a (List a)
        List = new ASTTyConstr("List");
        ASTTyConstr Cons = new ASTTyConstr("Cons");
        ASTTyConstr Nil = new ASTTyConstr("Nil");
        ASTVariable a = new ASTVariable("a");
        ASTConstrDecl ConsDecl = new ASTConstrDecl(Cons, a, new ASTTypeConstr(List, a));
        ASTDataDecl listDecl = new ASTDataDecl(List, a, new ASTConstrDecl(Nil), ConsDecl);
        try {
            typeChecker.addDataDeclaration(listDecl);
        } catch (TypeException.InconsistentDataDeclException e) {
            e.printStackTrace();
            fail();
        }

        // maybe type: data Maybe a = Nothing | Just a
        Maybe = new ASTTyConstr("Maybe");
        ASTTyConstr Just = new ASTTyConstr("Just");
        ASTTyConstr Nothing = new ASTTyConstr("Nothing");
        ASTConstrDecl JustDecl = new ASTConstrDecl(Just, a);
        ASTDataDecl maybeDecl = new ASTDataDecl(Maybe, a, new ASTConstrDecl(Nothing), JustDecl);
        try {
            typeChecker.addDataDeclaration(maybeDecl);
        } catch (TypeException.InconsistentDataDeclException e) {
            fail();
        }
    }

    @Test
    public void testInconsistentDataDecl() {
        ASTTyConstr AnotherList = new ASTTyConstr("AnotherList");
        ASTTyConstr Cons = new ASTTyConstr("Cons");
        ASTTyConstr Nil = new ASTTyConstr("Nil2");
        ASTVariable a = new ASTVariable("a");
        ASTConstrDecl ConsDecl = new ASTConstrDecl(Cons, a, new ASTTypeConstr(List, a));
        ASTDataDecl AnotherListDecl = new ASTDataDecl(AnotherList, a, new ASTConstrDecl(Nil), ConsDecl);

        try {
            typeChecker.addDataDeclaration(AnotherListDecl);
            fail("Inconsistend data decl was added.");
        } catch (TypeException.InconsistentDataDeclException e) {
            System.out.println("Excepcted error: " + e.getMessage());
        }
    }

    @Test
    public void testPrimitiveType() {
        ASTTerm lambda = new ASTConstant(5);
        try {
            ASTType type = typeChecker.checkType(lambda);
            System.out.println("typeof[" + lambda + "] = " + type);

            assertEquals(PredefinedType.INTEGER.getType(), type);
        } catch (TypeException e) {
            fail(e.getMessage());
        }

        lambda = new ASTConstant(PredefinedFunction.PLUS);
        try {
            ASTType type = typeChecker.checkType(lambda);
            System.out.println("typeof[" + lambda + "] = " + type);

            assertEquals(PredefinedFunction.PLUS.getType(), type);
        } catch (TypeException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testCustomType() {
        ASTTerm Nil = new ASTConstant(ConstructorReduction.getConstructor("Nil"));
        try {
            ASTType type = typeChecker.checkType(Nil);
            System.out.println("typeof[" + Nil + "] = " + type);

            assertTrue(type instanceof ASTTypeConstr);
            ASTTypeConstr typeConstr = (ASTTypeConstr) type;
            assertEquals(List, typeConstr.getTyConstr());
        } catch (TypeException e) {
            fail(e.getMessage());
        }

        ASTTerm Cons = new ASTConstant(ConstructorReduction.getConstructor("Cons"));
        try {
            ASTType type = typeChecker.checkType(Cons);
            System.out.println("typeof[" + Cons + "] = " + type);

            assertTrue(type instanceof ASTFuncType);
            ASTFuncType funcType = (ASTFuncType) type;

            assertTrue(funcType.getFrom() instanceof ASTVariable);
            ASTVariable b0 = (ASTVariable) funcType.getFrom();

            assertTrue(funcType.getTo() instanceof ASTFuncType);
            ASTFuncType listList = (ASTFuncType) funcType.getTo();

            assertTrue(listList.getFrom() instanceof ASTTypeConstr);
            assertTrue(listList.getTo() instanceof ASTTypeConstr);
            assertEquals(listList.getFrom(), listList.getTo());

            ASTTypeConstr listB0 = (ASTTypeConstr) listList.getFrom();
            assertEquals(1, listB0.getTypes().size());
            assertEquals(b0, listB0.getTypes().get(0));
        } catch (TypeException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testAbstractionType() {
        ASTTerm lambda = new ASTAbstraction(new lambda.ast.ASTVariable("x"), new ASTConstant(5));
        try {
            ASTType type = typeChecker.checkType(lambda);
            System.out.println("typeof[" + lambda + "] = " + type);

            assertTrue(type instanceof ASTFuncType);
            assertEquals(PredefinedType.INTEGER.getType(), ((ASTFuncType ) type).getTo());
            assertTrue(((ASTFuncType ) type).getFrom() instanceof ASTVariable);
        } catch (TypeException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testNestedAbstractionType() {
        ASTTerm lambda = new ASTAbstraction(new lambda.ast.ASTVariable("x"), new ASTAbstraction(new lambda.ast.ASTVariable("y"), new ASTConstant('a')));
        try {
            ASTType type = typeChecker.checkType(lambda);
            System.out.println("typeof[" + lambda + "] = " + type);

            assertTrue(type instanceof ASTFuncType);
            assertTrue(((ASTFuncType ) type).getFrom() instanceof ASTVariable);
            ASTType nested = ((ASTFuncType ) type).getTo();
            assertTrue(nested instanceof ASTFuncType);
            assertTrue(((ASTFuncType ) nested).getFrom() instanceof ASTVariable);
            assertEquals(PredefinedType.CHAR.getType(), ((ASTFuncType ) nested).getTo());
        } catch (TypeException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testApplicationType() {
        ASTTerm squareX = new ASTApplication(new ASTApplication(new ASTConstant(PredefinedFunction.PLUS), new lambda.ast.ASTVariable("x")), new lambda.ast.ASTVariable("x"));
        ASTTerm squareFunc = new ASTAbstraction(new lambda.ast.ASTVariable("x"), squareX);
        ASTTerm lambda = new ASTApplication(squareFunc, new ASTConstant(5));
        try {
            ASTType type = typeChecker.checkType(lambda);
            System.out.println("typeof[" + lambda + "] = " + type);
            assertEquals(PredefinedType.INTEGER.getType(), type);
        } catch (TypeException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testFact() {
        ASTTerm lambda = WHNOReducerTest.getFactFunction();
        try {
            ASTType type = typeChecker.checkType(lambda);
            System.out.println("typeof[" + lambda + "] = " + type);
            assertEquals(new ASTFuncType(PredefinedType.INTEGER.getType(), PredefinedType.INTEGER.getType()), type);
        } catch (TypeException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testIncorrectlyTypedPredefinedFunction() {
        ASTTerm lambda = new ASTApplication(new ASTApplication(new ASTConstant(PredefinedFunction.PLUS), new ASTConstant('a')), new ASTConstant(5));
        try {
            ASTType type = typeChecker.checkType(lambda);
            fail(lambda + " is incorrectly typed, but the type checker thinks it's of type " + type);
        } catch (TypeException.CannotUnifyException e) {
            System.out.println("Expected error: " + e.getMessage());
        } catch (TypeException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testIncorrectlyTypedFact() {
        ASTTerm fact = WHNOReducerTest.getFactFunction();
        ASTTerm lambda = new ASTApplication(fact, new ASTConstant(5.0f));
        try {
            ASTType type = typeChecker.checkType(lambda);
            fail(lambda + " is incorrectly typed, but the type checker thinks it of type " + type);
        } catch (TypeException e) {
            System.out.println("Expected error: " + e.getMessage());
        }
    }

    @Test
    public void testPatternMatchedType() {
        String programCode = "len Nil = 0\n" +
                "len (Cons _ xs) = (plus 1 (len xs))\n";

        String expressionCode = "len";

        Optional<ASTProgram> program = astGenerator.parseProgram(new ANTLRInputStream(programCode));
        Optional<ASTExpression> eval = astGenerator.parseExpression(new ANTLRInputStream(expressionCode));
        assertTrue(program.isPresent());
        assertTrue(eval.isPresent());

        System.out.println(program.get());
        System.out.print("typeof["+eval.get()+"] = ");

        try {
            ASTType lenType = getTypeOfExpression(program.get(), eval.get());
            System.out.println(lenType);

            assertTrue(lenType instanceof ASTFuncType);
            ASTFuncType lenFuncType = (ASTFuncType) lenType;

            assertEquals(PredefinedType.INTEGER.getType(), lenFuncType.getTo());

            assertTrue(lenFuncType.getFrom() instanceof ASTTypeConstr);
            ASTTypeConstr listA = (ASTTypeConstr) lenFuncType.getFrom();
            assertEquals(List, listA.getTyConstr());
            assertEquals(1, listA.getTypes().size());
            assertTrue(listA.getTypes().get(0) instanceof ASTVariable);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testNestedPatternMatchedType() {
        String programCode = "len Nil = 0\n" +
                "len (Cons _ xs) = (plus 1 (len xs))\n" +
                "square x = (mult x x)\n" +
                "squareLen xs = (square (len xs))\n";

        String expressionCode = "squareLen";

        Optional<ASTProgram> program = astGenerator.parseProgram(new ANTLRInputStream(programCode));
        Optional<ASTExpression> eval = astGenerator.parseExpression(new ANTLRInputStream(expressionCode));
        assertTrue(program.isPresent());
        assertTrue(eval.isPresent());

        System.out.println(program.get());
        System.out.print("typeof["+eval.get()+"] = ");

        try {
            ASTType lenType = getTypeOfExpression(program.get(), eval.get());
            System.out.println(lenType);

            assertTrue(lenType instanceof ASTFuncType);
            ASTFuncType lenFuncType = (ASTFuncType) lenType;

            assertEquals(PredefinedType.INTEGER.getType(), lenFuncType.getTo());

            assertTrue(lenFuncType.getFrom() instanceof ASTTypeConstr);
            ASTTypeConstr listA = (ASTTypeConstr) lenFuncType.getFrom();
            assertEquals(List, listA.getTyConstr());
            assertEquals(1, listA.getTypes().size());
            assertTrue(listA.getTypes().get(0) instanceof ASTVariable);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testDoublePatternMatching() {
        String programCode = "addm Nothing Nothing = 0.0\n" +
                "addm Nothing (Just x) = x\n" +
                "addm (Just x) Nothing = x\n" +
                "addm (Just x) (Just y) = (plusf x y)\n";

        String expressionCode = "addm";

        Optional<ASTProgram> program = astGenerator.parseProgram(new ANTLRInputStream(programCode));
        Optional<ASTExpression> eval = astGenerator.parseExpression(new ANTLRInputStream(expressionCode));
        assertTrue(program.isPresent());
        assertTrue(eval.isPresent());

        System.out.println(program.get());
        System.out.print("typeof["+eval.get()+"] = ");

        try {
            ASTType addmType = getTypeOfExpression(program.get(), eval.get());
            System.out.println(addmType);

            // (Maybe Float) -> ( (Maybe Float) -> Float )
            assertTrue(addmType instanceof ASTFuncType);
            ASTFuncType addmFuncType = (ASTFuncType) addmType;

            ASTType maybeFloat = new ASTTypeConstr(new ASTTyConstr("Maybe"), PredefinedType.FLOAT.getType());
            assertEquals(maybeFloat, addmFuncType.getFrom());

            assertTrue(addmFuncType.getTo() instanceof ASTFuncType);
            ASTFuncType addmFuncTypeRight = (ASTFuncType) addmFuncType.getTo();

            assertEquals(maybeFloat, addmFuncTypeRight.getFrom());
            assertEquals(PredefinedType.FLOAT.getType(), addmFuncTypeRight.getTo());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testGenList() {
        String programCode = "genList 0 = Nil\n" +
                "genList x = (Cons x (genList (decrement x)))\n" +
                "decrement x = (minus x 1)\n";

        String expressionCode = "genList";

        Optional<ASTProgram> program = astGenerator.parseProgram(new ANTLRInputStream(programCode));
        Optional<ASTExpression> eval = astGenerator.parseExpression(new ANTLRInputStream(expressionCode));
        assertTrue(program.isPresent());
        assertTrue(eval.isPresent());

        System.out.println(program.get());
        System.out.print("typeof["+eval.get()+"] = ");

        try {
            ASTType genListType = getTypeOfExpression(program.get(), eval.get());
            System.out.println(genListType);

            assertTrue(genListType instanceof ASTFuncType);
            ASTFuncType genListFuncType = (ASTFuncType) genListType;

            assertEquals(PredefinedType.INTEGER.getType(), genListFuncType.getFrom());

            assertTrue(genListFuncType.getTo() instanceof ASTTypeConstr);
            ASTTypeConstr listInt = (ASTTypeConstr) genListFuncType.getTo();

            assertEquals(List, listInt.getTyConstr());
            assertEquals(1, listInt.getTypes().size());
            assertEquals(PredefinedType.INTEGER.getType(), listInt.getTypes().get(0));
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    private ASTType getTypeOfExpression(ASTProgram program, ASTExpression expression) throws TypeException {
        List<ASTDecl> functionDeclarations = program.getDecls().stream().
                filter(decl -> decl instanceof ASTPatDecl || decl instanceof ASTFunDecl).
                collect(Collectors.toList());

        // init: create the expression: let prog in expr
        ASTExpression letProgInExpr;
        if (functionDeclarations.size() == 0) {
            // empty lets are not supported, so we simply evaluate the expression directly
            letProgInExpr = expression;
        }
        else {
            letProgInExpr = new ASTLet(functionDeclarations, expression);
        }

        // 1. reduce complex haskell expression to simple haskell expression
        ComplexToSimpleReducer complexToSimpleReducer = new ComplexToSimpleReducer();
        ASTExpression simpleExpr = complexToSimpleReducer.reduceToSimple(letProgInExpr);

        // 2. reduce simple haskell expression to lambda expression
        SimpleToLambdaReducer simpleToLambdaReducer = new SimpleToLambdaReducer();
        lambda.ast.ASTTerm lambdaTerm = simpleExpr.accept(simpleToLambdaReducer);

        // 3. do a static type check
        return typeChecker.checkType(lambdaTerm);
    }
}