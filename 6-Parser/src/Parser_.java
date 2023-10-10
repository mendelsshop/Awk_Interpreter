// import java.util.LinkedList;
// // import java.util.Optional;

// import java.util.function.Function;

// import Functional.CheckedBiFunction;

// public class Parser_ {
//     private TokenHandler tokens;
//     private int line = 0;
//     private int column = 0;

//     public Parser_(LinkedList<Token> tokenStream) {
//         tokens = new TokenHandler(tokenStream);
//     }

//     public ProgramNode Parse() throws AwkException {
//         var program = new ProgramNode();
//         while (tokens.MoreTokens()) {
//             if (!ParseFunction(program)) {
//                 ParseAction(program);
//             }
//         }
//         return program;
//     }

//     private boolean AcceptSeperators() {
//         boolean foundSeperators = false;
//         // since MatchAndRemove does out of bounds checks
//         while (MatchAndRemove(Token.TokenType.SEPERATOR).isPresent()) {
//             foundSeperators = true;
//         }
//         return foundSeperators;
//     }

//     // https://stackoverflow.com/questions/22687943/is-it-possible-to-declare-that-a-suppliert-needs-to-throw-an-AwkException

//     public interface CheckedSupplier<T> {
//         public T get() throws AwkException;
//     }

//     private boolean ParseFunction(ProgramNode program) throws AwkException {
//         if (MatchAndRemove(Token.TokenType.FUNCTION).isEmpty()) {
//             return false;
//         }
//         var functionName = MatchAndRemove(Token.TokenType.WORD)
//                 .orElseThrow(() -> createException("function without name")).getValue().get();
//         MatchAndRemove(Token.TokenType.OPENPAREN)
//                 .orElseThrow(() -> createException("function does not have parentheses before parameter"));
//         var parameters = new LinkedList<String>();
//         while (tokens.MoreTokens()) {
//             // parsing function signature
//             // CheckedSupplier is just like the Functional Supplier interface, but the
//             // lambda it takes can through catchable exceptions
//             if (MatchAndRemove(Token.TokenType.WORD).CheckedMap(
//                     c -> {
//                         parameters.add(c.getValue().get());
//                         return
//                         // if we reach a `)` we are done with the function signature
//                         MatchAndRemove(
//                                 Token.TokenType.CLOSEPAREN)
//                                 .map(
//                                         a -> true)
//                                 // if we hit a `,`, we make sure that the next token is also a function
//                                 // parameter
//                                 .CheckedOr(() -> MatchAndRemove(Token.TokenType.COMMA).CheckedMap(d -> tokens.Peek(0)
//                                         .filter(b -> b.getType() == Token.TokenType.WORD).map(h -> false)
//                                         // otherwise we through an AwkException
//                                         .orElseThrow(() -> createException(
//                                                 "comma in function parameter list must be followed by another parameter"))))
//                                 // if the next token after the name of a function parameter is not a `,` or `)`
//                                 // we know we have an invalid function signature so we give back an error
//                                 .orElseThrow(() -> createException(
//                                         "function parameter must be followed by a comma or closeing parenthesis"));
//                     }).or(() -> MatchAndRemove(Token.TokenType.CLOSEPAREN).map(c -> true))
//                     .orElseThrow(() -> createException("unknown token type in parameter list" + tokens.Peek(0)))) {
//                 break;
//             }
//         }

//         var block = ParseBlock().getStatements();
//         program.addFunction(new FunctionNode(functionName, parameters, block));
//         return true;
//     }

//     private boolean ParseAction(ProgramNode program) throws AwkException {
//         // order doesnt matter according to awk ie you can have the BEGIN after the END
//         if (MatchAndRemove(Token.TokenType.BEGIN).isPresent()) {
//             var block = ParseBlock();
//             program.addToBegin(block);
//             return true;
//         } else if (MatchAndRemove(Token.TokenType.END).isPresent()) {
//             var block = ParseBlock();
//             program.addToEnd(block);
//             return true;
//         }
//         var Condition = ParseOperation();
//         var block = ParseBlock();
//         block.setCondition(Condition);
//         program.addToRest(block);
//         return true;
//     }

//     public AwkException createException(String message) {
//         return new AwkException(tokens.Peek(0).map(Token::getLineNumber).orElse(line),
//                 tokens.Peek(0).map(Token::getStartPosition).orElse(column), message,
//                 AwkException.ExceptionType.ParseError);
//     }

//     // we use this method so that each time we actually remove we can update the
//     // poisition in case we reach EOF for proper error messages
//     public Optional<Token> MatchAndRemove(Token.TokenType type) {
//         return tokens.MatchAndRemove(type).map(c -> {
//             line = c.getLineNumber();
//             column = c.getStartPosition();
//             return c;
//         });
//     }

//     private BlockNode ParseBlock(boolean supportsSingleLine) throws AwkException {

//         return new BlockNode(
//                 MatchAndRemove(Token.TokenType.OPENBRACE).<CheckedSupplier<LinkedList<StatementNode>>>map(a -> () -> {
//                     LinkedList<StatementNode> nodes = new LinkedList<>();
//                     AcceptSeperators();
//                     while (!MatchAndRemove(Token.TokenType.CLOSEBRACE).isPresent()) {

//                         AcceptSeperators();
//                         if (tokens.MoreTokens()) {
//                             // TODO: better error message
//                             nodes.add((StatementNode) ParseOperation().orElseThrow(() -> createException("")));
//                         }
//                     }
//                     return nodes;
//                 }).orElse(() -> {
//                     if (supportsSingleLine) {
//                         return new LinkedList<>() {
//                             {
//                                 add((StatementNode) ParseOperation()
//                                         .orElseThrow(() -> createException("single line block without expression")));
//                             }
//                         };
//                     } else {
//                         throw createException("block without open curly brace at start");
//                     }
//                 }).get());
//         // .orElseThrow(() -> createException("block without open curly brace at
//         // start")).getValue();


//     }

//     private StatementNode ParseStatement() throws AwkException {

//     }

//     private IfNode ParseIf() throws AwkException {
//     }

//     private ContinueNode ParseContinue() throws AwkException {
//     }
//     private BreakNode ParseBreak() throws AwkException {
//     }
//         private ReturnNode ParseReturn() throws AwkException {
//     }


//     private WhileNode ParseWhile() throws AwkException {
//     }

//     private DoWhileNode ParseDoWhile() throws AwkException {
//     }

//     private StatementNode ParseFor() throws AwkException {
//     }

//     private FunctionCallNode ParseFunctionCall() throws AwkException {
//     }

//     @FunctionalInterface
//     private interface ParserBiFunction<T, U, R> {
//         R apply(T t, U u) throws AwkException;
//     }

//     private Optional<Node> ParseBottomLevel() throws AwkException {
//         ParserBiFunction<Token.TokenType, OperationNode.Operation, Optional<Node>> parseUnary = (type,
//                 operation) -> Optional
//                         // we use ofNullable to make it easier to it easy to make Optinal.Empty with
//                         // ternary operator
//                         .ofNullable(
//                                 MatchAndRemove(type).isPresent() ? new OperationNode(operation, ParseOperation().get())
//                                         : null);
//         Function<Optional<Token>, String> getValue = (token) -> token.get().getValue().get();
//         Optional<Token> string = MatchAndRemove(Token.TokenType.STRINGLITERAL);
//         if (string.isPresent()) {
//             return Optional.of(new ConstantNode(getValue.apply(string), ConstantNode.ValueType.String));
//         }
//         Optional<Token> number = MatchAndRemove(Token.TokenType.NUMBER);
//         if (number.isPresent()) {
//             return Optional.of(new ConstantNode(getValue.apply(number), ConstantNode.ValueType.Number));
//         }
//         Optional<Token> pattern = MatchAndRemove(Token.TokenType.PATTERN);
//         if (pattern.isPresent()) {
//             return Optional.of(new PatternNode(getValue.apply(pattern)));
//         } else if (MatchAndRemove(Token.TokenType.OPENPAREN).isPresent()) {
//             var operation = ParseOperation();
//             if (!MatchAndRemove(Token.TokenType.CLOSEPAREN).isPresent()) {
//                 throw createException("Expected close parenthesis after `(" + operation + "`");
//             }
//             // should probably check for optional operation being empty
//             if (operation.isEmpty()) {
//                 throw createException("value inbetween `(`, `)` is either empty or invalid");
//             }
//             return operation;
//         }
//         return parseUnary.apply(Token.TokenType.NOT, OperationNode.Operation.NOT)
//                 .CheckedOr(() -> parseUnary.apply(Token.TokenType.MINUS, OperationNode.Operation.UNARYNEG))
//                 .CheckedOr(() -> parseUnary.apply(Token.TokenType.PLUS, OperationNode.Operation.UNARYPOS))
//                 // Should ony work if expr is variable lvalue
//                 .CheckedOr(() -> parseUnary.apply(Token.TokenType.MINUSMINUS, OperationNode.Operation.PREINC))
//                 .CheckedOr(() -> parseUnary.apply(Token.TokenType.PLUSPLUS, OperationNode.Operation.POSTINC))
//                 .CheckedOr(() -> ParseLValue());
//     }

//     private Optional<Node> ParseLValue() throws AwkException {
//         // if (MatchAndRemove(Token.TokenType.DOLLAR).isPresent()) {
//         // var value = ParseBottomLevel();
//         // // probably need to unwrap value error if not present
//         // return Optional.of(new OperationNode(OperationNode.Operation.DOLLAR,
//         // value.get()));
//         // }
//         // var varName = MatchAndRemove(Token.TokenType.WORD);
//         // if (varName.isPresent()) {
//         // String name = varName.get().getValue().get();
//         // if (MatchAndRemove(Token.TokenType.OPENBRACKET).isPresent()) {
//         // var index = ParseOperation().get();
//         // // we should probably error if parseoperation returns empty optinal
//         // MatchAndRemove(Token.TokenType.CLOSEBRACKET).get();
//         // return Optional.of(new VariableReferenceNode(name, Optional.of(index)));
//         // }

//         // return Optional.of(new VariableReferenceNode(name));
//         // }

//         return MatchAndRemove(Token.TokenType.DOLLAR).<Optional<Node>, AwkException>CheckedMap(c -> {
//             var value = ParseBottomLevel();
//             // probably need to unwrap value error if not present
//             return Optional
//                     .of(new OperationNode(OperationNode.Operation.DOLLAR, value.orElseThrow(() -> createException(
//                             "`$` is either not followed by an expression or the expression is invalid"))));
//         }).CheckedOrElseGet(() -> MatchAndRemove(Token.TokenType.WORD).<Node, AwkException>CheckedMap(v -> {
//             String name = v.getValue().get();
//             return MatchAndRemove(Token.TokenType.OPENBRACKET).CheckedMap(g -> {
//                 var index = ParseOperation().orElseThrow(() -> createException(
//                         "found open bracket for indexing " + name + ", but no actual index value"));
//                 MatchAndRemove(Token.TokenType.CLOSEBRACKET).get();
//                 return new VariableReferenceNode(name, Optional.of(index));
//             }).orElse(new VariableReferenceNode(name));
//         }));
//     }

//     private Optional<Node> ParseOperation() throws AwkException {
//         return ParseBottomLevel();
//     }

//     // Post Increment / Decrement should this be ParseBottomLevel
//     // Exponents ^
//     private Node ParseExponent() throws AwkException {
//         var node = ParseBottomLevel().orElseThrow();
//         while (MatchAndRemove(Token.TokenType.EXPONENT).isPresent()) {
//             var right = ParseExponent();
//             node = new OperationNode(OperationNode.Operation.EXPONENT, node, right);
//         }
//         return node;
//     }

//     // Term / * (%)?
//     private Node ParseFactor() throws AwkException {
//         var left = ParseExponent();
//         do {
//             Optional<OperationNode.Operation> op = MatchAndRemove(Token.TokenType.MULTIPLY)
//                     .map(a -> OperationNode.Operation.MULTIPLY)
//                     .or(() -> MatchAndRemove(Token.TokenType.DIVIDE).map(a -> OperationNode.Operation.DIVIDE))
//                     .or(() -> MatchAndRemove(Token.TokenType.MODULO).map(a -> OperationNode.Operation.MODULO));
//             if (op.isEmpty()) {
//                 break;
//             }
//             // should be parseterm
//             var right = ParseExponent();
//             left = new OperationNode(op.get(), left, right);
//         } while (true);
//         return left;
//     }

//     // Expression + -
//     private Node ParseExpression() throws AwkException {
//         var left = ParseFactor();
//         do {
//             Optional<OperationNode.Operation> op = MatchAndRemove(Token.TokenType.PLUS)
//                     .map(a -> OperationNode.Operation.ADD)
//                     .or(() -> MatchAndRemove(Token.TokenType.MINUS).map(a -> OperationNode.Operation.SUBTRACT));
//             if (op.isEmpty()) {
//                 break;
//             }
//             // should be parseterm
//             var right = ParseFactor();
//             left = new OperationNode(op.get(), left, right);
//         } while (true);
//         return left;
//     }

//     // Term maybe means ParseBottomLevel
//     // Concatenation >>
//     // Boolean Compare <= < != ...
//     private Node ParseBooleanCompare() throws AwkException {
//         var left = ParseExpression();
//         var op = MatchAndRemove(Token.TokenType.LESSTHAN).map(a -> OperationNode.Operation.LT)
//                 .or(() -> MatchAndRemove(Token.TokenType.GREATERTHAN).map(a -> OperationNode.Operation.GT))
//                 .or(() -> MatchAndRemove(Token.TokenType.LESSTHANEQUAL).map(a -> OperationNode.Operation.LE))
//                 .or(() -> MatchAndRemove(Token.TokenType.GREATERTHANEQUAL).map(a -> OperationNode.Operation.GE))
//                 .or(() -> MatchAndRemove(Token.TokenType.EQUAL).map(a -> OperationNode.Operation.EQ))
//                 .or(() -> MatchAndRemove(Token.TokenType.NOTEQUAL).map(a -> OperationNode.Operation.NE));
//         if (op.isEmpty()) {
//             return left;
//         }
//         var right = ParseExpression();
//         return new OperationNode(op.get(), left, right);
//     }

//     // Match ~ !~
//     private Node ParseMatch() throws AwkException {
//         var left = ParseBooleanCompare();
//         var op = MatchAndRemove(Token.TokenType.MATCH).map(a -> OperationNode.Operation.MATCH)
//                 .or(() -> MatchAndRemove(Token.TokenType.NOTMATCH).map(a -> OperationNode.Operation.NOTMATCH));
//         if (op.isEmpty()) {
//             return left;
//         }
//         var right = ParseBooleanCompare();
//         return new OperationNode(op.get(), left, right);
//     }

//     // Array membership - already in ParseLValue
//     // AND &&
//     private Node ParseAnd() throws AwkException {
//         var node = ParseMatch();
//         while (MatchAndRemove(Token.TokenType.AND).isPresent()) {
//             var right = ParseAnd();
//             node = new OperationNode(OperationNode.Operation.AND, node, right);
//         }
//         return node;
//     }

//     // Or ||
//     private Node ParseOr() throws AwkException {
//         var node = ParseAnd();
//         while (MatchAndRemove(Token.TokenType.OR).isPresent()) {
//             var right = ParseOr();
//             node = new OperationNode(OperationNode.Operation.OR, node, right);
//         }
//         return node;
//     }

//     // Ternary ?: I dont get what right asscotivity does here shouldnt this call to
//     // ParseOperation
//     private Node ParseTernary() throws AwkException {
//         var cond = ParseOperation().orElseThrow();
//         return MatchAndRemove(Token.TokenType.QUESTION).<Node, AwkException>CheckedMap(s -> {
//             var then = ParseOperation().orElseThrow();
//             MatchAndRemove(Token.TokenType.COLON).orElseThrow();
//             var alt = ParseOperation().orElseThrow();
//             return new TernaryOperationNode(cond, then, alt);

//         }).orElse(cond);
//     }

//     // Assignment == += ...
//     private Node ParseAssignment() throws AwkException {
//         var var = ParseLValue().orElseThrow();
//         CheckedBiFunction<Token.TokenType, OperationNode.Operation, Optional<Node>, AwkException> OPEquals = (opeq,
//                 op) -> MatchAndRemove(opeq).<Node, AwkException>CheckedMap(
//                         s -> new OperationNode(op, var, ParseOperation().orElseThrow()));
//         return MatchAndRemove(Token.TokenType.ASSIGN)
//                 .<Node, AwkException>CheckedMap(s -> ParseOperation().orElseThrow())
//                 .CheckedOr(() -> OPEquals.apply(Token.TokenType.PLUSEQUAL, OperationNode.Operation.ADD))
//                 .CheckedOr(() -> OPEquals.apply(Token.TokenType.MODULOEQUAL, OperationNode.Operation.MODULO))
//                 .CheckedOr(() -> OPEquals.apply(Token.TokenType.MULTIPLYEQUAL, OperationNode.Operation.MULTIPLY))
//                 .CheckedOr(() -> OPEquals.apply(Token.TokenType.DIVIDEEQUAL, OperationNode.Operation.DIVIDE))
//                 .CheckedOr(() -> OPEquals.apply(Token.TokenType.MINUSEQUAL, OperationNode.Operation.SUBTRACT))
//                 .CheckedOr(() -> OPEquals.apply(Token.TokenType.PLUSEQUAL, OperationNode.Operation.ADD))
//                 .<Node>map(value -> new AssignmentNode(var, value))
//                 // if no assignment just return the variableNode itself?
//                 .orElse(var);
//     }

// }
