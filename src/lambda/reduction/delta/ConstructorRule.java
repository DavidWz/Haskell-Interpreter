package lambda.reduction.delta;

import lambda.ast.ASTApplication;
import lambda.ast.ASTConstant;
import lambda.ast.ASTTerm;

import java.util.List;
import java.util.Optional;

/**
 * Represents the isa_constr and argof_constr delta rules.
 */
public class ConstructorRule extends DeltaRule {
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
     * Represents the isa_constr function
     */
    public static class IsAConstr {
        private Constructor constr;

        public IsAConstr(Constructor constr) {
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

            IsAConstr isAConstr = (IsAConstr) o;

            return getConstr().equals(isAConstr.getConstr());

        }

        @Override
        public int hashCode() {
            return getConstr().hashCode();
        }

        @Override
        public String toString() {
            return "isa_constr_" + constr;
        }
    }

    /**
     * Represents the isa_int function
     */
    public static class IsAInt {
        private int n;

        public IsAInt(int n) {
            this.n = n;
        }

        public int getInt() {
            return n;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            IsAInt isAInt = (IsAInt) o;

            return n == isAInt.n;

        }

        @Override
        public int hashCode() {
            return n;
        }

        @Override
        public String toString() {
            return "isa_int_" + n;
        }
    }

    /**
     * Represents the isa_bool function
     */
    public static class IsABool {
        private boolean b;

        public IsABool(boolean b) {
            this.b = b;
        }

        public boolean getBool() {
            return b;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            IsABool isABool = (IsABool) o;

            return b == isABool.b;

        }

        @Override
        public int hashCode() {
            return (b ? 1 : 0);
        }

        @Override
        public String toString() {
            return "isa_bool_" + b;
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

    public ConstructorRule() {}

    public static Constructor getConstructor(String name) {
        return new Constructor(name);
    }

    public static IsAConstr getIsaOperator(Constructor constr) {
        return new IsAConstr(constr);
    }

    private static IsAInt getIsaIntOperator(int n) {
        return new IsAInt(n);
    }

    private static IsABool getIsaBoolOperator(boolean b) {
        return new IsABool(b);
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
        boolean isIsa = c.getValue() instanceof IsAConstr ||
                        c.getValue() instanceof IsAInt ||
                        c.getValue() instanceof IsABool;
        boolean isArgOf = c.getValue() instanceof ArgOf;
        return (isIsa || isArgOf);
    }

    @Override
    public Optional<ASTTerm> getRHS(ASTConstant constant, List<ASTTerm> terms) {
        // the constant must be isa_constr, isa_int or argof_constr and it has only one argument
        if (isSignatureMatching(constant, terms)) {
            // check if the argument is a fitting constr
            ASTTerm data = terms.get(0);
            ASTTerm constr = data.getLMOMTerm();
            List<ASTTerm> args = data.getLMOMArguments();

            if (constr instanceof ASTConstant) {
                ASTConstant constrConstant = (ASTConstant) constr;

                if (constrConstant.getValue() instanceof Constructor) {
                    // it's a constructor, so reduce it according to the operator

                    if (constant.getValue() instanceof IsAConstr) {
                        // it's isa
                        IsAConstr op = (IsAConstr) constant.getValue();

                        // check if the constructors match
                        if (op.getConstr().equals(constrConstant.getValue())) {
                            return Optional.of(new ASTConstant(true));
                        }
                        else {
                            return Optional.of(new ASTConstant(false));
                        }
                    }
                    else {
                        // it's argof
                        ArgOf op = (ArgOf) constant.getValue();

                        // check if the constructors match
                        if (op.getConstr().equals(constrConstant.getValue())) {

                            if (args.size() == 1) {
                                // there are no 1-sized tuples
                                return Optional.of(args.get(0));
                            }
                            else {
                                int n = args.size();
                                // return a tuple with the arguments
                                ASTTerm tuple = new ASTConstant(TupleRule.getTupleConstructor(n));
                                for (ASTTerm t : args) {
                                    tuple = new ASTApplication(tuple, t);
                                }
                                return Optional.of(tuple);
                            }
                        }
                    }
                }
                else if (constrConstant.getValue() instanceof Integer) {
                    // it's an integer, so try to reduce it with isa_int_ delta rules

                    if (constant.getValue() instanceof IsAInt) {
                        // it's isa_int
                        IsAInt op = (IsAInt) constant.getValue();
                        int val = (int) constrConstant.getValue();

                        // check if the constructors match
                        if (val == op.getInt()) {
                            return Optional.of(new ASTConstant(true));
                        }
                        else {
                            return Optional.of(new ASTConstant(false));
                        }
                    }
                }
                else if (constrConstant.getValue() instanceof Boolean) {
                    // it's a boolean, so try to reduce it with isa_bool_ delta rules

                    if (constant.getValue() instanceof IsABool) {
                        // it's isa_bool
                        IsABool op = (IsABool) constant.getValue();
                        boolean val = (boolean) constrConstant.getValue();

                        // check if the constructors match
                        if (val == op.getBool()) {
                            return Optional.of(new ASTConstant(true));
                        }
                        else {
                            return Optional.of(new ASTConstant(false));
                        }
                    }
                }
            }
        }

        return Optional.empty();
    }

    public static Optional<ASTConstant> toConst(String name) {
        if (name.startsWith("isa_constr_")) {
            String constrName = name.substring(11);
            return Optional.of(new ASTConstant(getIsaOperator(ConstructorRule.getConstructor(constrName))));
        }
        else if (name.startsWith("isa_int_")) {
            String val = name.substring(8);
            int n = Integer.parseInt(val);
            return Optional.of(new ASTConstant(getIsaIntOperator(n)));
        }
        else if (name.startsWith("isa_bool_")) {
            String val = name.substring(9);
            boolean b = Boolean.parseBoolean(val);
            return Optional.of(new ASTConstant(getIsaBoolOperator(b)));
        }
        else if (name.startsWith("argof_")) {
            String constrName = name.substring(6);
            return Optional.of(new ASTConstant(getArgOfOperator(ConstructorRule.getConstructor(constrName))));
        }
        else {
            return Optional.empty();
        }
    }
}
