import java.util.ArrayList;
import java.util.List;

interface Stmt {
}

record Let(String name, Expr value) implements Stmt {
}

record Print(Expr expr) implements Stmt {
}

interface Expr {
}

record Num(int value) implements Expr {
}

record Var(String name) implements Expr {
}

record BinOp(Expr left, char op, Expr right) implements Expr {
}

public class Parser {
    private final List<Lexer.Token> tokens;
    private int current = 0;

    public Parser(List<Lexer.Token> tokens) {
        this.tokens = tokens;
    }

    public List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();

        while (!isAtEnd()) {
            statements.add(statement());
        }

        return statements;
    }

    private Stmt statement() {
        if (match(Lexer.TokenType.LET)) {
            return letStatement();
        }
        if (match(Lexer.TokenType.PRINT)) {
            return printStatement();
        }
        throw error(peek(), "expected LET or PRINT, found " + found(peek()));
    }

    private Stmt letStatement() {
        Lexer.Token name = consume(Lexer.TokenType.IDENTIFIER, "expected variable name");
        consume(Lexer.TokenType.EQUAL, "expected '='");

        Expr value = expression();
        consume(Lexer.TokenType.SEMICOLON, "expected ';'");

        return new Let(name.lexeme(), value);
    }

    private Stmt printStatement() {
        Expr value = expression();
        consume(Lexer.TokenType.SEMICOLON, "expected ';'");

        return new Print(value);
    }

    private Expr expression() {
        Expr expr = term();

        while (match(Lexer.TokenType.PLUS, Lexer.TokenType.MINUS)) {
            char op = previous().lexeme().charAt(0);
            Expr right = term();
            expr = new BinOp(expr, op, right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = primary();

        while (match(Lexer.TokenType.STAR, Lexer.TokenType.SLASH)) {
            char op = previous().lexeme().charAt(0);
            Expr right = primary();
            expr = new BinOp(expr, op, right);
        }

        return expr;
    }

    private Expr primary() {
        if (match(Lexer.TokenType.NUMBER)) {
            try {
                return new Num(Integer.parseInt(previous().lexeme()));
            } catch (NumberFormatException e) {
                throw error(previous(), "number is too large");
            }
        }

        if (match(Lexer.TokenType.IDENTIFIER)) {
            return new Var(previous().lexeme());
        }

        throw error(peek(), "expected number or identifier, found " + found(peek()));
    }

    private boolean match(Lexer.TokenType... types) {
        for (Lexer.TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private boolean check(Lexer.TokenType type) {
        return peek().type() == type;
    }

    private Lexer.Token advance() {
        if (!isAtEnd()) {
            current++;
        }
        return previous();
    }

    private Lexer.Token peek() {
        return tokens.get(current);
    }

    private Lexer.Token previous() {
        return tokens.get(current - 1);
    }

    private boolean isAtEnd() {
        return peek().type() == Lexer.TokenType.EOF;
    }

    private Lexer.Token consume(Lexer.TokenType type, String message) {
        if (check(type)) {
            return advance();
        }

        throw error(peek(), message + ", found " + found(peek()));
    }

    private String found(Lexer.Token token) {
        if (token.type() == Lexer.TokenType.EOF) {
            return "end of file";
        }

        return "'" + token.lexeme() + "'";
    }

    private RuntimeException error(Lexer.Token token, String message) {
        return new RuntimeException(
                "Line " + token.line() + ", column " + token.column() + ": " + message
        );
    }

    public static String format(Stmt stmt) {
        if (stmt instanceof Let let) {
            return "(let " + let.name() + " " + format(let.value()) + ")";
        }
        if (stmt instanceof Print print) {
            return "(print " + format(print.expr()) + ")";
        }

        throw new IllegalStateException("unknown statement");
    }

    private static String format(Expr expr) {
        if (expr instanceof Num num) {
            return String.valueOf(num.value());
        }
        if (expr instanceof Var var) {
            return var.name();
        }
        if (expr instanceof BinOp bin) {
            return "(" + bin.op() + " " + format(bin.left()) + " " + format(bin.right()) + ")";
        }

        throw new IllegalStateException("unknown expression");
    }
}
