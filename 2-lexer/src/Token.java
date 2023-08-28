import java.util.Optional;

public class Token {
    private int startPosition;
    private int lineNumber;
    private TokenType type;
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
        WORD, NUMBER, StringLiteral, Pattern,
        SEPERATOR, WHILE, IF, DO, FOR, BREAK, CONTINUE, ELSE, RETURN, BEGIN, END, PRINT, PRINTF, NEXT, IN, DELETE, GETLINE, EXIT, NEXTFILE, FUNCTION
    }

    @Override
    public String toString() {
        return type + value.map(c -> "(" + c + ")").orElse("");
    }
}
