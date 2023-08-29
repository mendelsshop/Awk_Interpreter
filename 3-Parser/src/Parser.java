import java.util.LinkedList;
import java.util.Optional;

public class Parser {
    private TokenHandler tokens;

    public Parser(LinkedList<Token> tokenStream) {
        tokens = new TokenHandler(tokenStream);
    }

    public ProgramNode Parse() throws Exception {
        return null;
    }

    private boolean AcceptSeperators() {
        return false;
    }

    private boolean ParseFunctionCall(ProgramNode program) {
        return false;
    }

    private boolean ParseAction(ProgramNode program) {
        return false;
    }

    private BlockNode ParseBlock() {
        return null;
    }

    private Optional<Node> ParseOperation() {
        return Optional.empty();
    }
}
