package lambda.type;

import haskell.ast.ASTDataDecl;
import haskell.ast.ASTType;
import lambda.ast.ASTConstant;
import lambda.ast.ASTTerm;

/**
 * This exception is thrown when an expression is not correctly typed.
 */
public abstract class TypeException extends Exception {
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

    public static class InconsistentDataDeclException extends TypeException {
        private ASTDataDecl oldDecl;
        private ASTDataDecl newDecl;

        public InconsistentDataDeclException(ASTDataDecl oldDecl, ASTDataDecl newDecl) {
            this.oldDecl = oldDecl;
            this.newDecl = newDecl;
        }

        @Override
        public String getMessage() {
            StringBuilder builder = new StringBuilder();
            builder.append("The data type ").append(newDecl).append(" conflicts with ").append(oldDecl).append(".");
            return builder.toString();
        }
    }
}
