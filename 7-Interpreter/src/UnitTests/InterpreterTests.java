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
        }, "0", i -> {
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
}
