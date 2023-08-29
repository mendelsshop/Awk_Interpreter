import java.util.LinkedList;
import java.util.Optional;

public class Parser {
    private TokenHandler tokens;

    public Parser(LinkedList<Token> tokenStream) {
        tokens = new TokenHandler(tokenStream);
    }

    public ProgramNode Parse() throws Exception {
        var program = new ProgramNode();
        while (tokens.MoreTokens()) {
            if (!ParseFunctionCall(program)) {
                ParseAction(program);
            }
        }
        return program;
    }

    private boolean AcceptSeperators() {
        boolean foundSeperators = false;
        // since MatchAndRemove does out of bounds checks
        while (tokens.MatchAndRemove(Token.TokenType.SEPERATOR).isPresent()) {
            foundSeperators = true;
        }
        return foundSeperators;
    }

    private boolean ParseFunctionCall(ProgramNode program) {
        if (tokens.MatchAndRemove(Token.TokenType.FUNCTION).isEmpty()) {
            return false;
        }

        return false;
    }

    private boolean ParseAction(ProgramNode program) {
        if (tokens.MatchAndRemove(Token.TokenType.BEGIN).isPresent()) {

        } else if (tokens.MatchAndRemove(Token.TokenType.END).isPresent()) {

        }
        var Condition = ParseOperation();
        var block = ParseOperation();
        return true;
    }

    private BlockNode ParseBlock() {
        return null;

    }

    private Optional<Node> ParseOperation() {
        return Optional.empty();
    }
}
