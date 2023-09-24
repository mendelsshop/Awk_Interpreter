import java.util.LinkedList;
import java.util.Optional;

public class Parser {
    private TokenHandler tokens;
    private int line = 0;
    private int column = 0;

    public Parser(LinkedList<Token> tokenStream) {
        tokens = new TokenHandler(tokenStream);
    }

    public ProgramNode Parse() throws AwkException {
        var program = new ProgramNode();
        while (tokens.MoreTokens()) {
            // and is short circuiting 
            // so if ParseFunction returns true then !true && !ParseAction(program) will return false
            if (!ParseFunction(program) && !ParseAction(program)){
                throw createException("cannot parse program top level item was not a function or action");
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

    // https://stackoverflow.com/questions/22687943/is-it-possible-to-declare-that-a-suppliert-needs-to-throw-an-AwkException

    public interface CheckedSupplier<T> {
        public T get() throws AwkException;
    }

    private boolean ParseFunction(ProgramNode program) throws AwkException {
        if (MatchAndRemove(Token.TokenType.FUNCTION).isEmpty()) {
            return false;
        }
        var functionName = MatchAndRemove(Token.TokenType.WORD)
                .orElseThrow(() -> createException("function without name")).getValue().get();
        MatchAndRemove(Token.TokenType.OPENPAREN)
                .orElseThrow(() -> createException("function does not have parentheses before parameter"));
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
                                // parameter can have function_name(a ,,,) ...
                                .or(() -> MatchAndRemove(Token.TokenType.COMMA).map(d -> () -> {
                                    AcceptSeperators();
                                    return tokens.Peek(0)
                                            .filter(b -> b.getType() == Token.TokenType.WORD).map(h -> false)
                                            // otherwise we through an AwkException
                                            .orElseThrow(() -> createException(
                                                    "comma in function parameter list must be followed by another parameter"));
                                }))
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
        // eating up newline or ';' before block
        AcceptSeperators();
        var block = ParseBlock(false).getStatements();
        program.addFunction(new FunctionNode(functionName, parameters, block));
        return true;
    }

    private boolean ParseAction(ProgramNode program) throws AwkException {
        if (MatchAndRemove(Token.TokenType.BEGIN).isPresent()) {
            var block = ParseBlock(false);
            program.addToBegin(block);
            return true;
        } else if (MatchAndRemove(Token.TokenType.END).isPresent()) {
            var block = ParseBlock(false);
            program.addToEnd(block);
            return true;
        }
        var Condition = ParseOperation();
        var block = ParseBlock(false);
        block.setCondition(Condition);
        program.addToRest(block);
        return true;
    }

    // createException is here because it makes it easier to create exceptions, and not haveing to deal with keeping track of line numbers.
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

    private BlockNode ParseBlock(boolean supportsSingleLine) throws AwkException {
        return new BlockNode(new LinkedList<>());

    }

    private Optional<Node> ParseOperation() {
        return Optional.empty();
    }
}
