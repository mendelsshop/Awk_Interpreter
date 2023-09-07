import java.util.LinkedList;
import java.util.Optional;

public class Parser {
    private TokenHandler tokens;
    private int line = 0;
    private int column = 0;

    public Parser(LinkedList<Token> tokenStream) {
        tokens = new TokenHandler(tokenStream);
    }

    public ProgramNode Parse() throws Exception {
        var program = new ProgramNode();
        while (tokens.MoreTokens()) {
            if (!ParseFunction(program)) {
                ParseAction(program);
            }
        }
        return program;
    }

    private boolean AcceptSeperators() {
        boolean foundSeperators = false;
        // since MatchAndRemove does out of bounds checks
        while (MatchAndRemove(Token.TokenType.SEPERATOR).isPresent()) {
            foundSeperators = true;
        }
        return foundSeperators;
    }

    // https://stackoverflow.com/questions/22687943/is-it-possible-to-declare-that-a-suppliert-needs-to-throw-an-exception

    public interface CheckedSupplier<T> {
        public T get() throws AwkException;
    }

    private boolean ParseFunction(ProgramNode program) throws Exception {
        if (MatchAndRemove(Token.TokenType.FUNCTION).isEmpty()) {
            return false;
        }
        var functionName = MatchAndRemove(Token.TokenType.WORD)
                .orElseThrow(() -> new Exception("function without name")).getValue().get();
        MatchAndRemove(Token.TokenType.OPENPAREN)
                .orElseThrow(() -> new Exception("function does not have parentheses before parameter"));
        var parameters = new LinkedList<String>();
        while (tokens.MoreTokens()) {
            // parsing function signature
            // CheckedSupplier is just like the Functional Supplier interface, but the
            // lambda it takes can through catchable exceptions
            if (MatchAndRemove(Token.TokenType.WORD).<CheckedSupplier<Boolean>>map(
                    c -> () -> {
                        parameters.add(c.getValue().get());
                        return
                        // if we reach a `)` we are done with the function signature
                        MatchAndRemove(
                                Token.TokenType.CLOSEPAREN)
                                .<CheckedSupplier<Boolean>>map(
                                        a -> () -> true)
                                // if we hit a `,`, we make sure that the next token is also a function
                                // parameter
                                .or(() -> MatchAndRemove(Token.TokenType.COMMA).map(d -> () -> tokens.Peek(0)
                                        .filter(b -> b.getType() == Token.TokenType.WORD).map(h -> false)
                                        // otherwise we through an exception
                                        .orElseThrow(() -> createException(
                                                "comma in function parameter list must be followed by another parameter"))))
                                // if the next token after the name of a function parameter is not a `,` or `)`
                                // we know we have an invalid function signature so we give back an error
                                .orElseThrow(() -> createException(
                                        "function parameter must be followed by a comma or closeing parenthesis"))
                                .get();
                    }).or(() -> MatchAndRemove(Token.TokenType.CLOSEPAREN).map(c -> () -> true))
                    .orElseThrow(() -> createException("unknown token type in parameter list" + tokens.Peek(0)))
                    .get()) {
                break;
            }
        }

        var block = ParseBlock().getStatements();
        program.addFunction(new FunctionNode(functionName, parameters, block));
        return true;
    }

    private boolean ParseAction(ProgramNode program) throws Exception {
        if (MatchAndRemove(Token.TokenType.BEGIN).isPresent()) {
            var block = ParseBlock();
            program.addToBegin(block);
            return true;
        } else if (MatchAndRemove(Token.TokenType.END).isPresent()) {
            var block = ParseBlock();
            program.addToEnd(block);
            return true;
        }
        var Condition = ParseOperation();
        var block = ParseBlock();
        block.setCondition(Condition);
        return true;
    }

    public AwkException createException(String message) {
        return new AwkException(tokens.Peek(0).map(Token::getLineNumber).orElse(line),
                tokens.Peek(0).map(Token::getStartPosition).orElse(column), message,
                AwkException.ExceptionType.ParseError);
    }

    // we use this method so that each time we actually remove we can update the
    // poisition in case we reach EOF for proper error messages
    public Optional<Token> MatchAndRemove(Token.TokenType type) {
        return tokens.MatchAndRemove(type).map(c -> {
            line = c.getLineNumber();
            column = c.getStartPosition();
            return c;
        });
    }

    private BlockNode ParseBlock() throws Exception {
        MatchAndRemove(Token.TokenType.OPENBRACE)
                .orElseThrow(() -> new Exception("block without open curly brace at start")).getValue().get();
        while (!MatchAndRemove(Token.TokenType.CLOSEBRACE).isPresent()) {
            AcceptSeperators();
            ParseOperation();
        }
        return new BlockNode(new LinkedList<>());

    }

    private Optional<Node> ParseBottomLevel() {

    }

    private Optional<Node> ParseLValue() {
        if (MatchAndRemove(Token.TokenType.DOLLAR).isPresent()) {
            var value = ParseBottomLevel();
            // probably need to unwrap value error if not present 
            return Optional.of(new OperationNode(OperationNode.Operation.DIVIDE, null));
        }
        var varName = MatchAndRemove(Token.TokenType.WORD);
        if (varName.isPresent()) {
            String name = varName.get().getValue().get();
            if (MatchAndRemove(Token.TokenType.OPENBRACKET).isPresent()) {
                var index = ParseOperation();
                // we should probably error if parseoperation returns empty optinal
                return Optional.of(new VariableReferenceNode(name, index));
            }

            return Optional.of(new VariableReferenceNode(name));
        }
        return Optional.empty();
    }

    private Optional<Node> ParseOperation() {
        return ParseBottomLevel();
    }
}
