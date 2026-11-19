import java.util.List;

public class CodeGenerator {
    private final List<Stmt> program;

    public CodeGenerator(List<Stmt> program) {
        this.program = program;
    }

    public String generate() {
        StringBuilder out = new StringBuilder();

        out.append("public class Program {\n");
        out.append("    public static void main(String[] args) {\n");

        for (Stmt stmt : program) {
            if (stmt instanceof Let let) {
                out.append("        int ").append(let.name())
                        .append(" = ").append(expression(let.value())).append(";\n");
            } else if (stmt instanceof Print print) {
                out.append("        System.out.println(")
                        .append(expression(print.expr())).append(");\n");
            }
        }

        out.append("    }\n");
        out.append("}\n");
        return out.toString();
    }

    private String expression(Expr expr) {
        if (expr instanceof Num num) {
            return String.valueOf(num.value());
        }
        if (expr instanceof Var var) {
            return var.name();
        }
        if (expr instanceof BinOp bin) {
            return "(" + expression(bin.left()) + " " + bin.op() + " "
                    + expression(bin.right()) + ")";
        }

        throw new IllegalStateException("unknown expression");
    }
}
