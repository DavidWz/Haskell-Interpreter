package lambda.type;

import haskell.ast.ASTTyConstr;
import haskell.ast.ASTType;
import haskell.ast.ASTTypeConstr;

/**
 * A list of predefined types.
 */
public enum PredefinedType {
    INTEGER(new ASTTypeConstr(new ASTTyConstr("Integer"))),
    FLOAT(new ASTTypeConstr(new ASTTyConstr("Float"))),
    CHAR(new ASTTypeConstr(new ASTTyConstr("Char"))),
    BOOL(new ASTTypeConstr(new ASTTyConstr("Bool")));

    private final ASTType type;

    PredefinedType(ASTType type) {
        this.type = type;
    }

    public ASTType getType() {
        return type;
    }
}
