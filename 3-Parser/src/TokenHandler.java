import java.util.LinkedList;
import java.util.Optional;


public class TokenHandler {
    private LinkedList<Token> tokenStream;

    public TokenHandler(LinkedList<Token> tokenStream) {
        this.tokenStream = tokenStream;
    }

    public Optional<Token> MatchAndRemove(Token.TokenType t)  {
        return Optional.empty();
    }

    public boolean MoreTokens() {
        return false;
    }

    public Optional<Token> Peek(int j) {
        return Optional.empty();
    }
}
