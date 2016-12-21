package lambda.reduction.delta;

import haskell.ast.ASTFuncType;
import haskell.ast.ASTTupleType;
import haskell.ast.ASTType;
import haskell.ast.ASTVariable;
import lambda.ast.ASTConstant;
import lambda.ast.ASTTerm;
import lambda.type.PredefinedType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Represents the isa_n-tuple and sel_n,i delta rules.
 */
public class TupleReduction extends DeltaReduction {
    /**
     * Represents the tupleConstructor for tupels.
     */
    public static class TupleConstant {
        private int length;

        /**
         * Constructs a new tupel with the given length.
         * @param length the length. must be positive and not 1.
         */
        public TupleConstant(int length) {
            assert(length >= 0);
            assert(length != 1);
            this.length = length;
        }

        public int getLength() {
            return length;
        }

        public ASTType getType() {
            // tuple_n :: a1 -> a2 -> ... -> an -> (a1, ..., an)

            // (a1, ..., an)
            List<ASTType> vars = new ArrayList<>();
            for (int i = 1; i <= length; i++) {
                vars.add(new ASTVariable("a"+i));
            }
            ASTType type = new ASTTupleType(vars);

            // a1 -> a2 -> ... -> an -> tuple
            for (int i = length; i >= 1; i--) {
                type = new ASTFuncType(vars.get(i-1), type);
            }

            return type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TupleConstant that = (TupleConstant) o;

            return getLength() == that.getLength();

        }

        @Override
        public int hashCode() {
            return getLength();
        }

        @Override
        public String toString() {
            return "tupel_" + length;
        }
    }

    /**
     * Represents the isa_n-tuple function
     */
    public static class IsATuple {
        private int n;

        public IsATuple(int n) {
            assert(n >= 0);
            assert(n != 1);
            this.n = n;
        }

        public int getN() {
            return n;
        }

        public ASTType getType() {
            // isa_n-tuple :: (a1, ..., an) -> Bool

            // (a1, ..., an)
            List<ASTType> vars = new ArrayList<>();
            for (int i = 1; i <= n; i++) {
                vars.add(new ASTVariable("a"+i));
            }
            ASTType tuple = new ASTTupleType(vars);

            ASTType type = new ASTFuncType(tuple, PredefinedType.BOOL.getType());
            return type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            IsATuple isATuple = (IsATuple) o;

            return getN() == isATuple.getN();

        }

        @Override
        public int hashCode() {
            return getN();
        }

        @Override
        public String toString() {
            return "isa_" + n + "-tuple";
        }
    }

    /**
     * Represents the sel_n,i function
     */
    public static class Sel {
        private int n;
        private int i;

        public Sel(int n, int i) {
            assert(n >= 2);
            assert(i >= 1);
            assert(i <= n);
            this.n = n;
            this.i = i;
        }

        public int getN() {
            return n;
        }

        public int getI() {
            return i;
        }

        public ASTType getType() {
            // isa_n-tuple :: (a1, ..., an) -> Bool

            // (a1, ..., an)
            List<ASTType> vars = new ArrayList<>();
            for (int i = 1; i <= n; i++) {
                vars.add(new ASTVariable("a"+i));
            }
            ASTType tuple = new ASTTupleType(vars);

            ASTType type = new ASTFuncType(tuple, vars.get(i-1));
            return type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Sel sel = (Sel) o;

            if (getN() != sel.getN()) return false;
            return getI() == sel.getI();

        }

        @Override
        public int hashCode() {
            int result = getN();
            result = 31 * result + getI();
            return result;
        }

        @Override
        public String toString() {
            return "sel_" + n + "," + i;
        }
    }


    public TupleReduction() {}

    public static TupleConstant getTupleConstructor(int n) {
        return new TupleConstant(n);
    }

    public static IsATuple getIsaOperator(int n) {
        return new IsATuple(n);
    }

    public static Sel getSelOperator(int n, int i) {
        return new Sel(n, i);
    }

    @Override
    public int getNumberOfArguments() {
        return 1;
    }

    @Override
    public boolean isConstantMatching(ASTConstant c) {
        boolean isIsa = c.getValue() instanceof IsATuple;
        boolean isSel = c.getValue() instanceof Sel;
        return (isIsa || isSel);
    }

    @Override
    public Optional<ASTTerm> getRHS(ASTConstant function, List<ASTTerm> terms) {
        // the function must be Isa_n-tuple or sel_n,i and it has only one argument
        if (!isSignatureMatching(function, terms)) {
            return Optional.empty();
        }

        // check if the argument is a n-tupel
        ASTTerm tupel = terms.get(0);
        ASTTerm constr = tupel.getLMOMTerm();
        List<ASTTerm> args = tupel.getLMOMArguments();

        // we can only check if it's a tupel-constructor if it's actually a fully reduced function constant
        if (constr instanceof ASTConstant) {
            ASTConstant constrConstant = (ASTConstant) constr;
            if (constrConstant.getValue() instanceof TupleConstant) {

                // it's a tuple, so reduce it according to the operator
                if (function.getValue() instanceof IsATuple) {
                    // it's isa
                    IsATuple op = (IsATuple) function.getValue();
                    if (op.getN() == args.size()) {
                        return Optional.of(new ASTConstant(true));
                    }
                    else {
                        return Optional.of(new ASTConstant(false));
                    }
                }
                else {
                    // it's sel
                    Sel op = (Sel) function.getValue();
                    if (op.getN() == args.size()) {
                        // -1 because sel is starts counting at 1
                        return Optional.of(args.get(op.getI()-1));
                    }
                }
            }
        }

        return Optional.empty();
    }

    public static Optional<ASTConstant> toConst(String name) {
        if (name.startsWith("isa_tuple_")) {
            String tupleN = name.substring(10);
            int n = Integer.parseInt(tupleN);
            return Optional.of(new ASTConstant(getIsaOperator(n)));
        }
        else if(name.startsWith("sel_")) {
            String niStr = name.substring(4);
            String[] niArray = niStr.split("_");
            assert(niArray.length == 2);
            int n = Integer.parseInt(niArray[0]);
            int i = Integer.parseInt(niArray[1]);
            return Optional.of(new ASTConstant(getSelOperator(n, i)));
        }
        else {
            return Optional.empty();
        }
    }
}
