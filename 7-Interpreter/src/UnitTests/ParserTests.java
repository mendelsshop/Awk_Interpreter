import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.LinkedList;

import org.junit.Test;

public class ParserTests {
    @Test
    public void ParseBasicFunction() throws Exception {
        var lexer = UnitTests.testLexContent(
                "function function_name(argument1, argument2, a) {} BEGIN {} END {} BEGIN {}",
                new Token.TokenType[] { Token.TokenType.FUNCTION, Token.TokenType.WORD, Token.TokenType.OPENPAREN,
                        Token.TokenType.WORD, Token.TokenType.COMMA, Token.TokenType.WORD, Token.TokenType.COMMA,
                        Token.TokenType.WORD, Token.TokenType.CLOSEPAREN, Token.TokenType.OPENBRACE,
                        Token.TokenType.CLOSEBRACE, Token.TokenType.BEGIN, Token.TokenType.OPENBRACE,
                        Token.TokenType.CLOSEBRACE, Token.TokenType.END, Token.TokenType.OPENBRACE,
                        Token.TokenType.CLOSEBRACE, Token.TokenType.BEGIN, Token.TokenType.OPENBRACE,
                        Token.TokenType.CLOSEBRACE });
        var parser = new Parser(lexer);
        var parsed = parser.Parse();
        assertEquals(parsed.getEndBlocks().size(), 1);
        assertEquals(parsed.getBeginBlocks().size(), 2);
        assertEquals(parsed.getFunctions().size(), 1);
        assertEquals(parsed.getFunctions().get(0).getName(), "function_name");
        assertEquals(parsed.getFunctions().get(0).getParameters().size(), 3);
    }

    // CHATGPT TEST

    @Test
    public void ParseBasicFunctionSignature() throws Exception {
        var lexer = UnitTests.testLexContent(
                "function function_name(argument1, argument2, a) {} \n BEGIN {} \n END {} \n BEGIN {}",
                new Token.TokenType[] { Token.TokenType.FUNCTION, Token.TokenType.WORD, Token.TokenType.OPENPAREN,
                        Token.TokenType.WORD, Token.TokenType.COMMA, Token.TokenType.WORD, Token.TokenType.COMMA,
                        Token.TokenType.WORD, Token.TokenType.CLOSEPAREN, Token.TokenType.OPENBRACE,
                        Token.TokenType.CLOSEBRACE, Token.TokenType.SEPERATOR, Token.TokenType.BEGIN,
                        Token.TokenType.OPENBRACE,
                        Token.TokenType.CLOSEBRACE, Token.TokenType.SEPERATOR, Token.TokenType.END,
                        Token.TokenType.OPENBRACE,
                        Token.TokenType.CLOSEBRACE, Token.TokenType.SEPERATOR, Token.TokenType.BEGIN,
                        Token.TokenType.OPENBRACE,
                        Token.TokenType.CLOSEBRACE });
        var parser = new Parser(lexer);
        var parsed = parser.Parse();

        // Check the number of BEGIN and END tokens
        assertEquals(1, parsed.getEndBlocks().size());
        assertEquals(2, parsed.getBeginBlocks().size());

        // Check the number of functions and their details
        assertEquals(1, parsed.getFunctions().size());
        assertEquals("function_name", parsed.getFunctions().get(0).getName());
        assertEquals(3, parsed.getFunctions().get(0).getParameters().size());
    }

    @Test
    public void ParseFunctionWithNoParameters() throws Exception {
        var lexer = UnitTests.testLexContent("function func_no_params() {}\n BEGIN  {}\n END {}",
                new Token.TokenType[] { Token.TokenType.FUNCTION, Token.TokenType.WORD, Token.TokenType.OPENPAREN,
                        Token.TokenType.CLOSEPAREN, Token.TokenType.OPENBRACE, Token.TokenType.CLOSEBRACE,
                        Token.TokenType.SEPERATOR, Token.TokenType.BEGIN, Token.TokenType.OPENBRACE,
                        Token.TokenType.CLOSEBRACE, Token.TokenType.SEPERATOR, Token.TokenType.END,
                        Token.TokenType.OPENBRACE, Token.TokenType.CLOSEBRACE });
        var parser = new Parser(lexer);
        var parsed = parser.Parse();

        // Check the number of BEGIN and END tokens
        assertEquals(1, parsed.getEndBlocks().size());
        assertEquals(1, parsed.getBeginBlocks().size());

        // Check the number of functions and their details
        assertEquals(1, parsed.getFunctions().size());
        assertEquals("func_no_params", parsed.getFunctions().get(0).getName());
        assertEquals(0, parsed.getFunctions().get(0).getParameters().size());
    }

    @Test
    public void ParseFunctionWithMultipleParameters() throws Exception {
        var lexer = UnitTests.testLexContent(
                "function func_multi_params(param1, param2, param3) {} \n BEGIN {}\n END {}",
                new Token.TokenType[] { Token.TokenType.FUNCTION, Token.TokenType.WORD, Token.TokenType.OPENPAREN,
                        Token.TokenType.WORD, Token.TokenType.COMMA, Token.TokenType.WORD, Token.TokenType.COMMA,
                        Token.TokenType.WORD, Token.TokenType.CLOSEPAREN, Token.TokenType.OPENBRACE,
                        Token.TokenType.CLOSEBRACE, Token.TokenType.SEPERATOR, Token.TokenType.BEGIN,
                        Token.TokenType.OPENBRACE,
                        Token.TokenType.CLOSEBRACE, Token.TokenType.SEPERATOR, Token.TokenType.END,
                        Token.TokenType.OPENBRACE,
                        Token.TokenType.CLOSEBRACE });
        var parser = new Parser(lexer);
        var parsed = parser.Parse();

        // Check the number of BEGIN and END tokens
        assertEquals(1, parsed.getEndBlocks().size());
        assertEquals(1, parsed.getBeginBlocks().size());

        // Check the number of functions and their details
        assertEquals(1, parsed.getFunctions().size());
        assertEquals("func_multi_params", parsed.getFunctions().get(0).getName());
        assertEquals(3, parsed.getFunctions().get(0).getParameters().size());
    }

    @Test
    public void ParseMultipleFunctions() throws Exception {
        var lexer = UnitTests.testLexContent(
                "function func1() {}\n BEGIN {}\n END{} \n function func2(param1)\t\t{} \n BEGIN {}\n END\n{} END{}",
                new Token.TokenType[] { Token.TokenType.FUNCTION, Token.TokenType.WORD, Token.TokenType.OPENPAREN,
                        Token.TokenType.CLOSEPAREN, Token.TokenType.OPENBRACE, Token.TokenType.CLOSEBRACE,
                        Token.TokenType.SEPERATOR, Token.TokenType.BEGIN, Token.TokenType.OPENBRACE,
                        Token.TokenType.CLOSEBRACE, Token.TokenType.SEPERATOR, Token.TokenType.END,
                        Token.TokenType.OPENBRACE, Token.TokenType.CLOSEBRACE, Token.TokenType.SEPERATOR,
                        Token.TokenType.FUNCTION, Token.TokenType.WORD, Token.TokenType.OPENPAREN,
                        Token.TokenType.WORD, Token.TokenType.CLOSEPAREN, Token.TokenType.OPENBRACE,
                        Token.TokenType.CLOSEBRACE, Token.TokenType.SEPERATOR, Token.TokenType.BEGIN,
                        Token.TokenType.OPENBRACE,
                        Token.TokenType.CLOSEBRACE, Token.TokenType.SEPERATOR, Token.TokenType.END,
                        Token.TokenType.SEPERATOR, Token.TokenType.OPENBRACE,
                        Token.TokenType.CLOSEBRACE, Token.TokenType.END, Token.TokenType.OPENBRACE,
                        Token.TokenType.CLOSEBRACE });
        var parser = new Parser(lexer);
        var parsed = parser.Parse();

        // Check the number of BEGIN and END tokens
        assertEquals(3, parsed.getEndBlocks().size());
        assertEquals(2, parsed.getBeginBlocks().size());

        // Check the number of functions and their details
        assertEquals(2, parsed.getFunctions().size());
        assertEquals("func1", parsed.getFunctions().get(0).getName());
        assertEquals(0, parsed.getFunctions().get(0).getParameters().size());
        assertEquals("func2", parsed.getFunctions().get(1).getName());
        assertEquals(1, parsed.getFunctions().get(1).getParameters().size());
    }

    @Test(expected = AwkException.class)
    public void ParseInvalidFunctionSignature1() throws Exception {
        var lexer = UnitTests.testLexContent("function () \n BEGIN \n END",
                new Token.TokenType[] { Token.TokenType.FUNCTION, Token.TokenType.OPENPAREN,
                        Token.TokenType.CLOSEPAREN, Token.TokenType.SEPERATOR,
                        Token.TokenType.BEGIN, Token.TokenType.SEPERATOR, Token.TokenType.END });
        var parser = new Parser(lexer);
        parser.Parse(); // This should throw an AwkException
    }

    @Test(expected = AwkException.class)
    public void ParseInvalidFunctionSignature2() throws Exception {
        var lexer = UnitTests.testLexContent("function a a( \n BEGIN \n END",
                new Token.TokenType[] { Token.TokenType.FUNCTION, Token.TokenType.WORD, Token.TokenType.WORD,
                        Token.TokenType.OPENPAREN, Token.TokenType.SEPERATOR,
                        Token.TokenType.BEGIN, Token.TokenType.SEPERATOR, Token.TokenType.END });
        var parser = new Parser(lexer);
        parser.Parse(); // This should throw an AwkException
    }

    @Test(expected = AwkException.class)
    public void ParseInvalidFunctionSignature3() throws Exception {
        var lexer = UnitTests.testLexContent("function a(a,) \n BEGIN \n END",
                new Token.TokenType[] { Token.TokenType.FUNCTION, Token.TokenType.WORD, Token.TokenType.OPENPAREN,
                        Token.TokenType.WORD, Token.TokenType.COMMA, Token.TokenType.CLOSEPAREN,
                        Token.TokenType.SEPERATOR, Token.TokenType.BEGIN, Token.TokenType.SEPERATOR,
                        Token.TokenType.END });
        var parser = new Parser(lexer);
        parser.Parse(); // This should throw an AwkException
    }

    @Test(expected = AwkException.class)
    public void ParseInvalidFunctionSignature4() throws Exception {
        var lexer = UnitTests.testLexContent("function func(,) \n BEGIN \n END",
                new Token.TokenType[] { Token.TokenType.FUNCTION, Token.TokenType.WORD, Token.TokenType.OPENPAREN,
                        Token.TokenType.COMMA, Token.TokenType.CLOSEPAREN, Token.TokenType.SEPERATOR,
                        Token.TokenType.BEGIN, Token.TokenType.SEPERATOR, Token.TokenType.END });
        var parser = new Parser(lexer);
        parser.Parse(); // This should throw an AwkException
    }

    @Test(expected = AwkException.class)
    public void ParseInvalidFunctionSignature5() throws Exception {
        var lexer = UnitTests.testLexContent("function func(a,,b) \n BEGIN \n END",
                new Token.TokenType[] { Token.TokenType.FUNCTION, Token.TokenType.WORD, Token.TokenType.OPENPAREN,
                        Token.TokenType.WORD, Token.TokenType.COMMA, Token.TokenType.COMMA, Token.TokenType.WORD,
                        Token.TokenType.CLOSEPAREN, Token.TokenType.SEPERATOR,
                        Token.TokenType.BEGIN, Token.TokenType.SEPERATOR, Token.TokenType.END });
        var parser = new Parser(lexer);
        parser.Parse(); // This should throw an AwkException
    }

    @Test(expected = AwkException.class)
    public void ParseInvalidFunctionSignature6() throws Exception {
        var lexer = UnitTests.testLexContent("function func(a,b c) \n BEGIN \n END",
                new Token.TokenType[] { Token.TokenType.FUNCTION, Token.TokenType.WORD, Token.TokenType.OPENPAREN,
                        Token.TokenType.WORD, Token.TokenType.COMMA, Token.TokenType.WORD, Token.TokenType.WORD,
                        Token.TokenType.CLOSEPAREN, Token.TokenType.SEPERATOR,
                        Token.TokenType.BEGIN, Token.TokenType.SEPERATOR, Token.TokenType.END });
        var parser = new Parser(lexer);
        parser.Parse(); // This should throw an AwkException
    }

    @Test(expected = AwkException.class)
    public void ParseInvalidFunctionSignatureWithoutClosingParenthesis() throws Exception {
        var lexer = UnitTests.testLexContent("function func(a, b \n BEGIN \n END",
                new Token.TokenType[] { Token.TokenType.FUNCTION, Token.TokenType.WORD, Token.TokenType.OPENPAREN,
                        Token.TokenType.WORD, Token.TokenType.COMMA, Token.TokenType.WORD, Token.TokenType.SEPERATOR,
                        Token.TokenType.BEGIN, Token.TokenType.SEPERATOR, Token.TokenType.END });
        var parser = new Parser(lexer);
        parser.Parse(); // This should throw an AwkException
    }

    // parser 2 tests
    // these tests should use instanceof pattern matching (java 21 preview)
    @Test
    public void predecparse() throws Exception {
        var parser = new Parser(
                UnitTests.testLexContent("{--a}",
                        new Token.TokenType[] { Token.TokenType.OPENBRACE, Token.TokenType.MINUSMINUS,
                                Token.TokenType.WORD, Token.TokenType.CLOSEBRACE }));
        var res = parser.Parse();
        if (res.getRestBlocks().getFirst().getStatements().getFirst() instanceof AssignmentNode as
                && as.getTarget() instanceof VariableReferenceNode variable
                && as.getExpression() instanceof OperationNode op) {
            System.out.println(op);
            assertEquals(op.getOperation(), OperationNode.Operation.PREDEC);
            assertEquals(variable.getName(), "a");
        } else {
            throw new Exception("test failed");
        }
    }

    @Test
    public void preincparse() throws Exception {
        var parser = new Parser(
                UnitTests.testLexContent("{++a}",
                        new Token.TokenType[] { Token.TokenType.OPENBRACE, Token.TokenType.PLUSPLUS,
                                Token.TokenType.WORD, Token.TokenType.CLOSEBRACE }));
        var res = parser.Parse();
        if (res.getRestBlocks().getFirst().getStatements().getFirst() instanceof AssignmentNode as
                && as.getTarget() instanceof VariableReferenceNode variable
                && as.getExpression() instanceof OperationNode op) {
            System.out.println(op);
            assertEquals(op.getOperation(), OperationNode.Operation.PREINC);
            assertEquals(variable.getName(), "a");
        } else {
            throw new Exception("test failed");
        }
    }

    @Test
    public void constantparse() throws Exception {
        var parser = new Parser(
                UnitTests.testLexContent("1.75\n \"a\\\"aa\"\n `a[0]*`",
                        new Token.TokenType[] { Token.TokenType.NUMBER, Token.TokenType.SEPERATOR,
                                Token.TokenType.STRINGLITERAL, Token.TokenType.SEPERATOR,
                                Token.TokenType.PATTERN }));
        var num = parser.ParseOperation().get();
        parser.AcceptSeperators();
        var word = parser.ParseOperation().get();
        parser.AcceptSeperators();
        var pat = parser.ParseOperation().get();
        if (num instanceof ConstantNode number && word instanceof ConstantNode string
                && pat instanceof PatternNode pattern) {
            assertEquals(number.getValue(), "1.75");
            assertEquals(string.getValue(), "a\"aa");
            assertEquals(pattern.getPattern(), "a[0]*");
        } else {
            throw new Exception("test failed");
        }
    }

    @Test
    public void dollarparse() throws Exception {
        var parser = new Parser(
                UnitTests.testLexContent("$4\n$-1",
                        new Token.TokenType[] { Token.TokenType.DOLLAR, Token.TokenType.NUMBER,
                                Token.TokenType.SEPERATOR,
                                Token.TokenType.DOLLAR, Token.TokenType.MINUS, Token.TokenType.NUMBER }));
        var d1 = parser.ParseOperation().get();
        parser.AcceptSeperators();
        var d2 = parser.ParseOperation().get();
        if (d1 instanceof OperationNode d11 && d11.getLeft() instanceof ConstantNode i1
                && d2 instanceof OperationNode d22 && d22.getLeft() instanceof OperationNode i2
                && i2.getLeft() instanceof ConstantNode i22) {
            assertEquals(d11.getOperation(), OperationNode.Operation.DOLLAR);
            assertEquals(i1.getValue(), "4");
            assertEquals(d22.getOperation(), OperationNode.Operation.DOLLAR);
            assertEquals(i2.getOperation(), OperationNode.Operation.UNARYNEG);
            assertEquals(i22.getValue(), "1");
        } else {
            throw new Exception("test failed");
        }
    }

    @Test
    public void indexparse() throws Exception {
        var parser = new Parser(
                UnitTests.testLexContent("   variable\t[+(++ $ u )]  \t",
                        new Token.TokenType[] { Token.TokenType.WORD, Token.TokenType.OPENBRACKET,
                                Token.TokenType.PLUS, Token.TokenType.OPENPAREN, Token.TokenType.PLUSPLUS,
                                Token.TokenType.DOLLAR, Token.TokenType.WORD, Token.TokenType.CLOSEPAREN,
                                Token.TokenType.CLOSEBRACKET }));
        var v = parser.ParseOperation().get();
        System.out.println(v);
        if (v instanceof VariableReferenceNode v1 && v1.getIndex().get() instanceof OperationNode index
                && index.getLeft() instanceof OperationNode index1 && index1.getLeft() instanceof OperationNode index2
                && index2.getLeft() instanceof VariableReferenceNode index3) {
            assertEquals(v1.getName(), "variable");
            assertEquals(index.getOperation(), OperationNode.Operation.UNARYPOS);
            assertEquals(index1.getOperation(), OperationNode.Operation.PREINC);
            assertEquals(index2.getOperation(), OperationNode.Operation.DOLLAR);
            assertEquals(index3.getName(), "u");

        } else {
            throw new Exception("test failed");
        }
    }

    // testing invalid parsing
    private void testInvalidOperation(String content, Token.TokenType[] expected) throws Exception {
        var parser = new Parser(
                UnitTests.testLexContent(content, expected));
        assertThrows(AwkException.class, () -> parser.ParseOperation());

    }

    @Test
    public void noexpr() throws Exception {
        testInvalidOperation("()", new Token.TokenType[] { Token.TokenType.OPENPAREN, Token.TokenType.CLOSEPAREN });
        testInvalidOperation("+", new Token.TokenType[] { Token.TokenType.PLUS });
        testInvalidOperation("-", new Token.TokenType[] { Token.TokenType.MINUS });
        testInvalidOperation("!", new Token.TokenType[] { Token.TokenType.NOT });
        testInvalidOperation("++", new Token.TokenType[] { Token.TokenType.PLUSPLUS });
        testInvalidOperation("--", new Token.TokenType[] { Token.TokenType.MINUSMINUS });
        testInvalidOperation("var[]", new Token.TokenType[] { Token.TokenType.WORD, Token.TokenType.OPENBRACKET,
                Token.TokenType.CLOSEBRACKET });
    }

    @Test
    public void unbalancedbrace() throws Exception {
        testInvalidOperation("($--4", new Token.TokenType[] { Token.TokenType.OPENPAREN, Token.TokenType.DOLLAR,
                Token.TokenType.MINUSMINUS, Token.TokenType.NUMBER, });
        testInvalidOperation("var[`5#`",
                new Token.TokenType[] { Token.TokenType.WORD, Token.TokenType.OPENBRACKET, Token.TokenType.PATTERN
                });

    }

    // parser 5 unit tests

    @Test
    public void basic_math() throws Exception {
        var parser = new Parser(
                UnitTests.testLexContent("1+2\n2/3\n4-8\n9*5\n7^6",
                        new Token.TokenType[] { Token.TokenType.NUMBER, Token.TokenType.PLUS, Token.TokenType.NUMBER,
                                Token.TokenType.SEPERATOR,
                                Token.TokenType.NUMBER, Token.TokenType.DIVIDE, Token.TokenType.NUMBER,
                                Token.TokenType.SEPERATOR,
                                Token.TokenType.NUMBER, Token.TokenType.MINUS, Token.TokenType.NUMBER,
                                Token.TokenType.SEPERATOR,
                                Token.TokenType.NUMBER, Token.TokenType.MULTIPLY, Token.TokenType.NUMBER,
                                Token.TokenType.SEPERATOR,
                                Token.TokenType.NUMBER, Token.TokenType.EXPONENT, Token.TokenType.NUMBER,
                        }));

        var add = parser.ParseOperation().get();
        parser.AcceptSeperators();
        var div = parser.ParseOperation().get();
        parser.AcceptSeperators();
        var sub = parser.ParseOperation().get();
        parser.AcceptSeperators();
        var mul = parser.ParseOperation().get();
        parser.AcceptSeperators();
        var exp = parser.ParseOperation().get();
        parser.AcceptSeperators();
        assertEquals(add, new OperationNode(OperationNode.Operation.ADD, new ConstantNode("1"), new ConstantNode("2")));
        assertEquals(div,
                new OperationNode(OperationNode.Operation.DIVIDE, new ConstantNode("2"), new ConstantNode("3")));
        assertEquals(sub,
                new OperationNode(OperationNode.Operation.SUBTRACT, new ConstantNode("4"), new ConstantNode("8")));
        assertEquals(exp,
                new OperationNode(OperationNode.Operation.EXPONENT, new ConstantNode("7"), new ConstantNode("6")));
        assertEquals(mul,
                new OperationNode(OperationNode.Operation.MULTIPLY, new ConstantNode("9"), new ConstantNode("5")));
    }

    @Test
    public void really_complex() throws Exception {
        var parser = new Parser(
                UnitTests.testLexContent("z[a[5-8++]--] = 5 ?  (9<=9) ? 7 : 7 : a? 6:7\n" + //
                        "9++\n" + //
                        "a = 5 - 50.9  67 * 8 76 .6 .7? 6^-a:7\n" + //
                        "\n" + //
                        "\n" + //
                        "6 ~ 7? a=v=c=b-=7 : 6^7&&2||3^4\n" + //
                        "\n" + //
                        "1 * 3 + 6 ^ 6 - 7 / 6 % 6 >= 8 ? 8 == !8: 4~1>9\n" + //
                        "\n" + //
                        "(5 += (6 = 7 - 7 * 7 - -6) % 6--)--\n" + //
                        "\n" + //
                        "(a > 5 ? a : b) /= 7\n" + //
                        "\n" + //
                        "5 ? 1^2+3-5/4*6%7&&7||8>=5!~!6:6\n" + //
                        "\n" + //
                        "(--a[a=v=c=b-=7] `5%4` \"4\" .6 .6 * 7)++\n" + //
                        "\n" + //
                        "- + - - - - - - - - - - - - - - - - 6 ++\n" + //
                        "\n" + //
                        "!!!!!!!!!!!!7<!!!!!!!!!!!1+- (y&&l) / $7 ? a[`22`==7] : b\n" + //
                        "\n" + //
                        "-!+t^2+1",
                        new Token.TokenType[] { Token.TokenType.WORD, Token.TokenType.OPENBRACKET,
                                Token.TokenType.WORD, Token.TokenType.OPENBRACKET, Token.TokenType.NUMBER,
                                Token.TokenType.MINUS, Token.TokenType.NUMBER,
                                Token.TokenType.PLUSPLUS, Token.TokenType.CLOSEBRACKET, Token.TokenType.MINUSMINUS,
                                Token.TokenType.CLOSEBRACKET, Token.TokenType.ASSIGN, Token.TokenType.NUMBER,
                                Token.TokenType.QUESTION, Token.TokenType.OPENPAREN, Token.TokenType.NUMBER,
                                Token.TokenType.LESSTHANEQUAL, Token.TokenType.NUMBER, Token.TokenType.CLOSEPAREN,
                                Token.TokenType.QUESTION, Token.TokenType.NUMBER, Token.TokenType.COLON,
                                Token.TokenType.NUMBER,
                                Token.TokenType.COLON, Token.TokenType.WORD, Token.TokenType.QUESTION,
                                Token.TokenType.NUMBER,
                                Token.TokenType.COLON, Token.TokenType.NUMBER, Token.TokenType.SEPERATOR,
                                Token.TokenType.NUMBER, Token.TokenType.PLUSPLUS, Token.TokenType.SEPERATOR,
                                Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.NUMBER,
                                Token.TokenType.MINUS,
                                Token.TokenType.NUMBER, Token.TokenType.NUMBER, Token.TokenType.MULTIPLY,
                                Token.TokenType.NUMBER, Token.TokenType.NUMBER, Token.TokenType.NUMBER,
                                Token.TokenType.NUMBER,
                                Token.TokenType.QUESTION, Token.TokenType.NUMBER, Token.TokenType.EXPONENT,
                                Token.TokenType.MINUS, Token.TokenType.WORD, Token.TokenType.COLON,
                                Token.TokenType.NUMBER,
                                Token.TokenType.SEPERATOR, Token.TokenType.SEPERATOR, Token.TokenType.SEPERATOR,
                                Token.TokenType.NUMBER, Token.TokenType.MATCH, Token.TokenType.NUMBER,
                                Token.TokenType.QUESTION,
                                Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.WORD,
                                Token.TokenType.ASSIGN,
                                Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.WORD,
                                Token.TokenType.MINUSEQUAL,
                                Token.TokenType.NUMBER, Token.TokenType.COLON, Token.TokenType.NUMBER,
                                Token.TokenType.EXPONENT,
                                Token.TokenType.NUMBER, Token.TokenType.AND, Token.TokenType.NUMBER, Token.TokenType.OR,
                                Token.TokenType.NUMBER, Token.TokenType.EXPONENT, Token.TokenType.NUMBER,
                                Token.TokenType.SEPERATOR, Token.TokenType.SEPERATOR, Token.TokenType.NUMBER,
                                Token.TokenType.MULTIPLY, Token.TokenType.NUMBER, Token.TokenType.PLUS,
                                Token.TokenType.NUMBER,
                                Token.TokenType.EXPONENT, Token.TokenType.NUMBER, Token.TokenType.MINUS,
                                Token.TokenType.NUMBER,
                                Token.TokenType.DIVIDE, Token.TokenType.NUMBER, Token.TokenType.MODULO,
                                Token.TokenType.NUMBER,
                                Token.TokenType.GREATERTHANEQUAL, Token.TokenType.NUMBER, Token.TokenType.QUESTION,
                                Token.TokenType.NUMBER, Token.TokenType.EQUAL, Token.TokenType.NOT,
                                Token.TokenType.NUMBER,
                                Token.TokenType.COLON, Token.TokenType.NUMBER, Token.TokenType.MATCH,
                                Token.TokenType.NUMBER,
                                Token.TokenType.GREATERTHAN, Token.TokenType.NUMBER, Token.TokenType.SEPERATOR,
                                Token.TokenType.SEPERATOR, Token.TokenType.OPENPAREN,
                                Token.TokenType.NUMBER, Token.TokenType.PLUSEQUAL, Token.TokenType.OPENPAREN,
                                Token.TokenType.NUMBER, Token.TokenType.ASSIGN, Token.TokenType.NUMBER,
                                Token.TokenType.MINUS,
                                Token.TokenType.NUMBER, Token.TokenType.MULTIPLY, Token.TokenType.NUMBER,
                                Token.TokenType.MINUS,
                                Token.TokenType.MINUS, Token.TokenType.NUMBER, Token.TokenType.CLOSEPAREN,
                                Token.TokenType.MODULO, Token.TokenType.NUMBER, Token.TokenType.MINUSMINUS,
                                Token.TokenType.CLOSEPAREN, Token.TokenType.MINUSMINUS, Token.TokenType.SEPERATOR,
                                Token.TokenType.SEPERATOR, Token.TokenType.OPENPAREN, Token.TokenType.WORD,
                                Token.TokenType.GREATERTHAN, Token.TokenType.NUMBER, Token.TokenType.QUESTION,
                                Token.TokenType.WORD, Token.TokenType.COLON, Token.TokenType.WORD,
                                Token.TokenType.CLOSEPAREN,
                                Token.TokenType.DIVIDEEQUAL, Token.TokenType.NUMBER, Token.TokenType.SEPERATOR,
                                Token.TokenType.SEPERATOR, Token.TokenType.NUMBER, Token.TokenType.QUESTION,
                                Token.TokenType.NUMBER, Token.TokenType.EXPONENT, Token.TokenType.NUMBER,
                                Token.TokenType.PLUS,
                                Token.TokenType.NUMBER, Token.TokenType.MINUS, Token.TokenType.NUMBER,
                                Token.TokenType.DIVIDE,
                                Token.TokenType.NUMBER, Token.TokenType.MULTIPLY, Token.TokenType.NUMBER,
                                Token.TokenType.MODULO, Token.TokenType.NUMBER, Token.TokenType.AND,
                                Token.TokenType.NUMBER,
                                Token.TokenType.OR, Token.TokenType.NUMBER, Token.TokenType.GREATERTHANEQUAL,
                                Token.TokenType.NUMBER, Token.TokenType.NOTMATCH, Token.TokenType.NOT,
                                Token.TokenType.NUMBER,
                                Token.TokenType.COLON, Token.TokenType.NUMBER, Token.TokenType.SEPERATOR,
                                Token.TokenType.SEPERATOR, Token.TokenType.OPENPAREN, Token.TokenType.MINUSMINUS,
                                Token.TokenType.WORD, Token.TokenType.OPENBRACKET, Token.TokenType.WORD,
                                Token.TokenType.ASSIGN,
                                Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.WORD,
                                Token.TokenType.ASSIGN,
                                Token.TokenType.WORD, Token.TokenType.MINUSEQUAL, Token.TokenType.NUMBER,
                                Token.TokenType.CLOSEBRACKET, Token.TokenType.PATTERN, Token.TokenType.STRINGLITERAL,
                                Token.TokenType.NUMBER, Token.TokenType.NUMBER, Token.TokenType.MULTIPLY,
                                Token.TokenType.NUMBER, Token.TokenType.CLOSEPAREN, Token.TokenType.PLUSPLUS,
                                Token.TokenType.SEPERATOR, Token.TokenType.SEPERATOR, Token.TokenType.MINUS,
                                Token.TokenType.PLUS, Token.TokenType.MINUS, Token.TokenType.MINUS,
                                Token.TokenType.MINUS,
                                Token.TokenType.MINUS, Token.TokenType.MINUS, Token.TokenType.MINUS,
                                Token.TokenType.MINUS,
                                Token.TokenType.MINUS, Token.TokenType.MINUS, Token.TokenType.MINUS,
                                Token.TokenType.MINUS,
                                Token.TokenType.MINUS, Token.TokenType.MINUS, Token.TokenType.MINUS,
                                Token.TokenType.MINUS,
                                Token.TokenType.MINUS, Token.TokenType.NUMBER, Token.TokenType.PLUSPLUS,
                                Token.TokenType.SEPERATOR, Token.TokenType.SEPERATOR, Token.TokenType.NOT,
                                Token.TokenType.NOT,
                                Token.TokenType.NOT, Token.TokenType.NOT, Token.TokenType.NOT, Token.TokenType.NOT,
                                Token.TokenType.NOT, Token.TokenType.NOT, Token.TokenType.NOT, Token.TokenType.NOT,
                                Token.TokenType.NOT, Token.TokenType.NOT, Token.TokenType.NUMBER,
                                Token.TokenType.LESSTHAN,
                                Token.TokenType.NOT, Token.TokenType.NOT, Token.TokenType.NOT, Token.TokenType.NOT,
                                Token.TokenType.NOT, Token.TokenType.NOT, Token.TokenType.NOT, Token.TokenType.NOT,
                                Token.TokenType.NOT, Token.TokenType.NOT, Token.TokenType.NOT, Token.TokenType.NUMBER,
                                Token.TokenType.PLUS, Token.TokenType.MINUS, Token.TokenType.OPENPAREN,
                                Token.TokenType.WORD,
                                Token.TokenType.AND, Token.TokenType.WORD, Token.TokenType.CLOSEPAREN,
                                Token.TokenType.DIVIDE,
                                Token.TokenType.DOLLAR, Token.TokenType.NUMBER, Token.TokenType.QUESTION,
                                Token.TokenType.WORD,
                                Token.TokenType.OPENBRACKET, Token.TokenType.PATTERN, Token.TokenType.EQUAL,
                                Token.TokenType.NUMBER, Token.TokenType.CLOSEBRACKET, Token.TokenType.COLON,
                                Token.TokenType.WORD, Token.TokenType.SEPERATOR, Token.TokenType.SEPERATOR,
                                Token.TokenType.MINUS, Token.TokenType.NOT, Token.TokenType.PLUS, Token.TokenType.WORD,
                                Token.TokenType.EXPONENT, Token.TokenType.NUMBER, Token.TokenType.PLUS,
                                Token.TokenType.NUMBER }));

        while (true) {
            parser.AcceptSeperators();
            var parsed = parser.ParseOperation();
            if (parsed.isEmpty()) {
                break;
            }
        }
    }

    @Test
    public void error() throws Exception {
        var parser = new Parser(
                UnitTests.testLexContent("\na[--(7]\n" + //
                        "\n" + //
                        "(+a]\n" + //
                        "\n" + //
                        "aa &&\n" + //
                        "\n" + //
                        "y ^ (y -\n" + //
                        "\n" + //
                        "!\n" + //
                        "\n" + //
                        "5 ~ - + \n" + //
                        "\n" + //
                        "5^#t",
                        new Token.TokenType[] { Token.TokenType.SEPERATOR, Token.TokenType.WORD,
                                Token.TokenType.OPENBRACKET,
                                Token.TokenType.MINUSMINUS, Token.TokenType.OPENPAREN, Token.TokenType.NUMBER,
                                Token.TokenType.CLOSEBRACKET, Token.TokenType.SEPERATOR, Token.TokenType.SEPERATOR,
                                Token.TokenType.OPENPAREN, Token.TokenType.PLUS, Token.TokenType.WORD,
                                Token.TokenType.CLOSEBRACKET, Token.TokenType.SEPERATOR, Token.TokenType.SEPERATOR,
                                Token.TokenType.WORD, Token.TokenType.AND, Token.TokenType.SEPERATOR,
                                Token.TokenType.SEPERATOR,
                                Token.TokenType.WORD, Token.TokenType.EXPONENT, Token.TokenType.OPENPAREN,
                                Token.TokenType.WORD,
                                Token.TokenType.MINUS, Token.TokenType.SEPERATOR, Token.TokenType.SEPERATOR,
                                Token.TokenType.NOT, Token.TokenType.SEPERATOR, Token.TokenType.SEPERATOR,
                                Token.TokenType.NUMBER, Token.TokenType.MATCH, Token.TokenType.MINUS,
                                Token.TokenType.PLUS,
                                Token.TokenType.SEPERATOR, Token.TokenType.SEPERATOR, Token.TokenType.NUMBER,
                                Token.TokenType.EXPONENT }));

        while (parser.AcceptSeperators()) {
            assertThrows(AwkException.class, () -> parser.ParseOperation());
        }
    }

    @Test
    public void errorHandlingTest() throws Exception {
        var parser = new Parser(
                UnitTests.testLexContent(
                        "\n" +
                                "1 +\n" + // Incomplete addition
                                "2 *\n" + // Incomplete multiplication
                                "a &&\n" + // Incomplete logical AND
                                "b || c =\n" + // Incomplete assignment
                                "(3 + 4\n" + // Unbalanced parentheses
                                "x ? y \n" + // Ternary operator with missing colon
                                // "-- --a\n" + // Double decrement without operand
                                "1 ^ ^ 2\n" + // Consecutive exponents
                                "x % / y", // Consecutive modulo and division
                        new Token.TokenType[] {
                                Token.TokenType.SEPERATOR, Token.TokenType.NUMBER, Token.TokenType.PLUS,
                                Token.TokenType.SEPERATOR,
                                Token.TokenType.NUMBER, Token.TokenType.MULTIPLY, Token.TokenType.SEPERATOR,
                                Token.TokenType.WORD, Token.TokenType.AND, Token.TokenType.SEPERATOR,
                                Token.TokenType.WORD, Token.TokenType.OR, Token.TokenType.WORD, Token.TokenType.ASSIGN,
                                Token.TokenType.SEPERATOR,
                                Token.TokenType.OPENPAREN, Token.TokenType.NUMBER, Token.TokenType.PLUS,
                                Token.TokenType.NUMBER, Token.TokenType.SEPERATOR,
                                Token.TokenType.WORD, Token.TokenType.QUESTION, Token.TokenType.WORD,
                                Token.TokenType.SEPERATOR,
                                // Token.TokenType.MINUSMINUS, Token.TokenType.MINUSMINUS, Token.TokenType.WORD,
                                // Token.TokenType.SEPERATOR,
                                Token.TokenType.NUMBER, Token.TokenType.EXPONENT, Token.TokenType.EXPONENT,
                                Token.TokenType.NUMBER, Token.TokenType.SEPERATOR,
                                Token.TokenType.WORD, Token.TokenType.MODULO, Token.TokenType.DIVIDE,
                                Token.TokenType.WORD
                        }));

        while (parser.AcceptSeperators()) {
            assertThrows(AwkException.class, () -> parser.ParseOperation());
        }
    }

    @Test
    public void rest_op() throws Exception {
        // concat, compare, match, boolean, ternary, assignment
        var parser = new Parser(
                UnitTests.testLexContent(
                        "1 a[8]\n1<6\n55.6<=\"5\"\n`a`!=$3\n8==7\nq>7\n$r>=0\n`$3`~5\n44!~55\n0||1\n1&&1\n0? a:b\na^=6\nb%=7\nc*=8\nd/=9\ne+=10\nf-=11\ng=12",
                        new Token.TokenType[] {
                                Token.TokenType.NUMBER, Token.TokenType.WORD, Token.TokenType.OPENBRACKET,
                                Token.TokenType.NUMBER, Token.TokenType.CLOSEBRACKET, Token.TokenType.SEPERATOR,
                                Token.TokenType.NUMBER, Token.TokenType.LESSTHAN, Token.TokenType.NUMBER,
                                Token.TokenType.SEPERATOR, Token.TokenType.NUMBER, Token.TokenType.LESSTHANEQUAL,
                                Token.TokenType.STRINGLITERAL, Token.TokenType.SEPERATOR, Token.TokenType.PATTERN,
                                Token.TokenType.NOTEQUAL, Token.TokenType.DOLLAR, Token.TokenType.NUMBER,
                                Token.TokenType.SEPERATOR, Token.TokenType.NUMBER, Token.TokenType.EQUAL,
                                Token.TokenType.NUMBER, Token.TokenType.SEPERATOR, Token.TokenType.WORD,
                                Token.TokenType.GREATERTHAN, Token.TokenType.NUMBER, Token.TokenType.SEPERATOR,
                                Token.TokenType.DOLLAR, Token.TokenType.WORD, Token.TokenType.GREATERTHANEQUAL,
                                Token.TokenType.NUMBER, Token.TokenType.SEPERATOR, Token.TokenType.PATTERN,
                                Token.TokenType.MATCH, Token.TokenType.NUMBER, Token.TokenType.SEPERATOR,
                                Token.TokenType.NUMBER, Token.TokenType.NOTMATCH, Token.TokenType.NUMBER,
                                Token.TokenType.SEPERATOR, Token.TokenType.NUMBER, Token.TokenType.OR,
                                Token.TokenType.NUMBER, Token.TokenType.SEPERATOR, Token.TokenType.NUMBER,
                                Token.TokenType.AND, Token.TokenType.NUMBER, Token.TokenType.SEPERATOR,
                                Token.TokenType.NUMBER, Token.TokenType.QUESTION, Token.TokenType.WORD,
                                Token.TokenType.COLON, Token.TokenType.WORD, Token.TokenType.SEPERATOR,
                                Token.TokenType.WORD, Token.TokenType.EXPONENTEQUAL, Token.TokenType.NUMBER,
                                Token.TokenType.SEPERATOR, Token.TokenType.WORD, Token.TokenType.MODULOEQUAL,
                                Token.TokenType.NUMBER, Token.TokenType.SEPERATOR, Token.TokenType.WORD,
                                Token.TokenType.MULTIPLYEQUAL, Token.TokenType.NUMBER, Token.TokenType.SEPERATOR,
                                Token.TokenType.WORD, Token.TokenType.DIVIDEEQUAL, Token.TokenType.NUMBER,
                                Token.TokenType.SEPERATOR, Token.TokenType.WORD, Token.TokenType.PLUSEQUAL,
                                Token.TokenType.NUMBER, Token.TokenType.SEPERATOR, Token.TokenType.WORD,
                                Token.TokenType.MINUSEQUAL, Token.TokenType.NUMBER, Token.TokenType.SEPERATOR,
                                Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.NUMBER, }));
        var num1AndArrayAccess = parser.ParseOperation().get();
        parser.AcceptSeperators();

        // Parse the rest of the operations
        var lessThan = parser.ParseOperation().get();
        parser.AcceptSeperators();
        var lessThanOrEqual = parser.ParseOperation().get();
        parser.AcceptSeperators();
        var notEqual = parser.ParseOperation().get();
        parser.AcceptSeperators();
        var equal = parser.ParseOperation().get();
        parser.AcceptSeperators();
        var greaterThan = parser.ParseOperation().get();
        parser.AcceptSeperators();
        var greaterThanOrEqual = parser.ParseOperation().get();
        parser.AcceptSeperators();
        var patternMatch = parser.ParseOperation().get();
        parser.AcceptSeperators();
        var patternNotMatch = parser.ParseOperation().get();
        parser.AcceptSeperators();
        var logicalOr = parser.ParseOperation().get();
        parser.AcceptSeperators();
        var logicalAnd = parser.ParseOperation().get();
        parser.AcceptSeperators();
        var ternary = parser.ParseOperation().get();
        parser.AcceptSeperators();
        var exponentEqual = parser.ParseOperation().get();
        parser.AcceptSeperators();
        var moduloEqual = parser.ParseOperation().get();
        parser.AcceptSeperators();
        var multiplyEqual = parser.ParseOperation().get();
        parser.AcceptSeperators();
        var divideEqual = parser.ParseOperation().get();
        parser.AcceptSeperators();
        var plusEqual = parser.ParseOperation().get();
        parser.AcceptSeperators();
        var minusEqual = parser.ParseOperation().get();
        parser.AcceptSeperators();
        var assign = parser.ParseOperation().get();
        assertEquals(num1AndArrayAccess, new OperationNode(OperationNode.Operation.CONCATENATION,
                new ConstantNode("1"), new VariableReferenceNode("a", new ConstantNode("8"))));
        assertEquals(lessThan,
                new OperationNode(OperationNode.Operation.LT, new ConstantNode("1"), new ConstantNode("6")));
        assertEquals(lessThanOrEqual,
                new OperationNode(OperationNode.Operation.LE, new ConstantNode("55.6"), new ConstantNode("5")));
        assertEquals(notEqual,
                new OperationNode(OperationNode.Operation.NE, new PatternNode("a"),
                        new OperationNode(OperationNode.Operation.DOLLAR, new ConstantNode("3"))));
        assertEquals(equal,
                new OperationNode(OperationNode.Operation.EQ, new ConstantNode("8"), new ConstantNode("7")));
        assertEquals(greaterThan,
                new OperationNode(OperationNode.Operation.GT, new VariableReferenceNode("q"), new ConstantNode("7")));
        assertEquals(greaterThanOrEqual,
                new OperationNode(OperationNode.Operation.GE,
                        new OperationNode(OperationNode.Operation.DOLLAR, new VariableReferenceNode("r")),
                        new ConstantNode("0")));
        assertEquals(patternMatch,
                new OperationNode(OperationNode.Operation.MATCH, new PatternNode("$3"), new ConstantNode("5")));
        assertEquals(patternNotMatch,
                new OperationNode(OperationNode.Operation.NOTMATCH, new ConstantNode("44"), new ConstantNode("55")));
        assertEquals(logicalOr,
                new OperationNode(OperationNode.Operation.OR, new ConstantNode("0"), new ConstantNode("1")));
        assertEquals(logicalAnd,
                new OperationNode(OperationNode.Operation.AND, new ConstantNode("1"), new ConstantNode("1")));
        assertEquals(ternary, new TernaryOperationNode(new ConstantNode("0"), new VariableReferenceNode("a"),
                new VariableReferenceNode("b")));
        assertEquals(exponentEqual,
                new AssignmentNode(new VariableReferenceNode("a"), new OperationNode(OperationNode.Operation.EXPONENT,
                        new VariableReferenceNode("a"), new ConstantNode("6"))));
        assertEquals(moduloEqual,
                new AssignmentNode(new VariableReferenceNode("b"), new OperationNode(OperationNode.Operation.MODULO,
                        new VariableReferenceNode("b"), new ConstantNode("7"))));
        assertEquals(multiplyEqual,
                new AssignmentNode(new VariableReferenceNode("c"), new OperationNode(OperationNode.Operation.MULTIPLY,
                        new VariableReferenceNode("c"), new ConstantNode("8"))));
        assertEquals(divideEqual,
                new AssignmentNode(new VariableReferenceNode("d"), new OperationNode(OperationNode.Operation.DIVIDE,
                        new VariableReferenceNode("d"), new ConstantNode("9"))));
        assertEquals(plusEqual,
                new AssignmentNode(new VariableReferenceNode("e"), new OperationNode(OperationNode.Operation.ADD,
                        new VariableReferenceNode("e"), new ConstantNode("10"))));
        assertEquals(minusEqual,
                new AssignmentNode(new VariableReferenceNode("f"), new OperationNode(OperationNode.Operation.SUBTRACT,
                        new VariableReferenceNode("f"), new ConstantNode("11"))));
        assertEquals(assign, new AssignmentNode(new VariableReferenceNode("g"), new ConstantNode("12")));

    }

    // TODO: pemdas/precedence

    @Test
    public void pemdasTest() throws Exception {
        var parser = new Parser(UnitTests.testLexContent(
                "2 * (3 + 4)\n" +
                        "2^3\n" +
                        "2 * 3 / 4\n" +
                        "2 + 3 - 4\n" +
                        "2 * (3 + 4) - 5^2 / (6 + 3)",
                new Token.TokenType[] {
                        Token.TokenType.NUMBER, Token.TokenType.MULTIPLY, Token.TokenType.OPENPAREN,
                        Token.TokenType.NUMBER, Token.TokenType.PLUS, Token.TokenType.NUMBER,
                        Token.TokenType.CLOSEPAREN, Token.TokenType.SEPERATOR,

                        Token.TokenType.NUMBER, Token.TokenType.EXPONENT, Token.TokenType.NUMBER,
                        Token.TokenType.SEPERATOR,

                        Token.TokenType.NUMBER, Token.TokenType.MULTIPLY, Token.TokenType.NUMBER,
                        Token.TokenType.DIVIDE, Token.TokenType.NUMBER, Token.TokenType.SEPERATOR,

                        Token.TokenType.NUMBER, Token.TokenType.PLUS, Token.TokenType.NUMBER, Token.TokenType.MINUS,
                        Token.TokenType.NUMBER, Token.TokenType.SEPERATOR,

                        Token.TokenType.NUMBER, Token.TokenType.MULTIPLY, Token.TokenType.OPENPAREN,
                        Token.TokenType.NUMBER, Token.TokenType.PLUS, Token.TokenType.NUMBER,
                        Token.TokenType.CLOSEPAREN, Token.TokenType.MINUS,
                        Token.TokenType.NUMBER, Token.TokenType.EXPONENT, Token.TokenType.NUMBER,
                        Token.TokenType.DIVIDE, Token.TokenType.OPENPAREN,
                        Token.TokenType.NUMBER, Token.TokenType.PLUS, Token.TokenType.NUMBER, Token.TokenType.CLOSEPAREN
                }));

        var result1 = parser.ParseOperation().get();
        parser.AcceptSeperators();

        var result2 = parser.ParseOperation().get();
        parser.AcceptSeperators();

        var result3 = parser.ParseOperation().get();
        parser.AcceptSeperators();

        var result4 = parser.ParseOperation().get();
        parser.AcceptSeperators();

        var result5 = parser.ParseOperation().get();
        parser.AcceptSeperators();

        // Test cases to check if your parser follows PEMDAS/BODMAS correctly

        // Parentheses should have the highest precedence
        assertEquals(result1, new OperationNode(OperationNode.Operation.MULTIPLY, new ConstantNode("2"),
                new OperationNode(OperationNode.Operation.ADD, new ConstantNode("3"), new ConstantNode("4"))));

        // Exponents (^) should come next
        assertEquals(result2,
                new OperationNode(OperationNode.Operation.EXPONENT, new ConstantNode("2"), new ConstantNode("3")));

        // Multiplication (*) and Division (/) should have the same precedence and be
        // evaluated from left to right
        assertEquals(result3, new OperationNode(OperationNode.Operation.DIVIDE,
                new OperationNode(OperationNode.Operation.MULTIPLY, new ConstantNode("2"), new ConstantNode("3")),
                new ConstantNode("4")));

        // Addition (+) and Subtraction (-) should have the same precedence and be
        // evaluated from left to right
        assertEquals(result4,
                new OperationNode(OperationNode.Operation.SUBTRACT,
                        new OperationNode(OperationNode.Operation.ADD, new ConstantNode("2"), new ConstantNode("3")),
                        new ConstantNode("4")));

        // Combined expression
        assertEquals(result5,
                new OperationNode(OperationNode.Operation.SUBTRACT,
                        new OperationNode(OperationNode.Operation.MULTIPLY,
                                new ConstantNode("2"),
                                new OperationNode(OperationNode.Operation.ADD,
                                        new ConstantNode("3"),
                                        new ConstantNode("4"))),
                        new OperationNode(OperationNode.Operation.DIVIDE,
                                new OperationNode(OperationNode.Operation.EXPONENT,
                                        new ConstantNode("5"),
                                        new ConstantNode("2")),
                                new OperationNode(OperationNode.Operation.ADD,
                                        new ConstantNode("6"),
                                        new ConstantNode("3")))));
    }

    @Test
    public void orderOfOperationsTest() throws Exception {
        var parser = new Parser(UnitTests.testLexContent(
                "a + b * c\n" +
                        "d / e - f\n" +
                        "g % h ^ i\n" +
                        "j == k && l > m\n" +
                        "n || o != p\n" +
                        "q ? r + s : t - u\n" +
                        "v = w * x / y + z",
                new Token.TokenType[] {
                        Token.TokenType.WORD, Token.TokenType.PLUS, Token.TokenType.WORD, Token.TokenType.MULTIPLY,
                        Token.TokenType.WORD, Token.TokenType.SEPERATOR,
                        Token.TokenType.WORD, Token.TokenType.DIVIDE, Token.TokenType.WORD, Token.TokenType.MINUS,
                        Token.TokenType.WORD, Token.TokenType.SEPERATOR,
                        Token.TokenType.WORD, Token.TokenType.MODULO, Token.TokenType.WORD, Token.TokenType.EXPONENT,
                        Token.TokenType.WORD, Token.TokenType.SEPERATOR,
                        Token.TokenType.WORD, Token.TokenType.EQUAL, Token.TokenType.WORD, Token.TokenType.AND,
                        Token.TokenType.WORD, Token.TokenType.GREATERTHAN, Token.TokenType.WORD,
                        Token.TokenType.SEPERATOR,
                        Token.TokenType.WORD, Token.TokenType.OR, Token.TokenType.WORD, Token.TokenType.NOTEQUAL,
                        Token.TokenType.WORD, Token.TokenType.SEPERATOR,
                        Token.TokenType.WORD, Token.TokenType.QUESTION, Token.TokenType.WORD, Token.TokenType.PLUS,
                        Token.TokenType.WORD, Token.TokenType.COLON,
                        Token.TokenType.WORD, Token.TokenType.MINUS, Token.TokenType.WORD, Token.TokenType.SEPERATOR,
                        Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.WORD, Token.TokenType.MULTIPLY,
                        Token.TokenType.WORD, Token.TokenType.DIVIDE, Token.TokenType.WORD, Token.TokenType.PLUS,
                        Token.TokenType.WORD
                }));

        var result1 = parser.ParseOperation().get();
        parser.AcceptSeperators();

        var result2 = parser.ParseOperation().get();
        parser.AcceptSeperators();

        var result3 = parser.ParseOperation().get();
        parser.AcceptSeperators();

        var result4 = parser.ParseOperation().get();
        parser.AcceptSeperators();

        var result5 = parser.ParseOperation().get();
        parser.AcceptSeperators();

        var result6 = parser.ParseOperation().get();
        parser.AcceptSeperators();

        var result7 = parser.ParseOperation().get();
        parser.AcceptSeperators();

        // Test cases to check order of operations

        // Multiplication (*) should have higher precedence than addition (+)
        assertEquals(result1, new OperationNode(OperationNode.Operation.ADD,
                new VariableReferenceNode("a"),
                new OperationNode(OperationNode.Operation.MULTIPLY,
                        new VariableReferenceNode("b"),
                        new VariableReferenceNode("c"))));

        // Division (/) should have higher precedence than subtraction (-)
        assertEquals(result2, new OperationNode(OperationNode.Operation.SUBTRACT,
                new OperationNode(OperationNode.Operation.DIVIDE,
                        new VariableReferenceNode("d"),
                        new VariableReferenceNode("e")),
                new VariableReferenceNode("f")));

        // Exponent (^) should have higher precedence than modulo (%)
        assertEquals(result3, new OperationNode(OperationNode.Operation.MODULO,
                new VariableReferenceNode("g"),
                new OperationNode(OperationNode.Operation.EXPONENT,
                        new VariableReferenceNode("h"),
                        new VariableReferenceNode("i"))));

        // Comparison (==) should have higher precedence than logical AND (&&)
        assertEquals(result4, new OperationNode(OperationNode.Operation.AND,
                new OperationNode(OperationNode.Operation.EQ,
                        new VariableReferenceNode("j"),
                        new VariableReferenceNode("k")),
                new OperationNode(OperationNode.Operation.GT,
                        new VariableReferenceNode("l"),
                        new VariableReferenceNode("m"))));

        // Logical OR (||) should have lower precedence than inequality (!=)
        assertEquals(result5, new OperationNode(OperationNode.Operation.OR,
                new VariableReferenceNode("n"),
                new OperationNode(OperationNode.Operation.NE,
                        new VariableReferenceNode("o"),
                        new VariableReferenceNode("p"))));

        // Ternary operator should have lower precedence than addition (+) and
        // subtraction (-)
        assertEquals(result6, new TernaryOperationNode(
                new VariableReferenceNode("q"),
                new OperationNode(OperationNode.Operation.ADD,
                        new VariableReferenceNode("r"),
                        new VariableReferenceNode("s")),
                new OperationNode(OperationNode.Operation.SUBTRACT,
                        new VariableReferenceNode("t"),
                        new VariableReferenceNode("u"))));

        // Assignment (=) should have lower precedence than multiplication (*) and
        // division (/)
        assertEquals(result7, new AssignmentNode(
                new VariableReferenceNode("v"),
                new OperationNode(OperationNode.Operation.ADD,
                        new OperationNode(OperationNode.Operation.DIVIDE,
                                new OperationNode(OperationNode.Operation.MULTIPLY,
                                        new VariableReferenceNode("w"),
                                        new VariableReferenceNode("x")),
                                new VariableReferenceNode("y")),
                        new VariableReferenceNode("z"))));
    }

    @Test
    public void orderOfOperationsTest1() throws Exception {
        var parser = new Parser(UnitTests.testLexContent(
                "a + b * c\n" +
                        "d / e - f\n" +
                        "g % h ^ i\n" +
                        "j == k && l > m\n" +
                        "n || o != p\n" +
                        "q ? r + s : t - u\n" +
                        "v = w * x / y + z\n" +
                        "x++ * y--\n" +
                        "++a - --b",
                new Token.TokenType[] {
                        Token.TokenType.WORD, Token.TokenType.PLUS, Token.TokenType.WORD, Token.TokenType.MULTIPLY,
                        Token.TokenType.WORD, Token.TokenType.SEPERATOR,
                        Token.TokenType.WORD, Token.TokenType.DIVIDE, Token.TokenType.WORD, Token.TokenType.MINUS,
                        Token.TokenType.WORD, Token.TokenType.SEPERATOR,
                        Token.TokenType.WORD, Token.TokenType.MODULO, Token.TokenType.WORD, Token.TokenType.EXPONENT,
                        Token.TokenType.WORD, Token.TokenType.SEPERATOR,
                        Token.TokenType.WORD, Token.TokenType.EQUAL, Token.TokenType.WORD, Token.TokenType.AND,
                        Token.TokenType.WORD, Token.TokenType.GREATERTHAN, Token.TokenType.WORD,
                        Token.TokenType.SEPERATOR,
                        Token.TokenType.WORD, Token.TokenType.OR, Token.TokenType.WORD, Token.TokenType.NOTEQUAL,
                        Token.TokenType.WORD, Token.TokenType.SEPERATOR,
                        Token.TokenType.WORD, Token.TokenType.QUESTION, Token.TokenType.WORD, Token.TokenType.PLUS,
                        Token.TokenType.WORD, Token.TokenType.COLON,
                        Token.TokenType.WORD, Token.TokenType.MINUS, Token.TokenType.WORD, Token.TokenType.SEPERATOR,
                        Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.WORD, Token.TokenType.MULTIPLY,
                        Token.TokenType.WORD, Token.TokenType.DIVIDE, Token.TokenType.WORD, Token.TokenType.PLUS,
                        Token.TokenType.WORD, Token.TokenType.SEPERATOR,
                        Token.TokenType.WORD, Token.TokenType.PLUSPLUS, Token.TokenType.MULTIPLY, Token.TokenType.WORD,
                        Token.TokenType.MINUSMINUS, Token.TokenType.SEPERATOR,
                        Token.TokenType.PLUSPLUS, Token.TokenType.WORD, Token.TokenType.MINUS,
                        Token.TokenType.MINUSMINUS, Token.TokenType.WORD
                }));

        var result1 = parser.ParseOperation().get();
        parser.AcceptSeperators();

        var result2 = parser.ParseOperation().get();
        parser.AcceptSeperators();

        var result3 = parser.ParseOperation().get();
        parser.AcceptSeperators();

        var result4 = parser.ParseOperation().get();
        parser.AcceptSeperators();

        var result5 = parser.ParseOperation().get();
        parser.AcceptSeperators();

        var result6 = parser.ParseOperation().get();
        parser.AcceptSeperators();

        var result7 = parser.ParseOperation().get();
        parser.AcceptSeperators();

        var result8 = parser.ParseOperation().get();
        parser.AcceptSeperators();

        var result9 = parser.ParseOperation().get();
        parser.AcceptSeperators();

        // Test cases to check order of operations

        // Multiplication (*) should have higher precedence than addition (+)
        assertEquals(result1, new OperationNode(OperationNode.Operation.ADD,
                new VariableReferenceNode("a"),
                new OperationNode(OperationNode.Operation.MULTIPLY,
                        new VariableReferenceNode("b"),
                        new VariableReferenceNode("c"))));

        // Division (/) should have higher precedence than subtraction (-)
        assertEquals(result2, new OperationNode(OperationNode.Operation.SUBTRACT,
                new OperationNode(OperationNode.Operation.DIVIDE,
                        new VariableReferenceNode("d"),
                        new VariableReferenceNode("e")),
                new VariableReferenceNode("f")));

        // Exponent (^) should have higher precedence than modulo (%)
        assertEquals(result3, new OperationNode(OperationNode.Operation.MODULO,
                new VariableReferenceNode("g"),
                new OperationNode(OperationNode.Operation.EXPONENT,
                        new VariableReferenceNode("h"),
                        new VariableReferenceNode("i"))));

        // Comparison (==) should have higher precedence than logical AND (&&)
        assertEquals(result4, new OperationNode(OperationNode.Operation.AND,
                new OperationNode(OperationNode.Operation.EQ,
                        new VariableReferenceNode("j"),
                        new VariableReferenceNode("k")),
                new OperationNode(OperationNode.Operation.GT,
                        new VariableReferenceNode("l"),
                        new VariableReferenceNode("m"))));

        // Logical OR (||) should have lower precedence than inequality (!=)
        assertEquals(result5, new OperationNode(OperationNode.Operation.OR,
                new VariableReferenceNode("n"),
                new OperationNode(OperationNode.Operation.NE,
                        new VariableReferenceNode("o"),
                        new VariableReferenceNode("p"))));

        // Ternary operator should have lower precedence than addition (+) and
        // subtraction (-)
        assertEquals(result6, new TernaryOperationNode(
                new VariableReferenceNode("q"),
                new OperationNode(OperationNode.Operation.ADD,
                        new VariableReferenceNode("r"),
                        new VariableReferenceNode("s")),
                new OperationNode(OperationNode.Operation.SUBTRACT,
                        new VariableReferenceNode("t"),
                        new VariableReferenceNode("u"))));

        // Assignment (=) should have lower precedence than multiplication (*) and
        // division (/)
        assertEquals(result7, new AssignmentNode(
                new VariableReferenceNode("v"),
                new OperationNode(OperationNode.Operation.ADD,
                        new OperationNode(OperationNode.Operation.DIVIDE,
                                new OperationNode(OperationNode.Operation.MULTIPLY,
                                        new VariableReferenceNode("w"),
                                        new VariableReferenceNode("x")),
                                new VariableReferenceNode("y")),
                        new VariableReferenceNode("z"))));

        // Post-PLUSPLUS (++) should have higher precedence than multiplication (*)
        // Post-MINUSMINUS (--) should have higher precedence than multiplication (*)
        assertEquals(result8, new OperationNode(OperationNode.Operation.MULTIPLY,
                new OperationNode(OperationNode.Operation.POSTINC,
                        new VariableReferenceNode("x")),
                new OperationNode(OperationNode.Operation.POSTDEC,
                        new VariableReferenceNode("y"))));

        // Pre-PLUSPLUS (++) should have higher precedence than subtraction (-)
        // Pre-MINUSMINUS (--) should have higher precedence than subtraction (-)
        assertEquals(result9, new OperationNode(OperationNode.Operation.SUBTRACT,
                new OperationNode(OperationNode.Operation.PREINC,
                        new VariableReferenceNode("a")),
                new OperationNode(OperationNode.Operation.PREDEC,
                        new VariableReferenceNode("b"))));
    }

    @Test
    public void testBlockWithConditional() throws Exception {
        var program = UnitTests.parse("a==5 {}", new Token.TokenType[] { Token.TokenType.WORD, Token.TokenType.EQUAL,
                Token.TokenType.NUMBER, Token.TokenType.OPENBRACE, Token.TokenType.CLOSEBRACE }, 0, 0, 0, 1);
        var block = program.getRestBlocks().get(0);
        assertEquals(block.getCondition().get(),
                new OperationNode(OperationNode.Operation.EQ, new VariableReferenceNode("a"), new ConstantNode("5")));
        assertTrue(block.getStatements().isEmpty());
    }

    @Test
    public void testBlockWithConditionalAndStatement() throws Exception {
        var program = UnitTests.parse("a==5 {prints(a)}",
                new Token.TokenType[] { Token.TokenType.WORD, Token.TokenType.EQUAL,
                        Token.TokenType.NUMBER, Token.TokenType.OPENBRACE, Token.TokenType.WORD,
                        Token.TokenType.OPENPAREN,
                        Token.TokenType.WORD, Token.TokenType.CLOSEPAREN, Token.TokenType.CLOSEBRACE },
                0, 0, 0, 1);

        var block = program.getRestBlocks().get(0);
        assertEquals(block.getCondition().get(),
                new OperationNode(OperationNode.Operation.EQ, new VariableReferenceNode("a"), new ConstantNode("5")));
        assertEquals(block.getStatements().size(), 1);
        assertEquals(block.getStatements().get(0),
                new FunctionCallNode("prints", new LinkedList<>() {
                    {
                        add(new VariableReferenceNode("a"));
                    }
                }));

    }

    @Test
    public void parseIfElseIf() throws Exception {
        var program = UnitTests.parse(
                "  BEGIN {    if (z > --b) z -= 1; else if (z ~ `a`) { a[0] = v(2, 5 + r) # assign a[0]\n;;;  } else { delete a[0]; q = v[0] ? 1 : 0 }}",
                new Token.TokenType[] {
                        Token.TokenType.BEGIN, Token.TokenType.OPENBRACE, Token.TokenType.IF, Token.TokenType.OPENPAREN,
                        Token.TokenType.WORD, Token.TokenType.GREATERTHAN, Token.TokenType.MINUSMINUS,
                        Token.TokenType.WORD, Token.TokenType.CLOSEPAREN, Token.TokenType.WORD,
                        Token.TokenType.MINUSEQUAL, Token.TokenType.NUMBER, Token.TokenType.SEPERATOR,
                        Token.TokenType.ELSE, Token.TokenType.IF,
                        Token.TokenType.OPENPAREN, Token.TokenType.WORD, Token.TokenType.MATCH, Token.TokenType.PATTERN,
                        Token.TokenType.CLOSEPAREN, Token.TokenType.OPENBRACE, Token.TokenType.WORD,
                        Token.TokenType.OPENBRACKET, Token.TokenType.NUMBER, Token.TokenType.CLOSEBRACKET,
                        Token.TokenType.ASSIGN, Token.TokenType.WORD, Token.TokenType.OPENPAREN, Token.TokenType.NUMBER,
                        Token.TokenType.COMMA, Token.TokenType.NUMBER, Token.TokenType.PLUS, Token.TokenType.WORD,
                        Token.TokenType.CLOSEPAREN, Token.TokenType.SEPERATOR, Token.TokenType.SEPERATOR,
                        Token.TokenType.SEPERATOR, Token.TokenType.SEPERATOR, Token.TokenType.CLOSEBRACE,
                        Token.TokenType.ELSE, Token.TokenType.OPENBRACE, Token.TokenType.DELETE, Token.TokenType.WORD,
                        Token.TokenType.OPENBRACKET, Token.TokenType.NUMBER, Token.TokenType.CLOSEBRACKET,
                        Token.TokenType.SEPERATOR, Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.WORD,
                        Token.TokenType.OPENBRACKET, Token.TokenType.NUMBER, Token.TokenType.CLOSEBRACKET,
                        Token.TokenType.QUESTION, Token.TokenType.NUMBER, Token.TokenType.COLON, Token.TokenType.NUMBER,
                        Token.TokenType.CLOSEBRACE, Token.TokenType.CLOSEBRACE
                }, 1, 0, 0, 0);
        var block = program.getBeginBlocks().get(0);
        var ifelse = block.getStatements().get(0);
        if (ifelse instanceof IfNode ifs && ifs.getOtherwise().get() instanceof IfNode ifelses
                && ifelses.getOtherwise().get() instanceof BlockNode elses) {
            assertEquals(ifs.getCondition(),
                    new OperationNode(OperationNode.Operation.GT, new VariableReferenceNode("z"),
                            new OperationNode(OperationNode.Operation.PREDEC, new VariableReferenceNode("b"))));
            assertEquals(ifs.getThenBlock().getStatements().size(), 1);
            assertEquals(ifs.getThenBlock().getStatements().get(0),
                    new AssignmentNode(new VariableReferenceNode("z"),
                            new OperationNode(OperationNode.Operation.SUBTRACT, new VariableReferenceNode("z"),
                                    new ConstantNode("1"))));
            assertEquals(ifelses.getCondition(), new OperationNode(OperationNode.Operation.MATCH,
                    new VariableReferenceNode("z"), new PatternNode("a")));

            assertEquals(ifelses.getThenBlock().getStatements().size(), 1);
            assertEquals(ifelses.getThenBlock().getStatements().get(0),
                    new AssignmentNode(new VariableReferenceNode("a", new ConstantNode("0")),
                            new FunctionCallNode("v", new LinkedList<>() {
                                {
                                    add(new ConstantNode("2"));
                                    add(new OperationNode(OperationNode.Operation.ADD, new ConstantNode("5"),
                                            new VariableReferenceNode("r")));
                                }
                            })));

            assertEquals(elses.getStatements().size(), 2);
            assertEquals(elses.getStatements().get(0),
                    new DeleteNode(new VariableReferenceNode("a", new ConstantNode("0"))));
            assertEquals(elses.getStatements().get(1), new AssignmentNode(new VariableReferenceNode("q"),
                    new TernaryOperationNode(new VariableReferenceNode("v", new ConstantNode("0")),
                            new ConstantNode("1"), new ConstantNode("0"))));

        } else {
            fail();
        }
    }

    // for (..;..;..) and for (.. in ..)
    @Test
    public void parseFors() throws Exception {
        var program = UnitTests.parse(
                "BEGIN { for (; (!--i) <= b ? 0 : 5; i++) { pick(i); continue; }} END { a[0] = 5; a[1] = z ? --a : a[0]++\n"
                        + //
                        "for (i in a) ick(i); a = i > 0 ? i++: i*=i }",
                new Token.TokenType[] {
                        Token.TokenType.BEGIN, Token.TokenType.OPENBRACE, Token.TokenType.FOR,
                        Token.TokenType.OPENPAREN,
                        Token.TokenType.SEPERATOR, Token.TokenType.OPENPAREN, Token.TokenType.NOT,
                        Token.TokenType.MINUSMINUS,
                        Token.TokenType.WORD, Token.TokenType.CLOSEPAREN,
                        Token.TokenType.LESSTHANEQUAL, Token.TokenType.WORD, Token.TokenType.QUESTION,
                        Token.TokenType.NUMBER,
                        Token.TokenType.COLON, Token.TokenType.NUMBER, Token.TokenType.SEPERATOR, Token.TokenType.WORD,
                        Token.TokenType.PLUSPLUS, Token.TokenType.CLOSEPAREN, Token.TokenType.OPENBRACE,
                        Token.TokenType.WORD,
                        Token.TokenType.OPENPAREN, Token.TokenType.WORD, Token.TokenType.CLOSEPAREN,
                        Token.TokenType.SEPERATOR,
                        Token.TokenType.CONTINUE, Token.TokenType.SEPERATOR, Token.TokenType.CLOSEBRACE,
                        Token.TokenType.CLOSEBRACE,
                        Token.TokenType.END, Token.TokenType.OPENBRACE, Token.TokenType.WORD,
                        Token.TokenType.OPENBRACKET,
                        Token.TokenType.NUMBER, Token.TokenType.CLOSEBRACKET, Token.TokenType.ASSIGN,
                        Token.TokenType.NUMBER,
                        Token.TokenType.SEPERATOR, Token.TokenType.WORD, Token.TokenType.OPENBRACKET,
                        Token.TokenType.NUMBER,
                        Token.TokenType.CLOSEBRACKET, Token.TokenType.ASSIGN, Token.TokenType.WORD,
                        Token.TokenType.QUESTION,
                        Token.TokenType.MINUSMINUS, Token.TokenType.WORD, Token.TokenType.COLON, Token.TokenType.WORD,
                        Token.TokenType.OPENBRACKET, Token.TokenType.NUMBER, Token.TokenType.CLOSEBRACKET,
                        Token.TokenType.PLUSPLUS, Token.TokenType.SEPERATOR, Token.TokenType.FOR,
                        Token.TokenType.OPENPAREN,
                        Token.TokenType.WORD, Token.TokenType.IN, Token.TokenType.WORD, Token.TokenType.CLOSEPAREN,
                        Token.TokenType.WORD, Token.TokenType.OPENPAREN, Token.TokenType.WORD,
                        Token.TokenType.CLOSEPAREN, Token.TokenType.SEPERATOR, Token.TokenType.WORD,
                        Token.TokenType.ASSIGN, Token.TokenType.WORD,
                        Token.TokenType.GREATERTHAN,
                        Token.TokenType.NUMBER, Token.TokenType.QUESTION, Token.TokenType.WORD,
                        Token.TokenType.PLUSPLUS,
                        Token.TokenType.COLON, Token.TokenType.WORD, Token.TokenType.MULTIPLYEQUAL,
                        Token.TokenType.WORD, Token.TokenType.CLOSEBRACE,
                }, 1, 1, 0, 0);

        var block = program.getBeginBlocks().get(0);
        var forloop = block.getStatements().get(0);
        if (forloop instanceof ForNode fornode) {
            assertEquals(fornode.getCondition().get(),
                    new TernaryOperationNode(new OperationNode(OperationNode.Operation.LE,
                            new OperationNode(OperationNode.Operation.NOT,
                                    new OperationNode(OperationNode.Operation.PREDEC,
                                            new VariableReferenceNode("i"))),
                            new VariableReferenceNode("b")),
                            new ConstantNode("0"), new ConstantNode("5")));
            assertEquals(fornode.getIncrement().get(), new OperationNode(OperationNode.Operation.POSTINC,
                    new VariableReferenceNode("i")));
            assertEquals(fornode.getBlock().getStatements().size(), 2);
            assertEquals(fornode.getBlock().getStatements().get(0),
                    new FunctionCallNode("pick", new LinkedList<>() {
                        {
                            add(new VariableReferenceNode("i"));
                        }
                    }));
            assertEquals(fornode.getBlock().getStatements().get(1), new ContinueNode());

        } else {
            fail();
        }

        var block2 = program.getEndBlocks().get(0);
        var assignment = block2.getStatements().get(0);
        var assignment2 = block2.getStatements().get(1);
        var forloop2 = block2.getStatements().get(2);
        var ternary = block2.getStatements().get(3);
        System.out.println(ternary);
        System.out.println(forloop2);
        System.out.println(assignment2);
        System.out.println(assignment);
        if (assignment instanceof AssignmentNode assign && assignment2 instanceof AssignmentNode assign2
                && forloop2 instanceof ForEachNode fornode2 && ternary instanceof AssignmentNode ternary2) {
            assertEquals(assign.getTarget(), new VariableReferenceNode("a", new ConstantNode("0")));
            assertEquals(assign.getExpression(), new ConstantNode("5"));
            assertEquals(assign2.getTarget(), new VariableReferenceNode("a", new ConstantNode("1")));
            assertEquals(assign2.getExpression(),
                    new TernaryOperationNode(new VariableReferenceNode("z"),
                            new OperationNode(OperationNode.Operation.PREDEC, new VariableReferenceNode("a")),
                            new OperationNode(OperationNode.Operation.POSTINC,
                                    new VariableReferenceNode("a", new ConstantNode("0")))));
            assertEquals(fornode2.getIndex(), "i");
            assertEquals(fornode2.getIterable(), new VariableReferenceNode("a"));
            assertEquals(fornode2.getBlock().getStatements().size(), 1);
            assertEquals(fornode2.getBlock().getStatements().get(0),
                    new FunctionCallNode("ick", new LinkedList<>() {
                        {
                            add(new VariableReferenceNode("i"));
                        }
                    }));
            assertEquals(ternary2.getTarget(), new VariableReferenceNode("a"));
            assertEquals(ternary2.getExpression(),
                    new TernaryOperationNode(new OperationNode(OperationNode.Operation.GT,
                            new VariableReferenceNode("i"), new ConstantNode("0")),
                            new OperationNode(OperationNode.Operation.POSTINC, new VariableReferenceNode("i")),
                            new AssignmentNode(new VariableReferenceNode("i"),
                                    new OperationNode(OperationNode.Operation.MULTIPLY, new VariableReferenceNode("i"),
                                            new VariableReferenceNode("i")))));

        } else {
            fail();
        }
    }

    // while and do-while
    @Test
    public void testWhile() throws Exception {
        var program = UnitTests.parse("BEGIN { while (i < 10) { i++ } do { i-- } while (i > 0) }",
                new Token.TokenType[] {
                        Token.TokenType.BEGIN, Token.TokenType.OPENBRACE, Token.TokenType.WHILE,
                        Token.TokenType.OPENPAREN,
                        Token.TokenType.WORD, Token.TokenType.LESSTHAN, Token.TokenType.NUMBER,
                        Token.TokenType.CLOSEPAREN,
                        Token.TokenType.OPENBRACE, Token.TokenType.WORD, Token.TokenType.PLUSPLUS,
                        Token.TokenType.CLOSEBRACE,
                        Token.TokenType.DO, Token.TokenType.OPENBRACE, Token.TokenType.WORD,
                        Token.TokenType.MINUSMINUS,
                        Token.TokenType.CLOSEBRACE, Token.TokenType.WHILE, Token.TokenType.OPENPAREN,
                        Token.TokenType.WORD,
                        Token.TokenType.GREATERTHAN, Token.TokenType.NUMBER, Token.TokenType.CLOSEPAREN,
                        Token.TokenType.CLOSEBRACE
                }, 1, 0, 0, 0);
        var block = program.getBeginBlocks().get(0);
        var whileloop = block.getStatements().get(0);
        var dowhileloop = block.getStatements().get(1);
        if (whileloop instanceof WhileNode whilenode && dowhileloop instanceof DoWhileNode dowhilenode) {
            assertEquals(whilenode.getCondition(),
                    new OperationNode(OperationNode.Operation.LT, new VariableReferenceNode("i"),
                            new ConstantNode("10")));
            assertEquals(whilenode.getBlock().getStatements().size(), 1);
            assertEquals(whilenode.getBlock().getStatements().get(0),
                    (new AssignmentNode(new VariableReferenceNode("i"),
                            new OperationNode(OperationNode.Operation.POSTINC, new VariableReferenceNode("i")))));
            assertEquals(dowhilenode.getCondition(),
                    new OperationNode(OperationNode.Operation.GT, new VariableReferenceNode("i"),
                            new ConstantNode("0")));
            assertEquals(dowhilenode.getBlock().getStatements().size(), 1);
            assertEquals(dowhilenode.getBlock().getStatements().get(0),
                    (new AssignmentNode(new VariableReferenceNode("i"),
                            new OperationNode(OperationNode.Operation.POSTDEC, new VariableReferenceNode("i")))));
        } else {
            fail();
        }
    }

    // do-while not and more complex condition in a function
    @Test
    public void testDoWhile() throws Exception {
        var program = UnitTests.parse(
                "function a(i, j,\nl) { j+=i--+l; do z(i*j-6^7); while (i > 0 && j < 10) }",
                new Token.TokenType[] {
                        Token.TokenType.FUNCTION, Token.TokenType.WORD, Token.TokenType.OPENPAREN,
                        Token.TokenType.WORD,
                        Token.TokenType.COMMA, Token.TokenType.WORD, Token.TokenType.COMMA, Token.TokenType.SEPERATOR,
                        Token.TokenType.WORD,
                        Token.TokenType.CLOSEPAREN, Token.TokenType.OPENBRACE, Token.TokenType.WORD,
                        Token.TokenType.PLUSEQUAL,
                        Token.TokenType.WORD, Token.TokenType.MINUSMINUS, Token.TokenType.PLUS, Token.TokenType.WORD,
                        Token.TokenType.SEPERATOR, Token.TokenType.DO, Token.TokenType.WORD, Token.TokenType.OPENPAREN,
                        Token.TokenType.WORD, Token.TokenType.MULTIPLY, Token.TokenType.WORD, Token.TokenType.MINUS,
                        Token.TokenType.NUMBER, Token.TokenType.EXPONENT, Token.TokenType.NUMBER,
                        Token.TokenType.CLOSEPAREN, Token.TokenType.SEPERATOR,
                        Token.TokenType.WHILE, Token.TokenType.OPENPAREN, Token.TokenType.WORD,
                        Token.TokenType.GREATERTHAN,
                        Token.TokenType.NUMBER, Token.TokenType.AND, Token.TokenType.WORD, Token.TokenType.LESSTHAN,
                        Token.TokenType.NUMBER, Token.TokenType.CLOSEPAREN, Token.TokenType.CLOSEBRACE
                }, 0, 0, 1, 0);

        var function = program.getFunctions().get(0);
        var parameters = function.getParameters();
        var block = function.getStatements();
        var assignment = block.get(0);
        var dowhileloop = block.get(1);
        assertEquals(parameters.size(), 3);
        assertEquals(parameters.get(0), "i");
        assertEquals(parameters.get(1), "j");
        assertEquals(parameters.get(2), "l");
        if (assignment instanceof AssignmentNode assign && dowhileloop instanceof DoWhileNode dowhilenode) {
            assertEquals(assign.getTarget(), new VariableReferenceNode("j"));
            assertEquals(assign.getExpression(),
                    new OperationNode(OperationNode.Operation.ADD,
                            new VariableReferenceNode("j"), new OperationNode(OperationNode.Operation.ADD,
                                    new OperationNode(OperationNode.Operation.POSTDEC,
                                            new VariableReferenceNode("i")),
                                    new VariableReferenceNode("l"))));
            assertEquals(dowhilenode.getCondition(),
                    new OperationNode(OperationNode.Operation.AND,
                            new OperationNode(OperationNode.Operation.GT,
                                    new VariableReferenceNode("i"), new ConstantNode("0")),
                            new OperationNode(OperationNode.Operation.LT,
                                    new VariableReferenceNode("j"), new ConstantNode("10"))));
            assertEquals(dowhilenode.getBlock().getStatements().size(), 1);
            assertEquals(dowhilenode.getBlock().getStatements().get(0),
                    (new FunctionCallNode("z", new LinkedList<>() {
                        {
                            add(new OperationNode(OperationNode.Operation.SUBTRACT,
                                    new OperationNode(OperationNode.Operation.MULTIPLY,
                                            new VariableReferenceNode("i"), new VariableReferenceNode("j")),
                                    new OperationNode(OperationNode.Operation.EXPONENT,
                                            new ConstantNode("6"), new ConstantNode("7"))));
                        }
                    })));
        } else {
            fail();
        }
    }

    // break and continue return and delete
    @Test
    public void ControlFlowAndDelete() throws Exception {
        var program = UnitTests.parse(
                "BEGIN {for (i = 0; i < 10; i++) if (rand(i)) break; else prints(i);} function t(i) {return --i} NF == 6 {a[0] = 5\na[$0]=$1;delete a[$0]} END {j = 6; while (j > 0) {if (j ==2) continue; prints(j) }}",
                new Token.TokenType[] {
                        Token.TokenType.BEGIN, Token.TokenType.OPENBRACE,
                        Token.TokenType.FOR, Token.TokenType.OPENPAREN,
                        Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.NUMBER,
                        Token.TokenType.SEPERATOR,
                        Token.TokenType.WORD, Token.TokenType.LESSTHAN, Token.TokenType.NUMBER,
                        Token.TokenType.SEPERATOR,
                        Token.TokenType.WORD, Token.TokenType.PLUSPLUS,
                        Token.TokenType.CLOSEPAREN,
                        Token.TokenType.IF, Token.TokenType.OPENPAREN,
                        Token.TokenType.WORD, Token.TokenType.OPENPAREN, Token.TokenType.WORD,
                        Token.TokenType.CLOSEPAREN,
                        Token.TokenType.CLOSEPAREN,
                        Token.TokenType.BREAK, Token.TokenType.SEPERATOR,
                        Token.TokenType.ELSE, Token.TokenType.WORD, Token.TokenType.OPENPAREN, Token.TokenType.WORD,
                        Token.TokenType.CLOSEPAREN,
                        Token.TokenType.SEPERATOR, Token.TokenType.CLOSEBRACE,
                        Token.TokenType.FUNCTION, Token.TokenType.WORD, Token.TokenType.OPENPAREN, Token.TokenType.WORD,
                        Token.TokenType.CLOSEPAREN, Token.TokenType.OPENBRACE,
                        Token.TokenType.RETURN, Token.TokenType.MINUSMINUS, Token.TokenType.WORD,
                        Token.TokenType.CLOSEBRACE,
                        Token.TokenType.WORD, Token.TokenType.EQUAL, Token.TokenType.NUMBER, Token.TokenType.OPENBRACE,
                        Token.TokenType.WORD, Token.TokenType.OPENBRACKET, Token.TokenType.NUMBER,
                        Token.TokenType.CLOSEBRACKET, Token.TokenType.ASSIGN, Token.TokenType.NUMBER,
                        Token.TokenType.SEPERATOR,
                        Token.TokenType.WORD, Token.TokenType.OPENBRACKET, Token.TokenType.DOLLAR,
                        Token.TokenType.NUMBER, Token.TokenType.CLOSEBRACKET, Token.TokenType.ASSIGN,
                        Token.TokenType.DOLLAR, Token.TokenType.NUMBER,
                        Token.TokenType.SEPERATOR,
                        Token.TokenType.DELETE, Token.TokenType.WORD, Token.TokenType.OPENBRACKET,
                        Token.TokenType.DOLLAR, Token.TokenType.NUMBER, Token.TokenType.CLOSEBRACKET,
                        Token.TokenType.CLOSEBRACE,
                        Token.TokenType.END, Token.TokenType.OPENBRACE,
                        Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.NUMBER, Token.TokenType.SEPERATOR,
                        Token.TokenType.WHILE, Token.TokenType.OPENPAREN, Token.TokenType.WORD,
                        Token.TokenType.GREATERTHAN, Token.TokenType.NUMBER, Token.TokenType.CLOSEPAREN,
                        Token.TokenType.OPENBRACE,
                        Token.TokenType.IF, Token.TokenType.OPENPAREN, Token.TokenType.WORD, Token.TokenType.EQUAL,
                        Token.TokenType.NUMBER, Token.TokenType.CLOSEPAREN,
                        Token.TokenType.CONTINUE, Token.TokenType.SEPERATOR,
                        Token.TokenType.WORD, Token.TokenType.OPENPAREN, Token.TokenType.WORD,
                        Token.TokenType.CLOSEPAREN,
                        Token.TokenType.CLOSEBRACE,
                        Token.TokenType.CLOSEBRACE
                }, 1, 1, 1, 1);
        var beginblock = program.getBeginBlocks().get(0);
        var function = program.getFunctions().get(0);
        var endblock = program.getEndBlocks().get(0);
        var block = program.getRestBlocks().get(0);
        // assert function parameters
        assertEquals(function.getParameters().size(), 1);
        assertEquals(function.getParameters().get(0), "i");
        // assert begin block
        assertEquals(beginblock.getStatements().size(), 1);
        var forloop = beginblock.getStatements().get(0);
        if (forloop instanceof ForNode fornode) {
            assertEquals(fornode.getCondition().get(),
                    new OperationNode(OperationNode.Operation.LT, new VariableReferenceNode("i"),
                            new ConstantNode("10")));
            assertEquals(fornode.getIncrement().get(), new OperationNode(OperationNode.Operation.POSTINC,
                    new VariableReferenceNode("i")));
            assertEquals(fornode.getBlock().getStatements().size(), 1);
            if (fornode.getBlock().getStatements().get(0) instanceof IfNode ifnode
                    && ifnode.getOtherwise().get() instanceof BlockNode elseblock) {
                assertEquals(ifnode.getCondition(), new FunctionCallNode("rand", new LinkedList<>() {
                    {
                        add(new VariableReferenceNode("i"));
                    }
                }));
                assertEquals(ifnode.getThenBlock().getStatements().size(), 1);
                assertEquals(ifnode.getThenBlock().getStatements().get(0), new BreakNode());
                assertEquals(elseblock.getStatements().size(), 1);
                assertEquals(elseblock.getStatements().get(0), new FunctionCallNode("prints", new LinkedList<>() {
                    {
                        add(new VariableReferenceNode("i"));
                    }
                }));
            } else {
                fail();
            }
        } else {
            fail();
        }
        // assert function block
        assertEquals(function.getStatements().size(), 1);
        var returnnode = function.getStatements().get(0);
        if (returnnode instanceof ReturnNode returnnode2) {
            assertEquals(returnnode2.getReturnValue().get(),
                    new OperationNode(OperationNode.Operation.PREDEC, new VariableReferenceNode("i")));
        } else {
            fail();
        }

        // assert rest block
        assertEquals(block.getStatements().size(), 3);
        var condition = block.getCondition().get();
        assertEquals(condition, new OperationNode(OperationNode.Operation.EQ,
                new VariableReferenceNode("NF"), new ConstantNode("6")));
        var assignment = block.getStatements().get(0);
        var assignment2 = block.getStatements().get(1);
        var delete = block.getStatements().get(2);
        if (assignment instanceof AssignmentNode assign && assignment2 instanceof AssignmentNode assign2
                && delete instanceof DeleteNode delete2) {
            assertEquals(assign.getTarget(), new VariableReferenceNode("a", new ConstantNode("0")));
            assertEquals(assign.getExpression(), new ConstantNode("5"));
            assertEquals(assign2.getTarget(), new VariableReferenceNode("a", new OperationNode(
                    OperationNode.Operation.DOLLAR, new ConstantNode("0"))));
            assertEquals(assign2.getExpression(), new OperationNode(OperationNode.Operation.DOLLAR,
                    new ConstantNode("1")));
            assertEquals(delete2.getArray(), new VariableReferenceNode("a", new OperationNode(
                    OperationNode.Operation.DOLLAR, new ConstantNode("0"))));
        } else {
            fail();
        }

        // assert end block
        assertEquals(endblock.getStatements().size(), 2);
        var assignment3 = endblock.getStatements().get(0);
        if (assignment3 instanceof AssignmentNode assign3) {
            assertEquals(assign3.getTarget(), new VariableReferenceNode("j"));
            assertEquals(assign3.getExpression(), new ConstantNode("6"));
        } else {
            fail();
        }
        var whileloop = endblock.getStatements().get(1);
        if (whileloop instanceof WhileNode whilenode) {
            assertEquals(whilenode.getCondition(),
                    new OperationNode(OperationNode.Operation.GT, new VariableReferenceNode("j"),
                            new ConstantNode("0")));
            assertEquals(whilenode.getBlock().getStatements().size(), 2);
            var ifnode = whilenode.getBlock().getStatements().get(0);
            if (ifnode instanceof IfNode ifnode2) {
                assertEquals(ifnode2.getCondition(),
                        new OperationNode(OperationNode.Operation.EQ, new VariableReferenceNode("j"),
                                new ConstantNode("2")));
                assertEquals(ifnode2.getThenBlock().getStatements().size(), 1);
                assertEquals(ifnode2.getThenBlock().getStatements().get(0), new ContinueNode());
            } else {
                fail();
            }
            assertEquals(whilenode.getBlock().getStatements().get(1),
                    new FunctionCallNode("prints", new LinkedList<>() {
                        {
                            add(new VariableReferenceNode("j"));
                        }
                    }));
        } else {
            fail();
        }

    }

    // TODO: failure tests ie assertfails and tests were we dont assert the output
    // but just check if it parses
    private void parseError(String input, Token.TokenType[] tokens) throws Exception {
        var parser = new Parser(UnitTests.testLexContent(input, tokens));
        assertThrows(AwkException.class, () -> parser.Parse());
    }

    @Test
    public void invalidouterexpressions() throws Exception {
        // checks that anything besides for assignemnt (includes ++,--) and
        // functioncalls fail if they are outermost thing in block
        parseError("BEGIN {1}", new Token.TokenType[] { Token.TokenType.BEGIN, Token.TokenType.OPENBRACE,
                Token.TokenType.NUMBER, Token.TokenType.CLOSEBRACE });
        // with other operators
        parseError("BEGIN {1+1}", new Token.TokenType[] { Token.TokenType.BEGIN, Token.TokenType.OPENBRACE,
                Token.TokenType.NUMBER, Token.TokenType.PLUS, Token.TokenType.NUMBER, Token.TokenType.CLOSEBRACE });
        parseError("BEGIN {1-1}", new Token.TokenType[] { Token.TokenType.BEGIN, Token.TokenType.OPENBRACE,
                Token.TokenType.NUMBER, Token.TokenType.MINUS, Token.TokenType.NUMBER, Token.TokenType.CLOSEBRACE });
        parseError("BEGIN {1*1}", new Token.TokenType[] { Token.TokenType.BEGIN, Token.TokenType.OPENBRACE,
                Token.TokenType.NUMBER, Token.TokenType.MULTIPLY, Token.TokenType.NUMBER, Token.TokenType.CLOSEBRACE });
        // also use diferent types of expressions not just one
        parseError("{a==-b}", new Token.TokenType[] { Token.TokenType.OPENBRACE, Token.TokenType.WORD,
                Token.TokenType.EQUAL, Token.TokenType.MINUS, Token.TokenType.WORD, Token.TokenType.CLOSEBRACE });
        parseError("function a() {y^-5}", new Token.TokenType[] { Token.TokenType.FUNCTION, Token.TokenType.WORD,
                Token.TokenType.OPENPAREN, Token.TokenType.CLOSEPAREN, Token.TokenType.OPENBRACE, Token.TokenType.WORD,
                Token.TokenType.EXPONENT, Token.TokenType.MINUS, Token.TokenType.NUMBER, Token.TokenType.CLOSEBRACE });
        parseError("BEGIN {a~!a+`a`}", new Token.TokenType[] { Token.TokenType.BEGIN, Token.TokenType.OPENBRACE,
                Token.TokenType.WORD, Token.TokenType.MATCH, Token.TokenType.NOT, Token.TokenType.WORD,
                Token.TokenType.PLUS, Token.TokenType.PATTERN, Token.TokenType.CLOSEBRACE });
    }

    @Test
    public void missingseporators() throws Exception {
        // stuff like return a break
        // or y++ contine
        // or return break a()
        // ...
        parseError("BEGIN {return break}", new Token.TokenType[] { Token.TokenType.BEGIN, Token.TokenType.OPENBRACE,
                Token.TokenType.RETURN, Token.TokenType.BREAK, Token.TokenType.CLOSEBRACE });

        parseError("BEGIN {return break a()}", new Token.TokenType[] { Token.TokenType.BEGIN, Token.TokenType.OPENBRACE,
                Token.TokenType.RETURN, Token.TokenType.BREAK, Token.TokenType.WORD, Token.TokenType.OPENPAREN,
                Token.TokenType.CLOSEPAREN, Token.TokenType.CLOSEBRACE });

        parseError("BEGIN {continue break}", new Token.TokenType[] { Token.TokenType.BEGIN, Token.TokenType.OPENBRACE,
                Token.TokenType.CONTINUE, Token.TokenType.BREAK, Token.TokenType.CLOSEBRACE });

        parseError("BEGIN {continue while (a) {}}", new Token.TokenType[] {
                Token.TokenType.BEGIN, Token.TokenType.OPENBRACE,
                Token.TokenType.CONTINUE, Token.TokenType.WHILE, Token.TokenType.OPENPAREN, Token.TokenType.WORD,
                Token.TokenType.CLOSEPAREN, Token.TokenType.OPENBRACE, Token.TokenType.CLOSEBRACE,
                Token.TokenType.CLOSEBRACE });

        // also single line blocks
        parseError("a== 6{ while (true) puts(a) y++}", new Token.TokenType[] {
                Token.TokenType.WORD, Token.TokenType.EQUAL, Token.TokenType.NUMBER, Token.TokenType.OPENBRACE,
                Token.TokenType.WHILE, Token.TokenType.OPENPAREN, Token.TokenType.WORD, Token.TokenType.CLOSEPAREN,
                Token.TokenType.WORD, Token.TokenType.OPENPAREN, Token.TokenType.WORD, Token.TokenType.CLOSEPAREN,
                Token.TokenType.WORD, Token.TokenType.PLUSPLUS, Token.TokenType.CLOSEBRACE });

        parseError("NF  != 7 {if (false) return x else {}}", new Token.TokenType[] {
                Token.TokenType.WORD, Token.TokenType.NOTEQUAL, Token.TokenType.NUMBER, Token.TokenType.OPENBRACE,
                Token.TokenType.IF, Token.TokenType.OPENPAREN, Token.TokenType.WORD, Token.TokenType.CLOSEPAREN,
                Token.TokenType.RETURN, Token.TokenType.WORD, Token.TokenType.ELSE, Token.TokenType.OPENBRACE,
                Token.TokenType.CLOSEBRACE, Token.TokenType.CLOSEBRACE });

        parseError("END {do --x while (x < 10) a++} ", new Token.TokenType[] {
                Token.TokenType.END, Token.TokenType.OPENBRACE,
                Token.TokenType.DO, Token.TokenType.MINUSMINUS, Token.TokenType.WORD, Token.TokenType.WHILE,
                Token.TokenType.OPENPAREN, Token.TokenType.WORD, Token.TokenType.LESSTHAN, Token.TokenType.NUMBER,
                Token.TokenType.CLOSEPAREN, Token.TokenType.WORD, Token.TokenType.PLUSPLUS,
                Token.TokenType.CLOSEBRACE });
    }

    @Test
    public void singlelineinoutrerblock() throws Exception {
        // makes sure the error are thrown if there are single line block for begin end,
        // other and function blocks
        parseError("BEGIN 1", new Token.TokenType[] { Token.TokenType.BEGIN, Token.TokenType.NUMBER });
        parseError("END --a;", new Token.TokenType[] { Token.TokenType.END, Token.TokenType.MINUSMINUS,
                Token.TokenType.WORD, Token.TokenType.SEPERATOR });
        parseError("function a(i, b) return i", new Token.TokenType[] { Token.TokenType.FUNCTION, Token.TokenType.WORD,
                Token.TokenType.OPENPAREN, Token.TokenType.WORD, Token.TokenType.COMMA, Token.TokenType.WORD,
                Token.TokenType.CLOSEPAREN, Token.TokenType.RETURN, Token.TokenType.WORD });
        parseError("NR == 6 continue", new Token.TokenType[] { Token.TokenType.WORD, Token.TokenType.EQUAL,
                Token.TokenType.NUMBER, Token.TokenType.CONTINUE });
        parseError("while (true) {if (false) return x else {}}", new Token.TokenType[] {
                Token.TokenType.WHILE, Token.TokenType.OPENPAREN, Token.TokenType.WORD, Token.TokenType.CLOSEPAREN,
                Token.TokenType.OPENBRACE, Token.TokenType.IF, Token.TokenType.OPENPAREN, Token.TokenType.WORD,
                Token.TokenType.CLOSEPAREN, Token.TokenType.RETURN, Token.TokenType.WORD, Token.TokenType.ELSE,
                Token.TokenType.OPENBRACE, Token.TokenType.CLOSEBRACE, Token.TokenType.CLOSEBRACE });

        // all in one
        parseError("BEGIN 3+4; END 5+=1-5 function a(b, c) return b ? c : 0; bnf == 6 return 5;;;break",
                new Token.TokenType[] {
                        Token.TokenType.BEGIN, Token.TokenType.NUMBER, Token.TokenType.PLUS, Token.TokenType.NUMBER,
                        Token.TokenType.SEPERATOR, Token.TokenType.END, Token.TokenType.NUMBER,
                        Token.TokenType.PLUSEQUAL, Token.TokenType.NUMBER, Token.TokenType.MINUS,
                        Token.TokenType.NUMBER,
                        Token.TokenType.FUNCTION, Token.TokenType.WORD,
                        Token.TokenType.OPENPAREN, Token.TokenType.WORD, Token.TokenType.COMMA, Token.TokenType.WORD,
                        Token.TokenType.CLOSEPAREN, Token.TokenType.RETURN, Token.TokenType.WORD,
                        Token.TokenType.QUESTION,
                        Token.TokenType.WORD, Token.TokenType.COLON, Token.TokenType.NUMBER, Token.TokenType.SEPERATOR,
                        Token.TokenType.WORD, Token.TokenType.EQUAL, Token.TokenType.NUMBER,
                        Token.TokenType.RETURN, Token.TokenType.NUMBER, Token.TokenType.SEPERATOR,
                        Token.TokenType.SEPERATOR, Token.TokenType.SEPERATOR, Token.TokenType.BREAK
                });
    }

    @Test
    public void invalidconstuctsignature() throws Exception {
        // tests to make sure UnitTests.parse throws error if the signature of a
        // construct is
        // invalid
        // ie if the condition of a while loop empty or if the condition of one of them
        // has no parenthesis
        // that do while condition is at end, that for loop doesnt have two seperators
        // that delete doesnt have a target
        parseError("BEGIN {while () {}}", new Token.TokenType[] { Token.TokenType.BEGIN, Token.TokenType.OPENBRACE,
                Token.TokenType.WHILE, Token.TokenType.OPENPAREN, Token.TokenType.CLOSEPAREN,
                Token.TokenType.OPENBRACE, Token.TokenType.CLOSEBRACE, Token.TokenType.CLOSEBRACE });
        parseError("{for i in a {}}", new Token.TokenType[] { Token.TokenType.OPENBRACE, Token.TokenType.FOR,
                Token.TokenType.WORD, Token.TokenType.IN, Token.TokenType.WORD, Token.TokenType.OPENBRACE,
                Token.TokenType.CLOSEBRACE, Token.TokenType.CLOSEBRACE });
        parseError("BEGIN {do {} while}", new Token.TokenType[] { Token.TokenType.BEGIN, Token.TokenType.OPENBRACE,
                Token.TokenType.DO, Token.TokenType.OPENBRACE, Token.TokenType.CLOSEBRACE, Token.TokenType.WHILE,
                Token.TokenType.CLOSEBRACE });
        parseError("END {delete}", new Token.TokenType[] { Token.TokenType.END, Token.TokenType.OPENBRACE,
                Token.TokenType.DELETE, Token.TokenType.CLOSEBRACE });
        parseError("function a() {for (i;j)  {}}", new Token.TokenType[] { Token.TokenType.FUNCTION,
                Token.TokenType.WORD,
                Token.TokenType.OPENPAREN, Token.TokenType.CLOSEPAREN, Token.TokenType.OPENBRACE, Token.TokenType.FOR,
                Token.TokenType.OPENPAREN, Token.TokenType.WORD, Token.TokenType.SEPERATOR, Token.TokenType.WORD,
                Token.TokenType.CLOSEPAREN, Token.TokenType.OPENBRACE, Token.TokenType.CLOSEBRACE,
                Token.TokenType.CLOSEBRACE });

        parseError("NS == t {if {} else {}}", new Token.TokenType[] { Token.TokenType.WORD, Token.TokenType.EQUAL,
                Token.TokenType.WORD, Token.TokenType.OPENBRACE, Token.TokenType.IF, Token.TokenType.OPENBRACE,
                Token.TokenType.CLOSEBRACE, Token.TokenType.ELSE, Token.TokenType.OPENBRACE,
                Token.TokenType.CLOSEBRACE, Token.TokenType.CLOSEBRACE });

        parseError("BEGIN {if (true) {} else if t {}}", new Token.TokenType[] {
                Token.TokenType.BEGIN, Token.TokenType.OPENBRACE,
                Token.TokenType.IF, Token.TokenType.OPENPAREN, Token.TokenType.WORD, Token.TokenType.CLOSEPAREN,
                Token.TokenType.OPENBRACE, Token.TokenType.CLOSEBRACE, Token.TokenType.ELSE, Token.TokenType.IF,
                Token.TokenType.WORD, Token.TokenType.OPENBRACE, Token.TokenType.CLOSEBRACE, Token.TokenType.CLOSEBRACE
        });
    }

    @Test
    public void shouldparse() throws Exception {
        var parser = new Parser(UnitTests.testLexContent("function dos(a,\n" + //
                "b) {\n" + //
                "\treturn a;\n" + //
                "}\n" + //
                "BEGIN {\n" + //
                "   # print \"Welcome to JDoodle!\"\n" + //
                "   z[0] = 6\n" + //
                "   z[1] = 6\n" + //
                "   # z[0][1] = dos\n" + //
                "   a = 5\n" + //
                "   b= -7\n" + //
                "   b-=5 + (z[1] += 55)\n" + //
                "   asdf = 0?  a+=7 : b = 6\n" + //
                "   # means bacicly any operation can do mutation/assignent\n" + //
                "   v = a ? v ~ (p = `4`) : 0\n" + //
                "\n" + //
                "\tdos(2, 4)\n" + //
                "   # for (;;) {}\n" + //
                "   # while () {}\n" + //
                "#    for (6; 86 ;9) {print 6}\n" + //
                "#    do  {++x} while (x)\n" + //
                "}\n" + //
                "\n" + //
                "{y+=1;}\n" + //
                "\n" + //
                "y {\n" + //
                "    while(!x)   # Comment\n" + //
                "        if (++t) { y--;}\n" + //
                "        else { --x;}\n" + //
                "    --y;\n" + //
                "}\n" + //
                "\n" + //
                "NR!=1 {\n" + //
                "    b(z?d:e);\n" + //
                "\n" + //
                "    if (c=c=x&&y||!z) { \n" + //
                "        for(x-=a[a[1]||1]||(z&&0.0); c; --c) {\n" + //
                "            z = 1; # Comment\n" + //
                "        }\n" + //
                "        #`0`\n" + //
                "    }\n" + //
                "}\n" + //
                "\n" + //
                "BEGIN {\n" + //
                "    x = 1;\n" + //
                "    y = 2;\n" + //
                "    z = 0;\n" + //
                "}\n" + //
                "\n" + //
                "{\n" + //
                "    y += 1;\n" + //
                "    if (NR % 2 == 0) {\n" + //
                "        while (x) {\n" + //
                "            if (--y) {\n" + //
                "                for (i = 0; i < 5; i++) {\n" + //
                "                    z += i;\n" + //
                "                }\n" + //
                "            } else {\n" + //
                "                do {\n" + //
                "                    z = z ? z : 0; # Nested comment\n" + //
                "                } while (x && y);\n" + //
                "            }\n" + //
                "        }\n" + //
                "    }\n" + //
                "}\n" + //
                "\n" + //
                "END {\n" + //
                "    a = 1;\n" + //
                "    b = 2;\n" + //
                "    if (c = a && b || !z) {\n" + //
                "        for (x -= a[a[1] || 1] || (z && 0.0); c; --c) {\n" + //
                "            z = 1; # Set z to 1\n" + //
                "        }\n" + //
                "        #`0` - Comment\n" + //
                "    }\n" + //
                "}\n" + //
                "\n" + //
                "BEGIN {\n" + //
                "    x = 0;\n" + //
                "    y = 1;\n" + //
                "}\n" + //
                "\n" + //
                "{\n" + //
                "    y += 1;\n" + //
                "    if (NR % 3 == 0) {\n" + //
                "        do {\n" + //
                "            x = x ? x : 1; # Comment\n" + //
                "        } while (y && x);\n" + //
                "    } else {\n" + //
                "        for (i = 1; i <= 5; i++) {\n" + //
                "            z *= i;\n" + //
                "        }\n" + //
                "    }\n" + //
                "}\n" + //
                "\n" + //
                "END {\n" + //
                "    if (y > 10) {\n" + //
                "        b = 3;\n" + //
                "        c = 2;\n" + //
                "        if (c == 2 && b != 4) {\n" + //
                "            while (x) {\n" + //
                "                for (j = 1; j <= 3; j++) {\n" + //
                "                    z += j;\n" + //
                "                }\n" + //
                "            }\n" + //
                "        }\n" + //
                "        #`0` - Comment\n" + //
                "    } else {\n" + //
                "        d = 5;\n" + //
                "        e = 6;\n" + //
                "        if (d || e) {\n" + //
                "            for (x -= 4; x; x++) {\n" + //
                "                z = 1; # Set z to 1\n" + //
                "            }\n" + //
                "        }\n" + //
                "    }\n" + //
                "}\n" + //
                "{a=1;b=2;c=3;d=4;e=5;f=6;g=7;h=8;i=9;j=10;k=11;l=12;m=13;n=14;o=15;p=16;q=17;r=18;s=19;t=20;u=21;v=22;w=23;x=24;y=25;z=26}\n"
                + //
                "END{if(a==1){if(b==2){if(c==3){if(d==4){if(e==5){if(f==6){if(g==7){if(h==8){if(i==9){if(j==10){if(k==11){if(l==12){if(m==13){if(n==14){if(o==15){if(p==16){if(q==17){if(r==18){if(s==19){if(t==20){if(u==21){if(v==22){if(w==23){if(x==24){if(y==25){if(z==26){printg( \"abcdefghijklmnopqrstuvwxyz\")}}}}}}}}}}}}}}}}}}}}}}}}}}}\n"
                + //
                "\n" + //
                "function factorial(n) {\n" + //
                "    if (n == 0) {\n" + //
                "        return 1;\n" + //
                "    } else {\n" + //
                "        return n * factorial(n - 1);\n" + //
                "    }\n" + //
                "}\n" + //
                "\n" + //
                "BEGIN {\n" + //
                "    x = 5;\n" + //
                "    printg( \"Factorial of \" x \" is \" factorial(x));\n" + //
                "}\n" + //
                "\n" + //
                "BEGIN {\n" + //
                "    x = -5;\n" + //
                "    sign = x > 0 ? 1 : x < 0 ? -1 : 0;\n" + //
                "    printff(\"The sign of \" x \" is \" sign);\n" + //
                "}\n" + //
                "\n" + //
                "BEGIN {\n" + //
                "    a = b = c = d = 1\n" + //
                "    A = a\n" + //
                "    B = b\n" + //
                "    while (A) {\n" + //
                "        if (B) {\n" + //
                "            a--\n" + //
                "            B--\n" + //
                "        } else {\n" + //
                "            b++\n" + //
                "            B++\n" + //
                "        }\n" + //
                "        A--\n" + //
                "    }\n" + //
                "    c = B ? c-- : c\n" + //
                "    d = A ? d : d++\n" + //
                "\n" + //
                "}",
                new Token.TokenType[] {
                        Token.TokenType.FUNCTION, Token.TokenType.WORD, Token.TokenType.OPENPAREN, Token.TokenType.WORD,
                        Token.TokenType.COMMA, Token.TokenType.SEPERATOR, Token.TokenType.WORD,
                        Token.TokenType.CLOSEPAREN, Token.TokenType.OPENBRACE, Token.TokenType.SEPERATOR,
                        Token.TokenType.RETURN, Token.TokenType.WORD, Token.TokenType.SEPERATOR,
                        Token.TokenType.SEPERATOR, Token.TokenType.CLOSEBRACE, Token.TokenType.SEPERATOR,
                        Token.TokenType.BEGIN, Token.TokenType.OPENBRACE, Token.TokenType.SEPERATOR,
                        Token.TokenType.SEPERATOR, Token.TokenType.WORD, Token.TokenType.OPENBRACKET,
                        Token.TokenType.NUMBER, Token.TokenType.CLOSEBRACKET, Token.TokenType.ASSIGN,
                        Token.TokenType.NUMBER, Token.TokenType.SEPERATOR, Token.TokenType.WORD,
                        Token.TokenType.OPENBRACKET, Token.TokenType.NUMBER, Token.TokenType.CLOSEBRACKET,
                        Token.TokenType.ASSIGN, Token.TokenType.NUMBER, Token.TokenType.SEPERATOR,
                        Token.TokenType.SEPERATOR, Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.NUMBER,
                        Token.TokenType.SEPERATOR, Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.MINUS,
                        Token.TokenType.NUMBER, Token.TokenType.SEPERATOR, Token.TokenType.WORD,
                        Token.TokenType.MINUSEQUAL, Token.TokenType.NUMBER, Token.TokenType.PLUS,
                        Token.TokenType.OPENPAREN, Token.TokenType.WORD, Token.TokenType.OPENBRACKET,
                        Token.TokenType.NUMBER, Token.TokenType.CLOSEBRACKET, Token.TokenType.PLUSEQUAL,
                        Token.TokenType.NUMBER, Token.TokenType.CLOSEPAREN, Token.TokenType.SEPERATOR,
                        Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.NUMBER, Token.TokenType.QUESTION,
                        Token.TokenType.WORD, Token.TokenType.PLUSEQUAL, Token.TokenType.NUMBER, Token.TokenType.COLON,
                        Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.NUMBER, Token.TokenType.SEPERATOR,
                        Token.TokenType.SEPERATOR, Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.WORD,
                        Token.TokenType.QUESTION, Token.TokenType.WORD, Token.TokenType.MATCH,
                        Token.TokenType.OPENPAREN, Token.TokenType.WORD, Token.TokenType.ASSIGN,
                        Token.TokenType.PATTERN, Token.TokenType.CLOSEPAREN, Token.TokenType.COLON,
                        Token.TokenType.NUMBER, Token.TokenType.SEPERATOR, Token.TokenType.SEPERATOR,
                        Token.TokenType.WORD, Token.TokenType.OPENPAREN, Token.TokenType.NUMBER, Token.TokenType.COMMA,
                        Token.TokenType.NUMBER, Token.TokenType.CLOSEPAREN, Token.TokenType.SEPERATOR,
                        Token.TokenType.SEPERATOR, Token.TokenType.SEPERATOR, Token.TokenType.SEPERATOR,
                        Token.TokenType.SEPERATOR, Token.TokenType.CLOSEBRACE, Token.TokenType.SEPERATOR,
                        Token.TokenType.SEPERATOR, Token.TokenType.OPENBRACE, Token.TokenType.WORD,
                        Token.TokenType.PLUSEQUAL, Token.TokenType.NUMBER, Token.TokenType.SEPERATOR,
                        Token.TokenType.CLOSEBRACE, Token.TokenType.SEPERATOR, Token.TokenType.SEPERATOR,
                        Token.TokenType.WORD, Token.TokenType.OPENBRACE, Token.TokenType.SEPERATOR,
                        Token.TokenType.WHILE, Token.TokenType.OPENPAREN, Token.TokenType.NOT, Token.TokenType.WORD,
                        Token.TokenType.CLOSEPAREN, Token.TokenType.SEPERATOR, Token.TokenType.IF,
                        Token.TokenType.OPENPAREN, Token.TokenType.PLUSPLUS, Token.TokenType.WORD,
                        Token.TokenType.CLOSEPAREN, Token.TokenType.OPENBRACE, Token.TokenType.WORD,
                        Token.TokenType.MINUSMINUS, Token.TokenType.SEPERATOR, Token.TokenType.CLOSEBRACE,
                        Token.TokenType.SEPERATOR, Token.TokenType.ELSE, Token.TokenType.OPENBRACE,
                        Token.TokenType.MINUSMINUS, Token.TokenType.WORD, Token.TokenType.SEPERATOR,
                        Token.TokenType.CLOSEBRACE, Token.TokenType.SEPERATOR, Token.TokenType.MINUSMINUS,
                        Token.TokenType.WORD, Token.TokenType.SEPERATOR, Token.TokenType.SEPERATOR,
                        Token.TokenType.CLOSEBRACE, Token.TokenType.SEPERATOR, Token.TokenType.SEPERATOR,
                        Token.TokenType.WORD, Token.TokenType.NOTEQUAL, Token.TokenType.NUMBER,
                        Token.TokenType.OPENBRACE, Token.TokenType.SEPERATOR, Token.TokenType.WORD,
                        Token.TokenType.OPENPAREN, Token.TokenType.WORD, Token.TokenType.QUESTION, Token.TokenType.WORD,
                        Token.TokenType.COLON, Token.TokenType.WORD, Token.TokenType.CLOSEPAREN,
                        Token.TokenType.SEPERATOR, Token.TokenType.SEPERATOR, Token.TokenType.SEPERATOR,
                        Token.TokenType.IF, Token.TokenType.OPENPAREN, Token.TokenType.WORD, Token.TokenType.ASSIGN,
                        Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.WORD, Token.TokenType.AND,
                        Token.TokenType.WORD, Token.TokenType.OR, Token.TokenType.NOT, Token.TokenType.WORD,
                        Token.TokenType.CLOSEPAREN, Token.TokenType.OPENBRACE, Token.TokenType.SEPERATOR,
                        Token.TokenType.FOR, Token.TokenType.OPENPAREN, Token.TokenType.WORD,
                        Token.TokenType.MINUSEQUAL, Token.TokenType.WORD, Token.TokenType.OPENBRACKET,
                        Token.TokenType.WORD, Token.TokenType.OPENBRACKET, Token.TokenType.NUMBER,
                        Token.TokenType.CLOSEBRACKET, Token.TokenType.OR, Token.TokenType.NUMBER,
                        Token.TokenType.CLOSEBRACKET, Token.TokenType.OR, Token.TokenType.OPENPAREN,
                        Token.TokenType.WORD, Token.TokenType.AND, Token.TokenType.NUMBER, Token.TokenType.CLOSEPAREN,
                        Token.TokenType.SEPERATOR, Token.TokenType.WORD, Token.TokenType.SEPERATOR,
                        Token.TokenType.MINUSMINUS, Token.TokenType.WORD, Token.TokenType.CLOSEPAREN,
                        Token.TokenType.OPENBRACE, Token.TokenType.SEPERATOR, Token.TokenType.WORD,
                        Token.TokenType.ASSIGN, Token.TokenType.NUMBER, Token.TokenType.SEPERATOR,
                        Token.TokenType.SEPERATOR, Token.TokenType.CLOSEBRACE, Token.TokenType.SEPERATOR,
                        Token.TokenType.SEPERATOR, Token.TokenType.CLOSEBRACE, Token.TokenType.SEPERATOR,
                        Token.TokenType.CLOSEBRACE, Token.TokenType.SEPERATOR, Token.TokenType.SEPERATOR,
                        Token.TokenType.BEGIN, Token.TokenType.OPENBRACE, Token.TokenType.SEPERATOR,
                        Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.NUMBER, Token.TokenType.SEPERATOR,
                        Token.TokenType.SEPERATOR, Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.NUMBER,
                        Token.TokenType.SEPERATOR, Token.TokenType.SEPERATOR, Token.TokenType.WORD,
                        Token.TokenType.ASSIGN, Token.TokenType.NUMBER, Token.TokenType.SEPERATOR,
                        Token.TokenType.SEPERATOR, Token.TokenType.CLOSEBRACE, Token.TokenType.SEPERATOR,
                        Token.TokenType.SEPERATOR, Token.TokenType.OPENBRACE, Token.TokenType.SEPERATOR,
                        Token.TokenType.WORD, Token.TokenType.PLUSEQUAL, Token.TokenType.NUMBER,
                        Token.TokenType.SEPERATOR, Token.TokenType.SEPERATOR, Token.TokenType.IF,
                        Token.TokenType.OPENPAREN, Token.TokenType.WORD, Token.TokenType.MODULO, Token.TokenType.NUMBER,
                        Token.TokenType.EQUAL, Token.TokenType.NUMBER, Token.TokenType.CLOSEPAREN,
                        Token.TokenType.OPENBRACE, Token.TokenType.SEPERATOR, Token.TokenType.WHILE,
                        Token.TokenType.OPENPAREN, Token.TokenType.WORD, Token.TokenType.CLOSEPAREN,
                        Token.TokenType.OPENBRACE, Token.TokenType.SEPERATOR, Token.TokenType.IF,
                        Token.TokenType.OPENPAREN, Token.TokenType.MINUSMINUS, Token.TokenType.WORD,
                        Token.TokenType.CLOSEPAREN, Token.TokenType.OPENBRACE, Token.TokenType.SEPERATOR,
                        Token.TokenType.FOR, Token.TokenType.OPENPAREN, Token.TokenType.WORD, Token.TokenType.ASSIGN,
                        Token.TokenType.NUMBER, Token.TokenType.SEPERATOR, Token.TokenType.WORD,
                        Token.TokenType.LESSTHAN, Token.TokenType.NUMBER, Token.TokenType.SEPERATOR,
                        Token.TokenType.WORD, Token.TokenType.PLUSPLUS, Token.TokenType.CLOSEPAREN,
                        Token.TokenType.OPENBRACE, Token.TokenType.SEPERATOR, Token.TokenType.WORD,
                        Token.TokenType.PLUSEQUAL, Token.TokenType.WORD, Token.TokenType.SEPERATOR,
                        Token.TokenType.SEPERATOR, Token.TokenType.CLOSEBRACE, Token.TokenType.SEPERATOR,
                        Token.TokenType.CLOSEBRACE, Token.TokenType.ELSE, Token.TokenType.OPENBRACE,
                        Token.TokenType.SEPERATOR, Token.TokenType.DO, Token.TokenType.OPENBRACE,
                        Token.TokenType.SEPERATOR, Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.WORD,
                        Token.TokenType.QUESTION, Token.TokenType.WORD, Token.TokenType.COLON, Token.TokenType.NUMBER,
                        Token.TokenType.SEPERATOR, Token.TokenType.SEPERATOR, Token.TokenType.CLOSEBRACE,
                        Token.TokenType.WHILE, Token.TokenType.OPENPAREN, Token.TokenType.WORD, Token.TokenType.AND,
                        Token.TokenType.WORD, Token.TokenType.CLOSEPAREN, Token.TokenType.SEPERATOR,
                        Token.TokenType.SEPERATOR, Token.TokenType.CLOSEBRACE, Token.TokenType.SEPERATOR,
                        Token.TokenType.CLOSEBRACE, Token.TokenType.SEPERATOR, Token.TokenType.CLOSEBRACE,
                        Token.TokenType.SEPERATOR, Token.TokenType.CLOSEBRACE, Token.TokenType.SEPERATOR,
                        Token.TokenType.SEPERATOR, Token.TokenType.END, Token.TokenType.OPENBRACE,
                        Token.TokenType.SEPERATOR, Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.NUMBER,
                        Token.TokenType.SEPERATOR, Token.TokenType.SEPERATOR, Token.TokenType.WORD,
                        Token.TokenType.ASSIGN, Token.TokenType.NUMBER, Token.TokenType.SEPERATOR,
                        Token.TokenType.SEPERATOR, Token.TokenType.IF, Token.TokenType.OPENPAREN, Token.TokenType.WORD,
                        Token.TokenType.ASSIGN, Token.TokenType.WORD, Token.TokenType.AND, Token.TokenType.WORD,
                        Token.TokenType.OR, Token.TokenType.NOT, Token.TokenType.WORD, Token.TokenType.CLOSEPAREN,
                        Token.TokenType.OPENBRACE, Token.TokenType.SEPERATOR, Token.TokenType.FOR,
                        Token.TokenType.OPENPAREN, Token.TokenType.WORD, Token.TokenType.MINUSEQUAL,
                        Token.TokenType.WORD, Token.TokenType.OPENBRACKET, Token.TokenType.WORD,
                        Token.TokenType.OPENBRACKET, Token.TokenType.NUMBER, Token.TokenType.CLOSEBRACKET,
                        Token.TokenType.OR, Token.TokenType.NUMBER, Token.TokenType.CLOSEBRACKET, Token.TokenType.OR,
                        Token.TokenType.OPENPAREN, Token.TokenType.WORD, Token.TokenType.AND, Token.TokenType.NUMBER,
                        Token.TokenType.CLOSEPAREN, Token.TokenType.SEPERATOR, Token.TokenType.WORD,
                        Token.TokenType.SEPERATOR, Token.TokenType.MINUSMINUS, Token.TokenType.WORD,
                        Token.TokenType.CLOSEPAREN, Token.TokenType.OPENBRACE, Token.TokenType.SEPERATOR,
                        Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.NUMBER, Token.TokenType.SEPERATOR,
                        Token.TokenType.SEPERATOR, Token.TokenType.CLOSEBRACE, Token.TokenType.SEPERATOR,
                        Token.TokenType.SEPERATOR, Token.TokenType.CLOSEBRACE, Token.TokenType.SEPERATOR,
                        Token.TokenType.CLOSEBRACE, Token.TokenType.SEPERATOR, Token.TokenType.SEPERATOR,
                        Token.TokenType.BEGIN, Token.TokenType.OPENBRACE, Token.TokenType.SEPERATOR,
                        Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.NUMBER, Token.TokenType.SEPERATOR,
                        Token.TokenType.SEPERATOR, Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.NUMBER,
                        Token.TokenType.SEPERATOR, Token.TokenType.SEPERATOR, Token.TokenType.CLOSEBRACE,
                        Token.TokenType.SEPERATOR, Token.TokenType.SEPERATOR, Token.TokenType.OPENBRACE,
                        Token.TokenType.SEPERATOR, Token.TokenType.WORD, Token.TokenType.PLUSEQUAL,
                        Token.TokenType.NUMBER, Token.TokenType.SEPERATOR, Token.TokenType.SEPERATOR,
                        Token.TokenType.IF, Token.TokenType.OPENPAREN, Token.TokenType.WORD, Token.TokenType.MODULO,
                        Token.TokenType.NUMBER, Token.TokenType.EQUAL, Token.TokenType.NUMBER,
                        Token.TokenType.CLOSEPAREN, Token.TokenType.OPENBRACE, Token.TokenType.SEPERATOR,
                        Token.TokenType.DO, Token.TokenType.OPENBRACE, Token.TokenType.SEPERATOR, Token.TokenType.WORD,
                        Token.TokenType.ASSIGN, Token.TokenType.WORD, Token.TokenType.QUESTION, Token.TokenType.WORD,
                        Token.TokenType.COLON, Token.TokenType.NUMBER, Token.TokenType.SEPERATOR,
                        Token.TokenType.SEPERATOR, Token.TokenType.CLOSEBRACE, Token.TokenType.WHILE,
                        Token.TokenType.OPENPAREN, Token.TokenType.WORD, Token.TokenType.AND, Token.TokenType.WORD,
                        Token.TokenType.CLOSEPAREN, Token.TokenType.SEPERATOR, Token.TokenType.SEPERATOR,
                        Token.TokenType.CLOSEBRACE, Token.TokenType.ELSE, Token.TokenType.OPENBRACE,
                        Token.TokenType.SEPERATOR, Token.TokenType.FOR, Token.TokenType.OPENPAREN, Token.TokenType.WORD,
                        Token.TokenType.ASSIGN, Token.TokenType.NUMBER, Token.TokenType.SEPERATOR, Token.TokenType.WORD,
                        Token.TokenType.LESSTHANEQUAL, Token.TokenType.NUMBER, Token.TokenType.SEPERATOR,
                        Token.TokenType.WORD, Token.TokenType.PLUSPLUS, Token.TokenType.CLOSEPAREN,
                        Token.TokenType.OPENBRACE, Token.TokenType.SEPERATOR, Token.TokenType.WORD,
                        Token.TokenType.MULTIPLYEQUAL, Token.TokenType.WORD, Token.TokenType.SEPERATOR,
                        Token.TokenType.SEPERATOR, Token.TokenType.CLOSEBRACE, Token.TokenType.SEPERATOR,
                        Token.TokenType.CLOSEBRACE, Token.TokenType.SEPERATOR, Token.TokenType.CLOSEBRACE,
                        Token.TokenType.SEPERATOR, Token.TokenType.SEPERATOR, Token.TokenType.END,
                        Token.TokenType.OPENBRACE, Token.TokenType.SEPERATOR, Token.TokenType.IF,
                        Token.TokenType.OPENPAREN, Token.TokenType.WORD, Token.TokenType.GREATERTHAN,
                        Token.TokenType.NUMBER, Token.TokenType.CLOSEPAREN, Token.TokenType.OPENBRACE,
                        Token.TokenType.SEPERATOR, Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.NUMBER,
                        Token.TokenType.SEPERATOR, Token.TokenType.SEPERATOR, Token.TokenType.WORD,
                        Token.TokenType.ASSIGN, Token.TokenType.NUMBER, Token.TokenType.SEPERATOR,
                        Token.TokenType.SEPERATOR, Token.TokenType.IF, Token.TokenType.OPENPAREN, Token.TokenType.WORD,
                        Token.TokenType.EQUAL, Token.TokenType.NUMBER, Token.TokenType.AND, Token.TokenType.WORD,
                        Token.TokenType.NOTEQUAL, Token.TokenType.NUMBER, Token.TokenType.CLOSEPAREN,
                        Token.TokenType.OPENBRACE, Token.TokenType.SEPERATOR, Token.TokenType.WHILE,
                        Token.TokenType.OPENPAREN, Token.TokenType.WORD, Token.TokenType.CLOSEPAREN,
                        Token.TokenType.OPENBRACE, Token.TokenType.SEPERATOR, Token.TokenType.FOR,
                        Token.TokenType.OPENPAREN, Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.NUMBER,
                        Token.TokenType.SEPERATOR, Token.TokenType.WORD, Token.TokenType.LESSTHANEQUAL,
                        Token.TokenType.NUMBER, Token.TokenType.SEPERATOR, Token.TokenType.WORD,
                        Token.TokenType.PLUSPLUS, Token.TokenType.CLOSEPAREN, Token.TokenType.OPENBRACE,
                        Token.TokenType.SEPERATOR, Token.TokenType.WORD, Token.TokenType.PLUSEQUAL,
                        Token.TokenType.WORD, Token.TokenType.SEPERATOR, Token.TokenType.SEPERATOR,
                        Token.TokenType.CLOSEBRACE, Token.TokenType.SEPERATOR, Token.TokenType.CLOSEBRACE,
                        Token.TokenType.SEPERATOR, Token.TokenType.CLOSEBRACE, Token.TokenType.SEPERATOR,
                        Token.TokenType.SEPERATOR, Token.TokenType.CLOSEBRACE, Token.TokenType.ELSE,
                        Token.TokenType.OPENBRACE, Token.TokenType.SEPERATOR, Token.TokenType.WORD,
                        Token.TokenType.ASSIGN, Token.TokenType.NUMBER, Token.TokenType.SEPERATOR,
                        Token.TokenType.SEPERATOR, Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.NUMBER,
                        Token.TokenType.SEPERATOR, Token.TokenType.SEPERATOR, Token.TokenType.IF,
                        Token.TokenType.OPENPAREN, Token.TokenType.WORD, Token.TokenType.OR, Token.TokenType.WORD,
                        Token.TokenType.CLOSEPAREN, Token.TokenType.OPENBRACE, Token.TokenType.SEPERATOR,
                        Token.TokenType.FOR, Token.TokenType.OPENPAREN, Token.TokenType.WORD,
                        Token.TokenType.MINUSEQUAL, Token.TokenType.NUMBER, Token.TokenType.SEPERATOR,
                        Token.TokenType.WORD, Token.TokenType.SEPERATOR, Token.TokenType.WORD, Token.TokenType.PLUSPLUS,
                        Token.TokenType.CLOSEPAREN, Token.TokenType.OPENBRACE, Token.TokenType.SEPERATOR,
                        Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.NUMBER, Token.TokenType.SEPERATOR,
                        Token.TokenType.SEPERATOR, Token.TokenType.CLOSEBRACE, Token.TokenType.SEPERATOR,
                        Token.TokenType.CLOSEBRACE, Token.TokenType.SEPERATOR, Token.TokenType.CLOSEBRACE,
                        Token.TokenType.SEPERATOR, Token.TokenType.CLOSEBRACE, Token.TokenType.SEPERATOR,
                        Token.TokenType.OPENBRACE, Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.NUMBER,
                        Token.TokenType.SEPERATOR, Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.NUMBER,
                        Token.TokenType.SEPERATOR, Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.NUMBER,
                        Token.TokenType.SEPERATOR, Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.NUMBER,
                        Token.TokenType.SEPERATOR, Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.NUMBER,
                        Token.TokenType.SEPERATOR, Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.NUMBER,
                        Token.TokenType.SEPERATOR, Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.NUMBER,
                        Token.TokenType.SEPERATOR, Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.NUMBER,
                        Token.TokenType.SEPERATOR, Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.NUMBER,
                        Token.TokenType.SEPERATOR, Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.NUMBER,
                        Token.TokenType.SEPERATOR, Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.NUMBER,
                        Token.TokenType.SEPERATOR, Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.NUMBER,
                        Token.TokenType.SEPERATOR, Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.NUMBER,
                        Token.TokenType.SEPERATOR, Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.NUMBER,
                        Token.TokenType.SEPERATOR, Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.NUMBER,
                        Token.TokenType.SEPERATOR, Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.NUMBER,
                        Token.TokenType.SEPERATOR, Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.NUMBER,
                        Token.TokenType.SEPERATOR, Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.NUMBER,
                        Token.TokenType.SEPERATOR, Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.NUMBER,
                        Token.TokenType.SEPERATOR, Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.NUMBER,
                        Token.TokenType.SEPERATOR, Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.NUMBER,
                        Token.TokenType.SEPERATOR, Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.NUMBER,
                        Token.TokenType.SEPERATOR, Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.NUMBER,
                        Token.TokenType.SEPERATOR, Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.NUMBER,
                        Token.TokenType.SEPERATOR, Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.NUMBER,
                        Token.TokenType.SEPERATOR, Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.NUMBER,
                        Token.TokenType.CLOSEBRACE, Token.TokenType.SEPERATOR, Token.TokenType.END,
                        Token.TokenType.OPENBRACE, Token.TokenType.IF, Token.TokenType.OPENPAREN, Token.TokenType.WORD,
                        Token.TokenType.EQUAL, Token.TokenType.NUMBER, Token.TokenType.CLOSEPAREN,
                        Token.TokenType.OPENBRACE, Token.TokenType.IF, Token.TokenType.OPENPAREN, Token.TokenType.WORD,
                        Token.TokenType.EQUAL, Token.TokenType.NUMBER, Token.TokenType.CLOSEPAREN,
                        Token.TokenType.OPENBRACE, Token.TokenType.IF, Token.TokenType.OPENPAREN, Token.TokenType.WORD,
                        Token.TokenType.EQUAL, Token.TokenType.NUMBER, Token.TokenType.CLOSEPAREN,
                        Token.TokenType.OPENBRACE, Token.TokenType.IF, Token.TokenType.OPENPAREN, Token.TokenType.WORD,
                        Token.TokenType.EQUAL, Token.TokenType.NUMBER, Token.TokenType.CLOSEPAREN,
                        Token.TokenType.OPENBRACE, Token.TokenType.IF, Token.TokenType.OPENPAREN, Token.TokenType.WORD,
                        Token.TokenType.EQUAL, Token.TokenType.NUMBER, Token.TokenType.CLOSEPAREN,
                        Token.TokenType.OPENBRACE, Token.TokenType.IF, Token.TokenType.OPENPAREN, Token.TokenType.WORD,
                        Token.TokenType.EQUAL, Token.TokenType.NUMBER, Token.TokenType.CLOSEPAREN,
                        Token.TokenType.OPENBRACE, Token.TokenType.IF, Token.TokenType.OPENPAREN, Token.TokenType.WORD,
                        Token.TokenType.EQUAL, Token.TokenType.NUMBER, Token.TokenType.CLOSEPAREN,
                        Token.TokenType.OPENBRACE, Token.TokenType.IF, Token.TokenType.OPENPAREN, Token.TokenType.WORD,
                        Token.TokenType.EQUAL, Token.TokenType.NUMBER, Token.TokenType.CLOSEPAREN,
                        Token.TokenType.OPENBRACE, Token.TokenType.IF, Token.TokenType.OPENPAREN, Token.TokenType.WORD,
                        Token.TokenType.EQUAL, Token.TokenType.NUMBER, Token.TokenType.CLOSEPAREN,
                        Token.TokenType.OPENBRACE, Token.TokenType.IF, Token.TokenType.OPENPAREN, Token.TokenType.WORD,
                        Token.TokenType.EQUAL, Token.TokenType.NUMBER, Token.TokenType.CLOSEPAREN,
                        Token.TokenType.OPENBRACE, Token.TokenType.IF, Token.TokenType.OPENPAREN, Token.TokenType.WORD,
                        Token.TokenType.EQUAL, Token.TokenType.NUMBER, Token.TokenType.CLOSEPAREN,
                        Token.TokenType.OPENBRACE, Token.TokenType.IF, Token.TokenType.OPENPAREN, Token.TokenType.WORD,
                        Token.TokenType.EQUAL, Token.TokenType.NUMBER, Token.TokenType.CLOSEPAREN,
                        Token.TokenType.OPENBRACE, Token.TokenType.IF, Token.TokenType.OPENPAREN, Token.TokenType.WORD,
                        Token.TokenType.EQUAL, Token.TokenType.NUMBER, Token.TokenType.CLOSEPAREN,
                        Token.TokenType.OPENBRACE, Token.TokenType.IF, Token.TokenType.OPENPAREN, Token.TokenType.WORD,
                        Token.TokenType.EQUAL, Token.TokenType.NUMBER, Token.TokenType.CLOSEPAREN,
                        Token.TokenType.OPENBRACE, Token.TokenType.IF, Token.TokenType.OPENPAREN, Token.TokenType.WORD,
                        Token.TokenType.EQUAL, Token.TokenType.NUMBER, Token.TokenType.CLOSEPAREN,
                        Token.TokenType.OPENBRACE, Token.TokenType.IF, Token.TokenType.OPENPAREN, Token.TokenType.WORD,
                        Token.TokenType.EQUAL, Token.TokenType.NUMBER, Token.TokenType.CLOSEPAREN,
                        Token.TokenType.OPENBRACE, Token.TokenType.IF, Token.TokenType.OPENPAREN, Token.TokenType.WORD,
                        Token.TokenType.EQUAL, Token.TokenType.NUMBER, Token.TokenType.CLOSEPAREN,
                        Token.TokenType.OPENBRACE, Token.TokenType.IF, Token.TokenType.OPENPAREN, Token.TokenType.WORD,
                        Token.TokenType.EQUAL, Token.TokenType.NUMBER, Token.TokenType.CLOSEPAREN,
                        Token.TokenType.OPENBRACE, Token.TokenType.IF, Token.TokenType.OPENPAREN, Token.TokenType.WORD,
                        Token.TokenType.EQUAL, Token.TokenType.NUMBER, Token.TokenType.CLOSEPAREN,
                        Token.TokenType.OPENBRACE, Token.TokenType.IF, Token.TokenType.OPENPAREN, Token.TokenType.WORD,
                        Token.TokenType.EQUAL, Token.TokenType.NUMBER, Token.TokenType.CLOSEPAREN,
                        Token.TokenType.OPENBRACE, Token.TokenType.IF, Token.TokenType.OPENPAREN, Token.TokenType.WORD,
                        Token.TokenType.EQUAL, Token.TokenType.NUMBER, Token.TokenType.CLOSEPAREN,
                        Token.TokenType.OPENBRACE, Token.TokenType.IF, Token.TokenType.OPENPAREN, Token.TokenType.WORD,
                        Token.TokenType.EQUAL, Token.TokenType.NUMBER, Token.TokenType.CLOSEPAREN,
                        Token.TokenType.OPENBRACE, Token.TokenType.IF, Token.TokenType.OPENPAREN, Token.TokenType.WORD,
                        Token.TokenType.EQUAL, Token.TokenType.NUMBER, Token.TokenType.CLOSEPAREN,
                        Token.TokenType.OPENBRACE, Token.TokenType.IF, Token.TokenType.OPENPAREN, Token.TokenType.WORD,
                        Token.TokenType.EQUAL, Token.TokenType.NUMBER, Token.TokenType.CLOSEPAREN,
                        Token.TokenType.OPENBRACE, Token.TokenType.IF, Token.TokenType.OPENPAREN, Token.TokenType.WORD,
                        Token.TokenType.EQUAL, Token.TokenType.NUMBER, Token.TokenType.CLOSEPAREN,
                        Token.TokenType.OPENBRACE, Token.TokenType.IF, Token.TokenType.OPENPAREN, Token.TokenType.WORD,
                        Token.TokenType.EQUAL, Token.TokenType.NUMBER, Token.TokenType.CLOSEPAREN,
                        Token.TokenType.OPENBRACE, Token.TokenType.WORD, Token.TokenType.OPENPAREN,
                        Token.TokenType.STRINGLITERAL, Token.TokenType.CLOSEPAREN, Token.TokenType.CLOSEBRACE,
                        Token.TokenType.CLOSEBRACE, Token.TokenType.CLOSEBRACE, Token.TokenType.CLOSEBRACE,
                        Token.TokenType.CLOSEBRACE, Token.TokenType.CLOSEBRACE, Token.TokenType.CLOSEBRACE,
                        Token.TokenType.CLOSEBRACE, Token.TokenType.CLOSEBRACE, Token.TokenType.CLOSEBRACE,
                        Token.TokenType.CLOSEBRACE, Token.TokenType.CLOSEBRACE, Token.TokenType.CLOSEBRACE,
                        Token.TokenType.CLOSEBRACE, Token.TokenType.CLOSEBRACE, Token.TokenType.CLOSEBRACE,
                        Token.TokenType.CLOSEBRACE, Token.TokenType.CLOSEBRACE, Token.TokenType.CLOSEBRACE,
                        Token.TokenType.CLOSEBRACE, Token.TokenType.CLOSEBRACE, Token.TokenType.CLOSEBRACE,
                        Token.TokenType.CLOSEBRACE, Token.TokenType.CLOSEBRACE, Token.TokenType.CLOSEBRACE,
                        Token.TokenType.CLOSEBRACE, Token.TokenType.CLOSEBRACE, Token.TokenType.SEPERATOR,
                        Token.TokenType.SEPERATOR, Token.TokenType.FUNCTION, Token.TokenType.WORD,
                        Token.TokenType.OPENPAREN, Token.TokenType.WORD, Token.TokenType.CLOSEPAREN,
                        Token.TokenType.OPENBRACE, Token.TokenType.SEPERATOR, Token.TokenType.IF,
                        Token.TokenType.OPENPAREN, Token.TokenType.WORD, Token.TokenType.EQUAL, Token.TokenType.NUMBER,
                        Token.TokenType.CLOSEPAREN, Token.TokenType.OPENBRACE, Token.TokenType.SEPERATOR,
                        Token.TokenType.RETURN, Token.TokenType.NUMBER, Token.TokenType.SEPERATOR,
                        Token.TokenType.SEPERATOR, Token.TokenType.CLOSEBRACE, Token.TokenType.ELSE,
                        Token.TokenType.OPENBRACE, Token.TokenType.SEPERATOR, Token.TokenType.RETURN,
                        Token.TokenType.WORD, Token.TokenType.MULTIPLY, Token.TokenType.WORD, Token.TokenType.OPENPAREN,
                        Token.TokenType.WORD, Token.TokenType.MINUS, Token.TokenType.NUMBER, Token.TokenType.CLOSEPAREN,
                        Token.TokenType.SEPERATOR, Token.TokenType.SEPERATOR, Token.TokenType.CLOSEBRACE,
                        Token.TokenType.SEPERATOR, Token.TokenType.CLOSEBRACE, Token.TokenType.SEPERATOR,
                        Token.TokenType.SEPERATOR, Token.TokenType.BEGIN, Token.TokenType.OPENBRACE,
                        Token.TokenType.SEPERATOR, Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.NUMBER,
                        Token.TokenType.SEPERATOR, Token.TokenType.SEPERATOR, Token.TokenType.WORD,
                        Token.TokenType.OPENPAREN, Token.TokenType.STRINGLITERAL, Token.TokenType.WORD,
                        Token.TokenType.STRINGLITERAL, Token.TokenType.WORD, Token.TokenType.OPENPAREN,
                        Token.TokenType.WORD, Token.TokenType.CLOSEPAREN, Token.TokenType.CLOSEPAREN,
                        Token.TokenType.SEPERATOR, Token.TokenType.SEPERATOR, Token.TokenType.CLOSEBRACE,
                        Token.TokenType.SEPERATOR, Token.TokenType.SEPERATOR, Token.TokenType.BEGIN,
                        Token.TokenType.OPENBRACE, Token.TokenType.SEPERATOR, Token.TokenType.WORD,
                        Token.TokenType.ASSIGN, Token.TokenType.MINUS, Token.TokenType.NUMBER,
                        Token.TokenType.SEPERATOR, Token.TokenType.SEPERATOR, Token.TokenType.WORD,
                        Token.TokenType.ASSIGN, Token.TokenType.WORD, Token.TokenType.GREATERTHAN,
                        Token.TokenType.NUMBER, Token.TokenType.QUESTION, Token.TokenType.NUMBER, Token.TokenType.COLON,
                        Token.TokenType.WORD, Token.TokenType.LESSTHAN, Token.TokenType.NUMBER,
                        Token.TokenType.QUESTION, Token.TokenType.MINUS, Token.TokenType.NUMBER, Token.TokenType.COLON,
                        Token.TokenType.NUMBER, Token.TokenType.SEPERATOR, Token.TokenType.SEPERATOR,
                        Token.TokenType.WORD, Token.TokenType.OPENPAREN, Token.TokenType.STRINGLITERAL,
                        Token.TokenType.WORD, Token.TokenType.STRINGLITERAL, Token.TokenType.WORD,
                        Token.TokenType.CLOSEPAREN, Token.TokenType.SEPERATOR, Token.TokenType.SEPERATOR,
                        Token.TokenType.CLOSEBRACE, Token.TokenType.SEPERATOR, Token.TokenType.SEPERATOR,
                        Token.TokenType.BEGIN, Token.TokenType.OPENBRACE, Token.TokenType.SEPERATOR,
                        Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.WORD, Token.TokenType.ASSIGN,
                        Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.WORD, Token.TokenType.ASSIGN,
                        Token.TokenType.NUMBER, Token.TokenType.SEPERATOR, Token.TokenType.WORD, Token.TokenType.ASSIGN,
                        Token.TokenType.WORD, Token.TokenType.SEPERATOR, Token.TokenType.WORD, Token.TokenType.ASSIGN,
                        Token.TokenType.WORD, Token.TokenType.SEPERATOR, Token.TokenType.WHILE,
                        Token.TokenType.OPENPAREN, Token.TokenType.WORD, Token.TokenType.CLOSEPAREN,
                        Token.TokenType.OPENBRACE, Token.TokenType.SEPERATOR, Token.TokenType.IF,
                        Token.TokenType.OPENPAREN, Token.TokenType.WORD, Token.TokenType.CLOSEPAREN,
                        Token.TokenType.OPENBRACE, Token.TokenType.SEPERATOR, Token.TokenType.WORD,
                        Token.TokenType.MINUSMINUS, Token.TokenType.SEPERATOR, Token.TokenType.WORD,
                        Token.TokenType.MINUSMINUS, Token.TokenType.SEPERATOR, Token.TokenType.CLOSEBRACE,
                        Token.TokenType.ELSE, Token.TokenType.OPENBRACE, Token.TokenType.SEPERATOR,
                        Token.TokenType.WORD, Token.TokenType.PLUSPLUS, Token.TokenType.SEPERATOR, Token.TokenType.WORD,
                        Token.TokenType.PLUSPLUS, Token.TokenType.SEPERATOR, Token.TokenType.CLOSEBRACE,
                        Token.TokenType.SEPERATOR, Token.TokenType.WORD, Token.TokenType.MINUSMINUS,
                        Token.TokenType.SEPERATOR, Token.TokenType.CLOSEBRACE, Token.TokenType.SEPERATOR,
                        Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.WORD, Token.TokenType.QUESTION,
                        Token.TokenType.WORD, Token.TokenType.MINUSMINUS, Token.TokenType.COLON, Token.TokenType.WORD,
                        Token.TokenType.SEPERATOR, Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.WORD,
                        Token.TokenType.QUESTION, Token.TokenType.WORD, Token.TokenType.COLON, Token.TokenType.WORD,
                        Token.TokenType.PLUSPLUS, Token.TokenType.SEPERATOR, Token.TokenType.SEPERATOR,
                        Token.TokenType.CLOSEBRACE
                }));
        parser.Parse();
    }
}
