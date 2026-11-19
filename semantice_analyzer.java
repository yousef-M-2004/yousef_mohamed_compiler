import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SemanticAnalyzer {
    private final List<Stmt> program;
    private final Set<String> declared = new HashSet<>();
    private final List<String> errors = new ArrayList<>();

    public SemanticAnalyzer(List<Stmt> program) {
        this.program = program;
    }

    public List<String> analyze() {
        for (Stmt stmt : program) {
            if (stmt instanceof Let let) {
                checkExpr(let.value());
                if (!declared.add(let.name())) {
                    errors.add("variable '" + let.name() + "' is already declared");
                }
            } else if (stmt instanceof Print print) {
                checkExpr(print.expr());
            }
        }

        return errors;
    }

    private void checkExpr(Expr expr) {
        if (expr instanceof Var var) {
            if (!declared.contains(var.name())) {
                errors.add("variable '" + var.name() + "' is used before declaration");
            }
        } else if (expr instanceof BinOp bin) {
            checkExpr(bin.left());
            checkExpr(bin.right());

            if (bin.op() == '/' && bin.right() instanceof Num num && num.value() == 0) {
                errors.add("division by zero");
            }
        }
    }
}
