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
        WORD, NUMBER, SEPERATOR
    }

    @Override
    public String toString() {
        return type + value.map(c -> "(" + c + ")").orElse("") + "[" + startPosition + " " + lineNumber + "]";
    }
}
