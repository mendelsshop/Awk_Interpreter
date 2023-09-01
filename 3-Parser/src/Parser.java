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

    private boolean ParseFunctionCall(ProgramNode program) throws Exception {
        if (tokens.MatchAndRemove(Token.TokenType.FUNCTION).isEmpty()) {
            return false;
        }
        var functionName = tokens.MatchAndRemove(Token.TokenType.WORD)
                .orElseThrow(() -> new Exception("function without name")).getValue().get();
        tokens.MatchAndRemove(Token.TokenType.OPENPAREN)
                .orElseThrow(() -> new Exception("function does not have parentheses before parameter"));
        var parameters = new LinkedList<String>();
        while (tokens.MoreTokens()) {
            if (tokens.MatchAndRemove(Token.TokenType.WORD).<Boolean>map(c -> {
                parameters.add(c.getValue().get());

                return tokens.MatchAndRemove(Token.TokenType.CLOSEPAREN).map(a -> true)
                        .or(() -> tokens.MatchAndRemove(Token.TokenType.COMMA).map(d -> tokens.Peek(0)
                                .filter(b -> b.getType() == Token.TokenType.WORD).map(h -> false)
                                .orElseThrow(() -> {
                                    throw new ParseException(
                                            "comma in function parameter list must be followed by another parameter");
                                })))
                        .orElseThrow(() -> new RuntimeException(
                                "function parameter must be followed by a comma or closeing parenthesis"));
            }).or(() -> tokens.MatchAndRemove(Token.TokenType.CLOSEPAREN).map(c -> true))
                    .orElseThrow(() -> new Exception("unknown token type in parameter list" + tokens.Peek(0)))) {
                break;
            }
        }
        var block = ParseBlock().getStatements();
        program.addFunction(new FunctionNode(functionName, parameters, block));
        return true;
    }

    private boolean ParseAction(ProgramNode program) throws Exception{
        if (tokens.MatchAndRemove(Token.TokenType.BEGIN).isPresent()) {
            var block = ParseBlock();
            program.addToBegin(block);
            return true;
        } else if (tokens.MatchAndRemove(Token.TokenType.END).isPresent()) {
            var block = ParseBlock();
            program.addToEnd(block);
            return true;
        }
        var Condition = ParseOperation();
        var block = ParseBlock();
        block.setCondition(Condition);
        return true;
    }

    private BlockNode ParseBlock() throws Exception {
        tokens.MatchAndRemove(Token.TokenType.OPENBRACE)
                .orElseThrow(() -> new Exception("block without open curly brace at start")).getValue().get();
        return null;

    }

    private Optional<Node> ParseOperation() {
        return Optional.empty();
    }

    public class ParseException extends RuntimeException {
        public ParseException(String message) {
            
        }
    }
}
