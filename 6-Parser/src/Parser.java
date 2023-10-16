import java.util.LinkedList;
import java.util.function.Function;

import Functional.CheckedBiFunction;
import Functional.CheckedSupplier;

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
            // if its not an action of function error

            if (!(ParseFunction(program) || ParseAction(program))) {
                throw createException("cannot parse program top level item was not a function or action");
            }
            AcceptSeperators();
        }
        return program;
    }

    // kept package for unittests
    boolean AcceptSeperators() {
        boolean foundSeperators = false;
        // since MatchAndRemove does out of bounds checks
        while (MatchAndRemove(Token.TokenType.SEPERATOR).isPresent()) {
            foundSeperators = true;
        }
        return foundSeperators;
    }

    private boolean ParseFunction(ProgramNode program) throws AwkException {
        if (MatchAndRemove(Token.TokenType.FUNCTION).isEmpty()) {
            return false;
        }
        var functionName = MatchAndRemove(Token.TokenType.WORD)
                .orElseThrow(() -> createException("function without name")).getValue().get();
        LinkedList<String> parameters = parseDelimitedList(Token.TokenType.OPENPAREN, Token.TokenType.CLOSEPAREN,
                () -> MatchAndRemove(Token.TokenType.WORD).map(w -> w.getValue().get()), "function parameter list");
        // parse block eats up newlines before the {

        var block = ParseBlock(false).getStatements();
        program.addFunction(new FunctionNode(functionName, parameters, block));
        return true;
    }

    private <T> LinkedList<T> parseDelimitedList(Token.TokenType start, Token.TokenType end,
            CheckedSupplier<Optional<T>, AwkException> listvalue, String type) throws AwkException {
        MatchAndRemove(start)
                .orElseThrow(() -> createException(type + " missing start" + start));
        var ret = new LinkedList<T>();
        AcceptSeperators();
        listvalue.get().CheckedIfPresent(first -> {
            ret.add(first);
            AcceptSeperators();
            while (MatchAndRemove(Token.TokenType.COMMA).isPresent()) {
                AcceptSeperators();
                ret.add(listvalue.get().orElseThrow(() -> createException(type + " missing expression after comma")));
                AcceptSeperators();
            }
        });
        MatchAndRemove(end)
                .orElseThrow(() -> createException(type + " missing end" + end));
        return ret;
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
        AcceptSeperators();
        var Condition = ParseOperation();
        var block = ParseBlock(false);
        block.setCondition(Condition);
        program.addToRest(block);
        return true;
    }

    // createException is here because it makes it easier to create exceptions, and
    // not haveing to deal with keeping track of line numbers.
    private AwkException createException(String message) {
        return new AwkException(tokens.Peek(0).map(Token::getLineNumber).orElse(line),
                tokens.Peek(0).map(Token::getStartPosition).orElse(column), message,
                AwkException.ExceptionType.ParseError);
    }

    // we use this method so that each time we actually remove we can update the
    // poisition in case we reach EOF for proper error messages
    private Optional<Token> MatchAndRemove(Token.TokenType type) {
        return tokens.MatchAndRemove(type).map(c -> {
            line = c.getLineNumber();
            column = c.getStartPosition();
            return c;
        });
    }

    private BlockNode ParseBlock(boolean supportsSingleLine) throws AwkException {
        // return new BlockNode(new LinkedList<>());
        AcceptSeperators();
        return new BlockNode(
                MatchAndRemove(Token.TokenType.OPENBRACE)
                        .<CheckedSupplier<LinkedList<StatementNode>, AwkException>>map(a -> () -> {
                            LinkedList<StatementNode> nodes = new LinkedList<>();
                            AcceptSeperators();
                            while (!MatchAndRemove(Token.TokenType.CLOSEBRACE).isPresent()) {
                                if (tokens.MoreTokens()) {
                                    nodes.add(ParseStatement());
                                }
                                // if there is still tokenss left from blocknode we neet to make sure that there
                                // is seperator between each statement
                                AcceptSeperators();
                            }
                            return nodes;
                        }).orElse(() -> {
                            if (supportsSingleLine) {
                                return new LinkedList<>() {
                                    {
                                        add(ParseStatement());
                                    }
                                };
                            } else {
                                throw createException("block without open curly brace at start");
                            }
                        }).get());

    }

    // TODO: comment this
    private void CheckSeporators(String type) throws AwkException {
        var seperators = AcceptSeperators();
        if (!tokens.Peek(0).map(Token::getType).equals(Optional.of(Token.TokenType.CLOSEBRACE)) && !seperators) {
            throw createException("must be a separator between " + type + " and next expression");
        }
    }

    private StatementNode ParseStatement() throws AwkException {
        CheckedBiFunction<Token.TokenType, CheckedSupplier<StatementNode, AwkException>, Optional<StatementNode>, AwkException> tokenToStatement = (
                tt, statement) -> MatchAndRemove(tt).CheckedMap(s -> statement.get());
        return tokenToStatement.apply(Token.TokenType.IF, this::ParseIf)
                .CheckedOr(() -> tokenToStatement.apply(Token.TokenType.CONTINUE, this::ParseContinue))
                .CheckedOr(() -> tokenToStatement.apply(Token.TokenType.BREAK, this::ParseBreak))
                .CheckedOr(() -> tokenToStatement.apply(Token.TokenType.RETURN, this::ParseReturn))
                .CheckedOr(() -> tokenToStatement.apply(Token.TokenType.WHILE, this::ParseWhile))
                .CheckedOr(() -> tokenToStatement.apply(Token.TokenType.DO, this::ParseDoWhile))
                .CheckedOr(() -> tokenToStatement.apply(Token.TokenType.DELETE, this::ParseDelete))
                .CheckedOr(() -> tokenToStatement.apply(Token.TokenType.FOR, this::ParseFor))
                .CheckedOrElseGet(this::OperationAsStatement);
    }

    // make sure that 1. the result of parseoperation is present, 2. the result of
    // parseoperation falls into the category of statementnode
    // do we need to make op node extend statementnode (what i did)
    private StatementNode OperationAsStatement() throws AwkException {
        var result = ParseOperation().orElseThrow(
                () -> createException("expected expression in block but found invalid or empty expression"));
        Function<OperationNode, AssignmentNode> makeAssign = (op) -> new AssignmentNode(op.getLeft(), op);
        CheckSeporators(result.toString());
        switch (result) {
            case OperationNode op -> {
                // we turn ++, -- into = ++, = -- here instead of in parsebottom
                // level/ParsePostIncDec
                // b/c if we tuen pos/pre-dec/inc to assignemnt node in there respecitve
                // functions it would remove the effect of post inc/dec
                // but in the case the "effect" of post increment is effectivley not being used
                // as tis post/pre-inc/dec is the outermost part of block

                switch (op.getOperation()) {
                    // if its one of these cases turn into assingment node which inherits from
                    // statementnode

                    case POSTDEC, PREDEC, POSTINC, PREINC -> {

                        return makeAssign.apply(op);
                    }
                    // otherwise throw exception
                    default -> throw createException("operation of type " + op.getOperation()
                            + " is not supported in the outermost part of a block");
                }
            }
            case TernaryOperationNode op ->
                throw createException("ternary expression is not supported in the outermost part of a block");
            case VariableReferenceNode op -> throw createException(
                    "accesing but not assigning to a variable is invalid in the outermost part of a block");
            case ConstantNode op ->
                throw createException("constant expression is invalid in the outermost part of a block");
            case PatternNode op ->
                throw createException("pattern expression is invalid in the outermost part of a block");
            // other operations are valid ie function calls and assignment
            default -> {
            }
        }
        // technically any result of parseoperation is valid statement
        return (StatementNode) result;
    }

    private Node ParseCondition(String type) throws AwkException {
        MatchAndRemove(Token.TokenType.OPENPAREN)
                .orElseThrow(() -> createException("condition in " + type + " is missing open parentheses"));
        var result = ParseOperation()
                .orElseThrow(() -> createException("expression in condition of " + type + " is empty or not valid"));
        MatchAndRemove(Token.TokenType.CLOSEPAREN)
                .orElseThrow(() -> createException("condition in " + type + " is missing close parentheses"));
        return result;

    }

    private IfNode ParseIf() throws AwkException {
        var cond = ParseCondition("if");
        var cons = ParseBlock(true);
        Optional<Node> alt = Optional.empty();
        if (MatchAndRemove(Token.TokenType.ELSE).isPresent()) {
            if (MatchAndRemove(Token.TokenType.IF).isPresent()) {
                alt = Optional.of(ParseIf());
            } else {
                alt = Optional.of(ParseBlock(true));
            }
        }
        return new IfNode(cond, cons, alt);
    }

    private DeleteNode ParseDelete() throws AwkException {
        var array = ParseLValue().orElseThrow(() -> createException("delete missing array to delete"));
        CheckSeporators("delete");
        return new DeleteNode(array);

    }

    private ContinueNode ParseContinue() throws AwkException {
        CheckSeporators("continue");
        return new ContinueNode();
    }

    private BreakNode ParseBreak() throws AwkException {
        CheckSeporators("break");
        return new BreakNode();
    }

    private ReturnNode ParseReturn() throws AwkException {
        Optional<Node> ret = ParseOperation();
        CheckSeporators("return");
        return new ReturnNode(ret);
    }

    private WhileNode ParseWhile() throws AwkException {
        var cond = ParseCondition("while");
        var loop = ParseBlock(true);
        return new WhileNode(cond, loop);
    }

    private DoWhileNode ParseDoWhile() throws AwkException {
        var loop = ParseBlock(true);
        MatchAndRemove(Token.TokenType.WHILE)
                .orElseThrow(() -> createException("Do while loop missing `while` keyword before condition"));
        var condition = ParseCondition("do while");
        return new DoWhileNode(condition, loop);
    }

    private StatementNode ParseFor() throws AwkException {
        MatchAndRemove(Token.TokenType.OPENPAREN)
                .orElseThrow(() -> createException("Condition in `for` is missing open parentheses"));
        // technically valid awk
        // z[0][0] = 6
        // for (6 in z; 8 ;9) {print 6}
        // if the thing before in is not word must be normal for loop -> maybe should be
        // an error?
        // otherwise its an array for loop
        if (tokens.Peek(0).map(Token::getType).equals(Optional.of(Token.TokenType.WORD))
                && tokens.Peek(1).map(Token::getType).equals(Optional.of(Token.TokenType.IN))) {
            var index_var = MatchAndRemove(Token.TokenType.WORD).get().getValue().get();
            MatchAndRemove(Token.TokenType.IN).get();
            var array = ParseLValue().orElseThrow(() -> createException(
                    "You decided to use the \"new style for loop\" but you missed the main thing (the array to iterate over)"));
            MatchAndRemove(Token.TokenType.CLOSEPAREN).orElseThrow(() -> createException(
                    "Condition in `for` is missing close parentheses\nI know this is a little picky and loops should not need parentheses around their `signature` but this AWK."));
            var loop = ParseBlock(true);
            return new ForEachNode(index_var, array, loop);
        } else {
            var init = ParseOperation();
            MatchAndRemove(Token.TokenType.SEPERATOR).orElseThrow(() -> createException(
                    "Semicolon between init expression and condition expression is missing, learn how to use c-style for loops\nat https://www.tutorialspoint.com/cprogramming/c_for_loop.htm."));
            var cond = ParseOperation();
            MatchAndRemove(Token.TokenType.SEPERATOR).orElseThrow(() -> createException(
                    "Semicolon between condition expression and increment expression is missing, learn how to use c-style for loops\nat https://www.tutorialspoint.com/cprogramming/c_for_loop.htm."));
            var inc = ParseOperation();
            MatchAndRemove(Token.TokenType.CLOSEPAREN).orElseThrow(() -> createException(
                    "Signature of `for` is missing close parentheses\nI know this is a little picky and loops should not need parentheses around their `signature` but this AWK."));
            var loop = ParseBlock(true);
            return new ForNode(init, cond, inc, loop);
        }
    }

    // maybe this should be in parselvaue an not its own seprerate function and we
    // wouldn't have to do all this peek stuff
    private Optional<FunctionCallNode> ParseFunctionCall() throws AwkException {
        return Optional.ofNullable(tokens.Peek(0).map(Token::getType).equals(Optional.of(Token.TokenType.WORD))
                && tokens.Peek(1).map(Token::getType).equals(Optional.of(Token.TokenType.OPENPAREN))
                        ? new FunctionCallNode(MatchAndRemove(Token.TokenType.WORD).get().getValue().get(),
                                parseDelimitedList(Token.TokenType.OPENPAREN, Token.TokenType.CLOSEPAREN,
                                        () -> ParseOperation(), "function arguement list"))
                        : null);

    }

    private Optional<Node> ParseBottomLevel() throws AwkException {
        // a functiin that maps a tokentype into a OperationNode
        CheckedBiFunction<Token.TokenType, OperationNode.Operation, Optional<Node>, AwkException> parseUnary = (type,
                operation) -> Optional
                        // we use ofNullable to make it easier to it easy to make Optinal.Empty with
                        // ternary operator
                        .ofNullable(
                                MatchAndRemove(type).isPresent()
                                        ? new OperationNode(operation,
                                                ParseOperation().orElseThrow(() -> createException("operation "
                                                        + operation + " is not followed by an expression")))
                                        : null);

        // like above function but for ++, -- which only work with lvalues
        CheckedBiFunction<Token.TokenType, OperationNode.Operation, Optional<Node>, AwkException> parseUnaryL = (type,
                operation) -> Optional
                        // we use ofNullable to make it easier to it easy to make Optinal.Empty with
                        // ternary operator
                        .ofNullable(
                                MatchAndRemove(type).isPresent()
                                        ? ParseLValue().map(
                                                name -> new OperationNode(operation, name))
                                                .orElseThrow(() -> createException("operation "
                                                        + operation + " is not followed by an expression"))
                                        : null);
        // simplifies getting the constant value out of a token
        Function<Optional<Token>, String> getValue = (token) -> token.get().getValue().get();

        // constant nodes
        Optional<Token> string = MatchAndRemove(Token.TokenType.STRINGLITERAL);
        if (string.isPresent()) {
            return Optional.of(new ConstantNode(getValue.apply(string)));
        }
        Optional<Token> number = MatchAndRemove(Token.TokenType.NUMBER);
        if (number.isPresent()) {
            return Optional.of(new ConstantNode(getValue.apply(number)));
        }
        Optional<Token> pattern = MatchAndRemove(Token.TokenType.PATTERN);
        if (pattern.isPresent()) {
            return Optional.of(new PatternNode(getValue.apply(pattern)));
        }
        // (expr)
        else if (MatchAndRemove(Token.TokenType.OPENPAREN).isPresent()) {
            var operation = ParseOperation();
            if (!MatchAndRemove(Token.TokenType.CLOSEPAREN).isPresent()) {
                throw createException("Expected close parenthesis after `(" + operation.get() + "`");
            }
            if (operation.isEmpty()) {
                throw createException("value inbetween `(`, `)` is either empty or invalid");
            }
            return operation;
        }
        // prefix unary operation
        return parseUnary.apply(Token.TokenType.NOT, OperationNode.Operation.NOT)
                .CheckedOr(() -> parseUnary.apply(Token.TokenType.MINUS, OperationNode.Operation.UNARYNEG))
                .CheckedOr(() -> parseUnary.apply(Token.TokenType.PLUS, OperationNode.Operation.UNARYPOS))
                // both of these should be lvalue
                .CheckedOr(() -> parseUnaryL.apply(Token.TokenType.MINUSMINUS, OperationNode.Operation.PREDEC))
                .CheckedOr(() -> parseUnaryL.apply(Token.TokenType.PLUSPLUS, OperationNode.Operation.PREINC))
                .CheckedOr(() -> ParseFunctionCall())
                .CheckedOr(() -> ParseLValue());
    }

    private Optional<Node> ParseLValue() throws AwkException {
        return
        // first see if its a dollar operation
        MatchAndRemove(Token.TokenType.DOLLAR).<Node, AwkException>CheckedMap(c -> {
            var value = ParseBottomLevel();
            return new OperationNode(OperationNode.Operation.DOLLAR, value.orElseThrow(
                    () -> createException("`$` is either not followed by an expression or the expression is invalid")));
        }).
        // otherwise it might be a varaible refernce
        <AwkException>CheckedOr(() -> MatchAndRemove(Token.TokenType.WORD).<Node, AwkException>CheckedMap(v -> {
            String name = v.getValue().get();
            // check if its an array index
            return MatchAndRemove(Token.TokenType.OPENBRACKET).CheckedMap(g -> {
                var index = ParseOperation().orElseThrow(() -> createException(
                        "found open bracket for indexing " + name + ", but no actual index value"));
                MatchAndRemove(Token.TokenType.CLOSEBRACKET)
                        .orElseThrow(() -> createException("expected close bracket ([) for indexing " + name));
                return new VariableReferenceNode(name, index);

            }).orElse(new VariableReferenceNode(name));
        }));
    }

    // kept package for unittests
    Optional<Node> ParseOperation() throws AwkException {
        return ParseAssignment();
    }

    // all the following methods have a similar structure
    // call the function for the level before them if it returns something than we
    // check for op token like +, - ... and if that is present we check for another
    // expression if there isnt another expression we panic otherwise if theres is
    // no op token we just return first expression
    // wether we use loops/recursive functions depends on the associativity of the
    // op(s)

    // Post Increment / Decrement should this be ParseBottomLevel
    // this should be ParseBottomLevel b/c each variable can only be pre or post not
    // both so it can use lvalue instead of parseoperation
    // as i dont see any bugs like with assignment same for pre
    private Optional<Node> ParsePostIncDec() throws AwkException {
        return ParseBottomLevel()
                .<Node>map((expr) -> MatchAndRemove(Token.TokenType.PLUSPLUS).map(c -> OperationNode.Operation.POSTINC)
                        .or(() -> MatchAndRemove(Token.TokenType.MINUSMINUS).map(c -> OperationNode.Operation.POSTDEC))
                        .<Node>map(op -> new OperationNode(op, expr)).orElse(expr));
    }

    // Exponents ^
    private Optional<Node> ParseExponent() throws AwkException {
        return ParsePostIncDec().CheckedMap(node -> {
            if (MatchAndRemove(Token.TokenType.EXPONENT).isPresent()) {
                var right = ParseExponent()
                        .orElseThrow(() -> createException("Expected expression after ^ (exponent)"));
                node = new OperationNode(OperationNode.Operation.EXPONENT, node, right);
            }
            return node;
        });

    }

    // Term / * (%)?
    private Optional<Node> ParseFactor() throws AwkException {
        return ParseLeftAssociative(this::ParseExponent,
                // might require java21 preview
                token -> Optional.ofNullable(switch (token) {
                    case MULTIPLY -> OperationNode.Operation.MULTIPLY;
                    case DIVIDE -> OperationNode.Operation.DIVIDE;
                    case MODULO -> OperationNode.Operation.MODULO;
                    default -> null;
                }));

    }

    // since left associative parsing is very common, we create a function that
    // takes in a function to parse the next level (getExpression)
    // and another function check if the op token makes sense at the current level
    // and turns the op token into an OperationNode.Operation (getOp)
    private Optional<Node> ParseLeftAssociative(CheckedSupplier<Optional<Node>, AwkException> getExpression,
            Function<Token.TokenType, Optional<OperationNode.Operation>> getOP) throws AwkException {
        return getExpression.get().CheckedMap(left -> {
            do {
                // we peek instead of matchandremove as each level uses different ops
                // and we dont know how to transform each op token into an operation
                // so we use getOP
                var opToken = tokens.Peek(0).map(Token::getType);
                Optional<OperationNode.Operation> op = opToken.flatMap(getOP);
                if (op.isEmpty()) {
                    return left;
                }
                // absorb the peek
                MatchAndRemove(opToken.get());
                var right = getExpression.get()
                        .orElseThrow(() -> createException("Expected expression after " + op.get()));
                left = new OperationNode(op.get(), left, right);

            } while (true);
        });

    }

    // Expression + -
    private Optional<Node> ParseExpression() throws AwkException {
        return ParseLeftAssociative(this::ParseFactor,
                token -> Optional.ofNullable(token == Token.TokenType.PLUS ? OperationNode.Operation.ADD
                        : token == Token.TokenType.MINUS ? OperationNode.Operation.SUBTRACT : null));
    }

    // Term maybe means ParseBottomLevel
    // Concatenation expr expr
    private Optional<Node> ParseConcatination() throws AwkException {
        // keep going while there are more expressions
        return ParseExpression().CheckedMap(left -> {
            do {
                // concatenination does not have an op token
                var right = ParseExpression();
                if (right.isEmpty()) {
                    return left;
                }
                left = new OperationNode(OperationNode.Operation.CONCATENATION, left, right.get());
            } while (true);
        });
    }

    // kinda like left assocaitve but no loop
    private Optional<Node> ParseNoAssociative(CheckedSupplier<Optional<Node>, AwkException> getExpression,
            Function<Token.TokenType, Optional<OperationNode.Operation>> getOP) throws AwkException {
        return getExpression.get().CheckedMap(left -> {
            var opToken = tokens.Peek(0).map(Token::getType);
            return opToken.flatMap(getOP).<Node, AwkException>CheckedMap(op -> {
                // absorb the peek
                MatchAndRemove(opToken.get());
                var right = getExpression.get()
                        .orElseThrow(() -> createException("Expected expression after " + op));
                return new OperationNode(op, left, right);
            }).orElse(left);
        });
    }

    // Boolean Compare <= < != ...
    private Optional<Node> ParseBooleanCompare() throws AwkException {
        return ParseLeftAssociative(this::ParseConcatination,
                // might require java21 preview
                op -> Optional.ofNullable(switch (op) {
                    case LESSTHAN -> OperationNode.Operation.LT;
                    case GREATERTHAN -> OperationNode.Operation.GT;
                    case LESSTHANEQUAL -> OperationNode.Operation.LE;
                    case GREATERTHANEQUAL -> OperationNode.Operation.GE;
                    case EQUAL -> OperationNode.Operation.EQ;
                    case NOTEQUAL -> OperationNode.Operation.NE;
                    default -> null;
                }));

    }

    // Match ~ !~
    private Optional<Node> ParseMatch() throws AwkException {
        return ParseNoAssociative(this::ParseBooleanCompare,
                token -> Optional.ofNullable(token == Token.TokenType.NOTMATCH ? OperationNode.Operation.NOTMATCH
                        : token == Token.TokenType.MATCH ? OperationNode.Operation.MATCH : null));
    }

    // Array membership - already in ParseLValue need `in`
    private Optional<Node> ParseArrayIn() throws AwkException {
        // even though should be left associative the subset of awk we are parsing does
        // not provide a way to have associativity with in b/c of lvalue after in
        return ParseMatch().CheckedMap(left -> {

            var op = MatchAndRemove(Token.TokenType.IN);
            if (op.isEmpty()) {
                return left;
            }
            var right = ParseLValue()
                    .orElseThrow(() -> createException("Expected lvalue after `in`"));
            return new OperationNode(OperationNode.Operation.IN, left, right);

        });
    }

    // AND &&
    private Optional<Node> ParseAnd() throws AwkException {
        return ParseLeftAssociative(this::ParseArrayIn,
                token -> Optional.ofNullable(token == Token.TokenType.AND ? OperationNode.Operation.AND
                        : null));
    }

    // Or ||
    private Optional<Node> ParseOr() throws AwkException {
        return ParseLeftAssociative(this::ParseAnd,
                token -> Optional.ofNullable(token == Token.TokenType.OR ? OperationNode.Operation.OR
                        : null));
    }

    // Ternary ?:
    private Optional<Node> ParseTernary() throws AwkException {
        return ParseOr().<Node, AwkException>CheckedMap(cond -> {
            if (MatchAndRemove(Token.TokenType.QUESTION).isPresent()) {
                // we have to use parseoperation here b/c no way to go top level for parseor
                var then = ParseOperation().orElseThrow(() -> createException("ternary then part missing"));
                MatchAndRemove(Token.TokenType.COLON).orElseThrow(() -> createException("ternary colon part missing"));
                // should be parseternary again for right asssocative but that means the
                // alternative of a ternary epxression cannot be an assignment so what happens
                // is the whole ternarty expression turn into the lefthand of an assignment
                var alt = ParseOperation().orElseThrow(() -> createException("ternary alternate part missing"));
                cond = new TernaryOperationNode(cond, then, alt);
            }
            return cond;

        });
    }

    // Assignment == += ...
    private Optional<Node> ParseAssignment() throws AwkException {
        return ParseTernary().CheckedMap(var -> {
            // function that tranforms the var op assignment expr into var op expr
            CheckedBiFunction<Token.TokenType, OperationNode.Operation, Optional<Node>, AwkException> OPEquals = (opeq,
                    op) -> MatchAndRemove(opeq).<Node, AwkException>CheckedMap(
                            s -> new OperationNode(op, var, ParseAssignment().orElseThrow(
                                    () -> createException("assignment " + op + " missing assignment value"))));
            // if its just = then we get expression
            return MatchAndRemove(Token.TokenType.ASSIGN)
                    .<Node, AwkException>CheckedMap(s -> ParseAssignment()
                            .orElseThrow(() -> createException("assignment missing assignment value")))
                    // otherwise it must follow op eqauls so we use OPEquals function to get a+= 5
                    // -> a + 5
                    .CheckedOr(() -> OPEquals.apply(Token.TokenType.EXPONENTEQUAL, OperationNode.Operation.EXPONENT))
                    .CheckedOr(() -> OPEquals.apply(Token.TokenType.PLUSEQUAL, OperationNode.Operation.ADD))
                    .CheckedOr(() -> OPEquals.apply(Token.TokenType.MODULOEQUAL, OperationNode.Operation.MODULO))
                    .CheckedOr(() -> OPEquals.apply(Token.TokenType.MULTIPLYEQUAL, OperationNode.Operation.MULTIPLY))
                    .CheckedOr(() -> OPEquals.apply(Token.TokenType.DIVIDEEQUAL, OperationNode.Operation.DIVIDE))
                    .CheckedOr(() -> OPEquals.apply(Token.TokenType.MINUSEQUAL, OperationNode.Operation.SUBTRACT))
                    .CheckedOr(() -> OPEquals.apply(Token.TokenType.PLUSEQUAL, OperationNode.Operation.ADD))
                    // finally we take the expr part (5,or a+5 (in case of a+=5))
                    // and create assignment node
                    .<Node>map(value -> new AssignmentNode(var, value))
                    // if no assignment just return the variableNode itself (which is not really a
                    // variableNode)
                    .orElse(var);
        });
    }
}