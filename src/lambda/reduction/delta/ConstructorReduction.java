package lambda.reduction.delta;

import lambda.ast.ASTApplication;
import lambda.ast.ASTConstant;
import lambda.ast.ASTTerm;

import java.util.List;
import java.util.Optional;

/**
 * Represents the isa and argof delta rules.
 */
public class ConstructorReduction extends DeltaReduction {
    /**
     * Represents a custom data constructor.
     */
    public static class Constructor {
        private String name;

        public Constructor(String name) {
            assert(name != null);
            assert(!name.trim().equals(""));
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Constructor that = (Constructor) o;

            return getName().equals(that.getName());

        }

        @Override
        public int hashCode() {
            return getName().hashCode();
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * Represents the isa_ function.
     */
    public static class IsA<T> {
        private T value;

        public IsA(T value) {
            assert(value != null);
            this.value = value;
        }

        public T getValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            IsA<?> isa = (IsA<?>) o;

            return getValue().equals(isa.getValue());

        }

        @Override
        public int hashCode() {
            return getValue().hashCode();
        }

        @Override
        public String toString() {
            return "isa_" + value;
        }
    }

    /**
     * Represents the argof_constr function
     */
    public static class ArgOf {
        private Constructor constr;

        public ArgOf(Constructor constr) {
            assert(constr != null);
            this.constr = constr;
        }

        public Constructor getConstr() {
            return constr;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ArgOf argOf = (ArgOf) o;

            return getConstr().equals(argOf.getConstr());

        }

        @Override
        public int hashCode() {
            return getConstr().hashCode();
        }

        @Override
        public String toString() {
            return "argof_" + constr;
        }
    }

    public ConstructorReduction() {}

    public static Constructor getConstructor(String name) {
        return new Constructor(name);
    }

    public static <T> IsA<T> getIsaOperator(T constr) {
        return new IsA<>(constr);
    }

    public static ArgOf getArgOfOperator(Constructor constr) {
        return new ArgOf(constr);
    }

    @Override
    public int getNumberOfArguments() {
        return 1;
    }

    @Override
    public boolean isConstantMatching(ASTConstant c) {
        boolean isIsa = c.getValue() instanceof IsA;
        boolean isArgOf = c.getValue() instanceof ArgOf;
        return (isIsa || isArgOf);
    }

    @Override
    public Optional<ASTTerm> getRHS(ASTConstant function, List<ASTTerm> terms) {
        // the function must isa_constr, isa_int or argof_constr and it has only one argument
        if (!isSignatureMatching(function, terms)) {
            return Optional.empty();
        }

        ASTTerm constructorTerm = terms.get(0).getLMOMTerm();
        List<ASTTerm> args = terms.get(0).getLMOMArguments();

        // check if the argument is a constructor
        if (!(constructorTerm instanceof ASTConstant)) {
            return Optional.empty();
        }

        ASTConstant constructor = (ASTConstant) constructorTerm;

        // now we check if the function is IsA or Argof
        if (function.getValue() instanceof IsA) {
            // it's isa
            IsA isaFunction = (IsA) function.getValue();

            // we can only reduce it to true or false if the constructor classes match our isa_constructor
            if (constructor.getValue().getClass().isInstance(isaFunction.getValue())) {
                // now we can check if the constructor values actually match
                if (constructor.getValue().equals(isaFunction.getValue())) {
                    return Optional.of(new ASTConstant(true));
                } else {
                    return Optional.of(new ASTConstant(false));
                }
            }
        }
        else if (function.getValue() instanceof ArgOf) {
            // it's argof
            ArgOf argofFunction = (ArgOf) function.getValue();

            // check if the constructors match
            if (constructor.getValue().equals(argofFunction.getConstr())) {
                if (args.size() == 1) {
                    // there are no 1-sized tuples
                    return Optional.of(args.get(0));
                }
                else {
                    int n = args.size();
                    // return a tuple with the arguments
                    ASTTerm tuple = new ASTConstant(TupleReduction.getTupleConstructor(n));
                    for (ASTTerm t : args) {
                        tuple = new ASTApplication(tuple, t);
                    }
                    return Optional.of(tuple);
                }
            }
        }

        return Optional.empty();
    }

    public static Optional<ASTConstant> toConst(String name) {
        if (name.startsWith("isa_constr_")) {
            String constrName = name.substring(11);
            return Optional.of(new ASTConstant(getIsaOperator(ConstructorReduction.getConstructor(constrName))));
        }
        else if (name.startsWith("isa_int_")) {
            String val = name.substring(8);
            int n = Integer.parseInt(val);
            return Optional.of(new ASTConstant(getIsaOperator(n)));
        }
        else if (name.startsWith("isa_char_")) {
            String val = name.substring(9);
            char c = val.charAt(0);
            return Optional.of(new ASTConstant(getIsaOperator(c)));
        }
        else if (name.startsWith("isa_float_")) {
            String val = name.substring(10);
            float f = Float.parseFloat(val);
            return Optional.of(new ASTConstant(getIsaOperator(f)));
        }
        else if (name.startsWith("isa_bool_")) {
            String val = name.substring(9);
            boolean b = Boolean.parseBoolean(val);
            return Optional.of(new ASTConstant(getIsaOperator(b)));
        }
        else if (name.startsWith("argof_")) {
            String constrName = name.substring(6);
            return Optional.of(new ASTConstant(getArgOfOperator(ConstructorReduction.getConstructor(constrName))));
        }
        else {
            return Optional.empty();
        }
    }
}
