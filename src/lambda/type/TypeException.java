package lambda.type;

import haskell.complex.ast.ASTType;
import lambda.ast.ASTConstant;
import lambda.ast.ASTTerm;

/**
 * This exception is thrown when an expression is not correctly typed.
 */
public abstract class TypeException extends Exception {
    public static class IncorrectlyTypedException extends TypeException {
        private ASTTerm term;

        public IncorrectlyTypedException(ASTTerm term) {
            this.term = term;
        }

        @Override
        public String getMessage() {
            StringBuilder builder = new StringBuilder();
            builder.append("The following expression is incorrectly typed: ").append(term);
            return builder.toString();
        }
    }

    public static class TypeNotFoundException extends TypeException {
        private ASTConstant c;

        public TypeNotFoundException(ASTConstant c) {
            assert (c != null);
            this.c = c;
        }

        @Override
        public String getMessage() {
            StringBuilder builder = new StringBuilder();
            builder.append("Unknown constant ").append(c).append(".");
            return builder.toString();
        }
    }

    public static class CannotUnifyException extends TypeException {
        private ASTType type1;
        private ASTType type2;

        public CannotUnifyException(ASTType type1, ASTType type2) {
            assert (type1 != null);
            assert (type2 != null);
            this.type1 = type1;
            this.type2 = type2;
        }

        @Override
        public String getMessage() {
            StringBuilder builder = new StringBuilder();
            builder.append("Cannot unify ").append(type1).append(" with ").append(type2).append(".");
            return builder.toString();
        }
    }
}
