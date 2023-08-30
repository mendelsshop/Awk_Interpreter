import java.util.Optional;

public class Token {
    private int startPosition;
    private int lineNumber;
    private TokenType type;
    public TokenType getType() {
        return type;
    }

    private Optional<String> value = Optional.empty();

    public Token(int position, int line, TokenType type, String value) {
        startPosition = position;
        lineNumber = line;
        this.type = type;
        this.value = Optional.ofNullable(value);
    }

    public Token(int position, int line, TokenType type) {
        startPosition = position;
        lineNumber = line;
        this.type = type;
    }

    public enum TokenType {
        // TODO: somethings need better names
        WORD, NUMBER, STRINGLITERAL, PATTERN,
        SEPERATOR, OPENPAREN, CLOSEPAREN, ASSIGN, DOLLAR, MATCH, NOT, PLUS, MINUS, DIVIDE, MULTIPLY, MODULO, COLON,
        LESSTHAN, GREATERTHAN, OPENBRACKET, CLOSEBRACKET, VERTICALBAR, EXPONENT, OPENBRACE, CLOSEBRACE, QUESTION, COMMA,
        LESSTHANEQUAL, GREATERTHANEQUAL, EQUAL, PLUSEQUAL, MINUSEQUAL, DIVIDEQUAL, MULTIPLYEQUAL, PLUSPLUS, MINUSMINUS,
        EXPONENTEQUAL, NOTMATCH, OR, AND, NOTEQUAL, APPEND, MODULOEQUAL,
        WHILE, IF, DO, FOR, BREAK, CONTINUE, ELSE, RETURN, BEGIN, END, PRINT, PRINTF, NEXT, IN, DELETE, GETLINE, EXIT,
        NEXTFILE, FUNCTION

    }

    @Override
    public String toString() {
        return type + value.map(c -> "(" + c + ")").orElse("");
    }
}
