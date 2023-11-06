import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.function.Consumer;

import org.junit.Test;

public class InterpreterTests {
    // built in functions that dont print to console (everything besides print,
    // printf)

    private Interpreter emptyInterpreter() {
        try {
            return new Interpreter(new ProgramNode(), Optional.empty());
        } catch (IOException e) {
            // wont happen
            throw new RuntimeException();
        }
    }

    public void testBuiltIn(String name, Consumer<Interpreter> setUp, HashMap<String, InterpreterDataType> args,
            String expected,
            Consumer<Interpreter> tester) {
        var runner = emptyInterpreter();
        setUp.accept(runner);
        var fn = (BuiltInFunctionDefinitionNode) runner.getFunction(name);
        assertEquals(expected, fn.getExecute().apply(args));
        tester.accept(runner);
    }

    public void testBuiltIn(String name, HashMap<String, InterpreterDataType> args, String expected) {
        testBuiltIn(name, i -> {
        }, args, expected, i -> {
        });
    }

    public void testBuiltIn(String name, HashMap<String, InterpreterDataType> args, String expected,
            Consumer<Interpreter> tester) {
        testBuiltIn(name, i -> {
        }, args, expected, tester);
    }

    public void testBuiltIn(String name, Consumer<Interpreter> setUp, HashMap<String, InterpreterDataType> args,
            String expected) {
        testBuiltIn(name, setUp, args, expected, i -> {
        });
    }

    @Test
    public void testSprintf() {
        testBuiltIn("sprintf", new HashMap<String, InterpreterDataType>() {
            {
                put("format", new InterpreterDataType("\n%s-%s"));
                put("strings", new InterpreterArrayDataType(new HashMap<>() {
                    {
                        put("0", new InterpreterDataType("1"));
                        put("1", new InterpreterDataType("foo"));
                    }
                }));
            }
        }, "\n1-foo", i -> {
        });
    }

    @Test
    public void testGetlineEmpty() {
        testBuiltIn("getline", new HashMap<String, InterpreterDataType>() {
        }, "0", i -> {
        });
    }

    @Test
    public void testGetlineEmptyWithVar() {
        var var = new InterpreterDataType("null");
        testBuiltIn("getline", new HashMap<String, InterpreterDataType>() {
            {
                put("var", new InterpreterArrayDataType(new HashMap<>() {
                    {
                        put("0", var);
                    }
                }));
            }
        }, "0");
        assertEquals(var.getContents(), "null");
    }

    @Test
    public void testGetlineVar() {
        testBuiltIn("getline", initRecord("foo bar\nbaz sand"), new HashMap<String, InterpreterDataType>() {
        }, "1", i -> {
            assertEquals(i.getRecord().Get(0).getContents(), "baz sand");
        });
    }

    @Test
    public void testGetline() {
        var var = new InterpreterDataType("null");
        testBuiltIn("getline", initRecord("this is not csv\nr1 r2 r3"), new HashMap<String, InterpreterDataType>() {
            {
                put("var", new InterpreterArrayDataType(new HashMap<>() {
                    {
                        put("0", var);
                    }
                }));
            }
        }, "1", i -> {
            assertEquals(i.getRecord().Get(0).getContents(), "this is not csv");
        });
        assertEquals(var.getContents(), "r1 r2 r3");
    }

    @Test
    public void testNext() {
        try {
            testBuiltIn("next", new HashMap<String, InterpreterDataType>() {
            }, "", i -> {
            });
            fail("next should throw an excetpion to be handled later");
        } catch (Interpreter.Next e) {

        }
    }

    @Test
    public void testGSub() {
        InterpreterDataType value = new InterpreterDataType("1^1 is 1&1 two");
        testBuiltIn("gsub", new HashMap<String, InterpreterDataType>() {
            {
                put("pattern", new InterpreterDataType("1.1"));
                put("replacement", new InterpreterDataType("foo"));
                put("target", new InterpreterArrayDataType(new HashMap<>() {
                    {
                        put("0", value);
                    }
                }));
            }
        }, "", i -> {
        });
        assertEquals(value.getContents(), "foo is foo two");
    }

    @Test
    public void testSub() {
        InterpreterDataType value = new InterpreterDataType("1^1 is 1&1 two");
        testBuiltIn("sub", new HashMap<String, InterpreterDataType>() {
            {
                put("pattern", new InterpreterDataType("1.1"));
                put("replacement", new InterpreterDataType("foo"));
                put("target", new InterpreterArrayDataType(new HashMap<>() {
                    {
                        put("0", value);
                    }
                }));
            }
        }, "", i -> {
        });
        assertEquals(value.getContents(), "foo is 1&1 two");
    }

    @Test
    public void testSubDollar0() {
        testBuiltIn("sub", initRecord("once=onne=oncce=one"), new HashMap<String, InterpreterDataType>() {
            {
                put("pattern", new InterpreterDataType("on.e"));
                put("replacement", new InterpreterDataType("1"));
                put("target", new InterpreterArrayDataType(new HashMap<>()));
            }
        }, "", i -> {
            assertEquals(i.getRecord().Get(0).getContents(), "1=onne=oncce=one");
        });
    }

    @Test
    public void testGSubDollar0() {
        testBuiltIn("gsub", initRecord("1b-1a"), new HashMap<String, InterpreterDataType>() {
            {
                put("pattern", new InterpreterDataType("1(a|b)"));
                put("replacement", new InterpreterDataType("foo"));
                put("target", new InterpreterArrayDataType(new HashMap<>()));
            }
        }, "", i -> {
            assertEquals(i.getRecord().Get(0).getContents(), "foo-foo");
        });
    }

    @Test
    public void testToUpper() {
        testBuiltIn("toupper", new HashMap<String, InterpreterDataType>() {
            {
                put("string", new InterpreterDataType("Bars Of_Foos"));
            }
        }, "BARS OF_FOOS", i -> {
        });
    }

    @Test
    public void testToLower() {
        testBuiltIn("tolower", i -> {
        }, new HashMap<String, InterpreterDataType>() {
            {
                put("string", new InterpreterDataType("FoObARs"));
            }
        }, "foobars", i -> {
        });
    }

    @Test
    public void testSubStr() {
        testBuiltIn("substr", i -> {
        }, new HashMap<String, InterpreterDataType>() {
            {
                put("string", new InterpreterDataType("FoObARs"));
                put("start", new InterpreterDataType("4"));
                put("length", new InterpreterArrayDataType());
            }
        }, "bARs", i -> {
        });
    }

    @Test
    public void testSubStrN() {
        testBuiltIn("substr", i -> {
        }, new HashMap<String, InterpreterDataType>() {
            {
                put("string", new InterpreterDataType("FoObARs"));
                put("start", new InterpreterDataType("4"));
                put("length", new InterpreterArrayDataType(new HashMap<>() {
                    {
                        put("0", new InterpreterDataType("3"));
                    }
                }));
            }
        }, "bAR", i -> {
        });
    }

    @Test
    public void testSubStrNonNumber() {
        assertThrows(AwkRuntimeError.ExpectedNumberError.class, () -> testBuiltIn("substr", i -> {
        }, new HashMap<String, InterpreterDataType>() {
            {
                put("string", new InterpreterDataType("FoObARs"));
                put("start", new InterpreterDataType("pizza"));
                put("length", new InterpreterArrayDataType());
            }
        }, "bARs", i -> {
        }));
    }

    @Test
    public void testSubStrNNonNumber() {
        assertThrows(AwkRuntimeError.ExpectedNumberError.class, () -> testBuiltIn("substr", i -> {
        }, new HashMap<String, InterpreterDataType>() {
            {
                put("string", new InterpreterDataType("FoObARs"));
                put("start", new InterpreterDataType("1"));
                put("length", new InterpreterArrayDataType(new HashMap<>() {
                    {
                        put("0", new InterpreterDataType("java"));
                    }
                }));
            }
        }, "bARs", i -> {
        }));
    }

    @Test
    public void testSplit() {
        var array = new InterpreterArrayDataType();
        testBuiltIn("split", i -> {
        }, new HashMap<String, InterpreterDataType>() {
            {
                put("string", new InterpreterDataType("Fo3ObA3Rs"));
                put("array", array);
                put("sep", new InterpreterArrayDataType(new HashMap<>() {
                    {
                        put("0", new InterpreterDataType("3"));
                    }
                }));
            }
        }, "3");
        assertEquals(array.getHashMap(), new HashMap<>() {
            {
                put("1", new InterpreterDataType("Fo"));
                put("2", new InterpreterDataType("ObA"));
                put("3", new InterpreterDataType("Rs"));
            }
        });
    }

    @Test
    public void testSplitFS() {
        var array = new InterpreterArrayDataType();
        testBuiltIn("split", i -> {
        }, new HashMap<String, InterpreterDataType>() {
            {
                put("string", new InterpreterDataType("123 45 67 7 8"));
                put("array", array);
                put("sep", new InterpreterArrayDataType(new HashMap<>()));
            }
        }, "5");
        assertEquals(array.getHashMap(), new HashMap<>() {
            {
                put("1", new InterpreterDataType("123"));
                put("2", new InterpreterDataType("45"));
                put("3", new InterpreterDataType("67"));
                put("4", new InterpreterDataType("7"));
                put("5", new InterpreterDataType("8"));
            }
        });
    }

    @Test
    public void testLength() {
        testBuiltIn("length", new HashMap<String, InterpreterDataType>() {
            {
                put("string", new InterpreterArrayDataType(new HashMap<>() {
                    {
                        put("0", new InterpreterDataType("reallylongword"));
                    }
                }));
            }
        }, "14", i -> {
        });
    }

    public Consumer<Interpreter> initRecord(String input) {
        return i -> {
            i.setInput(input);
            ((BuiltInFunctionDefinitionNode) i.getFunction("getline")).getExecute().apply(new HashMap<>());
        };
    }

    @Test
    public void testLengthDollarZero() {
        testBuiltIn("length", initRecord("alsoreallyLongword\n\n"), new HashMap<String, InterpreterDataType>() {
            {
                put("string", new InterpreterArrayDataType(new HashMap<>() {
                }));
            }
        }, "18", i -> {
        });
    }

    @Test
    public void testMatch() {
        testBuiltIn("match", i -> {
        }, new HashMap<String, InterpreterDataType>() {
            {
                put("haystack", new InterpreterDataType("\n----\nnooodle"));
                put("needle", new InterpreterDataType("n.*dle"));
            }
        }, "7", i -> {
            assertEquals("7", i.getGlobal("RSTART").getContents());
            assertEquals("7", i.getGlobal("RLENGTH").getContents());
        });
    }

    @Test
    public void testIndex() {
        testBuiltIn("index", i -> {
        }, new HashMap<String, InterpreterDataType>() {
            {
                put("haystack", new InterpreterDataType("\n----\nponooodle"));
                put("needle", new InterpreterDataType("nooodle"));
            }
        }, "9");
    }

    // interpreter 2 - tests - GetIDT
    // interpreter 2 - tests - GetIDT - contants (String)
    @Test
    public void testGetIDTConstantString() throws Exception {
        var parser = new Parser(UnitTests.testLexContent("\"foo\"\n", new Token.TokenType[] {
                // constant (string)
                Token.TokenType.STRINGLITERAL, Token.TokenType.SEPERATOR,
        }));
        var interpreter = emptyInterpreter();
        var string = parser.ParseOperation().get();
        parser.AcceptSeperators();
        assertEquals(new ConstantNode("foo"), string);
        assertEquals("foo", interpreter.GetIDT(string, null).getContents());
    }

    // interpreter 2 - tests - GetIDT - contants (Number)
    @Test
    public void testGetIDTConstantNumber() throws Exception {
        var parser = new Parser(UnitTests.testLexContent("22\n", new Token.TokenType[] {
                // constant (number)
                Token.TokenType.NUMBER, Token.TokenType.SEPERATOR,
        }));
        var interpreter = emptyInterpreter();
        var number = parser.ParseOperation().get();
        parser.AcceptSeperators();
        assertEquals(new ConstantNode("22"), number);
        assertEquals("22", interpreter.GetIDT(number, null).getContents());
    }

    // interpreter 2 - tests - GetIDT - pattern
    @Test
    public void testGetIDTPattern() throws Exception {
        var parser = new Parser(UnitTests.testLexContent("`[0-9][a-z]*`\n", new Token.TokenType[] {
                // pattern
                Token.TokenType.PATTERN, Token.TokenType.SEPERATOR,
        }));
        var interpreter = emptyInterpreter();
        var pattern = parser.ParseOperation().get();
        parser.AcceptSeperators();
        assertEquals(new PatternNode("[0-9][a-z]*"), pattern);
        assertThrows(AwkRuntimeError.PatternError.class, () -> interpreter.GetIDT(pattern, null));
    }

    // interpreter 2 - tests - GetIDT - function call
    @Test
    public void testGetIDTFunctionCall() throws Exception {
        var parser = new Parser(UnitTests.testLexContent("bar(1, 3)\n", new Token.TokenType[] {
                // function call
                Token.TokenType.WORD, Token.TokenType.OPENPAREN, Token.TokenType.NUMBER, Token.TokenType.COMMA,
                Token.TokenType.NUMBER, Token.TokenType.CLOSEPAREN, Token.TokenType.SEPERATOR,
        }));
        var interpreter = emptyInterpreter();
        var functionCall = parser.ParseOperation().get();
        parser.AcceptSeperators();
        assertEquals(new FunctionCallNode("bar", new LinkedList<>() {
            {
                add(new ConstantNode("1"));
                add(new ConstantNode("3"));
            }
        }), functionCall);
        // rn function calls do nothing
        assertEquals("", interpreter.GetIDT(functionCall, null).getContents());
    }

    HashMap<String, InterpreterDataType> locals_1 = new HashMap<String, InterpreterDataType>() {
        {
            put("baz", new InterpreterDataType("bandaid"));
        }
    };

    HashMap<String, InterpreterDataType> locals_2 = new HashMap<String, InterpreterDataType>();

    HashMap<String, InterpreterDataType> initLocals_2() {
        return new HashMap<String, InterpreterDataType>();
    }

    // interpreter 2 - tests - GetIDT - variable
    @Test
    public void testGetIDTVariable() throws Exception {
        var parser = new Parser(UnitTests.testLexContent("baz\n", new Token.TokenType[] {
                // variable
                Token.TokenType.WORD, Token.TokenType.SEPERATOR,
        }));
        var interpreter = emptyInterpreter();
        var variable = parser.ParseOperation().get();
        parser.AcceptSeperators();
        assertEquals(new VariableReferenceNode("baz"), variable);

        // with locals
        assertEquals("bandaid", interpreter.GetIDT(variable, locals_1).getContents());
        // not present
        assertEquals("", interpreter.GetIDT(variable, locals_2).getContents());
        assertEquals("", locals_2.get("baz").getContents());

        // without locals
        // not present
        assertEquals("", interpreter.GetIDT(variable, null).getContents());
        interpreter.getGlobal("baz").setContents("bazinga");
        assertEquals("bazinga", interpreter.GetIDT(variable, null).getContents());
        interpreter.getGlobal("baz").setContents("");
    }

    InterpreterArrayDataType array_1 = new InterpreterArrayDataType(new HashMap<>() {
        {
            put("0", new InterpreterDataType("foo"));
        }
    });

    InterpreterArrayDataType array_2 = new InterpreterArrayDataType(new HashMap<>());

    // interpreter 2 - tests - GetIDT - array index
    @Test
    public void testGetIDTArrayIndex() throws Exception {
        var parser = new Parser(UnitTests.testLexContent("array[0]\n", new Token.TokenType[] {
                // array index
                Token.TokenType.WORD, Token.TokenType.OPENBRACKET, Token.TokenType.NUMBER,
                Token.TokenType.CLOSEBRACKET, Token.TokenType.SEPERATOR,
        }));
        var interpreter = emptyInterpreter();
        var arrayIndex = parser.ParseOperation().get();
        parser.AcceptSeperators();
        assertEquals(new VariableReferenceNode("array", new ConstantNode("0")), arrayIndex);

        locals_1.put("array", array_1);
        locals_2.put("array", array_2);
        // with locals
        assertEquals("foo", interpreter.GetIDT(arrayIndex, locals_1).getContents());
        // not present
        assertEquals("", interpreter.GetIDT(arrayIndex, locals_2).getContents());
        assertEquals("", array_2.getHashMap().get("0").getContents());
        assertEquals("", interpreter.getArray("array", locals_2).getHashMap().get("0").getContents());

        // without locals
        // not present
        assertEquals("", interpreter.GetIDT(arrayIndex, null).getContents());
        interpreter.getArray("array", null).getHashMap().put("0", new InterpreterDataType("bar"));
        assertEquals("bar", interpreter.GetIDT(arrayIndex, null).getContents());
        interpreter.getArray("array", null).getHashMap().put("0", new InterpreterDataType(""));
    }

    // interpreter 2 - tests - GetIDT - ternary
    @Test
    public void testGetIDTTernary() throws Exception {
        var parser = new Parser(UnitTests.testLexContent("banana == \"banana\" ? \"same\" : \"different\"\n",
                new Token.TokenType[] {
                        // ternary
                        Token.TokenType.WORD, Token.TokenType.EQUAL, Token.TokenType.STRINGLITERAL,
                        Token.TokenType.QUESTION, Token.TokenType.STRINGLITERAL, Token.TokenType.COLON,
                        Token.TokenType.STRINGLITERAL, Token.TokenType.SEPERATOR,
                }));
        var interpreter = emptyInterpreter();
        var ternary = parser.ParseOperation().get();
        parser.AcceptSeperators();
        assertEquals(new TernaryOperationNode(
                new OperationNode(OperationNode.Operation.EQ, new VariableReferenceNode("banana"),
                        new ConstantNode("banana")),
                new ConstantNode("same"), new ConstantNode("different")), ternary);

        locals_1.put("banana", new InterpreterDataType("banana"));

        // with locals
        assertEquals("same", interpreter.GetIDT(ternary, locals_1).getContents());
        // not present
        assertEquals("different", interpreter.GetIDT(ternary, locals_2).getContents());
        assertEquals("", locals_2.get("banana").getContents());

        // without locals
        // not present
        assertEquals("different", interpreter.GetIDT(ternary, null).getContents());
        interpreter.getGlobal("banana").setContents("banana");
        assertEquals("same", interpreter.GetIDT(ternary, null).getContents());
        interpreter.getGlobal("banana").setContents("");
    }

    // interpreter 2 - tests - GetIDT - whole record
    @Test
    public void testGetIDTWholeRecord() throws Exception {
        var parser = new Parser(UnitTests.testLexContent("$0\n", new Token.TokenType[] {
                // whole record
                Token.TokenType.DOLLAR, Token.TokenType.NUMBER, Token.TokenType.SEPERATOR,
        }));
        var interpreter = emptyInterpreter();
        var wholeRecord = parser.ParseOperation().get();
        parser.AcceptSeperators();

        assertEquals(new OperationNode(OperationNode.Operation.DOLLAR, new ConstantNode("0")), wholeRecord);
        // without new record
        assertEquals("", interpreter.GetIDT(wholeRecord, null).getContents());
        // with new record
        interpreter.setInput("foo, 4 ,5\nbar");
        ((BuiltInFunctionDefinitionNode) interpreter.getFunction("getline")).getExecute().apply(new HashMap<>());
        assertEquals("foo, 4 ,5", interpreter.GetIDT(wholeRecord, null).getContents());
        interpreter.setInput("");
        ((BuiltInFunctionDefinitionNode) interpreter.getFunction("getline")).getExecute().apply(new HashMap<>());
    }

    // interpreter 2 - tests - GetIDT - field 37
    @Test
    public void testGetIDTField37() throws Exception {
        var parser = new Parser(UnitTests.testLexContent("$37;", new Token.TokenType[] {
                // field 37
                Token.TokenType.DOLLAR, Token.TokenType.NUMBER, Token.TokenType.SEPERATOR,
        }));
        var interpreter = emptyInterpreter();
        var field37 = parser.ParseOperation().get();
        parser.AcceptSeperators();
        assertEquals(new OperationNode(OperationNode.Operation.DOLLAR, new ConstantNode("37")), field37);
        // without new record
        assertEquals("", interpreter.GetIDT(field37, null).getContents());
        // with new record
        interpreter.setInput(
                "foo, 4 ,5\nbar 3 n4  4 5  1 5 5 6  7 8 nine ten 11 twelve 27 38 49 60 71 foo bar baz 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15");
        ((BuiltInFunctionDefinitionNode) interpreter.getFunction("getline")).getExecute().apply(new HashMap<>());
        assertEquals("", interpreter.GetIDT(field37, null).getContents());
        ((BuiltInFunctionDefinitionNode) interpreter.getFunction("getline")).getExecute().apply(new HashMap<>());
        assertEquals("11", interpreter.GetIDT(field37, null).getContents());
        interpreter.setInput("");
        ((BuiltInFunctionDefinitionNode) interpreter.getFunction("getline")).getExecute().apply(new HashMap<>());
    }

    // interpreter 2 - tests - GetIDT - addition
    @Test
    public void testGetIDTAddition() throws Exception {
        var parser = new Parser(UnitTests.testLexContent("4 + 72.5\n", new Token.TokenType[] {
                // addition
                Token.TokenType.NUMBER, Token.TokenType.PLUS, Token.TokenType.NUMBER, Token.TokenType.SEPERATOR,
        }));
        var interpreter = emptyInterpreter();
        var addition = parser.ParseOperation().get();
        parser.AcceptSeperators();
        assertEquals(new OperationNode(OperationNode.Operation.ADD, new ConstantNode("4"), new ConstantNode("72.5")),
                addition);
        assertEquals("76.5", interpreter.GetIDT(addition, null).getContents());
    }

    // interpreter 2 - tests - GetIDT - and
    @Test
    public void testGetIDTAnd() throws Exception {
        var parser = new Parser(UnitTests.testLexContent("1 && 1\n", new Token.TokenType[] {
                // and
                Token.TokenType.NUMBER, Token.TokenType.AND, Token.TokenType.NUMBER, Token.TokenType.SEPERATOR,
        }));
        var interpreter = emptyInterpreter();
        var and = parser.ParseOperation().get();
        parser.AcceptSeperators();
        assertEquals(new OperationNode(OperationNode.Operation.AND, new ConstantNode("1"), new ConstantNode("1")),
                and);
        assertEquals("1", interpreter.GetIDT(and, null).getContents());
    }

    // interpreter 2 - tests - GetIDT - concatination
    @Test
    public void testGetIDTConcatination() throws Exception {
        var parser = new Parser(UnitTests.testLexContent("\"foo\" \"bar\"\n", new Token.TokenType[] {
                // concatination
                Token.TokenType.STRINGLITERAL, Token.TokenType.STRINGLITERAL, Token.TokenType.SEPERATOR,
        }));
        var interpreter = emptyInterpreter();
        var concatination = parser.ParseOperation().get();
        parser.AcceptSeperators();

        assertEquals(
                new OperationNode(OperationNode.Operation.CONCATENATION, new ConstantNode("foo"),
                        new ConstantNode("bar")),
                concatination);
        assertEquals("foobar", interpreter.GetIDT(concatination, null).getContents());
    }

    // interpreter 2 - tests - GetIDT - division
    @Test
    public void testGetIDTDivision() throws Exception {
        var parser = new Parser(UnitTests.testLexContent("4 / 1.35\n", new Token.TokenType[] {
                // division
                Token.TokenType.NUMBER, Token.TokenType.DIVIDE, Token.TokenType.NUMBER,
                Token.TokenType.SEPERATOR,
        }));
        var interpreter = emptyInterpreter();
        var division = parser.ParseOperation().get();
        parser.AcceptSeperators();
        assertEquals(new OperationNode(OperationNode.Operation.DIVIDE, new ConstantNode("4"), new ConstantNode("1.35")),
                division);
        assertEquals("2.9629629", interpreter.GetIDT(division, null).getContents());
    }

    // interpreter 2 - tests - GetIDT - equal
    @Test
    public void testGetIDTEqual() throws Exception {
        var parser = new Parser(UnitTests.testLexContent("5 == 6\n", new Token.TokenType[] {
                // equal
                Token.TokenType.NUMBER, Token.TokenType.EQUAL, Token.TokenType.NUMBER,
                Token.TokenType.SEPERATOR,
        }));
        var interpreter = emptyInterpreter();
        var equal = parser.ParseOperation().get();
        parser.AcceptSeperators();
        assertEquals(new OperationNode(OperationNode.Operation.EQ, new ConstantNode("5"), new ConstantNode("6")),
                equal);
        assertEquals("0", interpreter.GetIDT(equal, null).getContents());
    }

    // interpreter 2 - tests - GetIDT - exponent
    @Test
    public void testGetIDTExponent() throws Exception {
        var parser = new Parser(UnitTests.testLexContent("2 ^ 3\n", new Token.TokenType[] {
                // exponent
                Token.TokenType.NUMBER, Token.TokenType.EXPONENT, Token.TokenType.NUMBER,
                Token.TokenType.SEPERATOR,
        }));
        var interpreter = emptyInterpreter();
        var exponent = parser.ParseOperation().get();
        parser.AcceptSeperators();
        assertEquals(new OperationNode(OperationNode.Operation.EXPONENT, new ConstantNode("2"), new ConstantNode("3")),
                exponent);
        assertEquals("8", interpreter.GetIDT(exponent, null).getContents());
    }

    // interpreter 2 - tests - GetIDT - greater than or equal
    @Test
    public void testGetIDTGreaterThanEqual() throws Exception {
        var parser = new Parser(UnitTests.testLexContent("5 >= 5\n", new Token.TokenType[] {
                // greater than or equal
                Token.TokenType.NUMBER, Token.TokenType.GREATERTHANEQUAL, Token.TokenType.NUMBER,
                Token.TokenType.SEPERATOR,
        }));
        var interpreter = emptyInterpreter();
        var greaterThanEqual = parser.ParseOperation().get();
        parser.AcceptSeperators();
        assertEquals(new OperationNode(OperationNode.Operation.GE, new ConstantNode("5"), new ConstantNode("5")),
                greaterThanEqual);

        assertEquals("1", interpreter.GetIDT(greaterThanEqual, null).getContents());
    }

    // interpreter 2 - tests - GetIDT - greater than
    @Test
    public void testGetIDTGreaterThan() throws Exception {
        var parser = new Parser(UnitTests.testLexContent("72 > 5\n", new Token.TokenType[] {
                // greater than
                Token.TokenType.NUMBER, Token.TokenType.GREATERTHAN, Token.TokenType.NUMBER,
                Token.TokenType.SEPERATOR,
        }));
        var interpreter = emptyInterpreter();
        var greaterThan = parser.ParseOperation().get();
        parser.AcceptSeperators();
        assertEquals(new OperationNode(OperationNode.Operation.GT, new ConstantNode("72"), new ConstantNode("5")),
                greaterThan);
        assertEquals("1", interpreter.GetIDT(greaterThan, null).getContents());
    }

    // interpreter 2 - tests - GetIDT - in
    @Test
    public void testGetIDTIn() throws Exception {
        var parser = new Parser(UnitTests.testLexContent("1 in array\n", new Token.TokenType[] {
                // in
                Token.TokenType.NUMBER, Token.TokenType.IN, Token.TokenType.WORD, Token.TokenType.SEPERATOR,
        }));
        var interpreter = emptyInterpreter();
        var in = parser.ParseOperation().get();
        parser.AcceptSeperators();
        assertEquals(
                new OperationNode(OperationNode.Operation.IN, new ConstantNode("1"),
                        new VariableReferenceNode("array")),
                in);
        assertEquals("0", interpreter.GetIDT(in, locals_1).getContents());
        array_2.insert("1", new InterpreterDataType("here"));
        locals_2.put("array", array_2);
        assertEquals("1", interpreter.GetIDT(in, locals_2).getContents());

        // without locals
        // not present
        assertEquals("0", interpreter.GetIDT(in, null).getContents());
        interpreter.getArray("array", null).getHashMap().put("1", new InterpreterDataType("no, here"));
        assertEquals("1", interpreter.GetIDT(in, null).getContents());
    }

    // interpreter 2 - tests - GetIDT - less than or equal
    @Test
    public void testGetIDTLessThanEqual() throws Exception {
        var parser = new Parser(UnitTests.testLexContent("17 <= 45\n", new Token.TokenType[] {
                // less than or equal
                Token.TokenType.NUMBER, Token.TokenType.LESSTHANEQUAL, Token.TokenType.NUMBER,
                Token.TokenType.SEPERATOR,
        }));
        var interpreter = emptyInterpreter();
        var lessThanEqual = parser.ParseOperation().get();
        parser.AcceptSeperators();
        assertEquals(new OperationNode(OperationNode.Operation.LE, new ConstantNode("17"), new ConstantNode("45")),
                lessThanEqual);

        assertEquals("1", interpreter.GetIDT(lessThanEqual, null).getContents());
    }

    // interpreter 2 - tests - GetIDT - less than
    @Test
    public void testGetIDTLessThan() throws Exception {
        var parser = new Parser(UnitTests.testLexContent("1 < 2\n", new Token.TokenType[] {
                // less than
                Token.TokenType.NUMBER, Token.TokenType.LESSTHAN, Token.TokenType.NUMBER,
                Token.TokenType.SEPERATOR,
        }));
        var interpreter = emptyInterpreter();
        var lessThan = parser.ParseOperation().get();
        parser.AcceptSeperators();
        assertEquals(new OperationNode(OperationNode.Operation.LT, new ConstantNode("1"), new ConstantNode("2")),
                lessThan);
        assertEquals("1", interpreter.GetIDT(lessThan, null).getContents());
    }

    // interpreter 2 - tests - GetIDT - match
    @Test
    public void testGetIDTMatch() throws Exception {
        var parser = new Parser(UnitTests.testLexContent("\"foo\" ~ `fo*`\n", new Token.TokenType[] {
                // match
                Token.TokenType.STRINGLITERAL, Token.TokenType.MATCH, Token.TokenType.PATTERN,
                Token.TokenType.SEPERATOR,
        }));
        var interpreter = emptyInterpreter();
        var match = parser.ParseOperation().get();
        parser.AcceptSeperators();
        assertEquals(new OperationNode(OperationNode.Operation.MATCH, new ConstantNode("foo"), new PatternNode("fo*")),
                match);
        assertEquals("1", interpreter.GetIDT(match, null).getContents());
    }

    // interpreter 2 - tests - GetIDT - modulus
    @Test
    public void testGetIDTModulus() throws Exception {
        var parser = new Parser(UnitTests.testLexContent("5 % 2\n", new Token.TokenType[] {
                // modulus
                Token.TokenType.NUMBER, Token.TokenType.MODULO, Token.TokenType.NUMBER,
                Token.TokenType.SEPERATOR,
        }));
        var interpreter = emptyInterpreter();
        var modulus = parser.ParseOperation().get();
        parser.AcceptSeperators();
        assertEquals(new OperationNode(OperationNode.Operation.MODULO, new ConstantNode("5"), new ConstantNode("2")),
                modulus);
        assertEquals("1", interpreter.GetIDT(modulus, null).getContents());
    }

    // interpreter 2 - tests - GetIDT - multiplication
    @Test
    public void testGetIDTMultiplication() throws Exception {
        var parser = new Parser(UnitTests.testLexContent("3 * 4\n", new Token.TokenType[] {
                // multiplication
                Token.TokenType.NUMBER, Token.TokenType.MULTIPLY, Token.TokenType.NUMBER,
                Token.TokenType.SEPERATOR,
        }));
        var interpreter = emptyInterpreter();
        var multiplication = parser.ParseOperation().get();
        parser.AcceptSeperators();
        assertEquals(new OperationNode(OperationNode.Operation.MULTIPLY, new ConstantNode("3"), new ConstantNode("4")),
                multiplication);
        assertEquals("12", interpreter.GetIDT(multiplication, null).getContents());
    }

    // interpreter 2 - tests - GetIDT - not equal
    @Test
    public void testGetIDTNotEqual() throws Exception {
        var parser = new Parser(UnitTests.testLexContent("8 != 8\n", new Token.TokenType[] {
                // not equal
                Token.TokenType.NUMBER, Token.TokenType.NOTEQUAL, Token.TokenType.NUMBER,
                Token.TokenType.SEPERATOR,
        }));
        var interpreter = emptyInterpreter();
        var notEqual = parser.ParseOperation().get();
        parser.AcceptSeperators();
        assertEquals(new OperationNode(OperationNode.Operation.NE, new ConstantNode("8"), new ConstantNode("8")),
                notEqual);
        assertEquals("0", interpreter.GetIDT(notEqual, null).getContents());
    }

    // interpreter 2 - tests - GetIDT - not
    @Test
    public void testGetIDTNot() throws Exception {
        var parser = new Parser(UnitTests.testLexContent("!2;", new Token.TokenType[] {
                // not
                Token.TokenType.NOT, Token.TokenType.NUMBER, Token.TokenType.SEPERATOR,
        }));
        var interpreter = emptyInterpreter();
        var not = parser.ParseOperation().get();
        parser.AcceptSeperators();
        assertEquals(new OperationNode(OperationNode.Operation.NOT, new ConstantNode("2")), not);
        assertEquals("0", interpreter.GetIDT(not, null).getContents());
    }

    // interpreter 2 - tests - GetIDT - not match
    @Test
    public void testGetIDTNotMatch() throws Exception {
        var parser = new Parser(UnitTests.testLexContent("\"bar\" !~ `fo*`\n", new Token.TokenType[] {
                // not match
                Token.TokenType.STRINGLITERAL, Token.TokenType.NOTMATCH, Token.TokenType.PATTERN,
                Token.TokenType.SEPERATOR,
        }));
        var interpreter = emptyInterpreter();
        var notMatch = parser.ParseOperation().get();
        parser.AcceptSeperators();
        assertEquals(
                new OperationNode(OperationNode.Operation.NOTMATCH, new ConstantNode("bar"), new PatternNode("fo*")),
                notMatch);
        assertEquals("1", interpreter.GetIDT(notMatch, null).getContents());
    }

    // interpreter 2 - tests - GetIDT - or
    @Test
    public void testGetIDTOr() throws Exception {
        var parser = new Parser(UnitTests.testLexContent("0 || 1\n", new Token.TokenType[] {
                // or
                Token.TokenType.NUMBER, Token.TokenType.OR, Token.TokenType.NUMBER, Token.TokenType.SEPERATOR,
        }));
        var interpreter = emptyInterpreter();
        var or = parser.ParseOperation().get();
        parser.AcceptSeperators();
        assertEquals(new OperationNode(OperationNode.Operation.OR, new ConstantNode("0"), new ConstantNode("1")),
                or);
        assertEquals("1", interpreter.GetIDT(or, null).getContents());
    }

    // interpreter 2 - tests - GetIDT - post decrement
    @Test
    public void testGetIDTPostDecrement() throws Exception {
        var parser = new Parser(UnitTests.testLexContent("a--\n", new Token.TokenType[] {
                // post decrement
                Token.TokenType.WORD, Token.TokenType.MINUSMINUS, Token.TokenType.SEPERATOR,
        }));
        var interpreter = emptyInterpreter();
        var postDecrement = parser.ParseOperation().get();
        parser.AcceptSeperators();
        assertEquals(new OperationNode(OperationNode.Operation.POSTDEC, new VariableReferenceNode("a")),
                postDecrement);

        locals_1.put("a", new InterpreterDataType("5"));
        // with locals
        assertEquals("4", interpreter.GetIDT(postDecrement, locals_1).getContents());
        // not present
        assertEquals("-1", interpreter.GetIDT(postDecrement, locals_2).getContents());

        // without locals
        // not present
        assertEquals("-1", interpreter.GetIDT(postDecrement, null).getContents());
        interpreter.getGlobal("a").setContents("3");
        assertEquals("2", interpreter.GetIDT(postDecrement, null).getContents());
    }

    // interpreter 2 - tests - GetIDT - post increment
    @Test
    public void testGetIDTPostIncrement() throws Exception {
        var parser = new Parser(UnitTests.testLexContent("i++\n", new Token.TokenType[] {
                // post increment
                Token.TokenType.WORD, Token.TokenType.PLUSPLUS, Token.TokenType.SEPERATOR,
        }));
        var interpreter = emptyInterpreter();
        var postIncrement = parser.ParseOperation().get();
        parser.AcceptSeperators();
        assertEquals(new OperationNode(OperationNode.Operation.POSTINC, new VariableReferenceNode("i")),
                postIncrement);

        locals_1.put("i", new InterpreterDataType("4.5"));
        // with locals
        assertEquals("5.5", interpreter.GetIDT(postIncrement, locals_1).getContents());
        // not present
        assertEquals("1", interpreter.GetIDT(postIncrement, locals_2).getContents());

        // without locals
        // not present
        assertEquals("1", interpreter.GetIDT(postIncrement, null).getContents());
        interpreter.getGlobal("i").setContents("3.5");
        assertEquals("4.5", interpreter.GetIDT(postIncrement, null).getContents());
    }

    // interpreter 2 - tests - GetIDT - pre decrement
    @Test
    public void testGetIDTPreDecrement() throws Exception {
        var parser = new Parser(UnitTests.testLexContent("--i\n", new Token.TokenType[] {
                // pre decrement
                Token.TokenType.MINUSMINUS, Token.TokenType.WORD, Token.TokenType.SEPERATOR,
        }));
        var interpreter = emptyInterpreter();
        var preDecrement = parser.ParseOperation().get();
        parser.AcceptSeperators();
        assertEquals(new OperationNode(OperationNode.Operation.PREDEC, new VariableReferenceNode("i")),
                preDecrement);
        locals_1.put("i", new InterpreterDataType("4.5"));
        assertEquals("4.5", interpreter.GetIDT(preDecrement, locals_1).getContents());
        assertEquals("3.5", interpreter.GetIDT(preDecrement, locals_1).getContents());

        // without locals
        assertEquals("0", interpreter.GetIDT(preDecrement, null).getContents());
        assertEquals("-1", interpreter.GetIDT(preDecrement, null).getContents());
    }

    // interpreter 2 - tests - GetIDT - pre increment
    @Test
    public void testGetIDTPreIncrement() throws Exception {
        var parser = new Parser(UnitTests.testLexContent("++a\n", new Token.TokenType[] {
                // pre increment
                Token.TokenType.PLUSPLUS, Token.TokenType.WORD, Token.TokenType.SEPERATOR,
        }));
        var interpreter = emptyInterpreter();
        var preIncrement = parser.ParseOperation().get();
        parser.AcceptSeperators();
        assertEquals(new OperationNode(OperationNode.Operation.PREINC, new VariableReferenceNode("a")),
                preIncrement);

        locals_1.put("a", new InterpreterDataType("4"));
        assertEquals("4", interpreter.GetIDT(preIncrement, locals_1).getContents());
        assertEquals("5", interpreter.GetIDT(preIncrement, locals_1).getContents());

        // without locals
        assertEquals("0", interpreter.GetIDT(preIncrement, null).getContents());
        assertEquals("1", interpreter.GetIDT(preIncrement, null).getContents());
    }

    // interpreter 2 - tests - GetIDT - subtraction
    @Test
    public void testGetIDTSubtraction() throws Exception {
        var parser = new Parser(UnitTests.testLexContent("5 - 4\n", new Token.TokenType[] {
                // subtraction
                Token.TokenType.NUMBER, Token.TokenType.MINUS, Token.TokenType.NUMBER,
                Token.TokenType.SEPERATOR,
        }));
        var interpreter = emptyInterpreter();
        var subtraction = parser.ParseOperation().get();
        parser.AcceptSeperators();
        assertEquals(new OperationNode(OperationNode.Operation.SUBTRACT, new ConstantNode("5"), new ConstantNode("4")),
                subtraction);
        assertEquals("1", interpreter.GetIDT(subtraction, null).getContents());
    }

    // interpreter 2 - tests - GetIDT - negative
    @Test
    public void testGetIDTNegative() throws Exception {
        var parser = new Parser(UnitTests.testLexContent("-1\n", new Token.TokenType[] {
                // negative
                Token.TokenType.MINUS, Token.TokenType.NUMBER, Token.TokenType.SEPERATOR,
        }));
        var interpreter = emptyInterpreter();
        var negative = parser.ParseOperation().get();
        parser.AcceptSeperators();
        assertEquals(new OperationNode(OperationNode.Operation.UNARYNEG, new ConstantNode("1")), negative);
        assertEquals("-1", interpreter.GetIDT(negative, null).getContents());
    }

    // interpreter 2 - tests - GetIDT - positive
    @Test
    public void testGetIDTPositive() throws Exception {
        var parser = new Parser(UnitTests.testLexContent("+1\n", new Token.TokenType[] {
                // positive
                Token.TokenType.PLUS, Token.TokenType.NUMBER, Token.TokenType.SEPERATOR,
        }));
        var interpreter = emptyInterpreter();
        var positive = parser.ParseOperation().get();
        parser.AcceptSeperators();
        assertEquals(new OperationNode(OperationNode.Operation.UNARYPOS, new ConstantNode("1")), positive);
        assertEquals("1", interpreter.GetIDT(positive, null).getContents());
    }

    // interpreter 2 - tests - GetIDT - error - with numbers
    // like doing 1- "foo" ...
    @Test
    public void testGetIDTErrorWithNumbers() throws Exception {
        var parser = new Parser(
                UnitTests.testLexContent("2 * \"bar\"; 17 - \"baz\"; i+= \"foo\"\n 7 / \"banana\"; 1 % \"bar\"",
                        new Token.TokenType[] {
                                // multiplication
                                Token.TokenType.NUMBER, Token.TokenType.MULTIPLY, Token.TokenType.STRINGLITERAL,
                                Token.TokenType.SEPERATOR,
                                // subtraction
                                Token.TokenType.NUMBER, Token.TokenType.MINUS, Token.TokenType.STRINGLITERAL,
                                Token.TokenType.SEPERATOR,
                                // addition
                                Token.TokenType.WORD, Token.TokenType.PLUSEQUAL, Token.TokenType.STRINGLITERAL,
                                Token.TokenType.SEPERATOR,
                                // division
                                Token.TokenType.NUMBER, Token.TokenType.DIVIDE, Token.TokenType.STRINGLITERAL,
                                Token.TokenType.SEPERATOR,
                                // modulus
                                Token.TokenType.NUMBER, Token.TokenType.MODULO, Token.TokenType.STRINGLITERAL,
                        }));
        var interpreter = emptyInterpreter();
        var multiplication = parser.ParseOperation().get();
        parser.AcceptSeperators();
        assertEquals(
                new OperationNode(OperationNode.Operation.MULTIPLY, new ConstantNode("2"), new ConstantNode("bar")),
                multiplication);
        assertThrows(AwkRuntimeError.ExpectedNumberError.class, () -> interpreter.GetIDT(multiplication, null));

        var subtraction = parser.ParseOperation().get();
        parser.AcceptSeperators();
        assertEquals(
                new OperationNode(OperationNode.Operation.SUBTRACT, new ConstantNode("17"), new ConstantNode("baz")),
                subtraction);
        assertThrows(AwkRuntimeError.ExpectedNumberError.class, () -> interpreter.GetIDT(subtraction, null));

        var addition = parser.ParseOperation().get();
        parser.AcceptSeperators();
        assertEquals(new AssignmentNode(new VariableReferenceNode("i"),
                new OperationNode(OperationNode.Operation.ADD, new VariableReferenceNode("i"),
                        new ConstantNode("foo"))),
                addition);
        assertThrows(AwkRuntimeError.ExpectedNumberError.class, () -> interpreter.GetIDT(addition, null));

        var division = parser.ParseOperation().get();
        parser.AcceptSeperators();
        assertEquals(
                new OperationNode(OperationNode.Operation.DIVIDE, new ConstantNode("7"), new ConstantNode("banana")),
                division);
        assertThrows(AwkRuntimeError.ExpectedNumberError.class, () -> interpreter.GetIDT(division, null));

        var modulus = parser.ParseOperation().get();
        parser.AcceptSeperators();
        assertEquals(new OperationNode(OperationNode.Operation.MODULO, new ConstantNode("1"), new ConstantNode("bar")),
                modulus);
        assertThrows(AwkRuntimeError.ExpectedNumberError.class, () -> interpreter.GetIDT(modulus, null));
    }

    // interpreter 2 - tests - GetIDT - error - with assignment to non variable
    @Test
    public void testGetIDTErrorWithAssignmentToNonVariable() throws Exception {
        // pre inc/dec gets handled in parser
        var parser = new Parser(UnitTests.testLexContent("1 = 2\n 7--; \"potato\" *= 2\n \"banana\" /= 2; \"bar\" %= 2",
                new Token.TokenType[] {
                        // assignment
                        Token.TokenType.NUMBER, Token.TokenType.ASSIGN, Token.TokenType.NUMBER,
                        Token.TokenType.SEPERATOR,

                        // post decrement
                        Token.TokenType.NUMBER, Token.TokenType.MINUSMINUS, Token.TokenType.SEPERATOR,
                        // multiplication assignment
                        Token.TokenType.STRINGLITERAL, Token.TokenType.MULTIPLYEQUAL, Token.TokenType.NUMBER,
                        Token.TokenType.SEPERATOR,
                        // division assignment
                        Token.TokenType.STRINGLITERAL, Token.TokenType.DIVIDEEQUAL, Token.TokenType.NUMBER,
                        Token.TokenType.SEPERATOR,
                        // modulus assignment
                        Token.TokenType.STRINGLITERAL, Token.TokenType.MODULOEQUAL, Token.TokenType.NUMBER,
                }));

        var interpreter = emptyInterpreter();
        var assignment = parser.ParseOperation().get();
        parser.AcceptSeperators();
        assertEquals(new AssignmentNode(new ConstantNode("1"), new ConstantNode("2")), assignment);
        assertThrows(AwkRuntimeError.NotAVariableError.class, () -> interpreter.GetIDT(assignment, null));

        var postDecrement = parser.ParseOperation().get();
        parser.AcceptSeperators();
        assertEquals(new OperationNode(OperationNode.Operation.POSTDEC, new ConstantNode("7")), postDecrement);
        assertThrows(AwkRuntimeError.NotAVariableError.class, () -> interpreter.GetIDT(postDecrement, null));

        var multiplicationAssignment = parser.ParseOperation().get();
        parser.AcceptSeperators();
        assertEquals(new AssignmentNode(new ConstantNode("potato"),
                new OperationNode(OperationNode.Operation.MULTIPLY, new ConstantNode("potato"), new ConstantNode("2"))),
                multiplicationAssignment);

        // although the rest through expected number errors, thats just b/c a+= b -> a =
        // a + b (so if a was a number then the math would work and we would get not a
        // variable error)
        assertThrows(AwkRuntimeError.ExpectedNumberError.class,
                () -> interpreter.GetIDT(multiplicationAssignment, null));

        var divisionAssignment = parser.ParseOperation().get();
        parser.AcceptSeperators();
        assertEquals(new AssignmentNode(new ConstantNode("banana"),
                new OperationNode(OperationNode.Operation.DIVIDE, new ConstantNode("banana"), new ConstantNode("2"))),
                divisionAssignment);
        assertThrows(AwkRuntimeError.ExpectedNumberError.class, () -> interpreter.GetIDT(divisionAssignment, null));

        var modulusAssignment = parser.ParseOperation().get();
        parser.AcceptSeperators();
        assertEquals(new AssignmentNode(new ConstantNode("bar"),
                new OperationNode(OperationNode.Operation.MODULO, new ConstantNode("bar"), new ConstantNode("2"))),
                modulusAssignment);
        assertThrows(AwkRuntimeError.ExpectedNumberError.class, () -> interpreter.GetIDT(modulusAssignment, null));
    }

    // interpreter 2 - tests - GetIDT - error - (not)?match without pattern
    @Test
    public void testGetIDTErrorMatchWithoutPattern() throws Exception {
        var parser = new Parser(UnitTests.testLexContent("\"foo\" ~ \"bar\"\n \"banana\" !~ \"baz\"",
                new Token.TokenType[] {
                        // match
                        Token.TokenType.STRINGLITERAL, Token.TokenType.MATCH, Token.TokenType.STRINGLITERAL,
                        Token.TokenType.SEPERATOR,
                        // not match
                        Token.TokenType.STRINGLITERAL, Token.TokenType.NOTMATCH, Token.TokenType.STRINGLITERAL,
                }));
        var interpreter = emptyInterpreter();
        var match = parser.ParseOperation().get();
        parser.AcceptSeperators();
        assertEquals(
                new OperationNode(OperationNode.Operation.MATCH, new ConstantNode("foo"), new ConstantNode("bar")),
                match);
        assertThrows(AwkRuntimeError.ExpectedPatternError.class, () -> interpreter.GetIDT(match, null));

        var notMatch = parser.ParseOperation().get();
        parser.AcceptSeperators();
        assertEquals(
                new OperationNode(OperationNode.Operation.NOTMATCH, new ConstantNode("banana"),
                        new ConstantNode("baz")),
                notMatch);
        assertThrows(AwkRuntimeError.ExpectedPatternError.class, () -> interpreter.GetIDT(notMatch, null));
    }

    // interpreter 2 - tests - GetIDT - error - field index < 0
    @Test
    public void testGetIDTErrorFieldIndexLessThanZero() throws Exception {
        var parser = new Parser(UnitTests.testLexContent("$(37-10*40)\n", new Token.TokenType[] {
                // field index < 0
                Token.TokenType.DOLLAR, Token.TokenType.OPENPAREN, Token.TokenType.NUMBER, Token.TokenType.MINUS,
                Token.TokenType.NUMBER, Token.TokenType.MULTIPLY, Token.TokenType.NUMBER, Token.TokenType.CLOSEPAREN,
                Token.TokenType.SEPERATOR,
        }));
        var interpreter = emptyInterpreter();
        var fieldIndexLessThanZero = parser.ParseOperation().get();
        parser.AcceptSeperators();
        assertEquals(
                new OperationNode(OperationNode.Operation.DOLLAR,
                        new OperationNode(OperationNode.Operation.SUBTRACT, new ConstantNode("37"), new OperationNode(
                                OperationNode.Operation.MULTIPLY, new ConstantNode("10"), new ConstantNode("40")))),
                fieldIndexLessThanZero);
        assertThrows(AwkRuntimeError.NegativeFieldIndexError.class,
                () -> interpreter.GetIDT(fieldIndexLessThanZero, null));
    }

    // interpreter 2 - tests - GetIDT - complex math (test order of operations eval)
    @Test
    public void testGetIDTComplexMath() throws Exception {
        var parser = new Parser(UnitTests.testLexContent("1 + 2 * 3 - 4 / 5 ^ 6 % 7\n", new Token.TokenType[] {
                // complex math
                Token.TokenType.NUMBER, Token.TokenType.PLUS, Token.TokenType.NUMBER, Token.TokenType.MULTIPLY,
                Token.TokenType.NUMBER, Token.TokenType.MINUS, Token.TokenType.NUMBER, Token.TokenType.DIVIDE,
                Token.TokenType.NUMBER, Token.TokenType.EXPONENT, Token.TokenType.NUMBER, Token.TokenType.MODULO,
                Token.TokenType.NUMBER, Token.TokenType.SEPERATOR,
        }));
        var interpreter = emptyInterpreter();
        var complexMath = parser.ParseOperation().get();
        parser.AcceptSeperators();
        // op precedence is ^, * / %, + -
        // so ((1 ADD (2 MULTIPLY 3)) SUBTRACT ((4 DIVIDE (5 EXPONENT 6)) MODULO 7))
        assertEquals(new OperationNode(OperationNode.Operation.SUBTRACT,
                new OperationNode(OperationNode.Operation.ADD, new ConstantNode("1"),
                        new OperationNode(OperationNode.Operation.MULTIPLY, new ConstantNode("2"),
                                new ConstantNode("3"))),
                new OperationNode(OperationNode.Operation.MODULO,
                        new OperationNode(OperationNode.Operation.DIVIDE, new ConstantNode("4"),
                                new OperationNode(OperationNode.Operation.EXPONENT, new ConstantNode("5"),
                                        new ConstantNode("6"))),
                        new ConstantNode("7"))),
                complexMath);
        assertEquals("6.999744", interpreter.GetIDT(complexMath, null).getContents());
    }

    // interpreter 2 - tests - GetIDT - complex variables
    @Test
    public void testGetIDTComplexVariables() throws Exception {
        var parser = new Parser(UnitTests.testLexContent("a = 5; b -=a; c[a] = $1\n d = f ? a : c[b]\n a += b--",
                new Token.TokenType[] {
                        Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.NUMBER, Token.TokenType.SEPERATOR,
                        Token.TokenType.WORD, Token.TokenType.MINUSEQUAL, Token.TokenType.WORD,
                        Token.TokenType.SEPERATOR,
                        Token.TokenType.WORD, Token.TokenType.OPENBRACKET, Token.TokenType.WORD,
                        Token.TokenType.CLOSEBRACKET, Token.TokenType.ASSIGN, Token.TokenType.DOLLAR,
                        Token.TokenType.NUMBER, Token.TokenType.SEPERATOR,
                        Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.WORD, Token.TokenType.QUESTION,
                        Token.TokenType.WORD, Token.TokenType.COLON, Token.TokenType.WORD, Token.TokenType.OPENBRACKET, Token.TokenType.WORD, Token.TokenType.CLOSEBRACKET, Token.TokenType.SEPERATOR,
                        Token.TokenType.WORD, Token.TokenType.PLUSEQUAL, Token.TokenType.WORD,
                        Token.TokenType.MINUSMINUS,
                }));
        var interpreter = emptyInterpreter();
        var assignment = parser.ParseOperation().get();
        parser.AcceptSeperators();
        assertEquals(new AssignmentNode(new VariableReferenceNode("a"), new ConstantNode("5")), assignment);
        assertEquals("5", interpreter.GetIDT(assignment, null).getContents());

        var subtractionAssignment = parser.ParseOperation().get();
        parser.AcceptSeperators();
        assertEquals(new AssignmentNode(new VariableReferenceNode("b"),
                new OperationNode(OperationNode.Operation.SUBTRACT, new VariableReferenceNode("b"),
                        new VariableReferenceNode("a"))),
                subtractionAssignment);
        assertEquals("-5", interpreter.GetIDT(subtractionAssignment, null).getContents());


        var arrayAssignment = parser.ParseOperation().get();
        parser.AcceptSeperators();
        assertEquals(new AssignmentNode(new VariableReferenceNode("c", new VariableReferenceNode("a")),
                        new OperationNode(OperationNode.Operation.DOLLAR, new ConstantNode("1"))), arrayAssignment);
                interpreter.setInput("foo, 1, 2 ,3, bar\nbar");
                ((BuiltInFunctionDefinitionNode) interpreter.getFunction("getline")).getExecute().apply(new HashMap<>());
                assertEquals("foo,", interpreter.GetIDT(arrayAssignment, null).getContents());

        var ternary = parser.ParseOperation().get();
        parser.AcceptSeperators();
        assertEquals(new AssignmentNode(new VariableReferenceNode("d"),
                new TernaryOperationNode(new VariableReferenceNode("f"), new VariableReferenceNode("a"),
                        new VariableReferenceNode("c", new VariableReferenceNode("b")))),
                ternary);

        assertEquals("", interpreter.GetIDT(ternary, null).getContents());

        var additionAssignment = parser.ParseOperation().get();
        parser.AcceptSeperators();
        assertEquals(new AssignmentNode(new VariableReferenceNode("a"),
                new OperationNode(OperationNode.Operation.ADD, new VariableReferenceNode("a"),
                        new OperationNode(OperationNode.Operation.POSTDEC, new VariableReferenceNode("b")))),
                additionAssignment);
        assertEquals("-1", interpreter.GetIDT(additionAssignment, null).getContents());

        // assert all variables
        assertEquals("-1", interpreter.getGlobal("a").getContents());
        assertEquals("-6", interpreter.getGlobal("b").getContents());
        assertEquals("foo,", interpreter.getArray("c", null).getHashMap().get("5").getContents());
        assertEquals("", interpreter.getArray("c", null).getHashMap().get("-5").getContents());
        assertEquals("", interpreter.getGlobal("d").getContents());
        assertEquals("", interpreter.getGlobal("f").getContents());

        // $0 .. $5
        assertEquals("foo, 1, 2 ,3, bar", interpreter.getRecord().Get(0).getContents());
        assertEquals("foo,", interpreter.getRecord().Get(1).getContents());
        assertEquals("1,", interpreter.getRecord().Get(2).getContents());
        assertEquals("2", interpreter.getRecord().Get(3).getContents());
        assertEquals(",3,", interpreter.getRecord().Get(4).getContents());
        assertEquals("bar", interpreter.getRecord().Get(5).getContents());
    }

    // interpreter 2 - tests - GetIDT - error - mixing arrays and scalars
    @Test
    public void testGetIDTErrorScalarArray() throws Exception {
        var parser = new Parser(UnitTests.testLexContent("a = c= 2\n b[1] = 1; d[22] = 6;a=b;d[22]=b", 
            new Token.TokenType[] {
                Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.NUMBER, Token.TokenType.SEPERATOR,
                Token.TokenType.WORD, Token.TokenType.OPENBRACKET, Token.TokenType.NUMBER, Token.TokenType.CLOSEBRACKET, Token.TokenType.ASSIGN, Token.TokenType.NUMBER, Token.TokenType.SEPERATOR,
                Token.TokenType.WORD, Token.TokenType.OPENBRACKET, Token.TokenType.NUMBER, Token.TokenType.CLOSEBRACKET, Token.TokenType.ASSIGN, Token.TokenType.NUMBER, Token.TokenType.SEPERATOR,
                Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.WORD, Token.TokenType.SEPERATOR,
                Token.TokenType.WORD, Token.TokenType.OPENBRACKET, Token.TokenType.NUMBER, Token.TokenType.CLOSEBRACKET, Token.TokenType.ASSIGN, Token.TokenType.WORD, 

            }));

        var interpreter = emptyInterpreter();
        var assignment = parser.ParseOperation().get();
        parser.AcceptSeperators();
        assertEquals(new AssignmentNode(new VariableReferenceNode("a"), new AssignmentNode(new VariableReferenceNode("c"), new ConstantNode("2"))), assignment);
        var locals_1 = new HashMap<String, InterpreterDataType>();
        assertEquals("2", interpreter.GetIDT(assignment, locals_1).getContents());
        assertEquals("2", interpreter.GetIDT(assignment, null).getContents());

        var arrayAssignment = parser.ParseOperation().get();
        parser.AcceptSeperators();
        assertEquals(new AssignmentNode(new VariableReferenceNode("b", new ConstantNode("1")), new ConstantNode("1")), arrayAssignment);
        assertEquals("1", interpreter.GetIDT(arrayAssignment, locals_1).getContents());
        assertEquals("1", interpreter.GetIDT(arrayAssignment, null).getContents());

        var arrayAssignment2 = parser.ParseOperation().get();
        parser.AcceptSeperators();
        assertEquals(new AssignmentNode(new VariableReferenceNode("d", new ConstantNode("22")), new ConstantNode("6")), arrayAssignment2);
        assertEquals("6", interpreter.GetIDT(arrayAssignment2, locals_1).getContents());
        assertEquals("6", interpreter.GetIDT(arrayAssignment2, null).getContents());

        var invalidAssignment = parser.ParseOperation().get();
        parser.AcceptSeperators();
        assertEquals(new AssignmentNode(new VariableReferenceNode("a"), new VariableReferenceNode("b")), invalidAssignment);
        assertThrows(AwkRuntimeError.ExpectedScalarError.class, () -> interpreter.GetIDT(invalidAssignment, locals_1));
        assertThrows(AwkRuntimeError.ExpectedScalarError.class, () -> interpreter.GetIDT(invalidAssignment, null));

        var invalidAssignment2 = parser.ParseOperation().get();
        parser.AcceptSeperators();
        assertEquals(new AssignmentNode(new VariableReferenceNode("d", new ConstantNode("22")), new VariableReferenceNode("b")), invalidAssignment2);
        assertThrows(AwkRuntimeError.ExpectedScalarError.class, () -> interpreter.GetIDT(invalidAssignment2, locals_1));
        assertThrows(AwkRuntimeError.ExpectedScalarError.class, () -> interpreter.GetIDT(invalidAssignment2, null));

        // assert all variables
        assertEquals("2", interpreter.getGlobal("a").getContents());
        assertEquals("2", interpreter.getGlobal("c").getContents());
        assertEquals("1", interpreter.getArray("b", null).getHashMap().get("1").getContents());
        assertEquals("6", interpreter.getArray("d", null).getHashMap().get("22").getContents());

        // locals
        assertEquals("2", locals_1.get("a").getContents());
        assertEquals("2", locals_1.get("c").getContents());
        assertEquals("1", interpreter.getArray("b", locals_1).getHashMap().get("1").getContents());
        assertEquals("6", interpreter.getArray("d", locals_1).getHashMap().get("22").getContents());
    }

    // interpreter 2 - tests - GetIDT - error - index into scalar
    @Test
    public void testGetIDTErrorIndexScalar() throws Exception {
        var parser = new Parser(UnitTests.testLexContent("a = 5; c -= --a[b]", 
            new Token.TokenType[] {
                Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.NUMBER, Token.TokenType.SEPERATOR,
                Token.TokenType.WORD, Token.TokenType.MINUSEQUAL, Token.TokenType.MINUSMINUS, Token.TokenType.WORD, Token.TokenType.OPENBRACKET, Token.TokenType.WORD, Token.TokenType.CLOSEBRACKET,
            }));

        var interpreter = emptyInterpreter();
        var assignment = parser.ParseOperation().get();
        parser.AcceptSeperators();
        assertEquals(new AssignmentNode(new VariableReferenceNode("a"), new ConstantNode("5")), assignment);
        assertEquals("5", interpreter.GetIDT(assignment, null).getContents());

        var invalidIndex = parser.ParseOperation().get();
        parser.AcceptSeperators();
        assertEquals(new AssignmentNode(new VariableReferenceNode("c"), new OperationNode(OperationNode.Operation.SUBTRACT, new VariableReferenceNode("c"), new OperationNode(OperationNode.Operation.PREDEC, new VariableReferenceNode("a", new VariableReferenceNode("b"))))), invalidIndex);
        assertThrows(AwkRuntimeError.ExpectedArrayError.class, () -> interpreter.GetIDT(invalidIndex, null));
    }
}