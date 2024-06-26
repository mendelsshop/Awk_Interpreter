import java.util.LinkedList;

public class TokenHandler {
    private LinkedList<Token> tokenStream;

    public TokenHandler(LinkedList<Token> tokenStream) {
        this.tokenStream = tokenStream;
    }

    // does null checking for if we try to go past the end of the token stream
    public Optional<Token> MatchAndRemove(Token.TokenType t) {
        var peek = tokenStream.peekFirst();
        if (peek != null) {
            if (peek.getType() == t) {
                tokenStream.pop();
                return Optional.of(peek);
            }
        }
        return Optional.empty();
    }

    public boolean MoreTokens() {
        return !tokenStream.isEmpty();
    }

    // index starts at 0
    public Optional<Token> Peek(int j) {
        try {
            return Optional.of(tokenStream.get(j));
        } catch (IndexOutOfBoundsException e) {
            return Optional.empty();
        }

    }
}
