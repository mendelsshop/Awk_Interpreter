import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.HashMap;

import org.junit.Test;

public class LineManagerTests {

    public Interpreter init(String input) {
        try {
            var interpeter = new Interpreter(new ProgramNode(), Optional.empty());
            interpeter.setInput(input);
            return interpeter;
        } catch (IOException e) {
            // not happening
            throw new RuntimeException();
        }
    }

    public boolean callGetLine(Interpreter interpreter) {
        return ((BuiltInFunctionDefinitionNode) interpreter.getFunction("getline")).getExecute().apply(
                new HashMap<>() {
                    {
                        put("var", new InterpreterArrayDataType());
                    }
                }) == "1";
    }

    public boolean callGetLine(Interpreter interpreter, InterpreterDataType var) {
        return ((BuiltInFunctionDefinitionNode) interpreter.getFunction("getline")).getExecute().apply(
                new HashMap<>() {
                    {
                        put("var", new InterpreterArrayDataType(
                                new HashMap<>() {
                                    {
                                        put("0", var);
                                    }
                                }));
                    }
                }) == "1";
    }

    @Test
    public void testEmptyInput() throws Exception {
        var in = new Interpreter(new ProgramNode(), Optional.empty());
        assertEquals(callGetLine(in), false);
        var data = new InterpreterDataType("foo");
        assertEquals(callGetLine(in, data), false);
        assertEquals(data.getContents(), "foo");
    }

    @Test
    public void testSomeInput() {
        var in = init("foo\nbar\n");
        assertEquals(callGetLine(in), true);
        assertEquals(in.getRecord().Get(0).getContents(), "foo");
        var data = new InterpreterDataType("foo");
        assertEquals(
                callGetLine(in, data), true);
        assertEquals(data.getContents(), "bar");
    }

    @Test
    public void SkipAndSave() {
        var in = init("foo abc\nbar def\nbaz ghi\n1 2 3 4 n");
        var foo = new InterpreterDataType("bar");
        var bar = new InterpreterDataType("baz");
        var baz = new InterpreterDataType("foo");

        assertEquals(
                callGetLine(in, foo), true);
        assertEquals(
                callGetLine(in, bar), true);
        assertEquals(
                callGetLine(in, baz), true);
        assertEquals(foo.getContents(), "foo abc");
        assertEquals(bar.getContents(), "bar def");
        assertEquals(baz.getContents(), "baz ghi");

        assertEquals(callGetLine(in), true);
        assertEquals(in.getRecord().Get(0).getContents(), "1 2 3 4 n");

        assertEquals(in.getRecord().Get(1).getContents(), "1");
        assertEquals(in.getRecord().Get(2).getContents(), "2");
        assertEquals(in.getRecord().Get(3).getContents(), "3");
        assertEquals(in.getRecord().Get(4).getContents(), "4");
        assertEquals(in.getRecord().Get(5).getContents(), "n");
    }
}
