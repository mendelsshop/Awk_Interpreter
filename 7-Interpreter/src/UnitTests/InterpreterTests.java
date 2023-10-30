import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.HashMap;
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
        }, "0", i -> {
        });
    }

    @Test
    public void testGetlineVar() {

    }

    @Test
    public void testGetline() {

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
    }

    @Test
    public void testGSubDollar0() {
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

    }

    @Test
    public void testSplitFS() {
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
}
