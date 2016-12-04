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


    public TupleRule() {}

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
    public Optional<ASTTerm> getRHS(ASTConstant constant, List<ASTTerm> terms) {
        // the constant must be Isa_n-tuple or sel_n,i and it has only one argument
        if (isSignatureMatching(constant, terms)) {
            // check if the argument is a n-tupel
            ASTTerm tupel = terms.get(0);
            ASTTerm constr = tupel.getLMOMTerm();
            List<ASTTerm> args = tupel.getLMOMArguments();

            if (constr instanceof ASTConstant) {
                ASTConstant constrConstant = (ASTConstant) constr;
                if (constrConstant.getValue() instanceof TupleConstant) {
                    // it's an tuple, so reduce it according to the operator

                    if (constant.getValue() instanceof IsATuple) {
                        // it's isa
                        IsATuple op = (IsATuple) constant.getValue();
                        if (op.getN() == args.size()) {
                            return Optional.of(new ASTConstant(true));
                        }
                    }
                    else {
                        // it's sel
                        Sel op = (Sel) constant.getValue();
                        if (op.getN() == args.size()) {
                            // -1 because sel is starts counting at 1
                            return Optional.of(args.get(op.getI()-1));
                        }
                    }
                }
            }
        }

        return Optional.empty();
    }
}
