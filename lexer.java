import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Lexer {
    enum TokenType {
        LET,
        PRINT,
        IDENTIFIER,
        NUMBER,
        EQUAL,
        PLUS,
        MINUS,
        STAR,
        SLASH,
        SEMICOLON,
        EOF
    }

    record Token(TokenType type, String lexeme, int line, int column) {
    }

    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int current = 0;
    private int line = 1;
    private int column = 1;

    public Lexer(String source) {
        this.source = source;
    }

    public List<Token> scan() {
        while (!isAtEnd()) {
            int start = current;
            int tokenLine = line;
            int tokenColumn = column;
            char c = advance();

            if (Character.isWhitespace(c)) {
                continue;
            } else if (Character.isLetter(c)) {
                while (!isAtEnd() && Character.isLetterOrDigit(peek())) {
                    advance();
                }
                String text = source.substring(start, current);
                tokens.add(new Token(keyword(text), text, tokenLine, tokenColumn));
            } else if (Character.isDigit(c)) {
                while (!isAtEnd() && Character.isDigit(peek())) {
                    advance();
                }
                tokens.add(new Token(TokenType.NUMBER, source.substring(start, current), tokenLine, tokenColumn));
            } else {
                TokenType type = switch (c) {
                    case '=' -> TokenType.EQUAL;
                    case '+' -> TokenType.PLUS;
                    case '-' -> TokenType.MINUS;
                    case '*' -> TokenType.STAR;
                    case '/' -> TokenType.SLASH;
                    case ';' -> TokenType.SEMICOLON;
                    default -> throw new RuntimeException("Line " + line + ": unexpected character '" + c + "'");
                };
                tokens.add(new Token(type, String.valueOf(c), tokenLine, tokenColumn));
            }
        }

        tokens.add(new Token(TokenType.EOF, "", line, column));
        return tokens;
    }

    private TokenType keyword(String text) {
        if (text.equalsIgnoreCase("LET")) {
            return TokenType.LET;
        }
        if (text.equalsIgnoreCase("PRINT")) {
            return TokenType.PRINT;
        }
        return TokenType.IDENTIFIER;
    }

    private char advance() {
        char c = source.charAt(current++);

        if (c == '\n') {
            line++;
            column = 1;
        } else {
            column++;
        }

        return c;
    }

    private char peek() {
        return isAtEnd() ? '\0' : source.charAt(current);
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private static String classification(TokenType type) {
        return switch (type) {
            case LET, PRINT -> "KEYWORD";
            case IDENTIFIER -> "IDENTIFIER";
            case NUMBER -> "NUMBER";
            case EQUAL, PLUS, MINUS, STAR, SLASH -> "OPERATOR";
            case SEMICOLON -> "PUNCTUATION";
            case EOF -> "END OF FILE";
        };
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        StringBuilder source = new StringBuilder();

        System.out.println("Type your statements. Press Enter twice to finish:");

        while (scanner.hasNextLine()) {
            String inputLine = scanner.nextLine();

            if (inputLine.isEmpty()) {
                break;
            }

            source.append(inputLine).append('\n');
        }

        try {
            List<Token> tokens = new Lexer(source.toString()).scan();

            System.out.printf("%-12s %-12s %-12s %-6s %s%n",
                    "TYPE", "CLASS", "LEXEME", "LINE", "COLUMN");

            for (Token token : tokens) {
                System.out.printf("%-12s %-12s %-12s %-6d %d%n",
                        token.type(),
                        classification(token.type()),
                        "'" + token.lexeme() + "'",
                        token.line(),
                        token.column());
            }

            List<Stmt> program = new Parser(tokens).parse();

            System.out.println();
            System.out.println("AST:");

            for (Stmt stmt : program) {
                System.out.println(Parser.format(stmt));
            }
        } catch (RuntimeException e) {
            System.out.println("Error: " + e.getMessage());
        }
        scanner.close();
    }
}
