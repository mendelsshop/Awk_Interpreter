import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Interpreter {
    private class LineManager {
        List<String> lines;
        // we could keep track of the number of lines by using NR, but this much easier and more efficient b/c no string->number parsing
        int linesProcessed = 0;

        public LineManager(List<String> lines) {
            this.lines = lines;
        }

        public boolean SplitAndAssign() {
            if (!lines.isEmpty()) {
                variables.put("NR", new InterpreterDataType("" + (++linesProcessed)));
                // TODO: since no multi-file support nr = nfr
                variables.put("NFR", new InterpreterDataType("" + linesProcessed));
                String text = lines.remove(0);
                var line = text.split(variables.get("FS").getContents());
                variables.put("NR", new InterpreterDataType("" + lines.size()));
                // assign each variable to $n
                // assign lines and nf,fnr,nr
                // is $0 for whole line if so start from 1
                // also need to clear variables from previous line
                variables.put("$0", new InterpreterDataType(text));
                for (int i = 1; i <= line.length; i++) {
                    variables.put("$" + line, new InterpreterDataType(line[i]));
                }
                return true;
            }
            return false;
        }
    }


    // TODO: i assume that we need the program node for interpreter
    private ProgramNode program;
    private LineManager input;
    private HashMap<String, InterpreterDataType> variables = new HashMap<String, InterpreterDataType>() {
        {
            put("FS", new InterpreterDataType(" "));
            put("OFS", new InterpreterDataType(" "));
            put("OFT", new InterpreterDataType("%.6g"));
            put("ORS", new InterpreterDataType("\n"));
            put("NF", new InterpreterDataType());
            put("NFR", new InterpreterDataType());
            put("NR", new InterpreterDataType());
        }
    };
    private HashMap<String, FunctionNode> functions = new HashMap<String, FunctionNode>() {
        {
            // TODO: builtin functions also need to figure out what each function does
            put("print", new BuiltInFunctionDefinitionNode((vars) -> {
                System.out.println("");
                return "";
            }, true));
            put("printf", new BuiltInFunctionDefinitionNode((vars) -> {
                System.out.println("");
                return "";
            }, true));
            put("getline", new BuiltInFunctionDefinitionNode((vars) -> input.SplitAndAssign() ? "" : "", false));
            put("next", new BuiltInFunctionDefinitionNode((vars) -> input.SplitAndAssign() ? "" : "", false));
            put("gsub", null);
            put("match", null);
            put("sub", null);
            put("index", null);
            put("length", null);
            put("split", null);
            put("substr", null);
            put("tolower", null);
            put("toupper", null);
        }
    };

    public Interpreter(ProgramNode program, Optional<String> path) throws IOException {
        input = new LineManager(
                path.isPresent() ? Files.readAllLines(Paths.get(path.get())) : new LinkedList<String>());
        this.program = program;
        functions.putAll(
                program.getFunctions().stream().collect(Collectors.toMap(FunctionNode::getName, function -> function)));

    }
}
