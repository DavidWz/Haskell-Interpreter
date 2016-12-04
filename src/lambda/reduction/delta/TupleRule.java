package lambda.reduction.delta;

import lambda.ast.ASTConstant;
import lambda.ast.ASTTerm;

import java.util.List;
import java.util.Optional;

/**
 * Represents the isa_n-tuple and sel_n,i delta rules.
 */
public class TupleRule extends DeltaRule {
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

    private int n;
    private final IsATuple isaOperator;
    private final TupleConstant tupleConstructor;

    /**
     * Constructs the delta rules for tuples of size n.
     * @param n the tuple size. must be positive and not 1.
     */
    public TupleRule(int n) {
        assert(n >= 0);
        assert(n != 1);
        this.n = n;
        this.isaOperator = new IsATuple(n);
        this.tupleConstructor = new TupleConstant(n);
    }

    public int getN() {
        return n;
    }

    public IsATuple getIsaOperator() {
        return isaOperator;
    }

    public TupleConstant getTupleConstructor() {
        return tupleConstructor;
    }

    public Sel getSelOperator(int i) {
        return new Sel(n, i);
    }

    @Override
    public int getNumberOfArguments() {
        return 1;
    }

    @Override
    public boolean isConstantMatching(ASTConstant c) {
        boolean isIsa = c.getValue().equals(isaOperator);
        boolean isSel = c.getValue() instanceof Sel && ((Sel) c.getValue()).getN() == n;
        return (isIsa || isSel);
    }

    @Override
    public Optional<ASTTerm> getRHS(ASTConstant constant, List<ASTTerm> terms) {
        // the constant must be Isa_n-tuple and it has only one argument
        if (isSignatureMatching(constant, terms)) {
            // check if the argument is a n-tupel
            ASTTerm tupel = terms.get(0);
            ASTTerm constr = tupel.getLMOMTerm();
            List<ASTTerm> args = tupel.getLMOMArguments();

            if (constr instanceof ASTConstant && args.size() == n) {
                ASTConstant constrConstant = (ASTConstant) constr;
                if (constrConstant.getValue().equals(tupleConstructor)) {
                    // it's an n-tuple, so reduce it according to the operator

                    if (constant.getValue().equals(isaOperator)) {
                        // it's isa
                        return Optional.of(new ASTConstant(true));
                    }
                    else {
                        // it's sel
                        Sel op = (Sel) constant.getValue();
                        // -1 because sel is starts counting at 1
                        return Optional.of(args.get(op.getI()-1));
                    }
                }
            }
        }

        return Optional.empty();
    }
}
