import java.util.LinkedList;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

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
                                // parameter
                                .or(() -> MatchAndRemove(Token.TokenType.COMMA).map(d -> () -> tokens.Peek(0)
                                        .filter(b -> b.getType() == Token.TokenType.WORD).map(h -> false)
                                        // otherwise we through an AwkException
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

    private boolean ParseAction(ProgramNode program) throws AwkException {
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
        program.addToRest(block);
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

    private BlockNode ParseBlock() throws AwkException {
        MatchAndRemove(Token.TokenType.OPENBRACE)
                .orElseThrow(() -> createException("block without open curly brace at start"));
        var statements = new LinkedList<StatementNode>();
        while (!MatchAndRemove(Token.TokenType.CLOSEBRACE).isPresent()) {
            if (!tokens.MoreTokens()) {
                throw createException("block without closing curly brace");
            }
            AcceptSeperators();
            statements.add((StatementNode) ParseOperation().get());
            AcceptSeperators();
        }
        return new BlockNode(statements);

    }

    private Optional<Node> ParseBottomLevel() {
        BiFunction<Token.TokenType, OperationNode.Operation, Optional<Node>> parseUnary = (type, operation) -> Optional
                // we use ofNullable to make it easier to it easy to make Optinal.Empty with
                // ternary operator
                .ofNullable(
                        MatchAndRemove(type).isPresent() ? new OperationNode(operation, ParseOperation().get()) : null);
        Function<Optional<Token>, String> getValue = (token) -> token.get().getValue().get();
        Optional<Token> string = MatchAndRemove(Token.TokenType.STRINGLITERAL);
        if (string.isPresent()) {
            return Optional.of(new ConstantNode(getValue.apply(string), ConstantNode.ValueType.String));
        }
        Optional<Token> number = MatchAndRemove(Token.TokenType.NUMBER);
        if (number.isPresent()) {
            return Optional.of(new ConstantNode(getValue.apply(number), ConstantNode.ValueType.Number));
        }
        Optional<Token> pattern = MatchAndRemove(Token.TokenType.PATTERN);
        if (pattern.isPresent()) {
            return Optional.of(new PatternNode(getValue.apply(pattern)));
        } else if (MatchAndRemove(Token.TokenType.OPENPAREN).isPresent()) {
            var operation = ParseOperation();
            if (!MatchAndRemove(Token.TokenType.CLOSEPAREN).isPresent()) {
                // throw AwkException
            }
            return operation;
        }
        return parseUnary.apply(Token.TokenType.NOT, OperationNode.Operation.NOT)
                .or(() -> parseUnary.apply(Token.TokenType.MINUS, OperationNode.Operation.UNARYNEG))
                .or(() -> parseUnary.apply(Token.TokenType.PLUS, OperationNode.Operation.UNARYPOS))
                .or(() -> parseUnary.apply(Token.TokenType.MINUSMINUS, OperationNode.Operation.PREINC))
                .or(() -> parseUnary.apply(Token.TokenType.PLUSPLUS, OperationNode.Operation.POSTINC))
                .or(() -> ParseLValue());
    }

    private Optional<Node> ParseLValue() {
        // if (MatchAndRemove(Token.TokenType.DOLLAR).isPresent()) {
        //     var value = ParseBottomLevel();
        //     // probably need to unwrap value error if not present
        //     return Optional.of(new OperationNode(OperationNode.Operation.DOLLAR, value.get()));
        // }
        // var varName = MatchAndRemove(Token.TokenType.WORD);
        // if (varName.isPresent()) {
        //     String name = varName.get().getValue().get();
        //     if (MatchAndRemove(Token.TokenType.OPENBRACKET).isPresent()) {
        //         var index = ParseOperation().get();
        //         // we should probably error if parseoperation returns empty optinal
        //         MatchAndRemove(Token.TokenType.CLOSEBRACKET).get();
        //         return Optional.of(new VariableReferenceNode(name, Optional.of(index)));
        //     }

        //     return Optional.of(new VariableReferenceNode(name));
        // }
       return MatchAndRemove(Token.TokenType.DOLLAR).<Optional<Node>>map(c -> {
            var value = ParseBottomLevel();
            // probably need to unwrap value error if not present
            return Optional.of(new OperationNode(OperationNode.Operation.DOLLAR, value.get()));
        }).orElseGet(() -> MatchAndRemove(Token.TokenType.WORD).<Node>map(v -> {
            String name = v.getValue().get();
            return MatchAndRemove(Token.TokenType.OPENBRACKET).map(g -> {
                var index = ParseOperation().get();
                // we should probably error if parseoperation returns empty optinal
                MatchAndRemove(Token.TokenType.CLOSEBRACKET).get();
                return new VariableReferenceNode(name, Optional.of(index));
            }).orElse(new VariableReferenceNode(name));
        }));
        // return Optional.empty();
    }

    private Optional<Node> ParseOperation() {
        return ParseBottomLevel();
    }
}
