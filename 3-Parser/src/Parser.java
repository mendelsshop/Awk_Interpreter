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
            // parsing function signature
            // CheckedSupplier is just like the Functional Supplier interface, but the
            // lambda it takes can through catchable exceptions
            if (tokens.MatchAndRemove(Token.TokenType.WORD).<FunctionalLexer.CheckedSupplier<Boolean, Exception>>map(
                    c -> () -> {
                        parameters.add(c.getValue().get());
                        return tokens
                                // if we reach a `)` we are done with the function signature
                                .MatchAndRemove(
                                        Token.TokenType.CLOSEPAREN).<FunctionalLexer.CheckedSupplier<Boolean, Exception>>map(
                                                a -> () -> true)
                                // if we hit a `,`, we make sure that the next token is also a function
                                // parameter
                                .or(() -> tokens.MatchAndRemove(Token.TokenType.COMMA).map(d -> () -> tokens.Peek(0)
                                        .filter(b -> b.getType() == Token.TokenType.WORD).map(h -> false)
                                        // otherwise we through an exception
                                        .orElseThrow(() -> new Exception(
                                                "comma in function parameter list must be followed by another parameter"))))
                                // if the next token after the name of a function parameter is not a `,` or `)`
                                // we know we have an invalid function signature so we give back an error
                                .orElseThrow(() -> new Exception(
                                        "function parameter must be followed by a comma or closeing parenthesis"))
                                .get();
                    }).or(() -> tokens.MatchAndRemove(Token.TokenType.CLOSEPAREN).map(c -> () -> true))
                    .orElseThrow(() -> new Exception("unknown token type in parameter list" + tokens.Peek(0))).get()) {
                break;
            }
        }

        var block = ParseBlock().getStatements();
        program.addFunction(new FunctionNode(functionName, parameters, block));
        return true;
    }

    private boolean ParseAction(ProgramNode program) throws Exception {
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

    public class ParseException extends Exception {
        public ParseException(String message) {

        }
    }
}
